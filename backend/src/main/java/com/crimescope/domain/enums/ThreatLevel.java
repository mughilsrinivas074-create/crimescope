package com.crimescope.domain.enums;

public enum ThreatLevel {
    LOW("#00e5a3", "Low Activity"),
    MODERATE("#00f0ff", "Moderate Watch"),
    ELEVATED("#f59e0b", "Elevated Caution"),
    HIGH("#f97316", "High Alert"),
    CRITICAL("#ef4444", "Critical Response");

    private final String hexColor;
    private final String statusLabel;

    ThreatLevel(String hexColor, String statusLabel) {
        this.hexColor = hexColor;
        this.statusLabel = statusLabel;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getStatusLabel() {
        return statusLabel;
    }
}
