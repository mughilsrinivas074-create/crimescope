package com.crimescope.service;

import com.crimescope.domain.model.District;
import com.crimescope.repository.DistrictRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DistrictService {

    private final DistrictRepository districtRepository;

    public DistrictService(DistrictRepository districtRepository) {
        this.districtRepository = districtRepository;
    }

    public List<District> getAllDistricts() {
        return districtRepository.findAll();
    }

    public Optional<District> getDistrictById(Long id) {
        return districtRepository.findById(id);
    }

    public Optional<District> getDistrictByCode(String code) {
        return districtRepository.findByCodeIgnoreCase(code);
    }

    public List<District> getDistrictsByZone(String zone) {
        return districtRepository.findByZoneIgnoreCase(zone);
    }
}
