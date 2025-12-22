package com.puc.regionService.repository;

import com.puc.regionService.model.RegionDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RegionRepository extends MongoRepository<RegionDetails,String> {

    Optional<RegionDetails> findByRegionName(String regionName);
    boolean existsByRegionName(String regionName);
}
