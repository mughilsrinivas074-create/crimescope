package com.crimescope.repository;

import com.crimescope.domain.model.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    Optional<District> findByCodeIgnoreCase(String code);
    Optional<District> findByNameIgnoreCase(String name);
    List<District> findByZoneIgnoreCase(String zone);
    boolean existsByCodeIgnoreCase(String code);
}
