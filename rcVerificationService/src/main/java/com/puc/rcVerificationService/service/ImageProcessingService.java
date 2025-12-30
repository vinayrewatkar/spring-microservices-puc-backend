package com.puc.rcVerificationService.service;

import com.puc.rcVerificationService.dto.ProgressEvent;
import com.puc.rcVerificationService.dto.VehicleDetailsDto;
import com.puc.rcVerificationService.entity.VehicleDetailsEntity;
import com.puc.rcVerificationService.kafka.ProgressPublisher;
import com.puc.rcVerificationService.repository.VehicleDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final OcrProcessingService ocrProcessingService;
    private final RcNumberParsingService rcNumberParsingService;
    private final RcVerificationService rcVerificationService;
    private final VehicleDetailsRepository vehicleDetailsRepository;
    private final ProgressPublisher progressPublisher;

    public VehicleDetailsDto verifyVehicle(List<com.puc.rcVerificationService.utils.FileData> extractedFiles,
                                           String jobId,
                                           String userId) {

        // Entry
        publish(jobId, userId, "RC_SERVICE_STARTED", "STARTED", "RC verification service started", 55);

        // 1) OCR
        publish(jobId, userId, "OCR_STARTED", "STARTED", "Starting OCR processing", 60);
        List<String> ocrTexts = ocrProcessingService.processOcr(extractedFiles);
        if (ocrTexts.isEmpty()) {
            publish(jobId, userId, "OCR_FAILED", "FAILED", "No text extracted from images", 60);
            throw new RuntimeException("No text extracted from images");
        }
        publish(jobId, userId, "OCR_DONE", "DONE", "OCR processing completed", 70);

        // 2) Parse RC number
        publish(jobId, userId, "RC_PARSING_STARTED", "STARTED", "Parsing RC number from OCR text", 72);
        List<String> parsedRcNumbers = rcNumberParsingService.parseRcNumber(ocrTexts);
        if (parsedRcNumbers.isEmpty()) {
            publish(jobId, userId, "RC_PARSING_FAILED", "FAILED", "No RC number found in text", 72);
            throw new RuntimeException("No RC number parsed from OCR text");
        }
        String rcNumber = parsedRcNumbers.get(0);
        publish(jobId, userId, "RC_PARSING_DONE", "DONE", "RC number parsed: " + rcNumber, 75);

        // 3) Check DB
        publish(jobId, userId, "DB_CHECK_STARTED", "STARTED", "Checking database for existing records", 77);
        if (vehicleDetailsRepository.existsByRegNo(rcNumber)) {
            log.info("Vehicle details found in DB for RC number: {}", rcNumber);
            publish(jobId, userId, "DB_CHECK_DONE", "DONE", "Found in database", 100);
            VehicleDetailsEntity entity = vehicleDetailsRepository.findByRegNo(rcNumber)
                    .orElseThrow(() -> new RuntimeException("DB inconsistency"));
            return mapEntityToDto(entity);
        }
        publish(jobId, userId, "DB_CHECK_DONE", "DONE", "Not found in database, calling RC API", 80);

        // 4) Call RC API
        publish(jobId, userId, "RC_API_CALL_STARTED", "STARTED", "Calling external RC verification API", 82);
        VehicleDetailsDto vehicleDetailsDto = rcVerificationService.performPucValidation(rcNumber);
        publish(jobId, userId, "RC_API_CALL_DONE", "DONE", "RC API returned result", 90);

        // 5) Save to DB
        publish(jobId, userId, "DB_SAVE_STARTED", "STARTED", "Saving to database", 92);
        VehicleDetailsEntity toSave = mapDtoToEntity(vehicleDetailsDto);
        vehicleDetailsRepository.save(toSave);
        publish(jobId, userId, "DB_SAVE_DONE", "DONE", "Saved to database", 95);

        publish(jobId, userId, "RC_SERVICE_DONE", "DONE", "RC verification completed", 98);
        return vehicleDetailsDto;
    }

    private void publish(String jobId, String userId, String step, String status, String message, Integer progress) {
        progressPublisher.publish(ProgressEvent.builder()
                .jobId(jobId)
                .userId(userId)
                .service("rcVerificationService")
                .step(step)
                .status(status)
                .message(message)
                .progress(progress)
                .ts(System.currentTimeMillis())
                .build());
    }

    private VehicleDetailsDto mapEntityToDto(VehicleDetailsEntity entity) {
        return VehicleDetailsDto.builder()
                .regNo(entity.getRegNo())
                .ownerName(entity.getOwnerName())
                .model(entity.getModel())
                .state(entity.getState())
                .vehicleClassDesc(entity.getVehicleClassDesc())
                .fitnessUpto(entity.getFitnessUpto())
                .insuranceUpto(entity.getInsuranceUpto())
                .pucUpto(entity.getPucUpto())
                .build();
    }

    private VehicleDetailsEntity mapDtoToEntity(VehicleDetailsDto dto) {
        return VehicleDetailsEntity.builder()
                .regNo(dto.getRegNo())
                .ownerName(dto.getOwnerName())
                .model(dto.getModel())
                .state(dto.getState())
                .vehicleClassDesc(dto.getVehicleClassDesc())
                .fitnessUpto(dto.getFitnessUpto())
                .insuranceUpto(dto.getInsuranceUpto())
                .pucUpto(dto.getPucUpto())
                .build();
    }
}
