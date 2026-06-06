package com.interniq.controller;

import com.interniq.dto.ResumeScoreResponse;
import com.interniq.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resume")
@Tag(name = "AI Resume Scorer", description = "Upload resume → AI scores it section by section")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    // ── POST /api/v1/resume/score ────────────────────────────────────────────
    // Accepts multipart/form-data: file + optional jobDescription
    @PostMapping(value = "/score", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Score a resume with AI",
        description = "Upload your resume (PDF, DOCX, DOC, TXT). " +
                      "Optionally provide a job description to get a tailored score. " +
                      "Returns overall score (0-100) + section feedback for Skills, Experience, Summary."
    )
    public ResponseEntity<ResumeScoreResponse> scoreResume(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "jobDescription", required = false) String jobDescription,
            @AuthenticationPrincipal UserDetails userDetails) {

        ResumeScoreResponse response = resumeService.scoreResume(file, jobDescription, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // ── GET /api/v1/resume/history ───────────────────────────────────────────
    @GetMapping("/history")
    @Operation(
        summary = "Get past resume scores",
        description = "Returns all previous resume scores for the logged-in user, newest first."
    )
    public ResponseEntity<List<ResumeScoreResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ResumeScoreResponse> history = resumeService.getScoreHistory(userDetails.getUsername());
        return ResponseEntity.ok(history);
    }
}