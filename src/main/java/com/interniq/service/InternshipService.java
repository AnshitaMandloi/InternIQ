package com.interniq.service;

import java.util.List;

import com.interniq.response.InternshipResponse;
 
public interface InternshipService {
    List<InternshipResponse> fetchAndCacheInternships(String query, String location, int page);
    List<InternshipResponse> getCachedListings(String query);
}