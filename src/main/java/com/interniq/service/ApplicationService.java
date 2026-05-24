package com.interniq.service;

import com.interniq.dto.ApplicationRequest;
import com.interniq.dto.ApplicationResponse;

import java.util.List;

public interface ApplicationService {

    // Create a new application tracker entry for the authenticated user
    ApplicationResponse createApplication(ApplicationRequest request, String username);

    // Get all applications for the authenticated user
    List<ApplicationResponse> getUserApplications(String username);

    // Get a single application by ID (only if it belongs to the user)
    ApplicationResponse getApplicationById(Long id, String username);

    // Partial update — status, notes, deadline can all be updated independently
    ApplicationResponse updateApplication(Long id, ApplicationRequest request, String username);

    // Hard delete — removes the tracker entry (not the listing itself)
    void deleteApplication(Long id, String username);
}