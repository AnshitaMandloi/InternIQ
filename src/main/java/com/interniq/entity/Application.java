package com.interniq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ──────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)   // EAGER so toResponse() can read listing fields
    @JoinColumn(name = "listing_id")
    private InternshipListing internshipListing;

    // ── Status ─────────────────────────────────────────────────────────────────
    // Stored as VARCHAR matching the ENUM in the DB.
    // Validation happens in the service layer (not @Enumerated) so error messages are user-friendly.

    @Column(nullable = false)
    private String status = "SAVED";

    // ── User-provided fields ───────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    // ── Timestamps ─────────────────────────────────────────────────────────────

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Getters & Setters ──────────────────────────────────────────────────────

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

    public InternshipListing getInternshipListing() {
        return internshipListing;
    }

    public void setInternshipListing(InternshipListing internshipListing) {
        this.internshipListing = internshipListing;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}