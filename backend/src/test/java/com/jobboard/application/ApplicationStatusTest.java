package com.jobboard.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationStatusTest {

    @ParameterizedTest(name = "{0} -> {1} : {2}")
    @CsvSource({
            "RECEIVED,  INTERVIEW, true",
            "RECEIVED,  REJECTED,  true",
            "RECEIVED,  ACCEPTED,  false",
            "RECEIVED,  RECEIVED,  false",
            "INTERVIEW, ACCEPTED,  true",
            "INTERVIEW, REJECTED,  true",
            "INTERVIEW, RECEIVED,  false",
            "ACCEPTED,  REJECTED,  false",
            "REJECTED,  INTERVIEW, false"
    })
    void respects_the_application_lifecycle(ApplicationStatus from, ApplicationStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }

    @Test
    void accepted_and_rejected_are_final() {
        assertThat(ApplicationStatus.ACCEPTED.isFinal()).isTrue();
        assertThat(ApplicationStatus.REJECTED.isFinal()).isTrue();
        assertThat(ApplicationStatus.RECEIVED.isFinal()).isFalse();
        assertThat(ApplicationStatus.INTERVIEW.isFinal()).isFalse();
    }
}
