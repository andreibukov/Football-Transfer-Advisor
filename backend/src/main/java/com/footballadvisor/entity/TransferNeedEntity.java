package com.footballadvisor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_needs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferNeedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private ClubEntity club;

    private String neededPosition;
    private String playingStyle;
    private String preferredRole;

    private BigDecimal maxBudget;
    private Integer maxAge;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
