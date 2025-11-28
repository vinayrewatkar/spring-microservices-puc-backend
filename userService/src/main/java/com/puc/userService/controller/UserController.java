package com.puc.userService.controller;

import com.puc.userService.dto.UserDto;
import com.puc.userService.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserProfileService userProfileService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        UserDto registeredUser = userProfileService.registerUser(userDto);
        return ResponseEntity.status(201).body(registeredUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        return userProfileService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return userProfileService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userProfileService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
