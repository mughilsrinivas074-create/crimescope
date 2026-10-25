package com.crimescope;

import com.crimescope.web.dto.CrimeRecordDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Crime Record DTO Bean Validation Tests")
class CrimeRecordValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid DTO passes all validation rules")
    void testValidDtoPasses() {
        CrimeRecordDto dto = new CrimeRecordDto();
        dto.setDistrictId(1L);
        dto.setCategoryId(2L);
        dto.setReportingYear(2023);
        dto.setReportingMonth(5);
        dto.setCaseCount(100);
        dto.setArrestCount(80);
        dto.setConvictedCount(35);
        dto.setChargesheetedCount(85);

        Set<ConstraintViolation<CrimeRecordDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid DTO should have zero violations");
    }

    @Test
    @DisplayName("Missing mandatory fields trigger Bean Validation errors")
    void testMissingMandatoryFields() {
        CrimeRecordDto dto = new CrimeRecordDto(); // All fields null

        Set<ConstraintViolation<CrimeRecordDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("districtId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("categoryId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("reportingYear")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("caseCount")));
    }

    @Test
    @DisplayName("Invalid numerical ranges trigger Bean Validation errors")
    void testInvalidNumericRanges() {
        CrimeRecordDto dto = new CrimeRecordDto();
        dto.setDistrictId(1L);
        dto.setCategoryId(2L);
        dto.setReportingYear(1980); // < 2000
        dto.setReportingMonth(15);  // > 12
        dto.setCaseCount(-5);       // Negative count

        Set<ConstraintViolation<CrimeRecordDto>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("reportingYear")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("reportingMonth")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("caseCount")));
    }
}
