package com.mtn.aggregator.helpers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtn.aggregator.models.AuthorizationClaim;
import com.mtn.aggregator.models.request.transfer.TransferDsmRequestWrapper;
import com.mtn.aggregator.models.request.transfer.TransferRequest;
import com.mtn.madapi.commons.models.error.APIError;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.mtn.aggregator.constants.TransferConstant.*;

@Service
@Slf4j
public class AggregatorHelper {

    public String extractChannelId(String xAuthClaims) {

        String channelId = "";

        if (StringUtils.isEmpty(xAuthClaims)) {
            return channelId;
        }

        Base64.Decoder decoder = Base64.getDecoder();

        try {
            log.info("Start decode x-authorization-claims");

            byte[] decodedBytes = decoder.decode(xAuthClaims);

            log.info("Decoded x-authorization-claims");

            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

            ObjectMapper mapper =
                    new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            AuthorizationClaim authorizationClaim = mapper.readValue(decodedString, AuthorizationClaim.class);

            log.info("authorizationClaim {}", authorizationClaim.toString());

            log.info("CustomAttribute {}", authorizationClaim.getCustomAttributeProvider().toString());

            channelId = authorizationClaim.getCustomAttributeProvider().getChannelId();

            return channelId;

        } catch (IOException | NullPointerException | IllegalArgumentException e) {

            log.info(e.getMessage());
            return channelId;
        }
    }

    public static Map<String, String> getHeaders(TransferDsmRequestWrapper requestWrapper){
        Map<String,String> headers = new HashMap<>();
        headers.put(PARTNER_NAME_HEADER_LABEL,requestWrapper.getPartnerName());
        headers.put(SEQUENCE_NO_HEADER_LABEL,requestWrapper.getSequenceNo());
        headers.put(TRANSACTION_ID_HEADER_LABEL,requestWrapper.getTransactionId());
        return headers;
    }

    public static Map<String, String> getHeaders(String partnerName, String sequenceNo, String transactionId){
        Map<String,String> headers = new HashMap<>();
        headers.put(PARTNER_NAME_HEADER_LABEL,partnerName);
        headers.put(SEQUENCE_NO_HEADER_LABEL,sequenceNo);
        headers.put(TRANSACTION_ID_HEADER_LABEL,transactionId);
        return headers;
    }


    public static Map<String, String> getHeaders(TransferRequest requestWrapper){
        Map<String,String> headers = new HashMap<>();
        headers.put(PARTNER_NAME_HEADER_LABEL,requestWrapper.getPartnerName());
        headers.put(SEQUENCE_NO_HEADER_LABEL,requestWrapper.getSequenceNo());
        headers.put(TRANSACTION_ID_HEADER_LABEL,requestWrapper.getTransactionId());
        return headers;
    }

    public static APIError buiApiError(TransferDsmRequestWrapper wrapper){
        APIError apiError = new APIError();
        apiError.setSequenceNo(wrapper.getSequenceNo());
        apiError.setTransactionId(wrapper.getTransactionId());
        apiError.setPath(wrapper.getRequestPath());
        return apiError;
    }
    
    public  static TransferRequest toDmsTransferRequest(TransferDsmRequestWrapper request){
        return TransferRequest.builder()
                .agentId(request.getTransferRequest().getAgentId())
                .productCode(request.getTransferRequest().getProductCode())
                .receiverMsisdn(request.getTransferRequest().getReceiverMsisdn())
                .transferAmount(request.getTransferRequest().getTransferAmount())
                .pin(request.getTransferRequest().getPin())
                .targetSystem(request.getTransferRequest().getTargetSystem())
                .build();
    }
}
