package io.mosip.certify.util;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.face.FaceEncoder;
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
}
