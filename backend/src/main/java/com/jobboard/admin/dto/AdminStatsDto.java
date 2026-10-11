package com.jobboard.admin.dto;

import com.jobboard.application.ApplicationStatus;
import com.jobboard.offer.ContractType;
import com.jobboard.user.Role;

import java.util.List;
import java.util.Map;

public record AdminStatsDto(
        long totalUsers,
        Map<Role, Long> usersByRole,
        long totalCompanies,
        long openOffers,
        long closedOffers,
        long totalApplications,
        Map<ApplicationStatus, Long> applicationsByStatus,
        List<MonthlyCount> applicationsPerMonth,
        Map<ContractType, Long> offersByContractType,
        List<CityCount> topCities) {

    /**
     * @param month mois au format ISO "YYYY-MM"
     */
    public record MonthlyCount(String month, long count) {
    }

    public record CityCount(String city, long count) {
    }
}
