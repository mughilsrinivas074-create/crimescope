package com.crimescope.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CrimeRecordDto {

    private Long id;

    @NotNull(message = "District ID is required")
    private Long districtId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Reporting year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    @Max(value = 2030, message = "Year cannot exceed 2030")
    private Integer reportingYear;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer reportingMonth;

    @NotNull(message = "Case count is required")
    @Min(value = 0, message = "Case count cannot be negative")
    private Integer caseCount;

    @Min(value = 0, message = "Arrests cannot be negative")
    private Integer arrestCount;

    @Min(value = 0, message = "Convictions cannot be negative")
    private Integer convictedCount;

    @Min(value = 0, message = "Chargesheeted count cannot be negative")
    private Integer chargesheetedCount;

    private LocalDate incidentDate;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationDescription;
    private String reportingUnit;
    private String sourceReference;
    private Boolean isDemonstration = false;

    public CrimeRecordDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getReportingYear() {
        return reportingYear;
    }

    public void setReportingYear(Integer reportingYear) {
        this.reportingYear = reportingYear;
    }

    public Integer getReportingMonth() {
        return reportingMonth;
    }

    public void setReportingMonth(Integer reportingMonth) {
        this.reportingMonth = reportingMonth;
    }

    public Integer getCaseCount() {
        return caseCount;
    }

    public void setCaseCount(Integer caseCount) {
        this.caseCount = caseCount;
    }

    public Integer getArrestCount() {
        return arrestCount;
    }

    public void setArrestCount(Integer arrestCount) {
        this.arrestCount = arrestCount;
    }

    public Integer getConvictedCount() {
        return convictedCount;
    }

    public void setConvictedCount(Integer convictedCount) {
        this.convictedCount = convictedCount;
    }

    public Integer getChargesheetedCount() {
        return chargesheetedCount;
    }

    public void setChargesheetedCount(Integer chargesheetedCount) {
        this.chargesheetedCount = chargesheetedCount;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(String reportingUnit) {
        this.reportingUnit = reportingUnit;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public Boolean getIsDemonstration() {
        return isDemonstration;
    }

    public void setIsDemonstration(Boolean demonstration) {
        isDemonstration = demonstration;
    }
}
