package com.crimescope.web.controller;

import com.crimescope.service.mapillary.MapillaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller exposing Mapillary street-level exploration, image metadata,
 * and token diagnostics for geographic visualization in Tamil Nadu.
 */
@RestController
@RequestMapping("/api/mapillary")
public class MapillaryController {

    private final MapillaryService mapillaryService;

    public MapillaryController(MapillaryService mapillaryService) {
        this.mapillaryService = mapillaryService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(mapillaryService.getMapillaryStatus());
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(mapillaryService.getMapillaryConfig());
    }

    @GetMapping("/imagery-near")
    public ResponseEntity<Map<String, Object>> getImageryNear(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false, defaultValue = "1000") Integer radius) {
        return ResponseEntity.ok(mapillaryService.searchImageryNear(lat, lng, radius));
    }

    @GetMapping("/district-coverage")
    public ResponseEntity<Map<String, Object>> getDistrictCoverage(@RequestParam String districtCode) {
        return ResponseEntity.ok(mapillaryService.getDistrictStreetCoverage(districtCode));
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        return ResponseEntity.ok(mapillaryService.validateCandidateToken(token));
    }
}
