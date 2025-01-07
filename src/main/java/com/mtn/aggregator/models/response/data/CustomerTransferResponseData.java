package com.mtn.aggregator.models.response.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerTransferResponseData {

    private String description;
    private Long valueCharged;
    private String unit;
    private String productName;
    private String productId;
    private String productType;
    private String productCode;
    private String agentId;
    private String notification;
    private String localTransactionId;
    private String transactionState;

}
