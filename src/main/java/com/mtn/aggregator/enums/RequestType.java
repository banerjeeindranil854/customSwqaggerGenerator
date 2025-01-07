package com.mtn.aggregator.enums;

import lombok.Getter;

@Getter
public enum RequestType {

	TRANSFER_DATA("Transfer Data"),
    TRANSFER_AIRTIME("Transfer Airtime"),
    SME_DATA("SME Data"),
    AIRTIME_TRANSFER_CONSENT_RESPONSE_NOTIFICATION("Airtime Transfer Consent Response Notification"),
    UNMAPPED_REQUEST_TYPE("Unmapped request type"),
    PRIMO_CALLBACK("PRIMO_CALLBACK");

    RequestType(String value) {
        this.value = value;
    }

    private final String value;

    public static RequestType getEnum(String value) {
        for (RequestType v : values())
            if (v.getValue().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException();
    }

}