package com.crimescope.web.controller;

import com.crimescope.domain.model.AuditLog;
import com.crimescope.jdbc.EducationalJdbcCrimeDao;
import com.crimescope.repository.CrimeRecordRepository;
import com.crimescope.repository.DistrictRepository;
import com.crimescope.service.AuditLogService;
import com.crimescope.service.ai.GeminiAnalysisService;
import com.crimescope.service.mapillary.MapillaryService;
import com.crimescope.web.dto.SystemStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemStatusController {

    private final DataSource dataSource;
    private final CrimeRecordRepository crimeRecordRepository;
    private final DistrictRepository districtRepository;
    private final AuditLogService auditLogService;
    private final EducationalJdbcCrimeDao educationalJdbcCrimeDao;
    private final GeminiAnalysisService geminiAnalysisService;
    private final MapillaryService mapillaryService;
    private final Environment environment;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    public SystemStatusController(DataSource dataSource,
                                  CrimeRecordRepository crimeRecordRepository,
                                  DistrictRepository districtRepository,
                                  AuditLogService auditLogService,
                                  EducationalJdbcCrimeDao educationalJdbcCrimeDao,
                                  GeminiAnalysisService geminiAnalysisService,
                                  MapillaryService mapillaryService,
                                  Environment environment) {
        this.dataSource = dataSource;
        this.crimeRecordRepository = crimeRecordRepository;
        this.districtRepository = districtRepository;
        this.auditLogService = auditLogService;
        this.educationalJdbcCrimeDao = educationalJdbcCrimeDao;
        this.geminiAnalysisService = geminiAnalysisService;
        this.mapillaryService = mapillaryService;
        this.environment = environment;
    }

    @GetMapping("/status")
    public ResponseEntity<SystemStatusDto> getSystemStatus() {
        SystemStatusDto status = new SystemStatusDto();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            status.setDatabaseProductName(metaData.getDatabaseProductName());
            status.setDatabaseVersion(metaData.getDatabaseProductVersion());
        } catch (Exception e) {
            status.setDatabaseProductName("UNKNOWN (Error acquiring connection)");
            status.setDatabaseVersion(e.getMessage());
        }

        status.setActiveProfile(Arrays.toString(environment.getActiveProfiles()));
        status.setJavaVersion(System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        status.setTotalCrimeRecords(crimeRecordRepository.count());
        status.setTotalDistrictsCount(districtRepository.count());
        status.setGeminiModel(geminiModel);
        status.setGeminiKeyConfigured(geminiApiKey != null && !geminiApiKey.isBlank() && !geminiApiKey.equals("your_api_key_here"));

        return ResponseEntity.ok(status);
    }

    @GetMapping("/keys-status")
    public ResponseEntity<Map<String, Object>> getApiKeysStatus() {
        Map<String, Object> keysStatus = new LinkedHashMap<>();
        keysStatus.put("gemini", geminiAnalysisService.getGeminiStatus());
        keysStatus.put("mapillary", mapillaryService.getMapillaryStatus());
        return ResponseEntity.ok(keysStatus);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(auditLogService.getAuditLogsPaged(PageRequest.of(page, size)));
    }

    @GetMapping("/jdbc-demo")
    public ResponseEntity<Map<String, Object>> runJdbcDemo(
            @RequestParam(required = false, defaultValue = "CHN") String districtCode,
            @RequestParam(required = false, defaultValue = "2023") Integer year) {
        return ResponseEntity.ok(educationalJdbcCrimeDao.executeEducationalQuery(districtCode, year));
    }
}
