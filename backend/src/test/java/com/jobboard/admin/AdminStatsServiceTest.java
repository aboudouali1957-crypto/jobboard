package com.jobboard.admin;

import com.jobboard.admin.dto.AdminStatsDto;
import com.jobboard.application.ApplicationStatus;
import com.jobboard.application.JobApplicationRepository;
import com.jobboard.company.CompanyRepository;
import com.jobboard.offer.ContractType;
import com.jobboard.offer.OfferRepository;
import com.jobboard.offer.OfferStatus;
import com.jobboard.user.Role;
import com.jobboard.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private UserRepository users;
    @Mock
    private CompanyRepository companies;
    @Mock
    private OfferRepository offers;
    @Mock
    private JobApplicationRepository applications;

    private AdminStatsService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), ZoneOffset.UTC);
        service = new AdminStatsService(users, companies, offers, applications, clock);
    }

    @Test
    void months_without_applications_are_filled_with_zero() {
        when(applications.countGroupedByMonthSince(Instant.parse("2026-05-01T00:00:00Z")))
                .thenReturn(List.<Object[]>of(new Object[]{2026, 8, 3L}, new Object[]{2026, 10, 5L}));

        assertThat(service.applicationsPerMonth()).containsExactly(
                new AdminStatsDto.MonthlyCount("2026-05", 0),
                new AdminStatsDto.MonthlyCount("2026-06", 0),
                new AdminStatsDto.MonthlyCount("2026-07", 0),
                new AdminStatsDto.MonthlyCount("2026-08", 3),
                new AdminStatsDto.MonthlyCount("2026-09", 0),
                new AdminStatsDto.MonthlyCount("2026-10", 5));
    }

    @Test
    void compute_aggregates_every_indicator() {
        when(users.countGroupedByRole()).thenReturn(List.<Object[]>of(
                new Object[]{Role.CANDIDATE, 4L}, new Object[]{Role.RECRUITER, 3L}));
        when(applications.countGroupedByStatus()).thenReturn(List.<Object[]>of(
                new Object[]{ApplicationStatus.RECEIVED, 6L}, new Object[]{ApplicationStatus.INTERVIEW, 2L}));
        when(offers.countGroupedByContractType()).thenReturn(List.<Object[]>of(new Object[]{ContractType.CDI, 9L}));
        when(offers.countGroupedByCity(any(Pageable.class))).thenReturn(List.<Object[]>of(new Object[]{"Paris", 5L}));
        when(companies.count()).thenReturn(3L);
        when(offers.countByStatus(OfferStatus.OPEN)).thenReturn(12L);
        when(offers.countByStatus(OfferStatus.CLOSED)).thenReturn(2L);
        when(applications.countGroupedByMonthSince(any(Instant.class))).thenReturn(List.of());

        AdminStatsDto stats = service.compute();

        assertThat(stats.totalUsers()).isEqualTo(7);
        assertThat(stats.usersByRole()).containsEntry(Role.ADMIN, 0L).containsEntry(Role.CANDIDATE, 4L);
        assertThat(stats.totalApplications()).isEqualTo(8);
        assertThat(stats.applicationsByStatus()).containsEntry(ApplicationStatus.ACCEPTED, 0L);
        assertThat(stats.offersByContractType()).containsEntry(ContractType.CDI, 9L).containsEntry(ContractType.STAGE, 0L);
        assertThat(stats.topCities()).containsExactly(new AdminStatsDto.CityCount("Paris", 5));
        assertThat(stats.openOffers()).isEqualTo(12);
        assertThat(stats.closedOffers()).isEqualTo(2);
        assertThat(stats.totalCompanies()).isEqualTo(3);
        assertThat(stats.applicationsPerMonth()).hasSize(AdminStatsService.MONTHS);
    }
}
