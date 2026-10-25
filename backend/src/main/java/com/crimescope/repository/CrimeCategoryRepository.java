package com.crimescope.repository;

import com.crimescope.domain.enums.SeverityLevel;
import com.crimescope.domain.model.CrimeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrimeCategoryRepository extends JpaRepository<CrimeCategory, Long> {
    Optional<CrimeCategory> findByCodeIgnoreCase(String code);
    Optional<CrimeCategory> findByNameIgnoreCase(String name);
    List<CrimeCategory> findBySeverityLevel(SeverityLevel severityLevel);
    boolean existsByCodeIgnoreCase(String code);
}
