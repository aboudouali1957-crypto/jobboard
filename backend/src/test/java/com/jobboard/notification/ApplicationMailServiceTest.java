package com.jobboard.notification;

import com.jobboard.TestData;
import com.jobboard.application.ApplicationStatus;
import com.jobboard.application.ApplicationStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationMailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private ApplicationMailService service;

    @BeforeEach
    void setUp() {
        service = new ApplicationMailService(mailSender, TestData.properties());
    }

    @Test
    void sends_the_email_to_the_candidate() {
        service.sendStatusChanged(event(ApplicationStatus.RECEIVED, null));

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertThat(message.getValue().getTo()).containsExactly("camille@example.com");
        assertThat(message.getValue().getFrom()).isEqualTo("JobBoard <no-reply@jobboard.dev>");
        assertThat(message.getValue().getSubject()).isEqualTo("Candidature bien reçue - Développeur Java");
    }

    @Test
    void interview_email_contains_the_recruiter_message_and_a_link() {
        String body = service.body(event(ApplicationStatus.INTERVIEW, "Disponible mardi à 10h ?"));

        assertThat(body)
                .startsWith("Bonjour Camille,")
                .contains("entretien")
                .contains("Message du recruteur :\nDisponible mardi à 10h ?")
                .contains("http://localhost:4200/candidate/applications");
    }

    @Test
    void each_status_has_its_own_subject() {
        assertThat(service.subject(event(ApplicationStatus.INTERVIEW, null))).startsWith("Vous êtes invité(e) en entretien");
        assertThat(service.subject(event(ApplicationStatus.ACCEPTED, null))).startsWith("Bonne nouvelle");
        assertThat(service.subject(event(ApplicationStatus.REJECTED, null))).startsWith("Réponse à votre candidature");
    }

    @Test
    void rejection_email_has_no_recruiter_message_section_when_empty() {
        assertThat(service.body(event(ApplicationStatus.REJECTED, null))).doesNotContain("Message du recruteur");
    }

    private static ApplicationStatusChangedEvent event(ApplicationStatus status, String message) {
        return new ApplicationStatusChangedEvent(1L, "camille@example.com", "Camille",
                "Développeur Java", "Nimbus Tech", status, message);
    }
}
