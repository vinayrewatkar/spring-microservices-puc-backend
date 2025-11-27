package com.puc.rcVerificationService.service;

import com.puc.rcVerificationService.dto.VehicleDetailsDto;
import com.puc.rcVerificationService.entity.VehicleDetailsEntity;
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

    /**
     * Main flow: Accept image bytes, apply OCR, parse RC number,
     * check database, if missing call RC API, save and return data.
     *
     * @param extractedFiles list of images to OCR
     * @return VehicleDetailsDto with vehicle info
     */
    public VehicleDetailsDto verifyVehicle(List<com.puc.rcVerificationService.utils.FileData> extractedFiles) {
        // 1) Call OCR service to get text blocks
        List<String> ocrTexts = ocrProcessingService.processOcr(extractedFiles);
        if (ocrTexts.isEmpty()) {
            throw new RuntimeException("No text extracted from images");
        }

        // 2) Parse RC numbers from OCR text
        List<String> parsedRcNumbers = rcNumberParsingService.parseRcNumber(ocrTexts);
        if (parsedRcNumbers.isEmpty()) {
            throw new RuntimeException("No RC number parsed from OCR text");
        }

        // Pick first candidate RC number for verification
        String rcNumber = parsedRcNumbers.get(0);

        // 3) Check if vehicle details exist in DB
        if (vehicleDetailsRepository.existsByRegNo(rcNumber)) {
            log.info("Vehicle details found in DB for RC number: {}", rcNumber);
            VehicleDetailsEntity entity = vehicleDetailsRepository.findByRegNo(rcNumber)
                    .orElseThrow(() -> new RuntimeException("DB inconsistency: Vehicle details not found despite existence"));
            return mapEntityToDto(entity);
        }

        // 4) Call RC verification service for fresh data
        VehicleDetailsDto vehicleDetailsDto = rcVerificationService.performPucValidation(rcNumber);

        // 5) Save new details to DB
        VehicleDetailsEntity toSave = mapDtoToEntity(vehicleDetailsDto);
        vehicleDetailsRepository.save(toSave);

        return vehicleDetailsDto;
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
