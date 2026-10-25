package com.crimescope.web.controller;

import com.crimescope.domain.model.CrimeCategory;
import com.crimescope.service.CrimeCategoryService;
import com.crimescope.web.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crime-categories")
public class CrimeCategoryController {

    private final CrimeCategoryService categoryService;

    public CrimeCategoryController(CrimeCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CrimeCategory>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrimeCategory> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrimeCategory ID " + id + " not found.")));
    }
}
