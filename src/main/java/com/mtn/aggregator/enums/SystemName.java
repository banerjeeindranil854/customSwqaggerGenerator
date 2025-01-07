package com.mtn.aggregator.enums;

import lombok.Getter;

@Getter
public enum SystemName {

    CIS("CIS","CIS SYSTEM Services"),
    AIR("AIR","Air Server"),
    PRYMO("PRYMO","PRYMO Service"),
    COMVIVA("COMVIVA","COMVIVA Service"),
    ACS("ACS","ACS SYSTEM Service");

    private final String name;
    private final String description;

    SystemName(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}