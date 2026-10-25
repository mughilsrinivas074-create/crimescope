package com.crimescope.service;

import com.crimescope.domain.model.DatasetImport;
import com.crimescope.domain.model.DatasetSource;
import com.crimescope.domain.model.User;
import com.crimescope.repository.CrimeRecordRepository;
import com.crimescope.repository.DatasetImportRepository;
import com.crimescope.repository.DatasetSourceRepository;
import com.crimescope.repository.UserRepository;
import com.crimescope.service.file.CrimeCsvHandler;
import com.crimescope.service.file.CsvValidationResult;
import com.crimescope.service.file.MalformedRowRecord;
import com.crimescope.web.dto.ImportSummaryDto;
import com.crimescope.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Transactional
public class DatasetImportService {

    private final CrimeCsvHandler csvHandler;
    private final CrimeRecordRepository crimeRecordRepository;
    private final DatasetImportRepository importRepository;
    private final DatasetSourceRepository sourceRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public DatasetImportService(CrimeCsvHandler csvHandler,
                                CrimeRecordRepository crimeRecordRepository,
                                DatasetImportRepository importRepository,
                                DatasetSourceRepository sourceRepository,
                                UserRepository userRepository,
                                AuditLogService auditLogService) {
        this.csvHandler = csvHandler;
        this.crimeRecordRepository = crimeRecordRepository;
        this.importRepository = importRepository;
        this.sourceRepository = sourceRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public ImportSummaryDto importCrimeCsv(MultipartFile file, Long sourceId, String username, String ipAddress) throws IOException {
        long start = System.currentTimeMillis();

        DatasetSource source = null;
        if (sourceId != null) {
            source = sourceRepository.findById(sourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dataset Source " + sourceId + " not found."));
        } else {
            source = sourceRepository.findAll().stream().findFirst().orElse(null);
        }

        User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        CsvValidationResult result;
        try (InputStream is = file.getInputStream()) {
            result = csvHandler.parseCsv(is, source);
        }

        // Persist valid records in batch
        if (!result.getValidRecords().isEmpty()) {
            crimeRecordRepository.saveAll(result.getValidRecords());
        }

        long executionMs = System.currentTimeMillis() - start;

        // Create DatasetImport log record
        DatasetImport importRecord = new DatasetImport();
        importRecord.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded_crime_data.csv");
        importRecord.setSource(source);
        importRecord.setImportedByUser(user);
        importRecord.setTotalRowsRead(result.getTotalRowsProcessed());
        importRecord.setValidRecordsImported(result.getValidRecords().size());
        importRecord.setMalformedRowsCount(result.getMalformedRows().size());
        importRecord.setDuplicateRowsSkipped(result.getDuplicateHashesSkipped().size());
        importRecord.setExecutionTimeMs(executionMs);
        importRecord.setStatus(result.getMalformedRows().isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS");
        importRecord.setLogSummary(buildLogSummary(result));

        DatasetImport savedImport = importRepository.save(importRecord);

        auditLogService.recordAction(user != null ? user.getId() : null, username, "IMPORT_CSV_BATCH",
                "DatasetImport", savedImport.getId(),
                String.format("Imported %d records from %s (%d duplicates skipped, %d malformed)",
                        result.getValidRecords().size(), file.getOriginalFilename(),
                        result.getDuplicateHashesSkipped().size(), result.getMalformedRows().size()),
                ipAddress);

        return new ImportSummaryDto(
                savedImport.getId(),
                file.getOriginalFilename(),
                result.getTotalRowsProcessed(),
                result.getValidRecords().size(),
                result.getMalformedRows().size(),
                result.getDuplicateHashesSkipped().size(),
                executionMs,
                savedImport.getStatus(),
                result.getMalformedRows().stream().map(MalformedRowRecord::toString).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<DatasetImport> getRecentImports() {
        return importRepository.findTop20ByOrderByCreatedAtDesc();
    }

    private String buildLogSummary(CsvValidationResult res) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Processed: %d rows | Valid: %d | Duplicates Skipped: %d | Malformed: %d\n",
                res.getTotalRowsProcessed(), res.getValidRecords().size(),
                res.getDuplicateHashesSkipped().size(), res.getMalformedRows().size()));
        for (MalformedRowRecord m : res.getMalformedRows()) {
            sb.append(m.toString()).append("\n");
        }
        return sb.toString();
    }
}
