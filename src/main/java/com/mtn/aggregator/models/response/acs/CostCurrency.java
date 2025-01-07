package com.mtn.aggregator.models.response.acs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCurrency {
    @JsonProperty("USD")
    private String usd;
    @JsonProperty("LRD")
    private String lrd;



}
