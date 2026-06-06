package com.interniq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interniq.dao.InternshipListingRepository;
import com.interniq.entity.InternshipListing;
import com.interniq.response.InternshipResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InternshipServiceImpl implements InternshipService {

    @Autowired
    private InternshipListingRepository listingRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.serpapi.key}")
    private String serpApiKey;

    private static final String SERPAPI_URL = "https://serpapi.com/search";

    // Fetch from SerpAPI (Google Jobs) -> cache in MySQL -> return results
    @Override
    public List<InternshipResponse> fetchAndCacheInternships(String query, String location, int page) {
        System.out.println("InternIQ: Fetching from SerpAPI - query=" + query);

        String searchQuery = query + " internship " + (location != null && !location.isBlank() ? location : "India");

        String url = UriComponentsBuilder.fromHttpUrl(SERPAPI_URL)
                .queryParam("engine", "google_jobs")
                .queryParam("q", searchQuery)
                .queryParam("location", "India")
                .queryParam("hl", "en")
                .queryParam("api_key", serpApiKey)
                .toUriString();

        System.out.println("InternIQ: Calling URL = " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        System.out.println("RAW RESPONSE: " + response.getBody());

        return parseAndSave(response.getBody());
    }

    // Serve from DB cache -- no API call, no quota used
    @Override
    public List<InternshipResponse> getCachedListings(String query) {
        List<InternshipListing> listings = (query != null && !query.isBlank())
                ? listingRepository.searchByKeyword(query)
                : listingRepository.findTop50ByOrderByFetchedAtDesc();

        return listings.stream().map(this::toResponse).toList();
    }

    // Truncate helper -- prevents DB column overflow
    private String truncate(String value, int maxLength) {
        return value != null && value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    // Parse SerpAPI Google Jobs response and save to DB
    private List<InternshipResponse> parseAndSave(String json) {
        List<InternshipResponse> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);

            // SerpAPI returns "jobs_results" array
            JsonNode jobs = root.path("jobs_results");
            System.out.println("InternIQ: Total jobs found = " + jobs.size());

            for (JsonNode job : jobs) {

                // Unique ID -- truncated to 700 chars (SerpAPI job_id is a long base64 blob)
                String externalId = job.path("job_id").asText();
                if (externalId == null || externalId.isBlank()) {
                    externalId = job.path("title").asText()
                            + "_" + job.path("company_name").asText();
                }
                externalId = truncate(externalId, 700);

                // Already cached? Return from DB
                if (listingRepository.existsByExternalId(externalId)) {
                    listingRepository.findByExternalId(externalId)
                            .ifPresent(l -> results.add(toResponse(l)));
                    continue;
                }

                // Location
                String location = job.path("location").asText("India");

                // Remote check
                boolean isRemote = location.toLowerCase().contains("remote") ||
                        job.path("detected_extensions")
                           .path("work_from_home").asBoolean(false);

                // Apply URL -- from apply_options array
                String applyUrl = "";
                JsonNode applyOptions = job.path("apply_options");
                if (applyOptions.isArray() && applyOptions.size() > 0) {
                    applyUrl = applyOptions.get(0).path("link").asText("");
                }
                if (applyUrl.isBlank()) {
                    applyUrl = job.path("share_link").asText("");
                }

                // Save to DB
                InternshipListing listing = new InternshipListing();
                listing.setExternalId(externalId);
                listing.setTitle(job.path("title").asText("Unknown Title"));
                listing.setCompany(job.path("company_name").asText("Unknown Company"));
                listing.setLocation(location);
                listing.setIsRemote(isRemote);
                listing.setDescription(job.path("description").asText(""));
                listing.setApplyUrl(applyUrl);
                listing.setSource(InternshipListing.Source.JSEARCH);
                listing.setPostedAt(LocalDateTime.now());
                listing.setFetchedAt(LocalDateTime.now());

                listingRepository.save(listing);
                results.add(toResponse(listing));

                System.out.println("InternIQ: Saved - "
                        + listing.getTitle() + " @ " + listing.getCompany());
            }

        } catch (Exception e) {
            System.err.println("InternIQ: Error parsing SerpAPI response - " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    // Entity -> Response DTO
    private InternshipResponse toResponse(InternshipListing listing) {
        InternshipResponse response = new InternshipResponse();
        response.setId(listing.getId());
        response.setJobTitle(listing.getTitle());
        response.setCompanyName(listing.getCompany());
        response.setLocation(listing.getLocation());
        response.setIsRemote(listing.getIsRemote());
        response.setDescription(listing.getDescription());
        response.setApplyUrl(listing.getApplyUrl());
        response.setSource(listing.getSource().name());
        response.setPostedAt(listing.getPostedAt());
        return response;
    }
}