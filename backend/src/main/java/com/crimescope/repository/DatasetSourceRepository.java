package com.crimescope.repository;

import com.crimescope.domain.model.DatasetSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetSourceRepository extends JpaRepository<DatasetSource, Long> {
    List<DatasetSource> findByIsDemonstration(Boolean isDemonstration);
}
