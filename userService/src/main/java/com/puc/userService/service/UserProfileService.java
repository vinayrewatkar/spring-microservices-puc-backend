package com.puc.userService.service;


import com.puc.userService.dto.AuthResponse;
import com.puc.userService.dto.LoginRequest;
import com.puc.userService.dto.UserDto;
import com.puc.userService.entity.UserProfileEntity;
import com.puc.userService.repository.UserRepository;
import com.puc.userService.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserDto registerUser(UserDto userDto) {
        UserProfileEntity newUser = UserProfileEntity.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))  // Encode password
                .userType(userDto.getUserType())
                .createdAt(LocalDateTime.now())
                .build();

        UserProfileEntity savedUser = userRepository.save(newUser);
        return toDto(savedUser);
    }


    public AuthResponse authenticate(LoginRequest request){
        UserProfileEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid Credentials");
        }

        String token = jwtUtil.generateToken(user.getUsername(), String.valueOf(user.getUserType()));
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userType(String.valueOf(user.getUserType()))
                .build();
    }

    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
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

    public Optional<UserDto> getUserById(String id) {
        return userRepository.findById(id)
                .map(this::toDto);
    }

    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toDto);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
