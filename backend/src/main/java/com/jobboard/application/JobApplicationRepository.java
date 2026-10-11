package com.jobboard.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByOfferIdAndCandidateId(Long offerId, Long candidateId);

    Optional<JobApplication> findByIdAndCandidateId(Long id, Long candidateId);

    @Query("select a from JobApplication a where a.id = :id and a.offer.recruiter.id = :recruiterId")
    Optional<JobApplication> findByIdForRecruiter(@Param("id") Long id, @Param("recruiterId") Long recruiterId);

    @Query("""
            select a from JobApplication a
            where a.candidate.id = :candidateId
              and (:status is null or a.status = :status)
            """)
    Page<JobApplication> findForCandidate(@Param("candidateId") Long candidateId,
                                          @Param("status") ApplicationStatus status,
                                          Pageable pageable);

    @Query("""
            select a from JobApplication a
            where a.offer.recruiter.id = :recruiterId
              and (:offerId is null or a.offer.id = :offerId)
              and (:status is null or a.status = :status)
            """)
    Page<JobApplication> findForRecruiter(@Param("recruiterId") Long recruiterId,
                                          @Param("offerId") Long offerId,
                                          @Param("status") ApplicationStatus status,
                                          Pageable pageable);

    @Query("select a.offer.id from JobApplication a where a.candidate.id = :candidateId")
    List<Long> findOfferIdsByCandidateId(@Param("candidateId") Long candidateId);

    @Query("""
            select a.offer.id, a.status, count(a) from JobApplication a
            where a.offer.id in :offerIds
            group by a.offer.id, a.status
            """)
    List<Object[]> countGroupedByOfferAndStatus(@Param("offerIds") Collection<Long> offerIds);

    @Query("select a.status, count(a) from JobApplication a group by a.status")
    List<Object[]> countGroupedByStatus();

    @Query("""
            select year(a.createdAt), month(a.createdAt), count(a) from JobApplication a
            where a.createdAt >= :since
            group by year(a.createdAt), month(a.createdAt)
            """)
    List<Object[]> countGroupedByMonthSince(@Param("since") Instant since);

    @Modifying
    @Query("delete from JobApplication a where a.offer.id = :offerId")
    void deleteByOfferId(@Param("offerId") Long offerId);
}
