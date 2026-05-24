package com.interniq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ApplicationRequest {

    // Required: link to the internship listing being tracked
    @NotNull(message = "Listing ID is required")
    private Long listingId;

    // Optional: user notes (cover letter snippets, reminders, etc.)
    private String notes;

    // Optional: deadline for this application (ISO string: "2025-08-01T00:00:00")
    private String deadline;

    // Optional on create — defaults to SAVED if omitted
    private String status; // SAVED | APPLIED | INTERVIEW | OFFER | REJECTED

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}