package com.mtn.aggregator.models.response.acs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponseData {

    private String sender;
    private String receiver;
    private Double amount;
    private PackageTransferred packageTransferred;
    private PackageReceived packageReceived;
    private Date transferExpiry;
}
