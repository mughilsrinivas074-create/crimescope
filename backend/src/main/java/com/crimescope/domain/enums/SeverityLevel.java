package com.crimescope.domain.enums;

public enum SeverityLevel {
    LOW(1, "Low Severity"),
    MEDIUM(2, "Medium Severity"),
    HIGH(3, "High Severity"),
    CRITICAL(4, "Critical Threat");

    private final int weight;
    private final String displayLabel;

    SeverityLevel(int weight, String displayLabel) {
        this.weight = weight;
        this.displayLabel = displayLabel;
    }

    public int getWeight() {
        return weight;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }
}
