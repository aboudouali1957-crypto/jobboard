package com.jobboard.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillRequest(@NotBlank @Size(max = 60) String name) {
}
