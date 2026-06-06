package com.interniq.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ResumeScoreResponse {

    private Long id;
    private String fileName;

    // Overall score out of 100
    private Integer overallScore;

    // Section-wise feedback objects
    private SectionFeedback skills;
    private SectionFeedback experience;
    private SectionFeedback summary;

    // Quick lists
    private java.util.List<String> matchedSkills;
    private java.util.List<String> missingSkills;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;

    // ── Inner class for section feedback ──────────────────────────
    public static class SectionFeedback {
        private Integer score;       // section score out of 100
        private String feedback;     // what's good / bad
        private String suggestion;   // how to improve

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }

        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }

    // ── Getters & Setters ──────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Integer getOverallScore() { return overallScore; }
    public void setOverallScore(Integer overallScore) { this.overallScore = overallScore; }

    public SectionFeedback getSkills() { return skills; }
    public void setSkills(SectionFeedback skills) { this.skills = skills; }

    public SectionFeedback getExperience() { return experience; }
    public void setExperience(SectionFeedback experience) { this.experience = experience; }

    public SectionFeedback getSummary() { return summary; }
    public void setSummary(SectionFeedback summary) { this.summary = summary; }

    public java.util.List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(java.util.List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public java.util.List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(java.util.List<String> missingSkills) { this.missingSkills = missingSkills; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}