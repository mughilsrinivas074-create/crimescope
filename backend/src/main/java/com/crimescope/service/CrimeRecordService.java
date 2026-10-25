package com.crimescope.service;

import com.crimescope.domain.enums.VerificationStatus;
import com.crimescope.domain.model.CrimeCategory;
import com.crimescope.domain.model.CrimeRecord;
import com.crimescope.domain.model.District;
import com.crimescope.repository.CrimeCategoryRepository;
import com.crimescope.repository.CrimeRecordRepository;
import com.crimescope.repository.DistrictRepository;
import com.crimescope.service.file.CrimeCsvHandler;
import com.crimescope.web.dto.CrimeRecordDto;
import com.crimescope.web.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@Transactional
public class CrimeRecordService {

    private final CrimeRecordRepository crimeRecordRepository;
    private final DistrictRepository districtRepository;
    private final CrimeCategoryRepository categoryRepository;
    private final CrimeCsvHandler crimeCsvHandler;
    private final AuditLogService auditLogService;

    public CrimeRecordService(CrimeRecordRepository crimeRecordRepository,
                              DistrictRepository districtRepository,
                              CrimeCategoryRepository categoryRepository,
                              CrimeCsvHandler crimeCsvHandler,
                              AuditLogService auditLogService) {
        this.crimeRecordRepository = crimeRecordRepository;
        this.districtRepository = districtRepository;
        this.categoryRepository = categoryRepository;
        this.crimeCsvHandler = crimeCsvHandler;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<CrimeRecord> getFilteredRecordsPaged(Long districtId, Long categoryId,
                                                     Integer startYear, Integer endYear,
                                                     Boolean includeDemo, Pageable pageable) {
        return crimeRecordRepository.filterRecordsPaged(districtId, categoryId, startYear, endYear, includeDemo, pageable);
    }

    @Transactional(readOnly = true)
    public List<CrimeRecord> getFilteredRecords(Long districtId, Long categoryId,
                                                Integer startYear, Integer endYear,
                                                Boolean includeDemo) {
        return crimeRecordRepository.filterRecords(districtId, categoryId, startYear, endYear, includeDemo);
    }

    @Transactional(readOnly = true)
    public CrimeRecord getRecordById(Long id) {
        return crimeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrimeRecord with ID " + id + " not found."));
    }

    public CrimeRecord createRecord(CrimeRecordDto dto, String username, String ipAddress) {
        District district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District ID " + dto.getDistrictId() + " not found."));

        CrimeCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category ID " + dto.getCategoryId() + " not found."));

        CrimeRecord record = new CrimeRecord();
        record.setDistrict(district);
        record.setCategory(category);
        record.setReportingYear(dto.getReportingYear());
        record.setReportingMonth(dto.getReportingMonth());
        record.setCaseCount(dto.getCaseCount());
        record.setArrestCount(dto.getArrestCount());
        record.setConvictedCount(dto.getConvictedCount());
        record.setChargesheetedCount(dto.getChargesheetedCount());
        record.setIncidentDate(dto.getIncidentDate());
        record.setLatitude(dto.getLatitude());
        record.setLongitude(dto.getLongitude());
        record.setLocationDescription(dto.getLocationDescription());
        record.setReportingUnit(dto.getReportingUnit() != null ? dto.getReportingUnit() : district.getName() + " Police Unit");
        record.setSourceReference(dto.getSourceReference() != null ? dto.getSourceReference() : "MANUAL-ENTRY-" + System.currentTimeMillis());
        record.setIsDemonstration(Boolean.TRUE.equals(dto.getIsDemonstration()));
        record.setVerificationStatus(Boolean.TRUE.equals(dto.getIsDemonstration()) ? VerificationStatus.DEMONSTRATION : VerificationStatus.VERIFIED);

        String composite = String.format("%s-%s-%d-%s-%d-%d",
                district.getCode(), category.getCode(), dto.getReportingYear(),
                dto.getReportingMonth() != null ? dto.getReportingMonth().toString() : "A",
                dto.getCaseCount(), System.currentTimeMillis());
        record.setDataHash(computeHash(composite));

        CrimeRecord saved = crimeRecordRepository.save(record);

        auditLogService.recordAction(null, username, "CREATE_CRIME_RECORD", "CrimeRecord",
                saved.getId(), "Created record in " + district.getName() + " for category " + category.getName(), ipAddress);

        return saved;
    }

    public CrimeRecord updateRecord(Long id, CrimeRecordDto dto, String username, String ipAddress) {
        CrimeRecord record = getRecordById(id);

        if (dto.getDistrictId() != null) {
            District district = districtRepository.findById(dto.getDistrictId())
                    .orElseThrow(() -> new ResourceNotFoundException("District ID " + dto.getDistrictId() + " not found."));
            record.setDistrict(district);
        }

        if (dto.getCategoryId() != null) {
            CrimeCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category ID " + dto.getCategoryId() + " not found."));
            record.setCategory(category);
        }

        if (dto.getReportingYear() != null) record.setReportingYear(dto.getReportingYear());
        record.setReportingMonth(dto.getReportingMonth());
        if (dto.getCaseCount() != null) record.setCaseCount(dto.getCaseCount());
        record.setArrestCount(dto.getArrestCount());
        record.setConvictedCount(dto.getConvictedCount());
        record.setChargesheetedCount(dto.getChargesheetedCount());
        record.setIncidentDate(dto.getIncidentDate());
        record.setLatitude(dto.getLatitude());
        record.setLongitude(dto.getLongitude());
        if (dto.getLocationDescription() != null) record.setLocationDescription(dto.getLocationDescription());
        if (dto.getReportingUnit() != null) record.setReportingUnit(dto.getReportingUnit());

        CrimeRecord updated = crimeRecordRepository.save(record);

        auditLogService.recordAction(null, username, "UPDATE_CRIME_RECORD", "CrimeRecord",
                id, "Updated fields for record " + id, ipAddress);

        return updated;
    }

    public void deleteRecord(Long id, String username, String ipAddress) {
        CrimeRecord record = getRecordById(id);
        crimeRecordRepository.delete(record);

        auditLogService.recordAction(null, username, "DELETE_CRIME_RECORD", "CrimeRecord",
                id, "Deleted crime record " + id, ipAddress);
    }

    @Transactional(readOnly = true)
    public byte[] exportFilteredRecordsToCsv(Long districtId, Long categoryId,
                                             Integer startYear, Integer endYear,
                                             Boolean includeDemo) throws IOException {
        List<CrimeRecord> records = crimeRecordRepository.filterRecords(districtId, categoryId, startYear, endYear, includeDemo);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        crimeCsvHandler.exportToCsv(records, out);
        return out.toByteArray();
    }

    private String computeHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }
}
