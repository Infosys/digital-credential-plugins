package io.mosip.certify.mock.integration.service;


import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.ImageType;
import io.mosip.biometrics.util.Modality;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.face.ImageDataType;
import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.api.spi.DataProviderPlugin;
import io.mosip.certify.util.CSVReader;
import io.mosip.certify.util.ImageCompressorUtil;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.text.Segment;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ConditionalOnProperty(value = "mosip.certify.integration.data-provider-plugin", havingValue = "MockCSVDataProviderPlugin")
@Component
@Slf4j
public class MockCSVDataProviderPlugin implements DataProviderPlugin {
    static {
        /**
         * load OpenCV library nu.pattern.OpenCV.loadShared();
         * System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
         */
        /**
         * In Java >= 12 it is no longer possible to use addLibraryPath, which modifies
         * the ClassLoader's static usr_paths field. There does not seem to be any way
         * around this so we fall back to loadLocally() and return.
         */
        nu.pattern.OpenCV.loadLocally();
        Loader.load(opencv_java.class);
        System.setProperty("OPENCV_IO_ENABLE_JASPER", "1");
        log.info("OPENCV_IO_ENABLE_JASPER: {}", System.getenv("OPENCV_IO_ENABLE_JASPER"));
    }
    @Value("${mosip.certify.mock.vciplugin.id-uri:https://example.com/}")
    private String id;
    @Autowired
    private CSVReader csvReader;

    @Autowired
    private ImageCompressorUtil imageCompressorUtil;
    @Value("${mosip.certify.mock.data-provider.csv-registry-uri}")
    private String csvRegistryURI;
    @Value("${mosip.certify.mock.data-provider.csv.identifier-column}")
    private String identifierColumn;
    @Value("#{'${mosip.certify.mock.data-provider.csv.data-columns}'.split(',')}")
    private Set<String> dataColumns;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * initialize sets up a CSV data for this DataProviderPlugin on start of application
     * @return
     */
    @PostConstruct
    public File initialize() throws IOException, JSONException {
        File filePath;
        if (csvRegistryURI.startsWith("http")) {
            // download the file to a path: usecase(docker, spring cloud config)
            filePath = restTemplate.execute(csvRegistryURI, HttpMethod.GET, null, resp -> {
                File ret = File.createTempFile("download", "tmp");
                StreamUtils.copy(resp.getBody(), new FileOutputStream(ret));
                return ret;
            });
        } else if (csvRegistryURI.startsWith("classpath:")) {
            try {
                // usecase(local setup)
                filePath = ResourceUtils.getFile(csvRegistryURI);
            } catch (IOException e) {
                throw new FileNotFoundException("File not found in: " + csvRegistryURI);
            }
        } else {
            // usecase(local setup)
            filePath = new File(csvRegistryURI);
            if (!filePath.isFile()) {
                // TODO: make sure it crashes the application
                throw new FileNotFoundException("File not found: " + csvRegistryURI);
            }
        }
        csvReader.readCSV(filePath, identifierColumn, dataColumns);
        return filePath;
    }

    @Override
    public JSONObject fetchData(Map<String, Object> identityDetails) throws DataProviderExchangeException {
        try {
            String individualId = (String) identityDetails.get("sub");
            if (individualId != null) {
                JSONObject jsonRes = csvReader.getJsonObjectByIdentifier(individualId);
                if(jsonRes.has("face")) {
                    String imageData = jsonRes.getString("face");
//                    String compressedImageData = compressImageData(imageData);
                    String compressedImageData = extractAndCompressImage(imageData);
                    jsonRes.put("face", compressedImageData);
                }
                return jsonRes;
            }
        } catch (Exception e) {
            log.error("Failed to fetch json data for from data provider plugin", e);
            throw new DataProviderExchangeException("ERROR_FETCHING_IDENTITY_DATA");
        }
        throw new DataProviderExchangeException("No Data Found");
    }

    private String compressImageData(String imageData) throws DataProviderExchangeException {
        try {
            byte[] imageBytes = decodeDataUri(imageData);
            byte[] compressedBytes = imageCompressorUtil.compressImage(imageBytes);
//            if (compressedBytes.length > 1024) {
//                throw new DataProviderExchangeException("FACE_IMAGE_TOO_LARGE", "Compressed image exceeds 1 KB size limit.");
//            }
            byte[] jpegCompressedBytes = CommonUtil.convertJP2ToPNGBytes(compressedBytes);
            return Base64.getEncoder().encodeToString(jpegCompressedBytes);
        } catch (Exception e) {
            log.error("Image compression failed", e);
            throw new DataProviderExchangeException("ERROR_COMPRESSING_IMAGE", "Failed to compress image data. Check the image format and other properties.");
        }

    }

    public static byte[] decodeDataUri(String dataUri) {
        if (dataUri == null) throw new IllegalArgumentException("dataUri is null");
        String base64Part = dataUri;
        int commaIdx = dataUri.indexOf(',');
        if (commaIdx != -1) {
            base64Part = dataUri.substring(commaIdx + 1);
        }
        return Base64.getDecoder().decode(base64Part);
    }




    private String extractAndCompressImage(String imageData) throws DataProviderExchangeException {
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
            final String base64Data = imageData.substring(comma + 1).trim();
            final byte[] inputBytes = Base64.getDecoder().decode(base64Data);

            // Compress (assumed JP2 output)
            final byte[] jp2Bytes = imageCompressorUtil.compressImage(inputBytes);

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
