package com.mtn.aggregator.services;


import brave.baggage.BaggageField;
import com.mtn.aggregator.configuration.TransferAggregatorProperties;
import com.mtn.aggregator.enums.ConsentType;
import com.mtn.aggregator.enums.TransferType;
import com.mtn.aggregator.enums.response.CanonicalErrorCode;
import com.mtn.aggregator.exception.CanonicalErrorCodeException;
import com.mtn.aggregator.helpers.AggregatorHelper;
import com.mtn.aggregator.models.SystemEndpoint;
import com.mtn.aggregator.models.request.consent.ConsentRequest;
import com.mtn.aggregator.models.request.transfer.CustomerTransferRequest;
import com.mtn.aggregator.models.request.transfer.CustomerTransferRequestWrapper;
import com.mtn.aggregator.models.request.transfer.TransferDsmRequestWrapper;
import com.mtn.aggregator.models.request.transfer.TransferRequest;
import com.mtn.aggregator.models.response.CustomerTransferComvivaResponse;
import com.mtn.aggregator.models.response.CustomerTransferResponse;
import com.mtn.aggregator.models.response.acs.TransferResponse;
import com.mtn.aggregator.models.response.consent.ConsentNotificationResponse;
import com.mtn.aggregator.models.response.consent.ConsentResponseData;
import com.mtn.madapi.commons.models.error.APIError;
import com.mtn.madapi.commons.webclients.DefaultWebClientHttpService;
import javax.net.ssl.SSLException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.mtn.aggregator.constants.TransferConstant.*;
import static com.mtn.aggregator.enums.response.CanonicalErrorCode.*;
import static com.mtn.aggregator.helpers.MsisdnHelper.getSystemEndpoint;

@Slf4j
@Service
public class CustomerTransferService {

    private final TransferAggregatorProperties properties;
    private final WebClientHttpService webClientHttpService;
    private final DefaultWebClientHttpService defaultWebClientHttpService;
    private final BaggageField transactionIdTraceField;
    private final BaggageField sequenceNoTraceField;

    public CustomerTransferService(TransferAggregatorProperties properties, WebClientHttpService webClientHttpService,
                                   DefaultWebClientHttpService defaultWebClientHttpService,BaggageField transactionIdTraceField, BaggageField sequenceNoTraceField) {
        this.properties = properties;
        this.webClientHttpService = webClientHttpService;
        this.defaultWebClientHttpService = defaultWebClientHttpService;
        this.transactionIdTraceField = transactionIdTraceField;
        this.sequenceNoTraceField = sequenceNoTraceField;
    }


    public Mono<CustomerTransferResponse> transfer(String endpoint,String requestPath,  TransferRequest transferRequest,String partnerName) {


        log.info("inside airtime request to SL {} and partner name {}",transferRequest, partnerName);
        return webClientHttpService.post(endpoint,transferRequest, CustomerTransferResponse.class, AggregatorHelper.getHeaders(transferRequest))
                .map(response -> {
                    log.info("inside airtime response from SL {}",response);
                    CustomerTransferResponse customerTransferResponse = (CustomerTransferResponse) response;
                    customerTransferResponse.setSequenceNo(sequenceNoTraceField.getValue());
                    customerTransferResponse.setTransactionId(transactionIdTraceField.getValue());
                    if(customerTransferResponse.getStatusCode().equalsIgnoreCase(CanonicalErrorCode.OK.getCanonicalCode())){
                        return customerTransferResponse;
                    }

                    APIError error = buildApiError(customerTransferResponse.getStatusMessage(), customerTransferResponse.getSupportMessage(),
                          customerTransferResponse.getStatusCode(), customerTransferResponse.getSequenceNo(), customerTransferResponse.getTransactionId());
                    throw handleExceptionalResponseCode(requestPath, customerTransferResponse,error);

                }).onErrorResume(throwable -> Mono.error(handleOnErrorResume(throwable,requestPath, null)));
    }

