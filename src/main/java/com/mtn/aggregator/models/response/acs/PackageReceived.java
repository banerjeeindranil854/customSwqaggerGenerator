package com.mtn.aggregator.models.response.acs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageReceived {

    private String productName;
    private Double cost;
    private Integer productId;
    private Integer categoryId;
    private String categoryName;
    private String description;
    private Double validityDays;
    private String type;
    private Double validityHours;
    private CostCurrency costCurrency;
}
