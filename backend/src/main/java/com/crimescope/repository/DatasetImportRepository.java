package com.crimescope.repository;

import com.crimescope.domain.model.DatasetImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetImportRepository extends JpaRepository<DatasetImport, Long> {
    List<DatasetImport> findTop20ByOrderByCreatedAtDesc();
}
