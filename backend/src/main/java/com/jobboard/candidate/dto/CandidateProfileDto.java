package com.jobboard.candidate.dto;

import com.jobboard.skill.SkillDto;
import com.jobboard.user.User;

import java.time.Instant;
import java.util.List;

public record CandidateProfileDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String city,
        String headline,
        String bio,
        List<SkillDto> skills,
        Cv cv) {

    /**
     * Métadonnées du CV déposé, null si aucun CV.
     */
    public record Cv(String fileName, Instant uploadedAt) {
    }

    public static CandidateProfileDto from(User user) {
        Cv cv = user.hasCv() ? new Cv(user.getCvOriginalName(), user.getCvUploadedAt()) : null;
        return new CandidateProfileDto(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPhone(), user.getCity(), user.getHeadline(), user.getBio(),
                SkillDto.sorted(user.getSkills()), cv);
    }
}
