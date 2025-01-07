package com.mtn.aggregator.models.response.consent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mtn.aggregator.models.response.APIResponse;
import com.mtn.aggregator.models.response.HateoasContainer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentResponseData extends APIResponse {

    private String statusCode;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String statusMessage;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String transactionId;

    @JsonProperty("_links")
    private HateoasContainer _links;
}
