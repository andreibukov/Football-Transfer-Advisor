package com.footballadvisor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private String currentClub;
    private String league;
    private String position;

    private BigDecimal marketValue;

    private Integer speed;
    private Integer acceleration;
    private Integer passing;
    private Integer vision;
    private Integer ballControl;
    private Integer finishing;
    private Integer dribbling;
    private Integer stamina;
    private Integer strength;
    private Integer tackling;
    private Integer positioning;
    private Integer workRate;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}