package com.mtn.aggregator.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationClaim {

    @JsonProperty("developer_email")
    private String developerEmail;

    @JsonProperty("app_custom_attributes")
    private CustomAttributeProvider customAttributeProvider;

}

