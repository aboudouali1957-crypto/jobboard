package com.jobboard.candidate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 30)
        @Pattern(regexp = "^$|^[+0-9 ().-]{6,30}$", message = "numéro de téléphone invalide")
        String phone,
        @Size(max = 100) String city,
        @Size(max = 150) String headline,
        @Size(max = 3000) String bio,
        @Size(max = 20) Set<Long> skillIds) {
}
