package com.jobboard.offer.dto;

import com.jobboard.offer.OfferStatus;
import jakarta.validation.constraints.NotNull;

public record OfferStatusRequest(@NotNull OfferStatus status) {
}
