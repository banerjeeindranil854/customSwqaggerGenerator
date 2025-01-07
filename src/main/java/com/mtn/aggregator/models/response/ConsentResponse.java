package com.mtn.aggregator.models.response;

import com.mtn.aggregator.enums.response.CanonicalErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConsentResponse extends APIResponse {

    private String statusCode;
    private boolean sent;

    public CustomerTransferResponse toCustomerTransferResponse(String transactionId) {

        var transferResponse = CustomerTransferResponse.builder().build();
        transferResponse.setTransactionId(transactionId);

        transferResponse.setStatusCode(statusCode);
        CanonicalErrorCode canonicalErrorCode = CanonicalErrorCode.getCanonicalErrorCode(statusCode);

        if (canonicalErrorCode == null) {
            canonicalErrorCode = CanonicalErrorCode.INTERNAL_SERVER_ERROR;
        } else if (statusCode.equals(CanonicalErrorCode.OK.getCanonicalCode())) {
            canonicalErrorCode = CanonicalErrorCode.CONSENT_VALIDATION_DELIVERED;
        }

        transferResponse.setStatusMessage(canonicalErrorCode.getMessage());

        return transferResponse;
    }
}
