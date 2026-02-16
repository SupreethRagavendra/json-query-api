package com.assignment.jsonquery.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortByResponse {
    private List<Map<String, Object>> sortedRecords;
}
