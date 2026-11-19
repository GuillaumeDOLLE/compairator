package com.will.compairator.user.dto;

import jakarta.validation.constraints.*;
import com.will.compairator.user.model.UserEntity;
import java.time.Instant;

public class UserDTO {
    public record Registration(@NotBlank @Email @Size(max = 254) String email,
                               @NotBlank @Size(min = 12, max = 128) String password) {}
    public record Profile(Long id, String email, String role, Instant createdAt) {
        public static Profile from(UserEntity user) {
            return new Profile(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
        }
    }
}
