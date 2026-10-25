package com.crimescope;

import com.crimescope.domain.enums.SeverityLevel;
import com.crimescope.domain.model.CrimeCategory;
import com.crimescope.domain.model.CrimeRecord;
import com.crimescope.domain.model.District;
import com.crimescope.repository.CrimeCategoryRepository;
import com.crimescope.repository.CrimeRecordRepository;
import com.crimescope.repository.DistrictRepository;
import com.crimescope.service.file.CrimeCsvHandler;
import com.crimescope.service.file.CsvValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Crime CSV Handler RFC 4180 and File Handling Tests")
class CrimeCsvHandlerTest {

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private CrimeCategoryRepository categoryRepository;

    @Mock
    private CrimeRecordRepository crimeRecordRepository;

    private CrimeCsvHandler csvHandler;
    private District chennai;
    private CrimeCategory cyberCrime;

    @BeforeEach
    void setUp() {
        chennai = new District("CHN", "Chennai", "Chennai", "North",
                BigDecimal.valueOf(426.0), 8696010L, BigDecimal.valueOf(13.0827), BigDecimal.valueOf(80.2707));
        chennai.setId(1L);

        cyberCrime = new CrimeCategory("CYBER_CRIME", "Cyber & Financial Tech Crimes",
                "IT Act offences", SeverityLevel.HIGH, false);
        cyberCrime.setId(4L);

        when(districtRepository.findAll()).thenReturn(List.of(chennai));
        when(categoryRepository.findAll()).thenReturn(List.of(cyberCrime));

        csvHandler = new CrimeCsvHandler(districtRepository, categoryRepository, crimeRecordRepository);
    }

    @Test
    @DisplayName("Parse valid CSV with quoted fields and escaped commas")
    void testParseValidCsvWithQuotes() throws IOException {
        String csvData = """
                district_code,category_code,reporting_year,reporting_month,case_count,arrest_count,convicted_count,chargesheeted_count,incident_date,latitude,longitude,location_description,reporting_unit,source_reference,is_demonstration
                CHN,CYBER_CRIME,2023,6,150,110,45,130,2023-06-15,13.0827,80.2707,"Cyber Cell, Central Headquarters, Vepery","Chennai Police","SCRB-2023-01",false
                """;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        CsvValidationResult result = csvHandler.parseCsv(inputStream, null);

        assertNotNull(result);
        assertEquals(1, result.getValidRecords().size());
        assertEquals(0, result.getMalformedRows().size());

        CrimeRecord record = result.getValidRecords().getFirst();
        assertEquals("CHN", record.getDistrict().getCode());
        assertEquals("CYBER_CRIME", record.getCategory().getCode());
        assertEquals(2023, record.getReportingYear());
        assertEquals(150, record.getCaseCount());
        assertEquals("Cyber Cell, Central Headquarters, Vepery", record.getLocationDescription());
        assertFalse(record.getIsDemonstration());
    }

    @Test
    @DisplayName("Malformed row detection logs line number, field and failure reason")
    void testMalformedRowDetection() throws IOException {
        String csvWithErrors = """
                district_code,category_code,reporting_year,case_count
                UNKNOWN_DIST,CYBER_CRIME,2023,100
                CHN,INVALID_CAT,2023,100
                CHN,CYBER_CRIME,1985,100
                CHN,CYBER_CRIME,2023,-50
                CHN,CYBER_CRIME,2023,NaN
                """;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvWithErrors.getBytes(StandardCharsets.UTF_8));
        CsvValidationResult result = csvHandler.parseCsv(inputStream, null);

        assertNotNull(result);
        assertEquals(0, result.getValidRecords().size());
        assertEquals(5, result.getMalformedRows().size());

        assertEquals("district_code", result.getMalformedRows().get(0).getErrorField());
        assertEquals("category_code", result.getMalformedRows().get(1).getErrorField());
        assertEquals("reporting_year", result.getMalformedRows().get(2).getErrorField());
        assertEquals("case_count", result.getMalformedRows().get(3).getErrorField());
        assertEquals("case_count", result.getMalformedRows().get(4).getErrorField());
    }

    @Test
    @DisplayName("Duplicate record detection skips duplicate hashes")
    void testDuplicateRecordSkipping() throws IOException {
        String duplicateCsv = """
                district_code,category_code,reporting_year,reporting_month,case_count
                CHN,CYBER_CRIME,2023,6,150
                CHN,CYBER_CRIME,2023,6,150
                """;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(duplicateCsv.getBytes(StandardCharsets.UTF_8));
        CsvValidationResult result = csvHandler.parseCsv(inputStream, null);

        assertEquals(1, result.getValidRecords().size());
        assertEquals(1, result.getDuplicateHashesSkipped().size());
    }

    @Test
    @DisplayName("Export to CSV generates proper RFC 4180 quoted fields")
    void testExportToCsv() throws IOException {
        CrimeRecord record = new CrimeRecord();
        record.setId(1L);
        record.setDistrict(chennai);
        record.setCategory(cyberCrime);
        record.setReportingYear(2023);
        record.setCaseCount(100);
        record.setLocationDescription("Station A, Sub-division B");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        csvHandler.exportToCsv(List.of(record), out);

        String exported = out.toString(StandardCharsets.UTF_8);
        assertTrue(exported.contains("id,district_code,district_name"));
        assertTrue(exported.contains("\"Station A, Sub-division B\""));
    }
}
