package com.interniq.service;

import com.interniq.dao.ApplicationRepository;
import com.interniq.dao.InternshipListingRepository;
import com.interniq.dao.UserRepository;
import com.interniq.dto.ApplicationRequest;
import com.interniq.dto.ApplicationResponse;
import com.interniq.entity.Application;
import com.interniq.entity.InternshipListing;
import com.interniq.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    // Valid status values — must match the ENUM in the DB
    private static final List<String> VALID_STATUSES = Arrays.asList(
            "SAVED", "APPLIED", "INTERVIEW", "OFFER", "REJECTED"
    );

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InternshipListingRepository listingRepository;

    // ─── CREATE ─────────────────────────────────────────────────────────────────

    @Override
    public ApplicationResponse createApplication(ApplicationRequest request, String username) {
        User user = getUser(username);

        // Verify the listing exists
        InternshipListing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Internship listing not found with ID: " + request.getListingId()));

        // Prevent duplicate tracking entries for the same listing
        if (applicationRepository.existsByUserIdAndInternshipListing_Id(user.getId(), listing.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "You are already tracking this internship. Use PATCH to update it.");
        }

        Application app = new Application();
        app.setUser(user);
        app.setInternshipListing(listing);
        app.setNotes(request.getNotes());
        app.setAppliedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        // Default to SAVED if no status provided
        String status = (request.getStatus() != null) ? request.getStatus().toUpperCase() : "SAVED";
        validateStatus(status);
        app.setStatus(status);

        // Parse optional deadline
        if (request.getDeadline() != null && !request.getDeadline().isBlank()) {
            app.setDeadline(parseDeadline(request.getDeadline()));
        }

        Application saved = applicationRepository.save(app);
        System.out.println("Application created: user=" + username + " listingId=" + listing.getId() + " status=" + status);
        return toResponse(saved);
    }

    // ─── READ ALL ────────────────────────────────────────────────────────────────

    @Override
    public List<ApplicationResponse> getUserApplications(String username) {
        User user = getUser(username);
        List<Application> applications = applicationRepository.findByUserIdOrderByAppliedAtDesc(user.getId());
        return applications.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── READ ONE ────────────────────────────────────────────────────────────────

    @Override
    public ApplicationResponse getApplicationById(Long id, String username) {
        User user = getUser(username);
        Application app = getOwnedApplication(id, user.getId());
        return toResponse(app);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────────

    @Override
    public ApplicationResponse updateApplication(Long id, ApplicationRequest request, String username) {
        User user = getUser(username);
        Application app = getOwnedApplication(id, user.getId());

        // Only update fields that are explicitly provided (partial update / PATCH behaviour)
        if (request.getStatus() != null) {
            String status = request.getStatus().toUpperCase();
            validateStatus(status);
            app.setStatus(status);
        }

        if (request.getNotes() != null) {
            app.setNotes(request.getNotes());
        }

        if (request.getDeadline() != null) {
            if (request.getDeadline().isBlank()) {
                // Allow clearing the deadline by sending an empty string
                app.setDeadline(null);
            } else {
                app.setDeadline(parseDeadline(request.getDeadline()));
            }
        }

        app.setUpdatedAt(LocalDateTime.now());

        Application updated = applicationRepository.save(app);
        System.out.println("Application updated: id=" + id + " user=" + username);
        return toResponse(updated);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────────

    @Override
    public void deleteApplication(Long id, String username) {
        User user = getUser(username);
        Application app = getOwnedApplication(id, user.getId());
        applicationRepository.delete(app);
        System.out.println("Application deleted: id=" + id + " user=" + username);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────────

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Application getOwnedApplication(Long appId, Long userId) {
        return applicationRepository.findByIdAndUserId(appId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application not found or does not belong to you"));
    }

    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status '" + status + "'. Allowed values: " + String.join(", ", VALID_STATUSES));
        }
    }

    private LocalDateTime parseDeadline(String deadline) {
        try {
            // Accept both "2025-08-01T00:00:00" and "2025-08-01 00:00:00"
            return LocalDateTime.parse(deadline.replace(" ", "T"));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid deadline format. Use ISO format: yyyy-MM-ddTHH:mm:ss (e.g. 2025-08-01T00:00:00)");
        }
    }

    // ─── ENTITY → DTO MAPPER ─────────────────────────────────────────────────────

    private ApplicationResponse toResponse(Application app) {
        ApplicationResponse res = new ApplicationResponse();
        res.setId(app.getId());
        res.setStatus(app.getStatus());
        res.setNotes(app.getNotes());
        res.setDeadline(app.getDeadline());
        res.setAppliedAt(app.getAppliedAt());
        res.setUpdatedAt(app.getUpdatedAt());

        // Denormalise listing fields so the frontend doesn't need a second call
        if (app.getInternshipListing() != null) {
            res.setListingId(app.getInternshipListing().getId());
            res.setCompanyName(app.getInternshipListing().getCompany());
            res.setJobTitle(app.getInternshipListing().getTitle());
            res.setApplyUrl(app.getInternshipListing().getApplyUrl());
        }

        return res;
    }
}