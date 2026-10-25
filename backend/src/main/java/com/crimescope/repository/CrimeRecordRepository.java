package com.crimescope.repository;

import com.crimescope.domain.model.CrimeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrimeRecordRepository extends JpaRepository<CrimeRecord, Long>, JpaSpecificationExecutor<CrimeRecord> {

    Optional<CrimeRecord> findByDataHash(String dataHash);

    boolean existsByDataHash(String dataHash);

    List<CrimeRecord> findByDistrictId(Long districtId);

    List<CrimeRecord> findByReportingYear(Integer year);

    List<CrimeRecord> findByDistrictIdAndReportingYear(Long districtId, Integer year);

    List<CrimeRecord> findByIsDemonstration(Boolean isDemonstration);

    @Query("SELECT r FROM CrimeRecord r WHERE " +
           "(:districtId IS NULL OR r.district.id = :districtId) AND " +
           "(:categoryId IS NULL OR r.category.id = :categoryId) AND " +
           "(:startYear IS NULL OR r.reportingYear >= :startYear) AND " +
           "(:endYear IS NULL OR r.reportingYear <= :endYear) AND " +
           "(:includeDemo IS TRUE OR r.isDemonstration = FALSE)")
    List<CrimeRecord> filterRecords(
            @Param("districtId") Long districtId,
            @Param("categoryId") Long categoryId,
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear,
            @Param("includeDemo") Boolean includeDemo);

    @Query("SELECT r FROM CrimeRecord r WHERE " +
           "(:districtId IS NULL OR r.district.id = :districtId) AND " +
           "(:categoryId IS NULL OR r.category.id = :categoryId) AND " +
           "(:startYear IS NULL OR r.reportingYear >= :startYear) AND " +
           "(:endYear IS NULL OR r.reportingYear <= :endYear) AND " +
           "(:includeDemo IS TRUE OR r.isDemonstration = FALSE)")
    Page<CrimeRecord> filterRecordsPaged(
            @Param("districtId") Long districtId,
            @Param("categoryId") Long categoryId,
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear,
            @Param("includeDemo") Boolean includeDemo,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.caseCount), 0) FROM CrimeRecord r WHERE " +
           "(:includeDemo IS TRUE OR r.isDemonstration = FALSE)")
    long sumTotalCases(@Param("includeDemo") Boolean includeDemo);

    @Query("SELECT r.reportingYear as year, SUM(r.caseCount) as totalCases, " +
           "SUM(r.arrestCount) as totalArrests, SUM(r.convictedCount) as totalConvictions " +
           "FROM CrimeRecord r WHERE (:includeDemo IS TRUE OR r.isDemonstration = FALSE) " +
           "GROUP BY r.reportingYear ORDER BY r.reportingYear ASC")
    List<Object[]> findYearlyAggregates(@Param("includeDemo") Boolean includeDemo);

    @Query("SELECT r.district.name as districtName, SUM(r.caseCount) as totalCases " +
           "FROM CrimeRecord r WHERE (:includeDemo IS TRUE OR r.isDemonstration = FALSE) " +
           "GROUP BY r.district.name ORDER BY totalCases DESC")
    List<Object[]> findDistrictAggregates(@Param("includeDemo") Boolean includeDemo);

    @Query("SELECT r.category.name as categoryName, SUM(r.caseCount) as totalCases " +
           "FROM CrimeRecord r WHERE (:includeDemo IS TRUE OR r.isDemonstration = FALSE) " +
           "GROUP BY r.category.name ORDER BY totalCases DESC")
    List<Object[]> findCategoryAggregates(@Param("includeDemo") Boolean includeDemo);
}
