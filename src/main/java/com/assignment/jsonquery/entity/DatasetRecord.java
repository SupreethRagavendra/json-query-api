package com.assignment.jsonquery.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dataset_records", uniqueConstraints = @UniqueConstraint(columnNames = { "datasetName", "recordId" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String datasetName;

    @Column(nullable = false)
    private Long recordId;

    @Column(columnDefinition = "CLOB", nullable = false)
    private String jsonData;
}
