package com.mtn.aggregator.enums;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum ConsentType {

    USSD_FLOW("ussd"),
    SMS_FLOW("sms");

    private String flag;

    ConsentType(String flag) {
        this.flag = flag;
    }

    public String getFlag() {
        return this.flag;
    }

    public static Optional<ConsentType> getFlowTypeOptional(String value) {
        for (ConsentType v : values())
            if (v.getFlag().equalsIgnoreCase(value)) return Optional.of(v);
        return Optional.empty();
    }
}
