package com.mtn.aggregator.models.request.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferDsmRequestWrapper {
    private String senderMsisdn;
    private String endpoint;
    private String requestPath;
    private CustomerTransferRequest transferRequest;
    private String partnerName;
    private String sequenceNo;
    private String transactionId;
}
