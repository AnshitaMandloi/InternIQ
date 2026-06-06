package com.interniq.dao;

import com.interniq.entity.ResumeScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeScoreRepository extends JpaRepository<ResumeScore, Long> {

    // All scores for a user, newest first
    List<ResumeScore> findByUserIdOrderByGeneratedAtDesc(Long userId);
}