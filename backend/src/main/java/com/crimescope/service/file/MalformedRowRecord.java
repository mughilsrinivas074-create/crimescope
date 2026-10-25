package com.crimescope.service.file;

import java.io.Serializable;

public class MalformedRowRecord implements Serializable {

    private final int lineNumber;
    private final String rawContent;
    private final String errorField;
    private final String failureReason;

    public MalformedRowRecord(int lineNumber, String rawContent, String errorField, String failureReason) {
        this.lineNumber = lineNumber;
        this.rawContent = rawContent;
        this.errorField = errorField;
        this.failureReason = failureReason;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getRawContent() {
        return rawContent;
    }

    public String getErrorField() {
        return errorField;
    }

    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String toString() {
        return String.format("Line %d [Field: %s]: %s", lineNumber, errorField, failureReason);
    }
}
