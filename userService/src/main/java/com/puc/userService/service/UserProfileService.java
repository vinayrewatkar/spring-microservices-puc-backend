package com.puc.userService.service;


import com.puc.userService.dto.UserDto;
import com.puc.userService.entity.UserProfileEntity;
import com.puc.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

    public UserDto registerUser(UserDto userDto){
        UserProfileEntity newUser = toEntity(userDto);
        UserProfileEntity savedUser = userRepository.save(newUser);
        return toDto(savedUser);
    }

    public UserProfileEntity toEntity(UserDto userDto){
        return UserProfileEntity.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .userType(userDto.getUserType())
                .build();
    }

    public UserDto toDto(UserProfileEntity user){
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userType(user.getUserType())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
