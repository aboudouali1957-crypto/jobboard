package com.jobboard.admin.dto;

import jakarta.validation.constraints.NotNull;

public record UserEnabledRequest(@NotNull Boolean enabled) {
}
