package com.puc.rcVerificationService.repository;

import com.puc.rcVerificationService.entity.VehicleDetailsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleDetailsRepository extends MongoRepository<VehicleDetailsEntity, String> {
    boolean existsByRegNo(String regNo);
    Optional<VehicleDetailsEntity> findByRegNo(String regNo);
}
