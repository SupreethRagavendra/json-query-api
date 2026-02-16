package com.assignment.jsonquery.service;

import com.assignment.jsonquery.dto.InsertRecordResponse;
import com.assignment.jsonquery.entity.DatasetRecord;
import com.assignment.jsonquery.exception.*;
import com.assignment.jsonquery.repository.DatasetRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetServiceImpl implements DatasetService {

    private final DatasetRecordRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public InsertRecordResponse insertRecord(String datasetName, Map<String, Object> record) {
        validateDatasetName(datasetName);

        if (record == null || record.isEmpty()) {
            throw new IllegalArgumentException("Record body cannot be null or empty");
        }

        if (!record.containsKey("id")) {
            throw new IllegalArgumentException("Record must contain an 'id' field");
        }

        Long recordId = extractRecordId(record);

        if (repository.existsByDatasetNameAndRecordId(datasetName, recordId)) {
            throw new DuplicateRecordException(
                    String.format("Record with id %d already exists in dataset '%s'", recordId, datasetName));
        }

        String jsonData = serializeRecord(record);

        DatasetRecord entity = DatasetRecord.builder()
                .datasetName(datasetName)
                .recordId(recordId)
                .jsonData(jsonData)
                .build();

        repository.save(entity);

        log.info("Record with id {} inserted into dataset '{}'", recordId, datasetName);

        return InsertRecordResponse.builder()
                .message("Record added successfully")
                .dataset(datasetName)
                .recordId(recordId)
                .build();
    }

    @Override
    @Transactional
    public InsertRecordResponse batchInsert(String datasetName, List<Map<String, Object>> records) {
        validateDatasetName(datasetName);

        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("Record list cannot be null or empty");
        }

        List<DatasetRecord> entities = new ArrayList<>();
        List<Long> insertedIds = new ArrayList<>();

        for (Map<String, Object> record : records) {
            if (record == null || record.isEmpty())
                continue;

            if (!record.containsKey("id")) {
                throw new IllegalArgumentException("All records must contain an 'id' field");
            }

            Long recordId = extractRecordId(record);

            if (repository.existsByDatasetNameAndRecordId(datasetName, recordId)) {
                log.warn("Record with id {} already exists in dataset '{}', skipping...", recordId, datasetName);
                continue; // Skip duplicates in batch mode
            }

            String jsonData = serializeRecord(record);

            DatasetRecord entity = DatasetRecord.builder()
                    .datasetName(datasetName)
                    .recordId(recordId)
                    .jsonData(jsonData)
                    .build();

            entities.add(entity);
            insertedIds.add(recordId);
        }

        if (!entities.isEmpty()) {
            repository.saveAll(entities);
            log.info("{} records inserted into dataset '{}'", entities.size(), datasetName);
        }

        return InsertRecordResponse.builder()
                .message(entities.size() + " records added successfully")
                .dataset(datasetName)
                .recordId(insertedIds.isEmpty() ? null : insertedIds.get(0)) // Return first ID or null
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Map<String, Object>>> groupBy(String datasetName, String groupByField) {
        validateDatasetName(datasetName);
        validateFieldName(groupByField, "groupBy");

        List<Map<String, Object>> records = fetchAllRecords(datasetName);

        return records.stream()
                .collect(Collectors.groupingBy(
                        record -> {
                            Object value = record.get(groupByField);
                            if (value == null) {
                                return "null";
                            }
                            return String.valueOf(value);
                        },
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> sortBy(String datasetName, String sortByField, String order) {
        validateDatasetName(datasetName);
        validateFieldName(sortByField, "sortBy");

        String sortOrder = (order != null) ? order.toLowerCase() : "asc";
        if (!sortOrder.equals("asc") && !sortOrder.equals("desc")) {
            throw new InvalidQueryParameterException("Order must be 'asc' or 'desc', got: " + order);
        }

        List<Map<String, Object>> records = fetchAllRecords(datasetName);

        Comparator<Map<String, Object>> comparator = (r1, r2) -> {
            Object v1 = r1.get(sortByField);
            Object v2 = r2.get(sortByField);
            return compareValues(v1, v2);
        };

        if (sortOrder.equals("desc")) {
            comparator = comparator.reversed();
        }

        records.sort(comparator);
        return records;
    }

    private void validateDatasetName(String datasetName) {
        if (datasetName == null || datasetName.isBlank()) {
            throw new IllegalArgumentException("Dataset name cannot be null or blank");
        }
    }

    private void validateFieldName(String fieldName, String paramName) {
        if (fieldName == null || fieldName.isBlank()) {
            throw new InvalidQueryParameterException(paramName + " parameter cannot be null or blank");
        }
    }

    private Long extractRecordId(Map<String, Object> record) {
        Object idObj = record.get("id");
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(idObj));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'id' field must be a valid number, got: " + idObj);
        }
    }

    private String serializeRecord(Map<String, Object> record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize record to JSON: " + e.getMessage());
        }
    }

    private Map<String, Object> deserializeRecord(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON record: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> fetchAllRecords(String datasetName) {
        List<DatasetRecord> entities = repository.findByDatasetName(datasetName);

        if (entities.isEmpty()) {
            throw new DatasetNotFoundException(
                    String.format("No records found for dataset '%s'", datasetName));
        }

        return entities.stream()
                .map(entity -> deserializeRecord(entity.getJsonData()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private int compareValues(Object v1, Object v2) {
        if (v1 == null && v2 == null)
            return 0;
        if (v1 == null)
            return -1;
        if (v2 == null)
            return 1;

        // Both are numbers
        if (v1 instanceof Number && v2 instanceof Number) {
            return Double.compare(((Number) v1).doubleValue(), ((Number) v2).doubleValue());
        }

        // Both are Comparable of the same type
        if (v1 instanceof Comparable && v2 instanceof Comparable
                && v1.getClass().equals(v2.getClass())) {
            return ((Comparable) v1).compareTo(v2);
        }

        // Fallback: compare as strings
        return String.valueOf(v1).compareTo(String.valueOf(v2));
    }
}
