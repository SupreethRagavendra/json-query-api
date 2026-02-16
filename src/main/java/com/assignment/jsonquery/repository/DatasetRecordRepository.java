package com.assignment.jsonquery.repository;

import com.assignment.jsonquery.entity.DatasetRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRecordRepository extends JpaRepository<DatasetRecord, Long> {

    List<DatasetRecord> findByDatasetName(String datasetName);

    Optional<DatasetRecord> findByDatasetNameAndRecordId(String datasetName, Long recordId);

    boolean existsByDatasetNameAndRecordId(String datasetName, Long recordId);
}
