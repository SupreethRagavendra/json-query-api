package com.assignment.jsonquery.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsertRecordResponse {
    private String message;
    private String dataset;
    private Long recordId;
}
