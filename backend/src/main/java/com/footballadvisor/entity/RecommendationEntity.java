package com.footballadvisor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TransferNeedEntity transferNeed;

    @ManyToOne
    private PlayerEntity player;

    private Double positionMatch;
    private Double styleMatch;
    private Double roleMatch;
    private Double budgetMatch;
    private Double ageMatch;
    private Double totalScore;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
