package com.mtn.aggregator.models.request.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequest {
    private String receiverMsisdn;
    private String productCode;
    private String nodeId;
    private String pinCode;
    private BigDecimal transferAmount;
    private String callBack;
    private String channelId;
    private String network;
    private String pin;
    private String agentId;
    private String targetSystem;
    private String productName;
    private String productId;
    private ValidFor validFor;
    private boolean isAsync;
    private List<AdditionalInformation> additionalInformation;
    private String partnerName;
    private String sequenceNo;
    private String transactionId;
    private String type;
}
