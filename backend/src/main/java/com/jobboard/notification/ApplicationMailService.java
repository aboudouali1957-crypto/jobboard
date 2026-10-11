package com.jobboard.notification;

import com.jobboard.application.ApplicationStatusChangedEvent;
import com.jobboard.config.AppProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Rédige et envoie l'email adressé au candidat à chaque étape de sa candidature.
 */
@Service
public class ApplicationMailService {

    private final JavaMailSender mailSender;
    private final AppProperties properties;

    public ApplicationMailService(JavaMailSender mailSender, AppProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void sendStatusChanged(ApplicationStatusChangedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.mail().from());
        message.setTo(event.candidateEmail());
        message.setSubject(subject(event));
        message.setText(body(event));
        mailSender.send(message);
    }

    String subject(ApplicationStatusChangedEvent event) {
        String prefix = switch (event.status()) {
            case RECEIVED -> "Candidature bien reçue";
            case INTERVIEW -> "Vous êtes invité(e) en entretien";
            case ACCEPTED -> "Bonne nouvelle pour votre candidature";
            case REJECTED -> "Réponse à votre candidature";
        };
        return prefix + " - " + event.offerTitle();
    }

    String body(ApplicationStatusChangedEvent event) {
        String company = event.companyName();
        String offer = "« " + event.offerTitle() + " »";
        String core = switch (event.status()) {
            case RECEIVED -> "Votre candidature au poste " + offer + " chez " + company
                    + " a bien été transmise au recruteur. Vous serez informé(e) de chaque évolution.";
            case INTERVIEW -> "Votre profil a retenu l'attention de " + company + " pour le poste " + offer
                    + ". Le recruteur souhaite vous rencontrer en entretien.";
            case ACCEPTED -> "Félicitations ! " + company + " a retenu votre candidature pour le poste " + offer + ".";
            case REJECTED -> company + " a étudié avec attention votre candidature pour le poste " + offer
                    + " mais n'a pas souhaité y donner suite. Nous vous souhaitons pleine réussite dans vos recherches.";
        };

        StringBuilder text = new StringBuilder()
                .append("Bonjour ").append(event.candidateFirstName()).append(",\n\n")
                .append(core).append("\n");
        if (event.recruiterMessage() != null) {
            text.append("\nMessage du recruteur :\n").append(event.recruiterMessage()).append("\n");
        }
        text.append("\nSuivez toutes vos candidatures sur votre espace : ")
                .append(properties.frontendUrl()).append("/candidate/applications\n\n")
                .append("L'équipe JobBoard");
        return text.toString();
    }
}
