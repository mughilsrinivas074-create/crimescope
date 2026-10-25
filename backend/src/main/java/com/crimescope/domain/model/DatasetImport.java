package com.crimescope.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dataset_imports")
public class DatasetImport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private DatasetSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_by_user_id")
    private User importedByUser;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_checksum", length = 64)
    private String fileChecksum;

    @Column(name = "total_rows_read", nullable = false)
    private Integer totalRowsRead = 0;

    @Column(name = "valid_records_imported", nullable = false)
    private Integer validRecordsImported = 0;

    @Column(name = "malformed_rows_count", nullable = false)
    private Integer malformedRowsCount = 0;

    @Column(name = "duplicate_rows_skipped", nullable = false)
    private Integer duplicateRowsSkipped = 0;

    @Column(nullable = false, length = 50)
    private String status = "COMPLETED";

    @Column(name = "execution_time_ms", nullable = false)
    private Long executionTimeMs = 0L;

    @Column(name = "log_summary", columnDefinition = "TEXT")
    private String logSummary;

    public DatasetImport() {}

    public DatasetSource getSource() {
        return source;
    }

    public void setSource(DatasetSource source) {
        this.source = source;
    }

    public User getImportedByUser() {
        return importedByUser;
    }

    public void setImportedByUser(User importedByUser) {
        this.importedByUser = importedByUser;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public Integer getTotalRowsRead() {
        return totalRowsRead;
    }

    public void setTotalRowsRead(Integer totalRowsRead) {
        this.totalRowsRead = totalRowsRead;
    }

    public Integer getValidRecordsImported() {
        return validRecordsImported;
    }

    public void setValidRecordsImported(Integer validRecordsImported) {
        this.validRecordsImported = validRecordsImported;
    }

    public Integer getMalformedRowsCount() {
        return malformedRowsCount;
    }

    public void setMalformedRowsCount(Integer malformedRowsCount) {
        this.malformedRowsCount = malformedRowsCount;
    }

    public Integer getDuplicateRowsSkipped() {
        return duplicateRowsSkipped;
    }

    public void setDuplicateRowsSkipped(Integer duplicateRowsSkipped) {
        this.duplicateRowsSkipped = duplicateRowsSkipped;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getLogSummary() {
        return logSummary;
    }

    public void setLogSummary(String logSummary) {
        this.logSummary = logSummary;
    }
}
