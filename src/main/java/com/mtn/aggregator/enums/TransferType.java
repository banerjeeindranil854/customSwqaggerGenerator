package com.mtn.aggregator.enums;

import lombok.Getter;

public enum  TransferType {

    DATA("Data"),
    AIRTIME("Airtime"),
    SME_DATA("SME_DATA");


    @Getter
    private final String value;

    TransferType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static TransferType getEnum(String value) {
        for (TransferType v : values())
            if (v.getValue().equalsIgnoreCase(value)) return v;
        return null;
    }

}
