package com.jobboard.user;

/**
 * Vue minimale de l'utilisateur connecté, renvoyée avec le token.
 */
public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        Long companyId) {

    public static UserDto from(User user) {
        Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        return new UserDto(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole(), companyId);
    }
}
