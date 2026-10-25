package com.crimescope.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dataset_sources")
public class DatasetSource extends BaseEntity {

    @Column(name = "source_name", nullable = false, length = 150)
    private String sourceName;

    @Column(nullable = false, length = 150)
    private String agency;

    @Column(name = "publication_title", nullable = false, length = 255)
    private String publicationTitle;

    @Column(name = "reporting_year_start", nullable = false)
    private Integer reportingYearStart;

    @Column(name = "reporting_year_end", nullable = false)
    private Integer reportingYearEnd;

    @Column(nullable = false, length = 100)
    private String license;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "is_demonstration", nullable = false)
    private Boolean isDemonstration = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public DatasetSource() {}

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getPublicationTitle() {
        return publicationTitle;
    }

    public void setPublicationTitle(String publicationTitle) {
        this.publicationTitle = publicationTitle;
    }

    public Integer getReportingYearStart() {
        return reportingYearStart;
    }

    public void setReportingYearStart(Integer reportingYearStart) {
        this.reportingYearStart = reportingYearStart;
    }

    public Integer getReportingYearEnd() {
        return reportingYearEnd;
    }

    public void setReportingYearEnd(Integer reportingYearEnd) {
        this.reportingYearEnd = reportingYearEnd;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Boolean getIsDemonstration() {
        return isDemonstration;
    }

    public void setIsDemonstration(Boolean demonstration) {
        isDemonstration = demonstration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
