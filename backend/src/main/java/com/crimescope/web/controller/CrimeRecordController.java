package com.crimescope.web.controller;

import com.crimescope.domain.model.CrimeRecord;
import com.crimescope.service.CrimeRecordService;
import com.crimescope.web.dto.CrimeRecordDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crime-records")
public class CrimeRecordController {

    private final CrimeRecordService crimeRecordService;

    public CrimeRecordController(CrimeRecordService crimeRecordService) {
        this.crimeRecordService = crimeRecordService;
    }

    @GetMapping
    public ResponseEntity<Page<CrimeRecord>> getRecordsPaged(
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(defaultValue = "false") Boolean includeDemo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "reportingYear") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(crimeRecordService.getFilteredRecordsPaged(
                districtId, categoryId, startYear, endYear, includeDemo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrimeRecord> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(crimeRecordService.getRecordById(id));
    }

    @PostMapping
    public ResponseEntity<CrimeRecord> createRecord(@Valid @RequestBody CrimeRecordDto dto,
                                                    Authentication authentication,
                                                    HttpServletRequest request) {
        String username = authentication != null ? authentication.getName() : "ANONYMOUS_OPERATOR";
        String clientIp = request.getRemoteAddr();
        CrimeRecord created = crimeRecordService.createRecord(dto, username, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrimeRecord> updateRecord(@PathVariable Long id,
                                                    @Valid @RequestBody CrimeRecordDto dto,
                                                    Authentication authentication,
                                                    HttpServletRequest request) {
        String username = authentication != null ? authentication.getName() : "ANONYMOUS_OPERATOR";
        String clientIp = request.getRemoteAddr();
        CrimeRecord updated = crimeRecordService.updateRecord(id, dto, username, clientIp);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id,
                                             Authentication authentication,
                                             HttpServletRequest request) {
        String username = authentication != null ? authentication.getName() : "ANONYMOUS_OPERATOR";
        String clientIp = request.getRemoteAddr();
        crimeRecordService.deleteRecord(id, username, clientIp);
        return ResponseEntity.noContent().build();
    }
}
