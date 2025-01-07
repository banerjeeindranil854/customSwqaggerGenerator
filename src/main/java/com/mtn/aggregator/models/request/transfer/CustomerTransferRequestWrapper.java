package com.mtn.aggregator.models.request.transfer;


import com.mtn.aggregator.models.SystemEndpoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.Link;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTransferRequestWrapper {

    private Link link;
    private String channelId;
    private String partnerName;
    private String requestPath;
    private String senderMsisdn;
    private String transactionId;
    private SystemEndpoint systemEndpoint;
    private CustomerTransferRequest customerTransferRequest;
    private String sequenceNo;

}
