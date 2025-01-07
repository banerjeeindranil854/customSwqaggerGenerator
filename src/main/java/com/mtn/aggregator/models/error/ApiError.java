package com.mtn.aggregator.models.error;

import com.mtn.aggregator.models.response.APIResponse;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class ApiError extends APIResponse  implements Serializable {
    private static final long serialVersionUID = 529694948912412962L;

    private String statusCode;
    private String statusMessage;
    private String supportMessage;
    private String transactionId;
    private String sequenceNo;
    private String timestamp;
    private String path;
}
