package com.interniq.controller;

import com.interniq.dto.ApplicationRequest;
import com.interniq.dto.ApplicationResponse;
import com.interniq.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "Application Tracker", description = "Track internship applications with status, notes, and deadlines")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // ─── POST /api/v1/applications ───────────────────────────────────────────────
    // Save a new internship to the tracker. Default status = SAVED.
    @PostMapping
    @Operation(
        summary = "Track a new internship",
        description = "Add a listing to your tracker. Provide listingId (from /api/v1/internships). " +
                      "Status defaults to SAVED if not specified."
    )
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ApplicationResponse response = applicationService.createApplication(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── GET /api/v1/applications ────────────────────────────────────────────────
    // Returns all applications for the logged-in user, newest first.
    @GetMapping
    @Operation(
        summary = "Get all my tracked applications",
        description = "Returns every tracked application for the authenticated user, ordered by date added (newest first)."
    )
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ApplicationResponse> applications = applicationService.getUserApplications(userDetails.getUsername());
        return ResponseEntity.ok(applications);
    }

    // ─── GET /api/v1/applications/{id} ───────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(
        summary = "Get a single tracked application",
        description = "Returns details of one application by its ID. Returns 404 if it doesn't exist or belongs to another user."
    )
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ApplicationResponse response = applicationService.getApplicationById(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // ─── PATCH /api/v1/applications/{id} ─────────────────────────────────────────
    // Partial update — send only the fields you want to change.
    // e.g. { "status": "INTERVIEW" } to move the Kanban card.
    @PatchMapping("/{id}")
    @Operation(
        summary = "Update application status, notes, or deadline",
        description = "Partial update. Send only the fields to change. " +
                      "Valid statuses: SAVED, APPLIED, INTERVIEW, OFFER, REJECTED. " +
                      "Send deadline as empty string \"\" to clear it."
    )
    public ResponseEntity<ApplicationResponse> updateApplication(
            @PathVariable Long id,
            @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ApplicationResponse response = applicationService.updateApplication(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // ─── DELETE /api/v1/applications/{id} ────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Remove application from tracker",
        description = "Deletes the tracker entry. The internship listing itself is NOT deleted."
    )
    public ResponseEntity<Void> deleteApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        applicationService.deleteApplication(id, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // 204
    }
}