package com.footballadvisor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommendation_reasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationReasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private RecommendationEntity recommendation;

    @Column(columnDefinition = "TEXT")
    private String reasonText;
}