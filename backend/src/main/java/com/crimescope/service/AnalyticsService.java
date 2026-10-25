package com.crimescope.service;

import com.crimescope.domain.model.CrimeRecord;
import com.crimescope.domain.model.District;
import com.crimescope.domain.strategy.CrimeAnalyticsStrategy;
import com.crimescope.domain.strategy.StrategyResult;
import com.crimescope.repository.CrimeCategoryRepository;
import com.crimescope.repository.CrimeRecordRepository;
import com.crimescope.repository.DistrictRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final CrimeRecordRepository crimeRecordRepository;
    private final DistrictRepository districtRepository;
    private final CrimeCategoryRepository categoryRepository;
    private final Map<String, CrimeAnalyticsStrategy> strategyMap;

    public AnalyticsService(CrimeRecordRepository crimeRecordRepository,
                            DistrictRepository districtRepository,
                            CrimeCategoryRepository categoryRepository,
                            List<CrimeAnalyticsStrategy> strategies) {
        this.crimeRecordRepository = crimeRecordRepository;
        this.districtRepository = districtRepository;
        this.categoryRepository = categoryRepository;
        this.strategyMap = new HashMap<>();
        for (CrimeAnalyticsStrategy s : strategies) {
            this.strategyMap.put(s.getStrategyIdentifier(), s);
        }
    }

    /**
     * Top-level overview telemetry for Command Center heads-up display.
     */
    public Map<String, Object> getCommandCenterOverview(Boolean includeDemo) {
        Map<String, Object> response = new LinkedHashMap<>();

        long totalCases = crimeRecordRepository.sumTotalCases(includeDemo);
        List<District> allDistricts = districtRepository.findAll();
        long totalDistricts = allDistricts.size();

        List<Object[]> yearlyData = crimeRecordRepository.findYearlyAggregates(includeDemo);
        List<Map<String, Object>> yearlyTrends = new ArrayList<>();
        for (Object[] row : yearlyData) {
            yearlyTrends.add(Map.of(
                    "year", row[0],
                    "totalCases", row[1],
                    "arrests", row[2] != null ? row[2] : 0,
                    "convictions", row[3] != null ? row[3] : 0
            ));
        }

        List<Object[]> districtData = crimeRecordRepository.findDistrictAggregates(includeDemo);
        List<Map<String, Object>> topDistricts = new ArrayList<>();
        int count = 0;
        for (Object[] row : districtData) {
            if (count++ >= 8) break;
            topDistricts.add(Map.of("districtName", row[0], "totalCases", row[1]));
        }

        List<Object[]> categoryData = crimeRecordRepository.findCategoryAggregates(includeDemo);
        List<Map<String, Object>> categoryBreakdown = new ArrayList<>();
        for (Object[] row : categoryData) {
            categoryBreakdown.add(Map.of("categoryName", row[0], "totalCases", row[1]));
        }

        // Run Hotspot Density Strategy for full map choropleth & threat levels
        List<CrimeRecord> allRecords = crimeRecordRepository.filterRecords(null, null, 2018, 2023, includeDemo);
        CrimeAnalyticsStrategy hotspotStrategy = strategyMap.get("HOTSPOT_DENSITY");
        StrategyResult hotspotResult = hotspotStrategy != null ?
                hotspotStrategy.evaluate(allRecords, Map.of()) : null;

        response.put("totalRegisteredCases", totalCases);
        response.put("monitoredDistrictsCount", totalDistricts);
        response.put("reportingPeriod", "2018 - 2023 Historical SCRB/NCRB");
        response.put("yearlyTrends", yearlyTrends);
        response.put("topDistricts", topDistricts);
        response.put("categoryBreakdown", categoryBreakdown);
        response.put("districtHotspots", hotspotResult != null ? hotspotResult.getVisualizationData().get("hotspots") : List.of());
        response.put("systemStatus", "OPERATIONAL_NOMINAL");

        return response;
    }

    /**
     * Polymorphic strategy evaluator executing requested analytical algorithm.
     */
    public StrategyResult runStrategy(String strategyId, Long districtId, Long categoryId,
                                      Integer startYear, Integer endYear, Boolean includeDemo) {
        CrimeAnalyticsStrategy strategy = strategyMap.get(strategyId.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown analytical strategy identifier: " + strategyId);
        }

        List<CrimeRecord> records = crimeRecordRepository.filterRecords(districtId, categoryId, startYear, endYear, includeDemo);
        return strategy.evaluate(records, Map.of("districtId", districtId != null ? districtId : 0L));
    }

    /**
     * Multi-district direct comparative analysis.
     */
    public StrategyResult compareDistricts(List<Long> districtIds, Integer year, Boolean includeDemo) {
        CrimeAnalyticsStrategy strategy = strategyMap.get("DISTRICT_COMPARISON");
        List<CrimeRecord> records = new ArrayList<>();
        if (districtIds != null && !districtIds.isEmpty()) {
            for (Long dId : districtIds) {
                records.addAll(crimeRecordRepository.filterRecords(dId, null, year, year, includeDemo));
            }
        } else {
            records = crimeRecordRepository.filterRecords(null, null, year, year, includeDemo);
        }
        return strategy.evaluate(records, Map.of());
    }

    /**
     * Category distribution and severity index.
     */
    public StrategyResult getCategoryDistribution(Long districtId, Integer year, Boolean includeDemo) {
        CrimeAnalyticsStrategy strategy = strategyMap.get("CATEGORY_DISTRIBUTION");
        List<CrimeRecord> records = crimeRecordRepository.filterRecords(districtId, null, year, year, includeDemo);
        return strategy.evaluate(records, Map.of());
    }

    /**
     * Trajectory trend and year-over-year forecasting strategy.
     */
    public StrategyResult getHistoricalTrends(Long districtId, Long categoryId, Boolean includeDemo) {
        CrimeAnalyticsStrategy strategy = strategyMap.get("TREND_FORECASTING");
        List<CrimeRecord> records = crimeRecordRepository.filterRecords(districtId, categoryId, 2018, 2023, includeDemo);
        return strategy.evaluate(records, Map.of());
    }
}
