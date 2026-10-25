package com.crimescope.web.controller;

import com.crimescope.domain.strategy.StrategyResult;
import com.crimescope.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview(
            @RequestParam(defaultValue = "false") Boolean includeDemo) {
        return ResponseEntity.ok(analyticsService.getCommandCenterOverview(includeDemo));
    }

    @GetMapping("/district-compare")
    public ResponseEntity<StrategyResult> compareDistricts(
            @RequestParam(required = false) List<Long> districtIds,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "false") Boolean includeDemo) {
        return ResponseEntity.ok(analyticsService.compareDistricts(districtIds, year, includeDemo));
    }

    @GetMapping("/trends")
    public ResponseEntity<StrategyResult> getTrends(
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") Boolean includeDemo) {
        return ResponseEntity.ok(analyticsService.getHistoricalTrends(districtId, categoryId, includeDemo));
    }

    @GetMapping("/categories")
    public ResponseEntity<StrategyResult> getCategories(
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "false") Boolean includeDemo) {
        return ResponseEntity.ok(analyticsService.getCategoryDistribution(districtId, year, includeDemo));
    }

    @GetMapping("/strategy/{strategyId}")
    public ResponseEntity<StrategyResult> runCustomStrategy(
            @PathVariable String strategyId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(defaultValue = "false") Boolean includeDemo) {
        return ResponseEntity.ok(analyticsService.runStrategy(strategyId, districtId, categoryId, startYear, endYear, includeDemo));
    }
}
