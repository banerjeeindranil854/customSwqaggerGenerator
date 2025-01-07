package com.mtn.aggregator.callback.controller;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimoCallbackRequest {

    @JsonProperty("status")
    private String status;
    @JsonProperty("message")
    private String message;
    @JsonProperty("status-code")
    private String statusCode;
    @JsonProperty("trxn")
    private String transactionId;
    @JsonProperty("local-trxn-code")
    private String localTransactionCode;
    @JsonProperty("transaction-state")
    private String transactionState;
}
