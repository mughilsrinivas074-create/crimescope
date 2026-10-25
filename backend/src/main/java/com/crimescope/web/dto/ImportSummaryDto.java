package com.crimescope.web.dto;

import java.util.List;

public class ImportSummaryDto {

    private Long importId;
    private String fileName;
    private int totalRows;
    private int validRecordsImported;
    private int malformedRows;
    private int duplicateRowsSkipped;
    private long executionTimeMs;
    private String status;
    private List<String> malformedRowDetails;

    public ImportSummaryDto(Long importId, String fileName, int totalRows, int validRecordsImported,
                            int malformedRows, int duplicateRowsSkipped, long executionTimeMs,
                            String status, List<String> malformedRowDetails) {
        this.importId = importId;
        this.fileName = fileName;
        this.totalRows = totalRows;
        this.validRecordsImported = validRecordsImported;
        this.malformedRows = malformedRows;
        this.duplicateRowsSkipped = duplicateRowsSkipped;
        this.executionTimeMs = executionTimeMs;
        this.status = status;
        this.malformedRowDetails = malformedRowDetails;
    }

    public Long getImportId() {
        return importId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getValidRecordsImported() {
        return validRecordsImported;
    }

    public int getMalformedRows() {
        return malformedRows;
    }

    public int getDuplicateRowsSkipped() {
        return duplicateRowsSkipped;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getMalformedRowDetails() {
        return malformedRowDetails;
    }
}
