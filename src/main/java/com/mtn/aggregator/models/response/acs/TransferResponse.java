package com.mtn.aggregator.models.response.acs;

import com.mtn.aggregator.models.response.APIResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse extends APIResponse {
    private String statusCode;
    private String message;
    private Boolean success;
    private String transactionId;
    private String sequenceNo;
    private Integer code;
    private TransferResponseData data;
}
