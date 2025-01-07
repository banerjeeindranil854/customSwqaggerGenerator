package com.mtn.aggregator.models;


import com.mtn.aggregator.enums.SystemName;
import lombok.Data;

import java.io.Serializable;

@Data
public class SystemEndpoint implements Serializable {
    private SystemName systemId;
    private String consentEndpoint;
    private String description;
    private String smsShortCode;
    private String customerDataTransferEndPoint;
    private String customerAirtimeTransferEndPoint;
    private String customerSmeDataEndPoint;
    private int timeout;
    private String countryCode;
    private String country;
    private int msisdnLength;
    private String channelIds;
}
