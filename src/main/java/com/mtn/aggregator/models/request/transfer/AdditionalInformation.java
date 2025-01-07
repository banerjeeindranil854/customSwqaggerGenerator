package com.mtn.aggregator.models.request.transfer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalInformation {
    private String name;
    private String description;
}
