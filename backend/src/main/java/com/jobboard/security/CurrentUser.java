package com.jobboard.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Donne accès à l'identifiant de l'utilisateur connecté (claim "sub" du JWT).
 */
@Component
public class CurrentUser {

    public Long id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return Long.valueOf(jwtAuthentication.getToken().getSubject());
        }
        throw new AuthenticationCredentialsNotFoundException("Aucun utilisateur authentifié");
    }
}
