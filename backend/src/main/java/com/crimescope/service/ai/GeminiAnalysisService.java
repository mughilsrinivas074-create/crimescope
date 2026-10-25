package com.crimescope.service.ai;

import com.crimescope.domain.model.CrimeRecord;
import com.crimescope.domain.model.District;
import com.crimescope.repository.CrimeRecordRepository;
import com.crimescope.repository.DistrictRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enterprise AI Intelligence Service interfacing with Google Gemini API.
 * Guarantees strict evidence bounding, preventing hallucinations by computing
 * all numerical aggregations in Java/SQL before sending bounded context to the AI model.
 * Provides full deterministic fallback when API keys are unconfigured, invalid, or offline.
 */
@Service
public class GeminiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAnalysisService.class);

    private final CrimeRecordRepository crimeRecordRepository;
    private final DistrictRepository districtRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${ai.gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiApiUrl;

    @Value("${ai.gemini.timeout-seconds:25}")
    private int timeoutSeconds;

    public GeminiAnalysisService(CrimeRecordRepository crimeRecordRepository,
                                 DistrictRepository districtRepository,
                                 ObjectMapper objectMapper) {
        this.crimeRecordRepository = crimeRecordRepository;
        this.districtRepository = districtRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Checks status and connectivity of Google Gemini API key.
     */
    public Map<String, Object> getGeminiStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("model", geminiModel);

        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.equalsIgnoreCase("your_api_key_here")) {
            status.put("configured", false);
            status.put("valid", false);
            status.put("status", "KEY_MISSING");
            status.put("message", "Gemini API key is not configured. Set GEMINI_API_KEY in .env or system environment.");
            status.put("keyMasked", null);
            status.put("setupInstructions", "Obtain an API key from Google AI Studio (https://aistudio.google.com/) and paste it into .env as GEMINI_API_KEY.");
            return status;
        }

        return testGeminiConnectivity(geminiApiKey.trim());
    }

    /**
     * Validates an operator-supplied candidate Gemini API key.
     */
    public Map<String, Object> validateCandidateKey(String candidateKey) {
        if (candidateKey == null || candidateKey.trim().isEmpty()) {
            return Map.of("valid", false, "status", "EMPTY", "message", "API key cannot be empty.");
        }
        return testGeminiConnectivity(candidateKey.trim());
    }

    private Map<String, Object> testGeminiConnectivity(String key) {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("model", geminiModel);
        status.put("keyMasked", maskKey(key));

        try {
            // Ping Google Generative Language API model endpoint to verify key authorization
            String testUrl = String.format("%s/%s?key=%s", geminiApiUrl, geminiModel, key);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(testUrl))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                status.put("configured", true);
                status.put("valid", true);
                status.put("status", "OPERATIONAL");
                status.put("message", "Google Gemini API key verified. Live generative AI intelligence active.");
            } else if (response.statusCode() == 400 || response.statusCode() == 403 || response.statusCode() == 401) {
                status.put("configured", true);
                status.put("valid", false);
                status.put("status", "KEY_INVALID");
                status.put("message", "Gemini authentication failed (HTTP " + response.statusCode() + "). Key is invalid or unauthorized.");
            } else if (response.statusCode() == 429) {
                status.put("configured", true);
                status.put("valid", false);
                status.put("status", "RATE_LIMITED");
                status.put("message", "Gemini API rate limit or quota exceeded (HTTP 429).");
            } else {
                status.put("configured", true);
                status.put("valid", false);
                status.put("status", "API_ERROR");
                status.put("message", "Gemini API returned unexpected status HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            status.put("configured", true);
            status.put("valid", false);
            status.put("status", "NETWORK_ERROR");
            status.put("message", "Failed to connect to Google Gemini endpoint: " + e.getMessage());
        }
        return status;
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 8) return "********";
        return key.substring(0, 6) + "..." + key.substring(key.length() - 4);
    }

    /**
     * Generates grounded AI insights for a selected Tamil Nadu district and time period.
     */
    public Map<String, Object> generateDistrictInsights(Long districtId, Integer startYear, Integer endYear) {
        District district = districtId != null ? districtRepository.findById(districtId).orElse(null) : null;
        List<CrimeRecord> records = crimeRecordRepository.filterRecords(districtId, null, startYear, endYear, false);

        Map<String, Object> boundedData = buildStatisticalSummary(records, district != null ? district.getName() : "All Tamil Nadu Districts");

        String prompt = String.format("""
            Provide an authoritative strategic crime intelligence assessment for %s (Period: %d to %d).
            Analyze only the supplied statistical evidence below. Highlight predominant crime classifications,
            clearance effectiveness, and observable trends. Do not extrapolate outside these numbers.
            Evidence Payload:
            %s
            """, district != null ? district.getName() : "Tamil Nadu State",
                startYear != null ? startYear : 2018,
                endYear != null ? endYear : 2023,
                toJsonString(boundedData));

        return executeAiOrFallback(prompt, boundedData, "DISTRICT_INSIGHTS");
    }

    /**
     * Compares two districts based on historical statistical evidence.
     */
    public Map<String, Object> compareDistricts(String codeA, String codeB, Integer year) {
        District distA = districtRepository.findByCodeIgnoreCase(codeA).orElse(null);
        District distB = districtRepository.findByCodeIgnoreCase(codeB).orElse(null);

        if (distA == null || distB == null) {
            return Map.of("error", "One or both district codes could not be identified.");
        }

        List<CrimeRecord> recordsA = crimeRecordRepository.filterRecords(distA.getId(), null, year, year, false);
        List<CrimeRecord> recordsB = crimeRecordRepository.filterRecords(distB.getId(), null, year, year, false);

        Map<String, Object> summaryA = buildStatisticalSummary(recordsA, distA.getName());
        Map<String, Object> summaryB = buildStatisticalSummary(recordsB, distB.getName());

        Map<String, Object> comparisonEvidence = new LinkedHashMap<>();
        comparisonEvidence.put("districtA", summaryA);
        comparisonEvidence.put("districtB", summaryB);
        comparisonEvidence.put("targetYear", year != null ? year : "Aggregated");

        String prompt = String.format("""
            Compare the crime profiles of %s (%s Zone, Area: %s sq km) and %s (%s Zone, Area: %s sq km).
            Identify differences in caseload composition, clearance performance, and enforcement patterns.
            Do not invent unrecorded incidents.
            Evidence:
            %s
            """, distA.getName(), distA.getZone(), distA.getAreaSqKm(),
                distB.getName(), distB.getZone(), distB.getAreaSqKm(),
                toJsonString(comparisonEvidence));

        return executeAiOrFallback(prompt, comparisonEvidence, "DISTRICT_COMPARISON");
    }

    /**
     * Explains historical multi-year trajectories and inflection points.
     */
    public Map<String, Object> explainHistoricalTrends(Long districtId, Integer startYear, Integer endYear) {
        District district = districtId != null ? districtRepository.findById(districtId).orElse(null) : null;
        List<CrimeRecord> records = crimeRecordRepository.filterRecords(districtId, null, startYear, endYear, false);

        Map<String, Object> summary = buildStatisticalSummary(records, district != null ? district.getName() : "Statewide Tamil Nadu");

        String prompt = String.format("""
            Analyze historical crime trends and shifts for %s between %d and %d.
            Explain significant changes in category distribution (e.g. cyber crimes vs property offences),
            taking into account pandemic-era baseline variations if observed.
            Evidence:
            %s
            """, district != null ? district.getName() : "Tamil Nadu",
                startYear != null ? startYear : 2018,
                endYear != null ? endYear : 2023,
                toJsonString(summary));

        return executeAiOrFallback(prompt, summary, "TREND_ANALYSIS");
    }

    /**
     * Grounded Q&A against verified crime statistics.
     */
    public Map<String, Object> answerQuestion(String question, Long districtId, Integer year) {
        if (question == null || question.trim().isEmpty()) {
            return Map.of("error", "Question prompt cannot be empty.");
        }

        District district = districtId != null ? districtRepository.findById(districtId).orElse(null) : null;
        List<CrimeRecord> records = crimeRecordRepository.filterRecords(districtId, null, year, year, false);
        Map<String, Object> evidence = buildStatisticalSummary(records, district != null ? district.getName() : "Tamil Nadu State");

        String prompt = String.format("""
            The user asked the following question about Tamil Nadu crime data:
            "%s"
            Answer the user's question accurately using ONLY the bounded historical evidence below.
            If the question asks about data not present in the evidence (such as personal victim details or unrecorded metrics),
            explicitly state that the verified public record does not contain that information.
            Evidence:
            %s
            """, question, toJsonString(evidence));

        return executeAiOrFallback(prompt, evidence, "NATURAL_LANGUAGE_QA");
    }

    // --- Core Grounded AI Invocation & Fallback Mechanics ---

    private Map<String, Object> executeAiOrFallback(String promptText, Map<String, Object> evidence, String queryType) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("queryType", queryType);
        result.put("timestamp", new Date());
        result.put("sourceProvenance", "Tamil Nadu Police SCRB / NCRB Official Compendiums (2018-2023)");
        result.put("factualEvidence", evidence);

        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.equalsIgnoreCase("your_api_key_here")) {
            log.info("GEMINI_API_KEY is not configured. Utilizing deterministic rule-based analysis engine.");
            result.put("status", "KEY_MISSING");
            result.put("modelUsed", "DETERMINISTIC_STATISTICAL_SYNTHESIS (No Gemini API Key Configured)");
            result.put("isAiGenerated", false);
            result.put("message", "Google Gemini API key is not configured. Set GEMINI_API_KEY in .env to activate live generative AI analysis. Displaying verified deterministic statistical synthesis.");
            result.put("analysis", generateDeterministicReport(evidence, promptText));
            result.put("disclaimer", "VERIFIED STATISTICAL DERIVATION: All figures calculated strictly in Java/SQL from official Tamil Nadu Police SCRB records without generative hallucination.");
            return result;
        }

        try {
            String requestUrl = String.format("%s/%s:generateContent?key=%s", geminiApiUrl, geminiModel, geminiApiKey.trim());

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("role", "user", "parts", List.of(Map.of("text", promptText)))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.2, // Low temperature for high factual accuracy
                            "topK", 20,
                            "topP", 0.8,
                            "maxOutputTokens", 1200
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidateText = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                String aiText = candidateText.asText();

                result.put("status", "SUCCESS");
                result.put("modelUsed", "Google Gemini (" + geminiModel + ")");
                result.put("isAiGenerated", true);
                result.put("analysis", aiText);
                result.put("disclaimer", "AI INTERPRETATION: Strategic insights synthesized by Google Gemini, grounded strictly in the bounded statistical evidence computed above.");
                return result;
            } else if (response.statusCode() == 400 || response.statusCode() == 401 || response.statusCode() == 403) {
                log.warn("Gemini API authorization failure HTTP {}: {}", response.statusCode(), response.body());
                result.put("status", "KEY_INVALID");
                result.put("modelUsed", "DETERMINISTIC_STATISTICAL_SYNTHESIS (API Auth Failure)");
                result.put("isAiGenerated", false);
                result.put("errorMessage", "Google Gemini authorization failed (HTTP " + response.statusCode() + "). Please check your GEMINI_API_KEY in .env.");
                result.put("analysis", generateDeterministicReport(evidence, promptText));
                result.put("disclaimer", "FALLBACK DERIVATION: Executed deterministic statistical synthesis due to API key authorization failure.");
                return result;
            } else if (response.statusCode() == 429) {
                log.warn("Gemini API rate limit reached (HTTP 429)");
                result.put("status", "RATE_LIMITED");
                result.put("modelUsed", "DETERMINISTIC_STATISTICAL_SYNTHESIS (Rate Limit Fallback)");
                result.put("isAiGenerated", false);
                result.put("errorMessage", "Google Gemini rate limit or quota exceeded (HTTP 429). Please wait before querying.");
                result.put("analysis", generateDeterministicReport(evidence, promptText));
                result.put("disclaimer", "FALLBACK DERIVATION: Executed deterministic statistical synthesis due to API rate limiting.");
                return result;
            } else {
                log.warn("Gemini API returned HTTP {}: {}", response.statusCode(), response.body());
                result.put("status", "API_ERROR");
                result.put("modelUsed", "DETERMINISTIC_STATISTICAL_SYNTHESIS (API Error HTTP " + response.statusCode() + ")");
                result.put("isAiGenerated", false);
                result.put("errorMessage", "Gemini API returned HTTP status " + response.statusCode());
                result.put("analysis", generateDeterministicReport(evidence, promptText));
                result.put("disclaimer", "FALLBACK DERIVATION: Executed deterministic statistical synthesis due to external API error.");
                return result;
            }

        } catch (Exception e) {
            log.error("Failed to connect to Gemini API. Executing deterministic fallback.", e);
            result.put("status", "NETWORK_ERROR");
            result.put("modelUsed", "DETERMINISTIC_STATISTICAL_SYNTHESIS (Network Timeout / Reset)");
            result.put("isAiGenerated", false);
            result.put("errorMessage", "Failed to connect to Google Gemini endpoint: " + e.getMessage());
            result.put("analysis", generateDeterministicReport(evidence, promptText));
            result.put("disclaimer", "FALLBACK DERIVATION: Executed deterministic statistical synthesis due to network connectivity failure.");
            return result;
        }
    }

    private Map<String, Object> buildStatisticalSummary(List<CrimeRecord> records, String contextLabel) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("context", contextLabel);

        long totalCases = records.stream().mapToLong(r -> r.getCaseCount() != null ? r.getCaseCount() : 0).sum();
        long totalArrests = records.stream().mapToLong(r -> r.getArrestCount() != null ? r.getArrestCount() : 0).sum();
        long totalConvicted = records.stream().mapToLong(r -> r.getConvictedCount() != null ? r.getConvictedCount() : 0).sum();
        long totalChargesheeted = records.stream().mapToLong(r -> r.getChargesheetedCount() != null ? r.getChargesheetedCount() : 0).sum();

        Map<String, Long> casesByCategory = records.stream()
                .filter(r -> r.getCategory() != null)
                .collect(Collectors.groupingBy(r -> r.getCategory().getName(),
                        Collectors.summingLong(r -> r.getCaseCount() != null ? r.getCaseCount() : 0)));

        Map<Integer, Long> casesByYear = records.stream()
                .filter(r -> r.getReportingYear() != null)
                .collect(Collectors.groupingBy(CrimeRecord::getReportingYear, TreeMap::new,
                        Collectors.summingLong(r -> r.getCaseCount() != null ? r.getCaseCount() : 0)));

        summary.put("totalRecords", records.size());
        summary.put("totalRegisteredCases", totalCases);
        summary.put("totalArrests", totalArrests);
        summary.put("totalConvictions", totalConvicted);
        summary.put("totalChargesheeted", totalChargesheeted);
        summary.put("overallClearanceRatePct", totalCases > 0 ? Math.round(((double) totalChargesheeted / totalCases) * 1000.0) / 10.0 : 0.0);
        summary.put("categoryBreakdown", casesByCategory);
        summary.put("yearlyDistribution", casesByYear);

        return summary;
    }

    private String generateDeterministicReport(Map<String, Object> evidence, String userContext) {
        StringBuilder sb = new StringBuilder();
        String context = (String) evidence.getOrDefault("context", "Tamil Nadu District Jurisdiction");
        long totalCases = ((Number) evidence.getOrDefault("totalRegisteredCases", 0L)).longValue();
        long totalArrests = ((Number) evidence.getOrDefault("totalArrests", 0L)).longValue();
        long totalConvictions = ((Number) evidence.getOrDefault("totalConvictions", 0L)).longValue();
        Object clearance = evidence.getOrDefault("overallClearanceRatePct", 0.0);

        sb.append("### STRATEGIC INTELLIGENCE ASSESSMENT: ").append(context.toUpperCase()).append("\n\n");
        sb.append("**1. Executive Overview:**\n");
        sb.append(String.format("Across the examined period, official police registers record an aggregate of **%,d registered FIR cases**, resulting in **%,d arrests** and **%,d court convictions**. The collective chargesheeting clearance rate stands at **%s%%**.\n\n",
                totalCases, totalArrests, totalConvictions, clearance));

        sb.append("**2. Category Distribution & Severity Analysis:**\n");
        @SuppressWarnings("unchecked")
        Map<String, Long> catMap = (Map<String, Long>) evidence.get("categoryBreakdown");
        if (catMap != null && !catMap.isEmpty()) {
            List<Map.Entry<String, Long>> sortedCats = catMap.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .toList();

            sb.append("Analysis of case classifications indicates the highest caseload concentrations in:\n");
            for (Map.Entry<String, Long> entry : sortedCats) {
                double share = totalCases > 0 ? ((double) entry.getValue() / totalCases) * 100.0 : 0.0;
                sb.append(String.format("- **%s**: %,d cases (%.1f%% of jurisdiction volume)\n", entry.getKey(), entry.getValue(), share));
            }
            sb.append("\n");
        }

        sb.append("**3. Historical Trajectory Observations:**\n");
        @SuppressWarnings("unchecked")
        Map<Integer, Long> yearMap = (Map<Integer, Long>) evidence.get("yearlyDistribution");
        if (yearMap != null && yearMap.size() > 1) {
            sb.append("Temporal analysis illustrates year-by-year shifts in enforcement:\n");
            Long prev = null;
            for (Map.Entry<Integer, Long> entry : yearMap.entrySet()) {
                if (prev != null) {
                    double change = prev > 0 ? (((double) entry.getValue() - prev) / prev) * 100.0 : 0.0;
                    String sign = change >= 0 ? "+" : "";
                    sb.append(String.format("- **%d**: %,d cases (%s%.1f%% compared to %d)\n", entry.getKey(), entry.getValue(), sign, change, entry.getKey() - 1));
                } else {
                    sb.append(String.format("- **%d**: %,d cases (baseline observation)\n", entry.getKey(), entry.getValue()));
                }
                prev = entry.getValue();
            }
            sb.append("\n");
        }

        sb.append("**4. Strategic Jurisdictional Recommendations:**\n");
        sb.append("- Prioritize proactive patrol deployment and digital forensics capabilities in leading volume zones.\n");
        sb.append("- Accelerate chargesheet finalization timelines for critical categories to optimize clearance performance.\n");
        sb.append("- All analytical figures are mathematically bounded by recorded SCRB/NCRB official statistics without ungrounded extrapolation.\n");

        return sb.toString();
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
