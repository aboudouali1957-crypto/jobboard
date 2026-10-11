package com.jobboard.common.exception;

/**
 * Conflit avec l'état actuel des données (email déjà utilisé, candidature en double…). Renvoie un 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
