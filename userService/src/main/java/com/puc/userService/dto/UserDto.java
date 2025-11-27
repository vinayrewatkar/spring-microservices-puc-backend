package com.puc.userService.dto;

import com.puc.userService.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private String id;

    private String username;

    private String email;

    private String password;

    private UserType userType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
