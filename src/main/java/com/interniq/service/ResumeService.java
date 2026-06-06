package com.interniq.service;

import com.interniq.dto.ResumeScoreResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {

    // Upload resume file + optional job description → parse → score with OpenAI → save → return result
    ResumeScoreResponse scoreResume(MultipartFile file, String jobDescription, String userEmail);

    // Get all past scores for the logged-in user
    List<ResumeScoreResponse> getScoreHistory(String userEmail);
}