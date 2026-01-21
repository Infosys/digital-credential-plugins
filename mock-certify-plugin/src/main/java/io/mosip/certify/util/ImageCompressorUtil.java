package io.mosip.certify.util;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.face.FaceEncoder;
import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.mock.integration.service.ImageCompressorServiceImpl;
import io.mosip.image.compressor.sdk.constant.ResponseStatus;
import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.image.compressor.sdk.service.ImageCompressionService;
import io.mosip.image.compressor.sdk.utils.Util;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.*;
import io.mosip.kernel.biometrics.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ImageCompressorUtil {

    private final Environment env;

    public ImageCompressorUtil(Environment env) {
        this.env = env;
    }

    public byte[] compressImage(byte[] imageBytes) {
        BiometricRecord sample = new BiometricRecord();
        List<BiometricType> modalitiesToExtract = List.of(BiometricType.FACE);
        Map<String, String> flags = Map.of();

        ImageCompressorServiceImpl service =
                new ImageCompressorServiceImpl(env, sample, modalitiesToExtract, flags);

        return service.doResizeAndCompress(imageBytes);
    }

    public String extractAndCompressImage(String imageData) throws DataProviderExchangeException {
        try {
            // --- Require Data URI with prefix only ---
            if (imageData == null || imageData.isBlank() || !imageData.startsWith("data:") || !imageData.contains(";")
                    || !imageData.contains(",")) {
                throw new IllegalArgumentException("Invalid image format. Upload a proper image type.");
            }

            // Basic structure guards
            int colon = imageData.indexOf(':');   // should be 4 ("data:")
            int semi  = imageData.indexOf(';');
            int comma = imageData.indexOf(',');
            if (colon < 0 || semi < 0 || comma < 0 || colon >= semi || semi >= comma) {
                throw new IllegalArgumentException("Invalid image format. Upload a proper image type.");
            }

            // Extract MIME (e.g., image/png, image/jpeg)
            String mimeType = imageData.substring(colon + 1, semi).trim();

            // Extract the format (e.g., "png", "jpeg", "jpg"); default "" if malformed
            int slash = mimeType.indexOf('/');
            String formatName = (slash >= 0 && slash < mimeType.length() - 1)
                    ? mimeType.substring(slash + 1).toLowerCase()
                    : "";

            // Fallback rule: anything other than png/jpeg/jpg → force JPEG
            boolean isPng  = "png".equals(formatName);
            boolean usePng = isPng;         // only true when explicitly PNG

            // Extract Base64 payload and decode
            String base64Data = imageData.substring(comma + 1).trim();
            byte[] inputBytes = Base64.getDecoder().decode(base64Data);

            // Compress (assumed JP2 output)
            final int targetSize = 4096;   // 4 KB
            final int maxAttempts = 3;    // safety
            int attempts = 0;
            byte[] jp2Bytes;

            while (true) {
                jp2Bytes = compressImage(inputBytes);   // reuse existing method only
                attempts++;

                if (jp2Bytes.length <= targetSize) {
                    break;
                }
                if (attempts >= maxAttempts) {
                    throw new DataProviderExchangeException(
                            "FACE_IMAGE_TOO_LARGE",
                            "Unable to compress image to with available compression."
                    );
                }

                // use the last compressed output as the next input
                inputBytes = jp2Bytes;
            }

            // Convert JP2 → desired output format
            final byte[] outBytes;
            final String outMime;
            if (usePng) {
                outBytes = CommonUtil.convertJP2ToPNGBytes(jp2Bytes);
                outMime  = "image/png";
            } else {
                outBytes = CommonUtil.convertJP2ToJPEGBytes(jp2Bytes);
                outMime  = "image/jpeg";
            }

            // Encode and return as Data URI
            final String b64 = Base64.getEncoder().encodeToString(outBytes);
            return "data:" + outMime + ";base64," + b64;

        } catch (IllegalArgumentException iae) {
            throw new DataProviderExchangeException("INVALID_IMAGE_DATA", iae.getMessage());
        } catch (Exception e) {
            log.error("Image compression failed", e);
            throw new DataProviderExchangeException(
                    "ERROR_COMPRESSING_IMAGE",
                    "Failed to compress image data. Check the image format and other properties."
            );
        }
    }
}
