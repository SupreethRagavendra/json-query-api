package com.assignment.jsonquery.controller;

import com.assignment.jsonquery.dto.GroupByResponse;
import com.assignment.jsonquery.dto.InsertRecordResponse;
import com.assignment.jsonquery.dto.SortByResponse;
import com.assignment.jsonquery.exception.InvalidQueryParameterException;
import com.assignment.jsonquery.service.DatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dataset")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;

    // Insert a single record into the dataset
    @PostMapping("/{datasetName}/record")
    public ResponseEntity<InsertRecordResponse> insertRecord(
            @PathVariable String datasetName,
            @RequestBody Map<String, Object> record) {

        InsertRecordResponse response = datasetService.insertRecord(datasetName, record);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Batch insert multiple records
    @PostMapping("/{datasetName}/batch")
    public ResponseEntity<InsertRecordResponse> batchInsert(
            @PathVariable String datasetName,
            @RequestBody List<Map<String, Object>> records) {

        InsertRecordResponse response = datasetService.batchInsert(datasetName, records);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Query dataset with optional grouping or sorting
    @GetMapping("/{datasetName}/query")
    public ResponseEntity<?> queryDataset(
            @PathVariable String datasetName,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order) {

        if (groupBy != null && sortBy != null) {
            throw new InvalidQueryParameterException(
                    "Cannot use both 'groupBy' and 'sortBy' in the same query. Please use one at a time.");
        }

        if (groupBy == null && sortBy == null) {
            throw new InvalidQueryParameterException(
                    "At least one query parameter ('groupBy' or 'sortBy') must be provided.");
        }

        if (groupBy != null) {
            Map<String, List<Map<String, Object>>> grouped = datasetService.groupBy(datasetName, groupBy);
            GroupByResponse response = GroupByResponse.builder()
                    .groupedRecords(grouped)
                    .build();
            return ResponseEntity.ok(response);
        }

        // sortBy is not null
        List<Map<String, Object>> sorted = datasetService.sortBy(datasetName, sortBy, order);
        SortByResponse response = SortByResponse.builder()
                .sortedRecords(sorted)
                .build();
        return ResponseEntity.ok(response);
    }
}