    public Mono<TransferResponse> acsTransfer(String endpoint, String requestPath, TransferRequest transferRequest, String partnerName) {

        log.info("inside ACS data transfer request to SL {} and partner name {}",transferRequest, partnerName);
        return webClientHttpService.post(endpoint,transferRequest, TransferResponse.class, AggregatorHelper.getHeaders(transferRequest))
                .map(response -> {
                    log.info("inside ACS airtime response from SL {}",response);
                    TransferResponse customerTransferResponse = (TransferResponse) response;
                    customerTransferResponse.setTransactionId(transactionIdTraceField.getValue());
                    if(customerTransferResponse.getStatusCode().equalsIgnoreCase(CanonicalErrorCode.OK.getCanonicalCode())){
                        return customerTransferResponse;
                    }

                    APIError error = buildApiError(customerTransferResponse.getMessage(),customerTransferResponse.getMessage(), customerTransferResponse.getStatusCode(),"null", customerTransferResponse.getTransactionId());
                    throw handleAcsExceptionalResponseCode(requestPath, customerTransferResponse,error);

                }).onErrorResume(throwable -> Mono.error(handleOnErrorResume(throwable,requestPath, null)));
    }

    public Mono<CustomerTransferResponse> transactionStatus(String endpoint, String transactionId, String countryCode)
          throws SSLException {

        log.info("Calling prymo SL fo transactionStatus with transactionId {} ",transactionId);
        return defaultWebClientHttpService.get(endpoint,null, CustomerTransferResponse.class, countryCode)
              .map(response -> {
                  log.info("Response from SL {}",response);
                  var customerTransferResponse = (CustomerTransferResponse) response;
                  customerTransferResponse.setTransactionId(transactionIdTraceField.getValue());
                  if(customerTransferResponse.getStatusCode().equalsIgnoreCase(CanonicalErrorCode.OK.getCanonicalCode())){
                      return customerTransferResponse;
                  }

                  var error = buildApiError(customerTransferResponse.getStatusMessage(), customerTransferResponse.getStatusCode(),
                        customerTransferResponse.getSupportMessage(), customerTransferResponse.getSequenceNo(), customerTransferResponse.getTransactionId());
                  throw handleExceptionalResponseCode(endpoint, customerTransferResponse,error);

              }).onErrorResume(throwable -> Mono.error(handleOnErrorResume(throwable,endpoint, null)));
    }

    private CanonicalErrorCodeException handleExceptionalResponseCode(String endpoint, CustomerTransferResponse responseCode, APIError apiError) {
        var canonicalErrorCode = CanonicalErrorCode.getCanonicalErrorCode(responseCode.getStatusCode());

        if((StringUtils.isBlank(apiError.getStatusMessage()) || StringUtils.equalsIgnoreCase(apiError.getStatusMessage(),"null")) &&
                (StringUtils.isBlank(apiError.getSupportMessage()) || StringUtils.equalsIgnoreCase(apiError.getStatusMessage(),"null"))){
            apiError.setStatusMessage(canonicalErrorCode.getMessage());
            apiError.setSupportMessage(canonicalErrorCode.getMessage());
        }
        return new CanonicalErrorCodeException(canonicalErrorCode, endpoint, responseCode.getTransactionId(),
                responseCode.getStatusMessage(),apiError);
    }

    private CanonicalErrorCodeException handleAcsExceptionalResponseCode(String endpoint, TransferResponse responseCode, APIError apiError) {
        var canonicalErrorCode = CanonicalErrorCode.getCanonicalErrorCode(responseCode.getStatusCode());

        if((StringUtils.isBlank(apiError.getStatusMessage()) || StringUtils.equalsIgnoreCase(apiError.getStatusMessage(),"null")) &&
                (StringUtils.isBlank(apiError.getSupportMessage()) || StringUtils.equalsIgnoreCase(apiError.getStatusMessage(),"null"))){
            apiError.setStatusMessage(canonicalErrorCode.getMessage());
            apiError.setSupportMessage(canonicalErrorCode.getMessage());
        }
        return new CanonicalErrorCodeException(canonicalErrorCode, endpoint, responseCode.getTransactionId(),
                responseCode.getMessage(),apiError);
    }

