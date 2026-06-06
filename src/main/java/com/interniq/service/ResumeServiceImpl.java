package com.interniq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interniq.dao.ResumeScoreRepository;
import com.interniq.dao.UserRepository;
import com.interniq.dto.ResumeScoreResponse;
import com.interniq.entity.ResumeScore;
import com.interniq.entity.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements ResumeService {

	@Value("${groq.api.key}")
	private String apiKey;

	@Value("${groq.url}")
	private String apiUrl;

	@Value("${groq.model}")
	private String model;

    @Autowired
    private ResumeScoreRepository resumeScoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // ── SCORE ──────────────────────────────────────────────────────────────────

    @Override
    public ResumeScoreResponse scoreResume(MultipartFile file, String jobDescription, String userEmail) {
        User user = getUser(userEmail);

        // 1. Validate file
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please upload a resume file.");
        }

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume";
        String extension = getExtension(fileName).toLowerCase();

        if (!List.of("pdf", "doc", "docx", "txt").contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported file type. Please upload PDF, DOC, DOCX, or TXT.");
        }

        // 2. Parse text from file
        String resumeText = parseFile(file, extension);
        if (resumeText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not extract text from the uploaded file. Try a different format.");
        }

        System.out.println("Resume parsed: " + resumeText.length() + " chars, file=" + fileName);

        // 3. Call OpenAI
        String feedbackJson = callGroq(resumeText, jobDescription);

        // 4. Parse OpenAI response
        ResumeScoreResponse response = parseFeedback(feedbackJson);

        // 5. Save to DB
        ResumeScore entity = new ResumeScore();
        entity.setUser(user);
        entity.setJobDescription(jobDescription != null ? jobDescription : "");
        entity.setScore(response.getOverallScore());
        entity.setMatchedSkills(listToString(response.getMatchedSkills()));
        entity.setMissingSkills(listToString(response.getMissingSkills()));
        entity.setSuggestions(buildSuggestionsText(response));
        entity.setResumeText(resumeText);
        entity.setFeedbackJson(feedbackJson);
        entity.setFileName(fileName);

        ResumeScore saved = resumeScoreRepository.save(entity);
        response.setId(saved.getId());
        response.setGeneratedAt(saved.getGeneratedAt());
        response.setFileName(fileName);

        System.out.println("Resume scored: user=" + userEmail + " score=" + response.getOverallScore());
        return response;
    }

    // ── HISTORY ────────────────────────────────────────────────────────────────

    @Override
    public List<ResumeScoreResponse> getScoreHistory(String userEmail) {
        User user = getUser(userEmail);
        return resumeScoreRepository.findByUserIdOrderByGeneratedAtDesc(user.getId())
                .stream()
                .map(this::entityToResponse)
                .collect(Collectors.toList());
    }

    // ── FILE PARSING ───────────────────────────────────────────────────────────

    private String parseFile(MultipartFile file, String extension) {
        try (InputStream is = file.getInputStream()) {
            switch (extension) {
                case "pdf":  return parsePdf(file);
                case "docx": return parseDocx(file);
                case "doc":  return parseDocx(file); // POI handles both
                case "txt":  return new String(file.getBytes());
                default:     return "";
            }
        } catch (Exception e) {
            System.out.println("File parse error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Failed to read file: " + e.getMessage());
        }
    }

    private String parsePdf(MultipartFile file) throws Exception {
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }
    private String parseDocx(MultipartFile file) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            return doc.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }

    // ── OPENAI CALL ────────────────────────────────────────────────────────────

    private String callGroq(String resumeText, String jobDescription) {

        String jd = (jobDescription != null && !jobDescription.isBlank())
                ? "Job Description:\n" + jobDescription + "\n\n"
                : "";

        String trimmedResume = resumeText.length() > 4000
                ? resumeText.substring(0, 4000)
                : resumeText;

        String prompt =
                "You are an expert ATS Resume Analyzer. Carefully read the resume below and provide an HONEST, ACCURATE evaluation.\n\n" +
                "IMPORTANT RULES:\n" +
                "- Score based on what is ACTUALLY in the resume — do NOT use placeholder or example values.\n" +
                "- Every resume should get a DIFFERENT score based on its real content.\n" +
                "- Be specific in your feedback — mention actual skills, projects, or sections you see.\n\n" +

                "SCORING RUBRIC:\n" +
                "- skillsScore: 90-100 = many relevant technologies with proficiency levels; 70-89 = decent list; 50-69 = few skills; below 50 = very sparse\n" +
                "- experienceScore: 90-100 = strong internships/projects with measurable metrics; 70-89 = decent; 50-69 = vague; below 50 = minimal\n" +
                "- summaryScore: 90-100 = tailored, concise, role-specific; 70-89 = decent; 50-69 = generic; below 50 = missing or very weak\n" +
                "- overallScore = (skillsScore * 0.35) + (experienceScore * 0.40) + (summaryScore * 0.25), rounded to nearest integer\n\n" +

                (jd.isBlank() ? "" : jd) +
                "RESUME TO ANALYZE:\n" +
                "---\n" + trimmedResume + "\n---\n\n" +

                "Return ONLY a valid JSON object with NO markdown fences, NO explanation, just raw JSON in this exact structure:\n" +
                "{\n" +
                "  \"overallScore\": <integer computed from rubric above>,\n" +
                "  \"skills\": {\n" +
                "    \"score\": <integer 0-100 based on actual skills in resume>,\n" +
                "    \"feedback\": \"<specific observation about THIS resume's skills section>\",\n" +
                "    \"suggestion\": \"<one concrete actionable improvement>\"\n" +
                "  },\n" +
                "  \"experience\": {\n" +
                "    \"score\": <integer 0-100 based on actual experience/projects>,\n" +
                "    \"feedback\": \"<specific observation about THIS resume's experience>\",\n" +
                "    \"suggestion\": \"<one concrete actionable improvement>\"\n" +
                "  },\n" +
                "  \"summary\": {\n" +
                "    \"score\": <integer 0-100 based on the actual summary/objective>,\n" +
                "    \"feedback\": \"<specific observation about THIS resume's summary>\",\n" +
                "    \"suggestion\": \"<one concrete actionable improvement>\"\n" +
                "  },\n" +
                "  \"matchedSkills\": [<skills from resume that match the job description, empty array if no JD provided>],\n" +
                "  \"missingSkills\": [<skills in job description NOT found in resume, empty array if no JD provided>]\n" +
                "}";

        try {

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();

            body.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();

            messages.add(Map.of(
                    "role", "user",
                    "content", prompt
            ));

            body.put("messages", messages);

            body.put("temperature", 0.2);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            apiUrl,
                            HttpMethod.POST,
                            request,
                            String.class
                    );

            String responseBody = response.getBody();

            System.out.println("Groq Response:");
            System.out.println(responseBody);

            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode contentNode =
                    root.path("choices")
                        .get(0)
                        .path("message")
                        .path("content");

            if (contentNode.isMissingNode()) {
                throw new RuntimeException("No content returned by Groq");
            }

            return contentNode.asText();

        } catch (Exception e) {

            e.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Groq API Error: " + e.getMessage()
            );
        }
    }

    // ── PARSE OPENAI JSON RESPONSE ─────────────────────────────────────────────

    private ResumeScoreResponse parseFeedback(String feedbackJson) {
        ResumeScoreResponse res = new ResumeScoreResponse();
        try {
            // Strip markdown code fences if OpenAI wraps in ```json
        	String clean = feedbackJson
        	        .replace("```json", "")
        	        .replace("```", "")
        	        .trim();

        	int start = clean.indexOf('{');
        	int end = clean.lastIndexOf('}');

        	if (start >= 0 && end > start) {
        	    clean = clean.substring(start, end + 1);
        	}

            JsonNode node = objectMapper.readTree(clean);

            res.setOverallScore(node.path("overallScore").asInt(50));

            res.setSkills(parseSectionFeedback(node.path("skills")));
            res.setExperience(parseSectionFeedback(node.path("experience")));
            res.setSummary(parseSectionFeedback(node.path("summary")));

            res.setMatchedSkills(parseStringList(node.path("matchedSkills")));
            res.setMissingSkills(parseStringList(node.path("missingSkills")));

        } catch (Exception e) {
            System.out.println("Failed to parse OpenAI response: " + e.getMessage());
            // Return a fallback so we don't crash
            res.setOverallScore(0);
            res.setMatchedSkills(new ArrayList<>());
            res.setMissingSkills(new ArrayList<>());
        }
        return res;
    }

    private ResumeScoreResponse.SectionFeedback parseSectionFeedback(JsonNode node) {
        ResumeScoreResponse.SectionFeedback sf = new ResumeScoreResponse.SectionFeedback();
        sf.setScore(node.path("score").asInt(0));
        sf.setFeedback(node.path("feedback").asText(""));
        sf.setSuggestion(node.path("suggestion").asText(""));
        return sf;
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(n -> list.add(n.asText()));
        }
        return list;
    }

    // ── ENTITY → DTO ───────────────────────────────────────────────────────────

    private ResumeScoreResponse entityToResponse(ResumeScore entity) {
        if (entity.getFeedbackJson() != null) {
            ResumeScoreResponse res = parseFeedback(entity.getFeedbackJson());
            res.setId(entity.getId());
            res.setFileName(entity.getFileName());
            res.setGeneratedAt(entity.getGeneratedAt());
            res.setOverallScore(entity.getScore());
            return res;
        }
        // Fallback for old records without feedbackJson
        ResumeScoreResponse res = new ResumeScoreResponse();
        res.setId(entity.getId());
        res.setFileName(entity.getFileName());
        res.setOverallScore(entity.getScore());
        res.setGeneratedAt(entity.getGeneratedAt());
        res.setMatchedSkills(stringToList(entity.getMatchedSkills()));
        res.setMissingSkills(stringToList(entity.getMissingSkills()));
        return res;
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : "";
    }

    private String listToString(List<String> list) {
        return list != null ? String.join(", ", list) : "";
    }

    private List<String> stringToList(String str) {
        if (str == null || str.isBlank()) return new ArrayList<>();
        return Arrays.asList(str.split(",\\s*"));
    }

    private String buildSuggestionsText(ResumeScoreResponse res) {
        StringBuilder sb = new StringBuilder();
        if (res.getSkills() != null)     sb.append("Skills: ").append(res.getSkills().getSuggestion()).append(" | ");
        if (res.getExperience() != null) sb.append("Experience: ").append(res.getExperience().getSuggestion()).append(" | ");
        if (res.getSummary() != null)    sb.append("Summary: ").append(res.getSummary().getSuggestion());
        return sb.toString();
    }
}