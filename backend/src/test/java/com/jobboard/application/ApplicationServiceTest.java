package com.jobboard.application;

import com.jobboard.TestData;
import com.jobboard.application.dto.ApplyRequest;
import com.jobboard.application.dto.ChangeStatusRequest;
import com.jobboard.application.dto.RecruiterApplicationDto;
import com.jobboard.common.exception.BusinessRuleException;
import com.jobboard.common.exception.ConflictException;
import com.jobboard.common.exception.NotFoundException;
import com.jobboard.company.Company;
import com.jobboard.offer.Offer;
import com.jobboard.offer.OfferRepository;
import com.jobboard.offer.OfferStatus;
import com.jobboard.storage.FileStorageService;
import com.jobboard.user.User;
import com.jobboard.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    private static final Long CANDIDATE_ID = 1L;
    private static final Long RECRUITER_ID = 2L;
    private static final Long OFFER_ID = 3L;
    private static final Long APPLICATION_ID = 4L;

    @Mock
    private JobApplicationRepository applications;
    @Mock
    private OfferRepository offers;
    @Mock
    private UserRepository users;
    @Mock
    private FileStorageService storage;
    @Mock
    private ApplicationEventPublisher events;

    private ApplicationService service;
    private Offer offer;

    @BeforeEach
    void setUp() {
        service = new ApplicationService(applications, offers, users, storage, events);
        Company company = TestData.company(10L);
        User recruiter = TestData.recruiter(RECRUITER_ID, company);
        offer = TestData.offer(OFFER_ID, recruiter);
    }

    @Test
    void apply_requires_a_cv() {
        when(users.findById(CANDIDATE_ID)).thenReturn(Optional.of(TestData.candidate(CANDIDATE_ID, false)));
        when(offers.findById(OFFER_ID)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> service.apply(CANDIDATE_ID, new ApplyRequest(OFFER_ID, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CV");
        verify(applications, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void apply_refuses_a_closed_offer() {
        offer.setStatus(OfferStatus.CLOSED);
        when(users.findById(CANDIDATE_ID)).thenReturn(Optional.of(TestData.candidate(CANDIDATE_ID, true)));
        when(offers.findById(OFFER_ID)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> service.apply(CANDIDATE_ID, new ApplyRequest(OFFER_ID, null)))
                .isInstanceOf(BusinessRuleException.class);
        verify(applications, never()).save(any());
    }

    @Test
    void apply_refuses_a_duplicate_application() {
        when(users.findById(CANDIDATE_ID)).thenReturn(Optional.of(TestData.candidate(CANDIDATE_ID, true)));
        when(offers.findById(OFFER_ID)).thenReturn(Optional.of(offer));
        when(applications.existsByOfferIdAndCandidateId(OFFER_ID, CANDIDATE_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.apply(CANDIDATE_ID, new ApplyRequest(OFFER_ID, "Bonjour")))
                .isInstanceOf(ConflictException.class);
        verify(applications, never()).save(any());
    }

    @Test
    void apply_saves_the_application_and_notifies_the_candidate() {
        when(users.findById(CANDIDATE_ID)).thenReturn(Optional.of(TestData.candidate(CANDIDATE_ID, true)));
        when(offers.findById(OFFER_ID)).thenReturn(Optional.of(offer));
        when(applications.existsByOfferIdAndCandidateId(OFFER_ID, CANDIDATE_ID)).thenReturn(false);
        when(applications.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.apply(CANDIDATE_ID, new ApplyRequest(OFFER_ID, "  Ma lettre  "));

        assertThat(result.status()).isEqualTo(ApplicationStatus.RECEIVED);
        assertThat(result.offerTitle()).isEqualTo(offer.getTitle());

        ArgumentCaptor<ApplicationStatusChangedEvent> event = ArgumentCaptor.forClass(ApplicationStatusChangedEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().status()).isEqualTo(ApplicationStatus.RECEIVED);
        assertThat(event.getValue().candidateEmail()).isEqualTo("camille@example.com");
        assertThat(event.getValue().companyName()).isEqualTo("Nimbus Tech");
    }

    @Test
    void change_status_moves_the_application_and_publishes_an_event() {
        JobApplication application = receivedApplication();
        when(applications.findByIdForRecruiter(APPLICATION_ID, RECRUITER_ID)).thenReturn(Optional.of(application));

        RecruiterApplicationDto result = service.changeStatus(RECRUITER_ID, APPLICATION_ID,
                new ChangeStatusRequest(ApplicationStatus.INTERVIEW, " Disponible mardi à 10h ? "));

        assertThat(result.status()).isEqualTo(ApplicationStatus.INTERVIEW);
        assertThat(result.recruiterMessage()).isEqualTo("Disponible mardi à 10h ?");

        ArgumentCaptor<ApplicationStatusChangedEvent> event = ArgumentCaptor.forClass(ApplicationStatusChangedEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().status()).isEqualTo(ApplicationStatus.INTERVIEW);
        assertThat(event.getValue().recruiterMessage()).isEqualTo("Disponible mardi à 10h ?");
    }

    @Test
    void change_status_rejects_a_forbidden_transition() {
        JobApplication application = receivedApplication();
        when(applications.findByIdForRecruiter(APPLICATION_ID, RECRUITER_ID)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> service.changeStatus(RECRUITER_ID, APPLICATION_ID,
                new ChangeStatusRequest(ApplicationStatus.ACCEPTED, null)))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.RECEIVED);
        verifyNoInteractions(events);
    }

    @Test
    void change_status_hides_applications_of_other_recruiters() {
        when(applications.findByIdForRecruiter(APPLICATION_ID, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStatus(99L, APPLICATION_ID,
                new ChangeStatusRequest(ApplicationStatus.INTERVIEW, null)))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(events);
    }

    private JobApplication receivedApplication() {
        return TestData.withId(new JobApplication(offer, TestData.candidate(CANDIDATE_ID, true), null), APPLICATION_ID);
    }
}
