package com.interniq.controller;


import org.springframework.http.ResponseEntity;

import com.interniq.response.InternshipResponse;
import com.interniq.service.InternshipService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;

@RestController
@RequestMapping("/api/v1/internships")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Internships", description = "Fetch and search internship listings")
public class InternshipController {
   @Autowired
   private InternshipService internshipService ;
 
    /**
     * GET /api/v1/internships?query=java&location=india&page=1
     * Hits JSearch API → saves to MySQL → returns fresh results
     */
    @GetMapping
    @Operation(summary = "Fetch internships from JSearch API and cache in DB")
    public ResponseEntity<List<InternshipResponse>> fetchInternships(
            @RequestParam(defaultValue = "software") String query,
            @RequestParam(defaultValue = "India") String location,
            @RequestParam(defaultValue = "1") int page) {
 
        return ResponseEntity.ok(
                internshipService.fetchAndCacheInternships(query, location, page));
    }
 
    /**
     * GET /api/v1/internships/cached?search=react
     * Returns from DB only — no API call, no quota used
     */
    @GetMapping("/cached")
    @Operation(summary = "Search cached internship listings from DB")
    public ResponseEntity<List<InternshipResponse>> getCached(
            @RequestParam(required = false) String search) {
 
        return ResponseEntity.ok(internshipService.getCachedListings(search));
    }
}
 