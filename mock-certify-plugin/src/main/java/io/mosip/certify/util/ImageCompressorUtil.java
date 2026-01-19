package io.mosip.certify.util;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.face.FaceEncoder;
import io.mosip.certify.mock.integration.service.ImageCompressorServiceImpl;
import io.mosip.image.compressor.sdk.constant.ResponseStatus;
import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2;
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

    public Response<BiometricRecord> getCompressedImageResponse(byte[] imageBytes) {
        BiometricRecord sample = buildBiometricRecord(imageBytes);
        List<BiometricType> modalitiesToExtract = List.of(BiometricType.FACE);
        Map<String, String> flags = Map.of();

        ImageCompressionService imageCompressionService = new ImageCompressionService(env, sample, modalitiesToExtract, flags);

        return imageCompressionService.getExtractTemplateInfo();
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

    public byte[] convertFromImageToISO(String purpose, byte[] imageData) {
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

    public byte[] convertFromISOToImage(String purpose, byte[] isoImage) {
        ResponseStatus responseStatus = null;
        try {
            ConvertRequestDto requestDto = new ConvertRequestDto();
            requestDto.setModality("Face");
            requestDto.setPurpose(purpose);
            requestDto.setVersion("ISO19794_5_2011");

            // Convert Face ISO/IEC 19794-5: 2011 to JP2000
            if (isoImage != null) {
                requestDto.setImageType(0);// 0 = jp2, 1 = wsq
                requestDto.setInputBytes(isoImage);

                // get image quality = 40 by default
                return FaceDecoder.convertFaceISOToImageBytes(requestDto);
            }
        } catch (Exception ex) {
            log.error("doFaceConversion::error", ex);
            responseStatus = ResponseStatus.UNKNOWN_ERROR;
            throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
        }
        throw new SDKException(ResponseStatus.UNKNOWN_ERROR + "", "null");
    }

    public static byte[] getBirData(BIR bir) {
        BiometricType biometricType = bir.getBdbInfo().getType().get(0);
        PurposeType purposeType = bir.getBdbInfo().getPurpose();
        List<String> bioSubTypeList = bir.getBdbInfo().getSubtype();

        String bioSubType = null;
        if (bioSubTypeList != null && !bioSubTypeList.isEmpty()) {
            bioSubType = bioSubTypeList.get(0).trim();
            if (bioSubTypeList.size() >= 2)
                bioSubType += " " + bioSubTypeList.get(1).trim();
        }

        if (isValidBIRParams(bir, biometricType, bioSubType)) {
            return getBDBData(purposeType, biometricType, bioSubType, bir.getBdb());
        }
        throw new SDKException(ResponseStatus.UNKNOWN_ERROR + "", "null");
    }

    public static boolean isValidBIRParams(BIR segment, BiometricType bioType, String bioSubType) {
        ResponseStatus responseStatus = null;
        if (bioType == BiometricType.FACE)
            return true;
        else {
            log.error("isValidBIRParams::BiometricType{} BioSubType{}", bioType, bioSubType);
            responseStatus = ResponseStatus.MISSING_INPUT;
            throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
        }
    }

    public static byte[] getBDBData(PurposeType purposeType, BiometricType bioType, String bioSubType, byte[] bdbData) {
        ResponseStatus responseStatus = null;

        if (bdbData != null && bdbData.length != 0) {
            return getBiometricData(purposeType, bioType, bioSubType, Util.encodeToURLSafeBase64(bdbData));
        }

        responseStatus = ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF;
        throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
    }

    public static byte[] getBiometricData(PurposeType purposeType, BiometricType bioType, String bioSubType,
                                       String bdbData) {
        ResponseStatus responseStatus = null;
        if (bioType == BiometricType.FACE)
            return getFaceBdb(purposeType, bioSubType, bdbData);
        responseStatus = ResponseStatus.INVALID_INPUT;
        throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
    }

    public static byte[] getFaceBdb(PurposeType purposeType, String biometricSubType, String bdbData) {
        ResponseStatus responseStatus = null;
        try {
            ConvertRequestDto requestDto = new ConvertRequestDto();
            requestDto.setModality("Face");
            requestDto.setVersion("ISO19794_5_2011");
            byte[] bioData = Util.decodeURLSafeBase64(bdbData);
            requestDto.setInputBytes(bioData);

            FaceBDIR bdir = FaceDecoder.getFaceBDIR(requestDto);
            return bdir.getImage();
        } catch (Exception ex) {
            log.error("getFaceBdb -- error", ex);
            responseStatus = ResponseStatus.INVALID_INPUT;
            throw new SDKException(responseStatus.getStatusCode() + "",
                    responseStatus.getStatusMessage() + " " + ex.getLocalizedMessage());
        }
    }
}
