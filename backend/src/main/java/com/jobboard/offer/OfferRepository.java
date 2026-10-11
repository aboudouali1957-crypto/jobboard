package com.jobboard.offer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long>, JpaSpecificationExecutor<Offer> {

    Page<Offer> findByRecruiterId(Long recruiterId, Pageable pageable);

    long countByStatus(OfferStatus status);

    @Query("select distinct o.city from Offer o where o.status = :status order by o.city")
    List<String> findDistinctCities(@Param("status") OfferStatus status);

    @Query("select o.contractType, count(o) from Offer o group by o.contractType")
    List<Object[]> countGroupedByContractType();

    @Query("select o.city, count(o) from Offer o group by o.city order by count(o) desc")
    List<Object[]> countGroupedByCity(Pageable pageable);
}
