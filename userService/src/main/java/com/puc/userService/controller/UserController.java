package com.puc.userService.controller;

import com.puc.userService.dto.UserDto;
import com.puc.userService.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto){
        UserDto registeredUser = userProfileService.registerUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }
}
