package com.crimescope;

import com.crimescope.domain.enums.SeverityLevel;
import com.crimescope.domain.model.CrimeCategory;
import com.crimescope.domain.model.CrimeRecord;
import com.crimescope.domain.model.District;
import com.crimescope.domain.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Crime Analytics Strategy Polymorphism Tests")
class CrimeAnalyticsStrategyTest {

    private District chennai;
    private District coimbatore;
    private CrimeCategory violentCrime;
    private CrimeCategory cyberCrime;
    private List<CrimeRecord> mockRecords;

    @BeforeEach
    void setUp() {
        chennai = new District("CHN", "Chennai", "Chennai", "North",
                BigDecimal.valueOf(426.0), 8696010L, BigDecimal.valueOf(13.08), BigDecimal.valueOf(80.27));
        chennai.setId(1L);

        coimbatore = new District("CBE", "Coimbatore", "Coimbatore", "West",
                BigDecimal.valueOf(4723.0), 3934500L, BigDecimal.valueOf(11.01), BigDecimal.valueOf(76.95));
        coimbatore.setId(2L);

        violentCrime = new CrimeCategory("VIOLENT_PERSON", "Violent Crimes against Person",
                "Assault and homicide", SeverityLevel.HIGH, true);
        violentCrime.setId(1L);

        cyberCrime = new CrimeCategory("CYBER_CRIME", "Cyber & Financial Tech Crimes",
                "IT Act offences", SeverityLevel.HIGH, false);
        cyberCrime.setId(2L);

        mockRecords = new ArrayList<>();

        // Chennai record
        CrimeRecord r1 = new CrimeRecord();
        r1.setId(101L);
        r1.setDistrict(chennai);
        r1.setCategory(cyberCrime);
        r1.setReportingYear(2022);
        r1.setCaseCount(300);
        r1.setArrestCount(200);
        r1.setConvictedCount(90);
        r1.setChargesheetedCount(240);
        mockRecords.add(r1);

        // Chennai record next year (shows growth)
        CrimeRecord r2 = new CrimeRecord();
        r2.setId(102L);
        r2.setDistrict(chennai);
        r2.setCategory(cyberCrime);
        r2.setReportingYear(2023);
        r2.setCaseCount(450);
        r2.setArrestCount(310);
        r2.setConvictedCount(140);
        r2.setChargesheetedCount(360);
        mockRecords.add(r2);

        // Coimbatore record
        CrimeRecord r3 = new CrimeRecord();
        r3.setId(103L);
        r3.setDistrict(coimbatore);
        r3.setCategory(violentCrime);
        r3.setReportingYear(2023);
        r3.setCaseCount(150);
        r3.setArrestCount(130);
        r3.setConvictedCount(70);
        r3.setChargesheetedCount(140);
        mockRecords.add(r3);
    }

    @Test
    @DisplayName("DistrictComparisonStrategy evaluates comparative metrics and clearance rate")
    void testDistrictComparisonStrategy() {
        CrimeAnalyticsStrategy strategy = new DistrictComparisonStrategy();
        assertEquals("DISTRICT_COMPARISON", strategy.getStrategyIdentifier());

        StrategyResult result = strategy.evaluate(mockRecords, Map.of());
        assertNotNull(result);
        assertEquals(2, result.getNumericalMetrics().get("districtCount"));
        assertEquals(900L, result.getNumericalMetrics().get("totalOverallCases"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> table = (List<Map<String, Object>>) result.getVisualizationData().get("comparisonTable");
        assertEquals(2, table.size());

        // Chennai has 750 cases total, Coimbatore has 150
        assertEquals("Chennai", table.getFirst().get("districtName"));
        assertEquals(750L, table.getFirst().get("totalCases"));
        assertEquals("Coimbatore", table.get(1).get("districtName"));
        assertEquals(150L, table.get(1).get("totalCases"));
    }

    @Test
    @DisplayName("TrendForecastingStrategy computes trajectory and protects against zero division")
    void testTrendForecastingStrategy() {
        CrimeAnalyticsStrategy strategy = new TrendForecastingStrategy();
        assertEquals("TREND_FORECASTING", strategy.getStrategyIdentifier());

        StrategyResult result = strategy.evaluate(mockRecords, Map.of());
        assertNotNull(result);
        assertTrue(result.getSummaryDescription().contains("trajectory"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trends = (List<Map<String, Object>>) result.getVisualizationData().get("yearlyTrends");
        assertEquals(2, trends.size()); // 2022 and 2023

        // In 2022: 300 cases; in 2023: 600 cases total (450+150). Growth: (600-300)/300 = +100%
        Map<String, Object> point2023 = trends.get(1);
        assertEquals(2023, point2023.get("year"));
        assertEquals(600L, point2023.get("totalCases"));
        assertEquals(100.0, (Double) point2023.get("yoyChangePct"), 0.1);
    }

    @Test
    @DisplayName("HotspotDensityStrategy computes spatial density per 100 sq km")
    void testHotspotDensityStrategy() {
        CrimeAnalyticsStrategy strategy = new HotspotDensityStrategy();
        assertEquals("HOTSPOT_DENSITY", strategy.getStrategyIdentifier());

        StrategyResult result = strategy.evaluate(mockRecords, Map.of());
        assertNotNull(result);

        // Chennai area is 426 sq km, cases = 750 -> Density is ~176.0 cases per 100 sq km
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hotspots = (List<Map<String, Object>>) result.getVisualizationData().get("hotspots");
        assertEquals(2, hotspots.size());
        assertEquals("Chennai", hotspots.getFirst().get("districtName"));
        assertTrue((Double) hotspots.getFirst().get("densityPer100SqKm") > 150.0);
        assertEquals("CRITICAL", hotspots.getFirst().get("threatLevel"));
    }

    @Test
    @DisplayName("CategoryDistributionStrategy calculates severity-weighted crime index")
    void testCategoryDistributionStrategy() {
        CrimeAnalyticsStrategy strategy = new CategoryDistributionStrategy();
        assertEquals("CATEGORY_DISTRIBUTION", strategy.getStrategyIdentifier());

        StrategyResult result = strategy.evaluate(mockRecords, Map.of());
        assertNotNull(result);
        assertEquals(2, result.getNumericalMetrics().get("totalCategoriesRepresented"));
        assertEquals(900L, result.getNumericalMetrics().get("grandTotalCases"));
        assertEquals(3.0, (Double) result.getNumericalMetrics().get("severityWeightedIndex"), 0.05); // HIGH weight is 3
    }

    @Test
    @DisplayName("Empty record handling returns zeroed result without exception")
    void testEmptyRecordsSafety() {
        CrimeAnalyticsStrategy strategy = new DistrictComparisonStrategy();
        StrategyResult result = strategy.evaluate(List.of(), Map.of());
        assertEquals(0, result.getNumericalMetrics().get("totalRecords"));
    }
}
