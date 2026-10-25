package com.crimescope.web.controller;

import com.crimescope.domain.model.DatasetImport;
import com.crimescope.domain.model.DatasetSource;
import com.crimescope.repository.DatasetSourceRepository;
import com.crimescope.service.CrimeRecordService;
import com.crimescope.service.DatasetImportService;
import com.crimescope.service.file.CrimeCsvHandler;
import com.crimescope.web.dto.ImportSummaryDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataManagementController {

    private final DatasetImportService datasetImportService;
    private final CrimeRecordService crimeRecordService;
    private final CrimeCsvHandler crimeCsvHandler;
    private final DatasetSourceRepository sourceRepository;

    public DataManagementController(DatasetImportService datasetImportService,
                                    CrimeRecordService crimeRecordService,
                                    CrimeCsvHandler crimeCsvHandler,
                                    DatasetSourceRepository sourceRepository) {
        this.datasetImportService = datasetImportService;
        this.crimeRecordService = crimeRecordService;
        this.crimeCsvHandler = crimeCsvHandler;
        this.sourceRepository = sourceRepository;
    }

    @PostMapping("/import/csv")
    public ResponseEntity<ImportSummaryDto> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sourceId", required = false) Long sourceId,
            Authentication authentication,
            HttpServletRequest request) throws IOException {

        String username = authentication != null ? authentication.getName() : "ADMIN_OPERATOR";
        String clientIp = request.getRemoteAddr();

        ImportSummaryDto summary = datasetImportService.importCrimeCsv(file, sourceId, username, clientIp);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(defaultValue = "false") Boolean includeDemo) throws IOException {

        byte[] csvData = crimeRecordService.exportFilteredRecordsToCsv(districtId, categoryId, startYear, endYear, includeDemo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"crimescope_records_export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @GetMapping("/template")
    public ResponseEntity<String> getSampleTemplate() {
        String template = crimeCsvHandler.generateSampleTemplateCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"crimescope_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(template);
    }

    @GetMapping("/sources")
    public ResponseEntity<List<DatasetSource>> getDatasetSources() {
        return ResponseEntity.ok(sourceRepository.findAll());
    }

    @GetMapping("/imports")
    public ResponseEntity<List<DatasetImport>> getRecentImports() {
        return ResponseEntity.ok(datasetImportService.getRecentImports());
    }
}
