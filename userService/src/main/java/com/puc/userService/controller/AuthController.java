package com.puc.userService.controller;


import com.puc.userService.dto.AuthResponse;
import com.puc.userService.dto.LoginRequest;
import com.puc.userService.dto.UserDto;
import com.puc.userService.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserProfileService userProfileService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserDto userDto) {
        UserDto registered = userProfileService.registerUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = userProfileService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = userProfileService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}
