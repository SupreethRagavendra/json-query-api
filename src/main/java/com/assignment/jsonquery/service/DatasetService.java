package com.assignment.jsonquery.service;

import com.assignment.jsonquery.dto.InsertRecordResponse;

import java.util.List;
import java.util.Map;

public interface DatasetService {

    InsertRecordResponse insertRecord(String datasetName, Map<String, Object> record);

    Map<String, List<Map<String, Object>>> groupBy(String datasetName, String groupByField);

    List<Map<String, Object>> sortBy(String datasetName, String sortByField, String order);

    InsertRecordResponse batchInsert(String datasetName, List<Map<String, Object>> records);
}
