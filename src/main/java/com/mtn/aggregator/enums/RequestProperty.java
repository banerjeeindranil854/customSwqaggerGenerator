package com.mtn.aggregator.enums;

import lombok.Getter;

public enum RequestProperty {

    NODE_ID("nodeId");


    @Getter
    private String value;

    RequestProperty(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}