    private CanonicalErrorCodeException handleConsentErrorResponseCode(String  responseCode) {
        var canonicalErrorCode = CanonicalErrorCode.getCanonicalErrorCode(responseCode);
        return new CanonicalErrorCodeException(canonicalErrorCode, null, null, null);
    }

    private CanonicalErrorCodeException handleOnErrorResume(Throwable throwable,String endPoint, APIError apiError) {
        log.info("An exception occurred {} ",throwable.getLocalizedMessage());
        if (throwable instanceof TimeoutException) {
            throw new CanonicalErrorCodeException(TIMEOUT,endPoint, apiError);
        } else if (throwable instanceof CanonicalErrorCodeException) {
            throw  (CanonicalErrorCodeException) throwable;
        }
        throw new CanonicalErrorCodeException(INTERNAL_SERVER_ERROR, endPoint, apiError);
    }




    public Mono<CustomerTransferResponse> sendConsent(CustomerTransferRequestWrapper requestWrapper, String notificationUrl) {

        SystemEndpoint systemEndpoint = requestWrapper.getSystemEndpoint();
        String senderMsisdn = requestWrapper.getSenderMsisdn();
        CustomerTransferRequest customerTransferRequest = requestWrapper.getCustomerTransferRequest();
        String nodeId = requestWrapper.getChannelId();
        String transactionId = requestWrapper.getTransactionId();
        String sequenceNo = requestWrapper.getSequenceNo();
        String partnerName = requestWrapper.getPartnerName();


        String consentEndpoint = systemEndpoint.getConsentEndpoint().replace("{msisdn}", senderMsisdn);
        ConsentRequest consentRequest = buildConsentRequest(transactionId, systemEndpoint.getSmsShortCode(),  notificationUrl, customerTransferRequest, nodeId);

        log.info("ConsentRequest :: {}", consentRequest);
        log.info("notificationUrl :: {}", notificationUrl);

        Map<String,String> headers = AggregatorHelper.getHeaders(partnerName,sequenceNo,transactionId);

        return webClientHttpService.post(consentEndpoint, consentRequest,headers)
                .map(consentResponse -> {
                  log.info("Send Consent Response :  {} ", consentResponse);
                   return consentResponse.toCustomerTransferResponse(transactionId);
                })
                .onErrorResume(x -> {
                    log.info("Error on send consent message, Error {}, Message {}",
                            x.getClass().getSimpleName(), x.getLocalizedMessage());
                    return Mono.just(consentNotSentResponse(transactionId));
                });
    }

    public Mono<CustomerTransferResponse> handleUserConsentResponse(ConsentNotificationResponse consentNotificationResponse,
                                                                    String partnerName) {
        if (consentNotificationResponse.getStatusCode().equals(CanonicalErrorCode.OK.getCanonicalCode())) {
            return handleSuccessfulConsentResponse(consentNotificationResponse,partnerName);
        } else {
            throw handleConsentErrorResponseCode(consentNotificationResponse.getStatusCode());
        }
    }

    private Mono<CustomerTransferResponse> handleSuccessfulConsentResponse(ConsentNotificationResponse consentNotificationResponse,
                                                                           String partnerName) {
        log.info("Logging partner name ::: {}", partnerName);
        if (CollectionUtils.isEmpty(consentNotificationResponse.getCustomData()) ||
                (consentNotificationResponse.getCustomData().size() < SIZE_OF_CONSENT_CUSTOM_DATA)) {

            throw handleConsentErrorResponseCode(CanonicalErrorCode.INTERNAL_SERVER_ERROR.getCanonicalCode());
        } else {
            Optional<SystemEndpoint> systemEndpoint = getSystemEndpoint(consentNotificationResponse.getMsisdn(), properties);

            if (systemEndpoint.isPresent()) {
                return handleConsentGivenResponse(consentNotificationResponse, systemEndpoint.get());
            } else {
                throw handleConsentErrorResponseCode(consentNotificationResponse.getStatusCode());
            }
        }
    }

