package com.interniq.dao;

import com.interniq.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // All applications for a specific user, newest first
    List<Application> findByUserIdOrderByAppliedAtDesc(Long userId);

    // Single application — userId guard prevents users seeing each other's data
    Optional<Application> findByIdAndUserId(Long id, Long userId);

    // Check for duplicate: same user + same listing
    boolean existsByUserIdAndInternshipListing_Id(Long userId, Long listingId);

    // Filter by status for a user
    @Query("SELECT a FROM Application a WHERE a.user.id = :userId AND a.status = :status ORDER BY a.appliedAt DESC")
    List<Application> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    // ── Deadline Reminder Queries ──────────────────────────────────────────

    /**
     * Returns all applications whose deadline falls within [windowStart, windowEnd]
     * and whose status is not REJECTED or OFFER (no point reminding for closed apps).
     */
    @Query("""
            SELECT a FROM Application a
            WHERE a.deadline IS NOT NULL
              AND a.deadline >= :windowStart
              AND a.deadline <= :windowEnd
              AND a.status NOT IN ('REJECTED', 'OFFER')
            """)
    List<Application> findApplicationsWithDeadlineBetween(
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd
    );
}