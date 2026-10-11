package com.jobboard.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 5000) String description,
        @Size(max = 255)
        @Pattern(regexp = "^$|^https?://.+", message = "doit commencer par http:// ou https://")
        String website,
        @Size(max = 100) String city) {
}
