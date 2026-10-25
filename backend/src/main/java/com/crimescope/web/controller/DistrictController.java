package com.crimescope.web.controller;

import com.crimescope.domain.model.District;
import com.crimescope.service.DistrictService;
import com.crimescope.web.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
public class DistrictController {

    private final DistrictService districtService;

    public DistrictController(DistrictService districtService) {
        this.districtService = districtService;
    }

    @GetMapping
    public ResponseEntity<List<District>> getAllDistricts(@RequestParam(required = false) String zone) {
        if (zone != null && !zone.isBlank()) {
            return ResponseEntity.ok(districtService.getDistrictsByZone(zone));
        }
        return ResponseEntity.ok(districtService.getAllDistricts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<District> getDistrictById(@PathVariable Long id) {
        return ResponseEntity.ok(districtService.getDistrictById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District ID " + id + " not found.")));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<District> getDistrictByCode(@PathVariable String code) {
        return ResponseEntity.ok(districtService.getDistrictByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("District code " + code + " not found.")));
    }
}
