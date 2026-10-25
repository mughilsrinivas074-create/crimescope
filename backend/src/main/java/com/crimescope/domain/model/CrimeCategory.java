package com.crimescope.domain.model;

import com.crimescope.domain.enums.SeverityLevel;
import jakarta.persistence.*;

@Entity
@Table(name = "crime_categories")
public class CrimeCategory extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false, length = 20)
    private SeverityLevel severityLevel = SeverityLevel.MEDIUM;

    @Column(name = "is_ipc", nullable = false)
    private Boolean isIpc = true;

    public CrimeCategory() {}

    public CrimeCategory(String code, String name, String description, SeverityLevel severityLevel, Boolean isIpc) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.severityLevel = severityLevel;
        this.isIpc = isIpc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
    }

    public Boolean getIsIpc() {
        return isIpc;
    }

    public void setIsIpc(Boolean ipc) {
        isIpc = ipc;
    }
}
