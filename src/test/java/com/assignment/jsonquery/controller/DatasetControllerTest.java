package com.assignment.jsonquery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatasetControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private static final String BASE_URL = "/api/dataset";
        private static final String DATASET = "employee_dataset";

        @Test
        @Order(1)
        void shouldInsertFirstRecord() throws Exception {
                Map<String, Object> record = Map.of(
                                "id", 1,
                                "name", "John Doe",
                                "age", 30,
                                "department", "Engineering");

                mockMvc.perform(post(BASE_URL + "/" + DATASET + "/record")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(record)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("Record added successfully"))
                                .andExpect(jsonPath("$.dataset").value(DATASET))
                                .andExpect(jsonPath("$.recordId").value(1));
        }

        @Test
        @Order(2)
        void shouldInsertSecondRecord() throws Exception {
                Map<String, Object> record = Map.of(
                                "id", 2,
                                "name", "Jane Smith",
                                "age", 25,
                                "department", "Engineering");

                mockMvc.perform(post(BASE_URL + "/" + DATASET + "/record")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(record)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.recordId").value(2));
        }

        @Test
        @Order(3)
        void shouldInsertThirdRecord() throws Exception {
                Map<String, Object> record = Map.of(
                                "id", 3,
                                "name", "Alice Brown",
                                "age", 28,
                                "department", "Marketing");

                mockMvc.perform(post(BASE_URL + "/" + DATASET + "/record")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(record)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.recordId").value(3));
        }

        @Test
        @Order(4)
        void shouldRejectDuplicateRecord() throws Exception {
                Map<String, Object> record = Map.of(
                                "id", 1,
                                "name", "Duplicate",
                                "age", 99,
                                "department", "HR");

                mockMvc.perform(post(BASE_URL + "/" + DATASET + "/record")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(record)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("Conflict"));
        }

        @Test
        @Order(5)
        void shouldRejectRecordWithoutId() throws Exception {
                Map<String, Object> record = Map.of(
                                "name", "No Id",
                                "age", 20);

                mockMvc.perform(post(BASE_URL + "/" + DATASET + "/record")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(record)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @Order(6)
        void shouldGroupByDepartment() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + DATASET + "/query")
                                .param("groupBy", "department"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.groupedRecords.Engineering", hasSize(2)))
                                .andExpect(jsonPath("$.groupedRecords.Marketing", hasSize(1)));
        }

        @Test
        @Order(7)
        void shouldSortByAgeAscending() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + DATASET + "/query")
                                .param("sortBy", "age")
                                .param("order", "asc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.sortedRecords", hasSize(3)))
                                .andExpect(jsonPath("$.sortedRecords[0].age").value(25))
                                .andExpect(jsonPath("$.sortedRecords[1].age").value(28))
                                .andExpect(jsonPath("$.sortedRecords[2].age").value(30));
        }

        @Test
        @Order(8)
        void shouldSortByAgeDescending() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + DATASET + "/query")
                                .param("sortBy", "age")
                                .param("order", "desc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.sortedRecords[0].age").value(30))
                                .andExpect(jsonPath("$.sortedRecords[2].age").value(25));
        }

        @Test
        @Order(9)
        void shouldSortByNameAscending() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + DATASET + "/query")
                                .param("sortBy", "name")
                                .param("order", "asc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.sortedRecords[0].name").value("Alice Brown"))
                                .andExpect(jsonPath("$.sortedRecords[1].name").value("Jane Smith"))
                                .andExpect(jsonPath("$.sortedRecords[2].name").value("John Doe"));
        }

        @Test
        @Order(10)
        void shouldRejectQueryWithNoParameters() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + DATASET + "/query"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @Order(11)
        void shouldRejectQueryWithBothParameters() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + DATASET + "/query")
                                .param("groupBy", "department")
                                .param("sortBy", "age"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @Order(12)
        void shouldReturn404ForNonexistentDataset() throws Exception {
                mockMvc.perform(get(BASE_URL + "/nonexistent/query")
                                .param("groupBy", "department"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(13)
        void shouldRejectInvalidSortOrder() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + DATASET + "/query")
                                .param("sortBy", "age")
                                .param("order", "invalid"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @Order(14)
        void shouldReturn405MethodNotAllowed() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + DATASET + "/record"))
                                .andExpect(status().isMethodNotAllowed())
                                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
        }

        @Test
        @Order(15)
        void shouldReturn404ForRootUrl() throws Exception {
                mockMvc.perform(get("/"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Not Found"));
        }
}