    private Mono<CustomerTransferResponse> handleConsentGivenResponse(
            ConsentNotificationResponse consentNotificationResponse, SystemEndpoint systemEndpoint) {

        var callbackUrl = consentNotificationResponse.getCustomData().get(INDEX_CALLBACK_URL_CUSTOM_DATA);
        var transferRequest = buildAirtimeRequest(consentNotificationResponse);
        var senderMsisdn = consentNotificationResponse.getMsisdn();
        var airtimeEndpoint = systemEndpoint.getCustomerAirtimeTransferEndPoint().replace("{senderMsisdn}", senderMsisdn);
        log.info(String.format("customer Airtime transfer system layer endpoint: %s", airtimeEndpoint));

        String partnerName = consentNotificationResponse.getPartnerName();
        String sequenceNo = consentNotificationResponse.getSequenceNo();
        String transactionId = consentNotificationResponse.getTransactionId();

        Map<String,String> headers = AggregatorHelper.getHeaders(partnerName,sequenceNo,transactionId);

       return transfer(airtimeEndpoint, null, transferRequest,partnerName)
               .map(customerTransferResponse -> {
                   log.info("Airtime Transfer response {} ", customerTransferResponse.toString());
                   webClientHttpService.postVoid(callbackUrl, customerTransferResponse,headers).subscribe();
                   return customerTransferResponse;
               });
    }

    private TransferRequest buildAirtimeRequest(ConsentNotificationResponse consentNotificationResponse) {

        List<String> customData = consentNotificationResponse.getCustomData();

        return TransferRequest.builder()
                .receiverMsisdn(customData.get(INDEX_RECEIVER_MSISDN_CUSTOM_DATA))
                .productCode(customData.get(INDEX_PRODUCT_CODE_CUSTOM_DATA))
                .nodeId(customData.get(INDEX_NODE_ID_CUSTOM_DATA))
                .pinCode(consentNotificationResponse.getUserResponse())
                .transferAmount(new BigDecimal(String.valueOf(customData.get(INDEX_TRANSFER_AMOUNT_CUSTOM_DATA))))
                .build();
    }

    private ConsentRequest buildConsentRequest(String transactionId, String smsShortCode, String notificationUrl, CustomerTransferRequest customerTransferRequest, String nodeId) {
        return ConsentRequest.builder()
                .smsShortCode(smsShortCode)
                .flowType(ConsentType.SMS_FLOW.getFlag())
                .confirmationMessage(buildConfirmationMessage(customerTransferRequest.getReceiverMsisdn()))
                .callbackUrl(notificationUrl)
                .customData(
                        List.of(transactionId,
                                customerTransferRequest.getReceiverMsisdn(),
                                customerTransferRequest.getProductCode(),
                                customerTransferRequest.getType(),
                                customerTransferRequest.getAgentId() != null ? customerTransferRequest.getAgentId() : "" ,
                                String.valueOf(customerTransferRequest.getTransferAmount()),
                                customerTransferRequest.getCallbackUrl(),
                                nodeId)
                )
                .build();
    }

    private String buildConfirmationMessage(String receiverMsisdn) {

        return String.format(
                        "Please you're about to do Airtime transfer to %s, pls reply with your pin to complete the transfer.", receiverMsisdn);
    }

