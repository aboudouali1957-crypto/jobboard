package com.jobboard.admin.dto;

import com.jobboard.user.Role;
import com.jobboard.user.User;

import java.time.Instant;

public record AdminUserDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        boolean enabled,
        String companyName,
        Instant createdAt) {

    public static AdminUserDto from(User user) {
        String companyName = user.getCompany() != null ? user.getCompany().getName() : null;
        return new AdminUserDto(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole(), user.isEnabled(), companyName, user.getCreatedAt());
    }
}
