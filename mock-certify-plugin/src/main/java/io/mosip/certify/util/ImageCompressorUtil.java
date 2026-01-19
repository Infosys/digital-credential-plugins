package io.mosip.certify.util;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceEncoder;
import io.mosip.certify.mock.integration.service.ImageCompressorServiceImpl;
import io.mosip.image.compressor.sdk.constant.ResponseStatus;
import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2;
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

    @Autowired
    private ImageCompressorSDKV2 imageCompressorSDKV2;

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

    public Response<BiometricRecord> getCompressesImageResponse(byte[] imageBytes) {
        BiometricRecord sample = buildBiometricRecord(imageBytes);
        List<BiometricType> modalitiesToExtract = List.of(BiometricType.FACE);
        Map<String, String> flags = Map.of();

        Response<BiometricRecord> response = imageCompressorSDKV2.extractTemplate(sample, modalitiesToExtract, flags);
        return response;
    }

    public BiometricRecord buildBiometricRecord(byte[] imageData) {
        BiometricRecord biometricRecord = new BiometricRecord();
        VersionType versionType = new VersionType(1, 1);
        VersionType cbeffVersionType = new VersionType(1, 1);
        BIRInfo birInfo = new BIRInfo.BIRInfoBuilder().withIntegrity(false).build();
        RegistryIDType registryIDType = new RegistryIDType("257", "8");
        QualityType qualityType = new QualityType();
        qualityType.setAlgorithm(new RegistryIDType("HMAC", "SHA-256"));
        qualityType.setScore(100L);
        BDBInfo bdbInfo = new BDBInfo.BDBInfoBuilder()
                .withFormat(registryIDType)
                .withCreationDate(LocalDateTime.now())
                .withType(List.of(BiometricType.FACE))
                .withSubtype(List.of("Unknown"))
                .withPurpose(PurposeType.ENROLL)
                .withQuality(qualityType)
                .withLevel(ProcessedLevelType.RAW)
                .build();
        BIR bir = new BIR.BIRBuilder()
                .withBdbInfo(bdbInfo)
                .withBdb(imageData)
                .withBirInfo(birInfo)
                .withVersion(versionType)
                .withCbeffversion(cbeffVersionType)
                .build();
        biometricRecord.setBirInfo(birInfo);
        biometricRecord.setSegments(List.of(bir));
        biometricRecord.setOthers(new HashMap<>());

        return biometricRecord;
    }

    public byte[] doFaceConversion(String purpose, byte[] imageData) {
        ResponseStatus responseStatus = null;
        try {
            ConvertRequestDto requestDto = new ConvertRequestDto();
            requestDto.setModality("Face");
            requestDto.setPurpose(purpose);
            requestDto.setVersion("ISO19794_5_2011");

            // Convert JP2000 to Face ISO/IEC 19794-5: 2011
            if (imageData != null) {
                requestDto.setImageType(0);// 0 = jp2, 1 = wsq
                requestDto.setInputBytes(imageData);

                // get image quality = 40 by default
                return FaceEncoder.convertFaceImageToISO(requestDto);
            }
        } catch (Exception ex) {
            log.error("doFaceConversion::error", ex);
            responseStatus = ResponseStatus.UNKNOWN_ERROR;
            throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
        }
        throw new SDKException(ResponseStatus.UNKNOWN_ERROR + "", "null");
    }
}
