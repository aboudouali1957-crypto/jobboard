package com.jobboard.company;

public record CompanyDto(
        Long id,
        String name,
        String description,
        String website,
        String city) {

    public static CompanyDto from(Company company) {
        return new CompanyDto(company.getId(), company.getName(), company.getDescription(),
                company.getWebsite(), company.getCity());
    }
}
