package com.mtn.aggregator.models.response;


import com.mtn.madapi.commons.models.error.APIError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTransferComvivaResponse extends APIResponse{
    private String statusCode;
    private APIError error;
    private String sequenceNo;
    private String transactionId;
    private CustomerTransferResponse data;
    private String statusMessage;
}
