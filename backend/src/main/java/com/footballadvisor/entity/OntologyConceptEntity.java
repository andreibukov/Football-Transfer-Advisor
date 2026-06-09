package com.footballadvisor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ontology_concepts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OntologyConceptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conceptName;
    private String conceptType;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String source;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.source == null) {
            this.source = "USER_DEFINED";
        }
    }
}