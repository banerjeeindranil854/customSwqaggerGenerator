package com.mtn.aggregator.models.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtn.aggregator.models.response.data.CustomerTransferResponseData;
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
public class CustomerTransferResponse extends APIResponse{
    private String statusCode;
    private String statusMessage;
    private String supportMessage;
    private String transactionId;
    private String sequenceNo;
    private CustomerTransferResponseData data;
}