    public  CustomerTransferResponse consentNotSentResponse(String transactionId) {
         ConsentResponseData.builder()
                .statusCode(CanonicalErrorCode.CONSENT_NOT_DELIVERED.getCanonicalCode())
                .transactionId(transactionId)
                .statusMessage(CanonicalErrorCode.CONSENT_NOT_DELIVERED.getMessage())
                .build();
        var customerTransferResponse = CustomerTransferResponse.builder().build();
        customerTransferResponse.setStatusCode(CanonicalErrorCode.CONSENT_NOT_DELIVERED.getCanonicalCode());
        customerTransferResponse.setTransactionId(transactionId);
        customerTransferResponse.setStatusMessage(CanonicalErrorCode.CONSENT_NOT_DELIVERED.getMessage());
        return customerTransferResponse;

    }


    public Mono<CustomerTransferResponse> transferAirTimeComviva(TransferDsmRequestWrapper requestWrapper) {
        if (requestWrapper.getTransferRequest().getAgentId().isBlank()) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(AGENT_ID_REQUIRED,
                    requestWrapper.getRequestPath(), requestWrapper.getTransactionId()));
        }

        if (requestWrapper.getTransferRequest().getPin().isBlank()) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(PIN_REQUIRED,
                    requestWrapper.getRequestPath(), requestWrapper.getTransactionId()));
        }

        if(requestWrapper.getTransferRequest().getTransferAmount().compareTo(new BigDecimal("0")) < 0) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(INVALID_TRANSFER_AMOUNT,
                  requestWrapper.getRequestPath(), requestWrapper.getTransactionId()));
        }

        if(requestWrapper.getTransactionId().length() > 20) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(TRANSACTION_ID_TOO_LONG,
                    requestWrapper.getRequestPath(), requestWrapper.getTransactionId()));
        }
        if(!StringUtils.isNumeric(requestWrapper.getTransactionId())) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(NON_NUMERIC_TRANSACTION_ID,
                    requestWrapper.getRequestPath(), requestWrapper.getTransactionId()));
        }

        var request = AggregatorHelper.toDmsTransferRequest(requestWrapper);
        log.info("inside airtime request to COMVIVA SL {}",requestWrapper.getTransferRequest());
        var apiError = AggregatorHelper.buiApiError(requestWrapper);
        return webClientHttpService.post(requestWrapper.getEndpoint(),request,
                        CustomerTransferComvivaResponse.class, AggregatorHelper.getHeaders(requestWrapper))
                .cast(CustomerTransferComvivaResponse.class)
                .map(response -> {
                    log.info("inside airtime response from COMVIVA SL {}",response);
                    if(response.getStatusCode().equalsIgnoreCase(CanonicalErrorCode.OK.getCanonicalCode())){
                        CustomerTransferResponse comvivaResponse =response.getData();
                        comvivaResponse.getData().setProductType(TransferType.AIRTIME.getValue());
                        return comvivaResponse;
                    }

                    if(response.getError() == null){
                        APIError error = buildApiError(response.getStatusMessage(), null, response.getStatusCode(),
                                response.getSequenceNo(), response.getTransactionId());
                        response.setError(error);
                    }

                    response.setData(CustomerTransferResponse.builder()
                            .statusCode(response.getStatusCode())
                            .sequenceNo(response.getSequenceNo())
                            .transactionId(response.getTransactionId())
                            .build());

                    throw handleExceptionalResponseCode(requestWrapper.getRequestPath(),
                            response.getData(),response.getError());

                }).onErrorResume(throwable -> Mono.error(handleOnErrorResume(throwable,requestWrapper.getRequestPath(),apiError)));
    }

    private APIError buildApiError(String statusMessage, String supportMessage, String statusCode, String sequenceNumber, String transactionId){
        APIError error = new APIError();
        error.setStatusMessage(statusMessage);
        error.setSupportMessage(StringUtils.isBlank(supportMessage) ? statusMessage : supportMessage);
        error.setSequenceNo(sequenceNumber);
        error.setTransactionId(transactionId);
        error.setStatusCode(statusCode);
        return error;
    }

}
