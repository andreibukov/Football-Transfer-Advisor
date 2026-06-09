package com.footballadvisor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ontology_relations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OntologyRelationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourceConcept;
    private String relationType;
    private String targetConcept;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}