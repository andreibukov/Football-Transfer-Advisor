package com.footballadvisor.dto;

import lombok.Data;

@Data
public class TransferNeedRequest {

    private String neededPosition;
    private String playingStyle;
    private String preferredRole;
    private Double maxBudget;
    private Integer maxAge;
}
