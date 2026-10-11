package com.jobboard.notification;

import com.jobboard.application.ApplicationStatusChangedEvent;
import com.jobboard.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envoie la notification uniquement si la transaction métier a été validée,
 * et de manière asynchrone pour ne pas ralentir ni faire échouer la requête HTTP.
 */
@Component
public class ApplicationNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(ApplicationNotificationListener.class);

    private final ApplicationMailService mailService;
    private final AppProperties properties;

    public ApplicationNotificationListener(ApplicationMailService mailService, AppProperties properties) {
        this.mailService = mailService;
        this.properties = properties;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(ApplicationStatusChangedEvent event) {
        if (!properties.mail().enabled()) {
            log.info("Emails désactivés : notification {} ignorée pour la candidature {}",
                    event.status(), event.applicationId());
            return;
        }
        try {
            mailService.sendStatusChanged(event);
            log.info("Email {} envoyé à {} (candidature {})",
                    event.status(), event.candidateEmail(), event.applicationId());
        } catch (MailException e) {
            log.warn("Échec de l'envoi de l'email pour la candidature {} : {}", event.applicationId(), e.getMessage());
        }
    }
}
