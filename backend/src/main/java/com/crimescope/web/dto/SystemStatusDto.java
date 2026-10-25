package com.crimescope.web.dto;

import java.time.LocalDateTime;

public class SystemStatusDto {

    private String systemName = "CrimeScope AI - Tamil Nadu Intelligence Command Center";
    private String status = "OPERATIONAL_NOMINAL";
    private String databaseProductName;
    private String databaseVersion;
    private String activeProfile;
    private String javaVersion;
    private long totalCrimeRecords;
    private long totalDistrictsCount;
    private String geminiModel;
    private boolean geminiKeyConfigured;
    private LocalDateTime serverTime = LocalDateTime.now();

    public SystemStatusDto() {}

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatabaseProductName() {
        return databaseProductName;
    }

    public void setDatabaseProductName(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public long getTotalCrimeRecords() {
        return totalCrimeRecords;
    }

    public void setTotalCrimeRecords(long totalCrimeRecords) {
        this.totalCrimeRecords = totalCrimeRecords;
    }

    public long getTotalDistrictsCount() {
        return totalDistrictsCount;
    }

    public void setTotalDistrictsCount(long totalDistrictsCount) {
        this.totalDistrictsCount = totalDistrictsCount;
    }

    public String getGeminiModel() {
        return geminiModel;
    }

    public void setGeminiModel(String geminiModel) {
        this.geminiModel = geminiModel;
    }

    public boolean isGeminiKeyConfigured() {
        return geminiKeyConfigured;
    }

    public void setGeminiKeyConfigured(boolean geminiKeyConfigured) {
        this.geminiKeyConfigured = geminiKeyConfigured;
    }

    public LocalDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }
}
