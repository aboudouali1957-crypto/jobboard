package com.jobboard.auth.dto;

final class PasswordRules {

    static final String REGEX = "^(?=.*[A-Za-z])(?=.*\\d).+$";
    static final String MESSAGE = "doit contenir au moins une lettre et un chiffre";

    private PasswordRules() {
    }
}
