package com.mtn.aggregator.models.request.consent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {

    public String smsShortCode;
    public String flowType;
    public String confirmationMessage;
    public List<String> customData;
    public String callbackUrl;

}
