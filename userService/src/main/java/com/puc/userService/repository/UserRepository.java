package com.puc.userService.repository;

import com.puc.userService.entity.UserProfileEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserProfileEntity,String> {

    boolean existsByEmail(String email);

}