package com.mtn.aggregator.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomAttributeProvider {

    @JsonIgnore
    @JsonProperty("DisplayName")
    private String displayName;

    @JsonIgnore
    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("channelId")
    private String channelId;
}
