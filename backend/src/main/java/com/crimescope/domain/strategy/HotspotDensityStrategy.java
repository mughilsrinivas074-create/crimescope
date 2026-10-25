package com.crimescope.domain.strategy;

import com.crimescope.domain.enums.ThreatLevel;
import com.crimescope.domain.model.CrimeRecord;
import com.crimescope.domain.model.District;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy evaluating geographic crime density per 100 square kilometers,
 * calculating spatial risk ranking and assign dynamic threat levels.
 */
@Component("hotspotDensityStrategy")
public class HotspotDensityStrategy implements CrimeAnalyticsStrategy {

    @Override
    public StrategyResult evaluate(List<CrimeRecord> records, Map<String, Object> parameters) {
        if (records == null || records.isEmpty()) {
            return new StrategyResult(getStrategyIdentifier(), "No records available for density evaluation.",
                    Map.of("hotspots", 0), Map.of(), 0.0);
        }

        Map<District, List<CrimeRecord>> byDistrict = records.stream()
                .filter(r -> r.getDistrict() != null)
                .collect(Collectors.groupingBy(CrimeRecord::getDistrict));

        List<Map<String, Object>> hotspotList = new ArrayList<>();

        for (Map.Entry<District, List<CrimeRecord>> entry : byDistrict.entrySet()) {
            District district = entry.getKey();
            List<CrimeRecord> dRecords = entry.getValue();

            long totalCases = dRecords.stream().mapToLong(r -> r.getCaseCount() != null ? r.getCaseCount() : 0).sum();
            BigDecimal area = district.getAreaSqKm();

            // Cases per 100 sq km (safe calculation)
            double casesPer100SqKm = 0.0;
            if (area != null && area.doubleValue() > 0) {
                casesPer100SqKm = (totalCases / area.doubleValue()) * 100.0;
            }

            // Assign threat level dynamically based on density thresholds
            ThreatLevel threat;
            if (casesPer100SqKm > 150.0 || totalCases > 2500) {
                threat = ThreatLevel.CRITICAL;
            } else if (casesPer100SqKm > 80.0 || totalCases > 1800) {
                threat = ThreatLevel.HIGH;
            } else if (casesPer100SqKm > 40.0 || totalCases > 1200) {
                threat = ThreatLevel.ELEVATED;
            } else if (casesPer100SqKm > 15.0 || totalCases > 600) {
                threat = ThreatLevel.MODERATE;
            } else {
                threat = ThreatLevel.LOW;
            }

            Map<String, Object> hData = new LinkedHashMap<>();
            hData.put("districtId", district.getId());
            hData.put("districtCode", district.getCode());
            hData.put("districtName", district.getName());
            hData.put("zone", district.getZone());
            hData.put("totalCases", totalCases);
            hData.put("areaSqKm", area != null ? area.doubleValue() : null);
            hData.put("densityPer100SqKm", Math.round(casesPer100SqKm * 10.0) / 10.0);
            hData.put("threatLevel", threat.name());
            hData.put("threatColor", threat.getHexColor());
            hData.put("threatLabel", threat.getStatusLabel());
            hData.put("centerLat", district.getCenterLat());
            hData.put("centerLng", district.getCenterLng());

            hotspotList.add(hData);
        }

        hotspotList.sort((a, b) -> Double.compare((Double) b.get("densityPer100SqKm"), (Double) a.get("densityPer100SqKm")));

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("analyzedDistricts", hotspotList.size());
        metrics.put("highestDensityDistrict", hotspotList.isEmpty() ? "None" : hotspotList.getFirst().get("districtName"));
        metrics.put("peakDensityScore", hotspotList.isEmpty() ? 0.0 : hotspotList.getFirst().get("densityPer100SqKm"));

        Map<String, Object> vizData = new LinkedHashMap<>();
        vizData.put("hotspots", hotspotList);

        String summary = String.format("Spatial density analysis mapped %d districts. Highest geographic density identified in %s.",
                hotspotList.size(), metrics.get("highestDensityDistrict"));

        return new StrategyResult(getStrategyIdentifier(), summary, metrics, vizData,
                hotspotList.isEmpty() ? 0.0 : (Double) hotspotList.getFirst().get("densityPer100SqKm"));
    }

    @Override
    public String getStrategyIdentifier() {
        return "HOTSPOT_DENSITY";
    }
}
