package com.crimescope.service.file;

import com.crimescope.domain.model.CrimeRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CsvValidationResult implements Serializable {

    private int totalRowsProcessed = 0;
    private final List<CrimeRecord> validRecords = new ArrayList<>();
    private final List<MalformedRowRecord> malformedRows = new ArrayList<>();
    private final List<String> duplicateHashesSkipped = new ArrayList<>();
    private long executionTimeMs = 0L;

    public CsvValidationResult() {}

    public void incrementTotalRows() {
        this.totalRowsProcessed++;
    }

    public void addValidRecord(CrimeRecord record) {
        this.validRecords.add(record);
    }

    public void addMalformedRow(MalformedRowRecord malformed) {
        this.malformedRows.add(malformed);
    }

    public void addDuplicateSkipped(String hash) {
        this.duplicateHashesSkipped.add(hash);
    }

    public int getTotalRowsProcessed() {
        return totalRowsProcessed;
    }

    public void setTotalRowsProcessed(int totalRowsProcessed) {
        this.totalRowsProcessed = totalRowsProcessed;
    }

    public List<CrimeRecord> getValidRecords() {
        return validRecords;
    }

    public List<MalformedRowRecord> getMalformedRows() {
        return malformedRows;
    }

    public List<String> getDuplicateHashesSkipped() {
        return duplicateHashesSkipped;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public boolean isSuccessful() {
        return !validRecords.isEmpty() && malformedRows.isEmpty();
    }
}
