package com.crimescope.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "districts")
public class District extends BaseEntity {

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String headquarters;

    @Column(nullable = false, length = 50)
    private String zone;

    @Column(name = "area_sq_km", precision = 10, scale = 2)
    private BigDecimal areaSqKm;

    @Column(name = "population_2021_est")
    private Long population2021Est;

    @Column(name = "center_lat", precision = 9, scale = 6)
    private BigDecimal centerLat;

    @Column(name = "center_lng", precision = 9, scale = 6)
    private BigDecimal centerLng;

    public District() {}

    public District(String code, String name, String headquarters, String zone,
                    BigDecimal areaSqKm, Long population2021Est,
                    BigDecimal centerLat, BigDecimal centerLng) {
        this.code = code;
        this.name = name;
        this.headquarters = headquarters;
        this.zone = zone;
        this.areaSqKm = areaSqKm;
        this.population2021Est = population2021Est;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
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

    public String getHeadquarters() {
        return headquarters;
    }

    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public BigDecimal getAreaSqKm() {
        return areaSqKm;
    }

    public void setAreaSqKm(BigDecimal areaSqKm) {
        this.areaSqKm = areaSqKm;
    }

    public Long getPopulation2021Est() {
        return population2021Est;
    }

    public void setPopulation2021Est(Long population2021Est) {
        this.population2021Est = population2021Est;
    }

    public BigDecimal getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(BigDecimal centerLat) {
        this.centerLat = centerLat;
    }

    public BigDecimal getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(BigDecimal centerLng) {
        this.centerLng = centerLng;
    }
}
