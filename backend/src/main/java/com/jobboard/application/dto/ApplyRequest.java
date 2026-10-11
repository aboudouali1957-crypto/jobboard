package com.jobboard.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplyRequest(
        @NotNull Long offerId,
        @Size(max = 5000) String coverLetter) {
}
