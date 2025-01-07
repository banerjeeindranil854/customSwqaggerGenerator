package com.mtn.aggregator.models.response.consent;


import com.mtn.aggregator.models.response.APIResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsentNotificationResponse  extends APIResponse {

    private String statusCode;
    private String msisdn;
    private String userResponse;
    private List<String> customData;
    private String partnerName;
    private String sequenceNo;
    private String transactionId;

}
