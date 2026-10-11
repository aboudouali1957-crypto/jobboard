package com.jobboard.application;

/**
 * Cycle de vie d'une candidature : RECEIVED → INTERVIEW → ACCEPTED | REJECTED.
 * Une candidature peut aussi être refusée directement après réception.
 */
public enum ApplicationStatus {
    RECEIVED,
    INTERVIEW,
    ACCEPTED,
    REJECTED;

    public boolean canTransitionTo(ApplicationStatus target) {
        return switch (this) {
            case RECEIVED -> target == INTERVIEW || target == REJECTED;
            case INTERVIEW -> target == ACCEPTED || target == REJECTED;
            case ACCEPTED, REJECTED -> false;
        };
    }

    public boolean isFinal() {
        return this == ACCEPTED || this == REJECTED;
    }
}
