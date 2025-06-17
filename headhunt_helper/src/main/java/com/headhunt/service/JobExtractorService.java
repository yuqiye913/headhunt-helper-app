package com.headhunt.service;

import com.headhunt.model.JobApplication;
import com.headhunt.model.ApplicationStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JobExtractorService {
    private static final Logger logger = LoggerFactory.getLogger(JobExtractorService.class);
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${anthropic.api.key}")
    private String anthropicApiKey;

    @Value("${anthropic.api.model}")
    private String anthropicModel;

    private final RestTemplate restTemplate;
    private final JobApplicationService jobApplicationService;

    private static final int NOTES_MAX_LENGTH = 4000;
    private static final int URL_MAX_LENGTH = 1000;
    private static final int TEXT_MAX_LENGTH = 255;

    public JobExtractorService(JobApplicationService jobApplicationService) {
        this.restTemplate = new RestTemplate();
        this.jobApplicationService = jobApplicationService;
    }

    private String extractVisibleText(String htmlContent) {
        // Remove script and style elements
        String cleanedHtml = htmlContent.replaceAll("<script[^>]*>.*?</script>", "")
                                      .replaceAll("<style[^>]*>.*?</style>", "")
                                      .replaceAll("<!--.*?-->", ""); // Remove comments

        // Remove all HTML tags and clean up whitespace
        String visibleText = cleanedHtml.replaceAll("<[^>]*>", " ")
                                      .replaceAll("\\s+", " ")
                                      .trim();

        // Truncate to stay within token limits
        int maxChars = 750000; // ~187,500 tokens
        if (visibleText.length() > maxChars) {
            logger.warn("Visible text too long ({} chars), truncating to {} chars", 
                visibleText.length(), maxChars);
            return visibleText.substring(0, maxChars);
        }

        return visibleText;
    }

    public Map<String, Object> extractAndCreateJob(String htmlContent) {
        logger.info("Starting job extraction from HTML content");
        logger.debug("HTML content length: {}", htmlContent.length());

        // Extract and truncate visible text
        String visibleText = extractVisibleText(htmlContent);
        logger.debug("Extracted visible text length: {}", visibleText.length());

        String prompt = """
            Extract job information from the following text content. Return a JSON object with these fields:
            - companyName: The name of the company (REQUIRED, String)
            - position: The job position/title (REQUIRED, String)
            - jobUrl: The URL of the job posting (REQUIRED, String)
            - location: The job location (if available, String)
            - salary: The salary information (if available, String)
            - contactPerson: The contact person's name (if available, String)
            - contactEmail: The contact person's email (if available, String)
            - notes: A brief summary as a single String (max 200 words). Include:
              * Key qualifications
              * Main responsibilities
              * Notable benefits
              * Important requirements

            IMPORTANT: All fields must be returned as String values, not objects or arrays.
            If any REQUIRED field cannot be found, set it to "Unknown" rather than null.
            Keep the notes field concise and focused on the most important information.
            Text Content:
            %s
            """.formatted(visibleText);

        logger.info("Sending request to Claude API with model: {}", anthropicModel);
        String extractedInfo = callClaudeApi(prompt);
        logger.debug("Received response from Claude API: {}", extractedInfo);

        Map<String, Object> result = parseAndCreateJob(extractedInfo);
        logger.info("Successfully extracted and created job application with ID: {}", result.get("id"));
        return result;
    }

    private String callClaudeApi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", anthropicApiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", anthropicModel);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.3);
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", prompt)
            });

            logger.debug("Sending request to Claude API: {}", requestBody);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                ANTHROPIC_API_URL,
                HttpMethod.POST,
                request,
                Map.class
            );

            Map responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("content")) {
                String content = ((String) ((Map) ((java.util.List) responseBody.get("content")).get(0)).get("text"));
                logger.debug("Received response from Claude API: {}", content);
                return content;
            }
            logger.error("Invalid response from Claude API: {}", responseBody);
            throw new RuntimeException("Failed to get response from Claude API");
        } catch (Exception e) {
            logger.error("Error calling Claude API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Claude API: " + e.getMessage());
        }
    }

    private String truncateText(String text, int maxLength, String fieldName) {
        if (text == null) return null;
        if (text.length() > maxLength) {
            logger.warn("Truncating {} from {} to {} characters", fieldName, text.length(), maxLength);
            return text.substring(0, maxLength);
        }
        return text;
    }

    private Map<String, Object> parseAndCreateJob(String response) {
        try {
            // Extract just the JSON part from the response
            int startIndex = response.indexOf('{');
            int endIndex = response.lastIndexOf('}') + 1;
            
            if (startIndex == -1 || endIndex == 0) {
                logger.error("No JSON object found in response: {}", response);
                throw new RuntimeException("No valid JSON object found in response");
            }
            
            String jsonStr = response.substring(startIndex, endIndex);
            logger.debug("Extracted JSON string: {}", jsonStr);
            
            Map<String, Object> extractedData = objectMapper.readValue(
                jsonStr,
                new TypeReference<Map<String, Object>>() {}
            );

            // Extract required fields with defaults
            String companyName = String.valueOf(extractedData.getOrDefault("companyName", "Unknown"));
            String position = String.valueOf(extractedData.getOrDefault("position", "Unknown"));
            String jobUrl = String.valueOf(extractedData.getOrDefault("jobUrl", "Unknown"));

            // Create and save the job application
            JobApplication application = new JobApplication();
            application.setCompanyName(truncateText(companyName, TEXT_MAX_LENGTH, "companyName"));
            application.setPosition(truncateText(position, TEXT_MAX_LENGTH, "position"));
            application.setJobUrl(truncateText(jobUrl, URL_MAX_LENGTH, "jobUrl"));
            application.setStatus(ApplicationStatus.APPLIED);
            application.setLocation(truncateText(String.valueOf(extractedData.get("location")), TEXT_MAX_LENGTH, "location"));
            application.setSalary(truncateText(String.valueOf(extractedData.get("salary")), TEXT_MAX_LENGTH, "salary"));
            application.setContactPerson(truncateText(String.valueOf(extractedData.get("contactPerson")), TEXT_MAX_LENGTH, "contactPerson"));
            application.setContactEmail(truncateText(String.valueOf(extractedData.get("contactEmail")), TEXT_MAX_LENGTH, "contactEmail"));
            application.setNotes(truncateText(String.valueOf(extractedData.get("notes")), NOTES_MAX_LENGTH, "notes"));

            JobApplication savedApplication = jobApplicationService.createApplication(application);
            logger.info("Successfully saved job application: {}", savedApplication);

            // Add the saved application ID to the response
            extractedData.put("id", savedApplication.getId());
            extractedData.put("status", savedApplication.getStatus());
            extractedData.put("appliedTime", savedApplication.getAppliedTime());

            // Add validation status to response
            extractedData.put("validation", Map.of(
                "companyName", !companyName.equals("Unknown"),
                "position", !position.equals("Unknown"),
                "jobUrl", !jobUrl.equals("Unknown")
            ));

            return extractedData;
        } catch (Exception e) {
            logger.error("Failed to parse job information: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse job information: " + e.getMessage());
        }
    }
} 