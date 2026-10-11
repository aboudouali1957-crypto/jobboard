package com.jobboard.admin;

import com.jobboard.admin.dto.AdminStatsDto;
import com.jobboard.admin.dto.AdminStatsDto.CityCount;
import com.jobboard.admin.dto.AdminStatsDto.MonthlyCount;
import com.jobboard.application.ApplicationStatus;
import com.jobboard.application.JobApplicationRepository;
import com.jobboard.company.CompanyRepository;
import com.jobboard.offer.ContractType;
import com.jobboard.offer.OfferRepository;
import com.jobboard.offer.OfferStatus;
import com.jobboard.user.Role;
import com.jobboard.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agrège les indicateurs du tableau de bord administrateur.
 * Tous les décomptes sont calculés en base par des requêtes GROUP BY.
 */
@Service
@Transactional(readOnly = true)
public class AdminStatsService {

    static final int MONTHS = 6;
    private static final int TOP_CITIES = 5;

    private final UserRepository users;
    private final CompanyRepository companies;
    private final OfferRepository offers;
    private final JobApplicationRepository applications;
    private final Clock clock;

    @Autowired
    public AdminStatsService(UserRepository users, CompanyRepository companies, OfferRepository offers,
                             JobApplicationRepository applications) {
        this(users, companies, offers, applications, Clock.systemUTC());
    }

    /** Constructeur utilisé par les tests pour figer la date courante. */
    AdminStatsService(UserRepository users, CompanyRepository companies, OfferRepository offers,
                      JobApplicationRepository applications, Clock clock) {
        this.users = users;
        this.companies = companies;
        this.offers = offers;
        this.applications = applications;
        this.clock = clock;
    }

    public AdminStatsDto compute() {
        Map<Role, Long> usersByRole = countByEnum(Role.class, users.countGroupedByRole());
        Map<ApplicationStatus, Long> applicationsByStatus =
                countByEnum(ApplicationStatus.class, applications.countGroupedByStatus());
        Map<ContractType, Long> offersByContract =
                countByEnum(ContractType.class, offers.countGroupedByContractType());

        List<CityCount> topCities = offers.countGroupedByCity(PageRequest.of(0, TOP_CITIES)).stream()
                .map(row -> new CityCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        return new AdminStatsDto(
                sum(usersByRole),
                usersByRole,
                companies.count(),
                offers.countByStatus(OfferStatus.OPEN),
                offers.countByStatus(OfferStatus.CLOSED),
                sum(applicationsByStatus),
                applicationsByStatus,
                applicationsPerMonth(),
                offersByContract,
                topCities);
    }

    /**
     * Nombre de candidatures sur les {@value #MONTHS} derniers mois (mois courant inclus),
     * les mois sans candidature valant 0.
     */
    List<MonthlyCount> applicationsPerMonth() {
        YearMonth current = YearMonth.now(clock);
        YearMonth first = current.minusMonths(MONTHS - 1L);

        Map<YearMonth, Long> counts = new HashMap<>();
        for (Object[] row : applications.countGroupedByMonthSince(first.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC))) {
            YearMonth month = YearMonth.of(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
            counts.put(month, ((Number) row[2]).longValue());
        }

        List<MonthlyCount> result = new ArrayList<>(MONTHS);
        for (YearMonth month = first; !month.isAfter(current); month = month.plusMonths(1)) {
            result.add(new MonthlyCount(month.toString(), counts.getOrDefault(month, 0L)));
        }
        return result;
    }

    private static <E extends Enum<E>> Map<E, Long> countByEnum(Class<E> type, List<Object[]> rows) {
        Map<E, Long> result = new EnumMap<>(type);
        for (E value : type.getEnumConstants()) {
            result.put(value, 0L);
        }
        for (Object[] row : rows) {
            result.put(type.cast(row[0]), ((Number) row[1]).longValue());
        }
        return result;
    }

    private static long sum(Map<?, Long> counts) {
        return counts.values().stream().mapToLong(Long::longValue).sum();
    }
}
