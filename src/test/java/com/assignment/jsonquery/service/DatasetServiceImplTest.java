package com.assignment.jsonquery.service;

import com.assignment.jsonquery.dto.InsertRecordResponse;
import com.assignment.jsonquery.entity.DatasetRecord;
import com.assignment.jsonquery.exception.*;
import com.assignment.jsonquery.repository.DatasetRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatasetServiceImplTest {

    @Mock
    private DatasetRecordRepository repository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private DatasetServiceImpl datasetService;

    private Map<String, Object> sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = new LinkedHashMap<>();
        sampleRecord.put("id", 1);
        sampleRecord.put("name", "John Doe");
        sampleRecord.put("age", 30);
        sampleRecord.put("department", "Engineering");
    }

    @Test
    void insertRecord_shouldReturnSuccessResponse() {
        when(repository.existsByDatasetNameAndRecordId("test", 1L)).thenReturn(false);
        when(repository.save(any(DatasetRecord.class))).thenReturn(DatasetRecord.builder().id(1L).build());

        InsertRecordResponse response = datasetService.insertRecord("test", sampleRecord);

        assertThat(response.getMessage()).isEqualTo("Record added successfully");
        assertThat(response.getDataset()).isEqualTo("test");
        assertThat(response.getRecordId()).isEqualTo(1L);
        verify(repository).save(any(DatasetRecord.class));
    }

    @Test
    void insertRecord_shouldThrowOnDuplicate() {
        when(repository.existsByDatasetNameAndRecordId("test", 1L)).thenReturn(true);

        assertThatThrownBy(() -> datasetService.insertRecord("test", sampleRecord))
                .isInstanceOf(DuplicateRecordException.class);
    }

    @Test
    void insertRecord_shouldThrowOnMissingId() {
        Map<String, Object> noIdRecord = Map.of("name", "Test");

        assertThatThrownBy(() -> datasetService.insertRecord("test", noIdRecord))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
    }

    @Test
    void insertRecord_shouldThrowOnEmptyRecord() {
        assertThatThrownBy(() -> datasetService.insertRecord("test", Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void insertRecord_shouldThrowOnBlankDatasetName() {
        assertThatThrownBy(() -> datasetService.insertRecord("  ", sampleRecord))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void groupBy_shouldGroupRecords() {
        List<DatasetRecord> entities = List.of(
                DatasetRecord.builder().id(1L).datasetName("test").recordId(1L)
                        .jsonData("{\"id\":1,\"name\":\"John\",\"department\":\"Engineering\"}").build(),
                DatasetRecord.builder().id(2L).datasetName("test").recordId(2L)
                        .jsonData("{\"id\":2,\"name\":\"Jane\",\"department\":\"Engineering\"}").build(),
                DatasetRecord.builder().id(3L).datasetName("test").recordId(3L)
                        .jsonData("{\"id\":3,\"name\":\"Alice\",\"department\":\"Marketing\"}").build());

        when(repository.findByDatasetName("test")).thenReturn(entities);

        Map<String, List<Map<String, Object>>> result = datasetService.groupBy("test", "department");

        assertThat(result).containsKeys("Engineering", "Marketing");
        assertThat(result.get("Engineering")).hasSize(2);
        assertThat(result.get("Marketing")).hasSize(1);
    }

    @Test
    void groupBy_shouldThrowOnEmptyDataset() {
        when(repository.findByDatasetName("empty")).thenReturn(List.of());

        assertThatThrownBy(() -> datasetService.groupBy("empty", "department"))
                .isInstanceOf(DatasetNotFoundException.class);
    }

    @Test
    void sortBy_shouldSortAscending() {
        List<DatasetRecord> entities = List.of(
                DatasetRecord.builder().id(1L).datasetName("test").recordId(1L)
                        .jsonData("{\"id\":1,\"age\":30}").build(),
                DatasetRecord.builder().id(2L).datasetName("test").recordId(2L)
                        .jsonData("{\"id\":2,\"age\":25}").build(),
                DatasetRecord.builder().id(3L).datasetName("test").recordId(3L)
                        .jsonData("{\"id\":3,\"age\":28}").build());

        when(repository.findByDatasetName("test")).thenReturn(entities);

        List<Map<String, Object>> result = datasetService.sortBy("test", "age", "asc");

        assertThat(result).hasSize(3);
        assertThat(((Number) result.get(0).get("age")).intValue()).isEqualTo(25);
        assertThat(((Number) result.get(1).get("age")).intValue()).isEqualTo(28);
        assertThat(((Number) result.get(2).get("age")).intValue()).isEqualTo(30);
    }

    @Test
    void sortBy_shouldSortDescending() {
        List<DatasetRecord> entities = List.of(
                DatasetRecord.builder().id(1L).datasetName("test").recordId(1L)
                        .jsonData("{\"id\":1,\"age\":30}").build(),
                DatasetRecord.builder().id(2L).datasetName("test").recordId(2L)
                        .jsonData("{\"id\":2,\"age\":25}").build());

        when(repository.findByDatasetName("test")).thenReturn(entities);

        List<Map<String, Object>> result = datasetService.sortBy("test", "age", "desc");

        assertThat(((Number) result.get(0).get("age")).intValue()).isEqualTo(30);
        assertThat(((Number) result.get(1).get("age")).intValue()).isEqualTo(25);
    }

    @Test
    void sortBy_shouldThrowOnInvalidOrder() {
        List<DatasetRecord> entities = List.of(
                DatasetRecord.builder().id(1L).datasetName("test").recordId(1L)
                        .jsonData("{\"id\":1,\"age\":30}").build());

        // The validation happens before fetching, so no stubbing needed for invalid
        // order
        assertThatThrownBy(() -> datasetService.sortBy("test", "age", "invalid"))
                .isInstanceOf(InvalidQueryParameterException.class);
    }

    @Test
    void sortBy_shouldDefaultToAscWhenOrderIsNull() {
        List<DatasetRecord> entities = List.of(
                DatasetRecord.builder().id(1L).datasetName("test").recordId(1L)
                        .jsonData("{\"id\":1,\"age\":30}").build(),
                DatasetRecord.builder().id(2L).datasetName("test").recordId(2L)
                        .jsonData("{\"id\":2,\"age\":25}").build());

        when(repository.findByDatasetName("test")).thenReturn(entities);

        List<Map<String, Object>> result = datasetService.sortBy("test", "age", null);

        assertThat(((Number) result.get(0).get("age")).intValue()).isEqualTo(25);
        assertThat(((Number) result.get(1).get("age")).intValue()).isEqualTo(30);
    }

    @Test
    void groupBy_shouldHandleNullFieldValues() {
        List<DatasetRecord> entities = List.of(
                DatasetRecord.builder().id(1L).datasetName("test").recordId(1L)
                        .jsonData("{\"id\":1,\"department\":\"Engineering\"}").build(),
                DatasetRecord.builder().id(2L).datasetName("test").recordId(2L)
                        .jsonData("{\"id\":2}").build());

        when(repository.findByDatasetName("test")).thenReturn(entities);

        Map<String, List<Map<String, Object>>> result = datasetService.groupBy("test", "department");

        assertThat(result).containsKeys("Engineering", "null");
    }
}
