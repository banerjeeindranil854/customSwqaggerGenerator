package com.mtn.aggregator.models.request.transfer;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerTransferRequest {

    @NotBlank
    private String receiverMsisdn;

    private String productCode;

    private Integer productId;

    @NotBlank
    private String type;

    private String agentId;

    private BigDecimal transferAmount;

    private String callbackUrl;

    private String pin;

    @NotNull
    private String targetSystem;
}
