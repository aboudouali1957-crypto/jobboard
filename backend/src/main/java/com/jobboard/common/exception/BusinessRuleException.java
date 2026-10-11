package com.jobboard.common.exception;

/**
 * Requête syntaxiquement valide mais contraire à une règle métier
 * (transition de statut interdite, CV manquant…). Renvoie un 422.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
