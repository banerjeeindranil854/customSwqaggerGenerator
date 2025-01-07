package com.mtn.aggregator.models.logging;

import com.mtn.aggregator.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLogItem {

    private String senderMsisdn;
    private String nodeId;
    private Object customerTransferRequest;
    private Object requestBody;
    private Object operation;
    private String countryCode;
    private String partnerName;
    private String network;
    private String sequenceNo;
    private String transactionId;
}