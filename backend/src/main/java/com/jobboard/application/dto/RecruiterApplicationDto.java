package com.jobboard.application.dto;

import com.jobboard.application.ApplicationStatus;
import com.jobboard.application.JobApplication;
import com.jobboard.skill.SkillDto;
import com.jobboard.user.User;

import java.time.Instant;
import java.util.List;

/**
 * Candidature vue par le recruteur, avec le profil du candidat.
 */
public record RecruiterApplicationDto(
        Long id,
        Long offerId,
        String offerTitle,
        ApplicationStatus status,
        String coverLetter,
        String recruiterMessage,
        Candidate candidate,
        Instant createdAt,
        Instant updatedAt) {

    public record Candidate(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            String city,
            String headline,
            List<String> skills,
            boolean hasCv) {

        static Candidate from(User user) {
            return new Candidate(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                    user.getPhone(), user.getCity(), user.getHeadline(), SkillDto.names(user.getSkills()), user.hasCv());
        }
    }

    public static RecruiterApplicationDto from(JobApplication application) {
        return new RecruiterApplicationDto(
                application.getId(),
                application.getOffer().getId(),
                application.getOffer().getTitle(),
                application.getStatus(),
                application.getCoverLetter(),
                application.getRecruiterMessage(),
                Candidate.from(application.getCandidate()),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }
}
