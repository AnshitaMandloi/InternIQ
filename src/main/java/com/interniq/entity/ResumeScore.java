package com.interniq.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_scores")

public class ResumeScore {

   public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getMatchedSkills() {
		return matchedSkills;
	}

	public void setMatchedSkills(String matchedSkills) {
		this.matchedSkills = matchedSkills;
	}

	public String getMissingSkills() {
		return missingSkills;
	}

	public void setMissingSkills(String missingSkills) {
		this.missingSkills = missingSkills;
	}

	public String getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(String suggestions) {
		this.suggestions = suggestions;
	}

	public LocalDateTime getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(LocalDateTime generatedAt) {
		this.generatedAt = generatedAt;
	}

@Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "user_id", nullable = false)
   private User user;

   @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
   private String jobDescription;

   private Integer score;

   @Column(name = "matched_skills", columnDefinition = "TEXT")
   private String matchedSkills;

   @Column(name = "missing_skills", columnDefinition = "TEXT")
   private String missingSkills;

   @Column(columnDefinition = "TEXT")
   private String suggestions;

   @Column(name = "generated_at")
   private LocalDateTime generatedAt;

   @PrePersist
   protected void onCreate() {
       generatedAt = LocalDateTime.now();
   }
}