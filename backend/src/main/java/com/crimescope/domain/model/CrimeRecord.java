package com.crimescope.domain.model;

import com.crimescope.domain.enums.VerificationStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Encapsulated Core Domain Entity: Historical and Registered Crime Record.
 * Distinguishes genuine official records from demonstration records.
 * Maintains referential integrity and precision geospatial metadata when available.
 */
@Entity
@Table(name = "crime_records")
public class CrimeRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CrimeCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private DatasetSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id")
    private DatasetImport datasetImport;

    @Column(name = "reporting_year", nullable = false)
    private Integer reportingYear;

    @Column(name = "reporting_month")
    private Integer reportingMonth;

    @Column(name = "case_count", nullable = false)
    private Integer caseCount = 0;

    @Column(name = "arrest_count")
    private Integer arrestCount;

    @Column(name = "convicted_count")
    private Integer convictedCount;

    @Column(name = "chargesheeted_count")
    private Integer chargesheetedCount;

    @Column(name = "is_demonstration", nullable = false)
    private Boolean isDemonstration = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.VERIFIED;

    @Column(name = "reporting_unit", length = 100)
    private String reportingUnit = "District Police Office";

    @Column(name = "incident_date")
    private LocalDate incidentDate;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "location_description", length = 255)
    private String locationDescription;

    @Column(name = "source_reference", length = 150)
    private String sourceReference;

    @Column(name = "data_hash", length = 64, unique = true)
    private String dataHash;

    public CrimeRecord() {}

    /**
     * Domain method calculating conviction/clearance rate safely with zero denominator protection.
     */
    public Double calculateClearanceRate() {
        if (caseCount == null || caseCount <= 0 || chargesheetedCount == null) {
            return null;
        }
        return Math.min(100.0, ((double) chargesheetedCount / caseCount) * 100.0);
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public CrimeCategory getCategory() {
        return category;
    }

    public void setCategory(CrimeCategory category) {
        this.category = category;
    }

    public DatasetSource getSource() {
        return source;
    }

    public void setSource(DatasetSource source) {
        this.source = source;
    }

    public DatasetImport getDatasetImport() {
        return datasetImport;
    }

    public void setDatasetImport(DatasetImport datasetImport) {
        this.datasetImport = datasetImport;
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

    public Boolean getIsDemonstration() {
        return isDemonstration;
    }

    public void setIsDemonstration(Boolean demonstration) {
        isDemonstration = demonstration;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getReportingUnit() {
        return reportingUnit;
    }

    public void setReportingUnit(String reportingUnit) {
        this.reportingUnit = reportingUnit;
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

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public String getDataHash() {
        return dataHash;
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }
}
