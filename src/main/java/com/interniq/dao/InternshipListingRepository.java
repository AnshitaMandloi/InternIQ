package com.interniq.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.interniq.entity.InternshipListing;

import java.util.List;
import java.util.Optional;
 
public interface InternshipListingRepository extends JpaRepository<InternshipListing, Long> {
 
    boolean existsByExternalId(String externalId);
 
    Optional<InternshipListing> findByExternalId(String externalId);
 
    List<InternshipListing> findTop50ByOrderByFetchedAtDesc();
 
    @Query("SELECT l FROM InternshipListing l WHERE " +
           "LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<InternshipListing> searchByKeyword(@Param("keyword") String keyword);
}