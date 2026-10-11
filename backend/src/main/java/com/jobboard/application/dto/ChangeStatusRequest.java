package com.jobboard.application.dto;

import com.jobboard.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @param status  nouveau statut
 * @param message message facultatif transmis au candidat (date d'entretien, motif…)
 */
public record ChangeStatusRequest(
        @NotNull ApplicationStatus status,
        @Size(max = 1000) String message) {
}
