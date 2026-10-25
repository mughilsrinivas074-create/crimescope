package com.crimescope.web.controller;

import com.crimescope.service.ai.GeminiAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class GeminiController {

    private final GeminiAnalysisService geminiAnalysisService;

    public GeminiController(GeminiAnalysisService geminiAnalysisService) {
        this.geminiAnalysisService = geminiAnalysisService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(geminiAnalysisService.getGeminiStatus());
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateKey(@RequestBody Map<String, String> payload) {
        String key = payload.get("key");
        return ResponseEntity.ok(geminiAnalysisService.validateCandidateKey(key));
    }

    @PostMapping("/insights")
    public ResponseEntity<Map<String, Object>> generateInsights(@RequestBody Map<String, Object> payload) {
        Long districtId = payload.get("districtId") != null ? Long.valueOf(payload.get("districtId").toString()) : null;
        Integer startYear = payload.get("startYear") != null ? Integer.valueOf(payload.get("startYear").toString()) : null;
        Integer endYear = payload.get("endYear") != null ? Integer.valueOf(payload.get("endYear").toString()) : null;

        return ResponseEntity.ok(geminiAnalysisService.generateDistrictInsights(districtId, startYear, endYear));
    }

    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareDistricts(@RequestBody Map<String, Object> payload) {
        String codeA = (String) payload.get("districtCodeA");
        String codeB = (String) payload.get("districtCodeB");
        Integer year = payload.get("year") != null ? Integer.valueOf(payload.get("year").toString()) : null;

        return ResponseEntity.ok(geminiAnalysisService.compareDistricts(codeA, codeB, year));
    }

    @PostMapping("/trends")
    public ResponseEntity<Map<String, Object>> explainTrends(@RequestBody Map<String, Object> payload) {
        Long districtId = payload.get("districtId") != null ? Long.valueOf(payload.get("districtId").toString()) : null;
        Integer startYear = payload.get("startYear") != null ? Integer.valueOf(payload.get("startYear").toString()) : null;
        Integer endYear = payload.get("endYear") != null ? Integer.valueOf(payload.get("endYear").toString()) : null;

        return ResponseEntity.ok(geminiAnalysisService.explainHistoricalTrends(districtId, startYear, endYear));
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody Map<String, Object> payload) {
        String question = (String) payload.get("question");
        Long districtId = payload.get("districtId") != null ? Long.valueOf(payload.get("districtId").toString()) : null;
        Integer year = payload.get("year") != null ? Integer.valueOf(payload.get("year").toString()) : null;

        return ResponseEntity.ok(geminiAnalysisService.answerQuestion(question, districtId, year));
    }
}
