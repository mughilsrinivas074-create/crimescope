package com.crimescope.service;

import com.crimescope.domain.model.CrimeCategory;
import com.crimescope.repository.CrimeCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CrimeCategoryService {

    private final CrimeCategoryRepository categoryRepository;

    public CrimeCategoryService(CrimeCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CrimeCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<CrimeCategory> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<CrimeCategory> getCategoryByCode(String code) {
        return categoryRepository.findByCodeIgnoreCase(code);
    }
}
