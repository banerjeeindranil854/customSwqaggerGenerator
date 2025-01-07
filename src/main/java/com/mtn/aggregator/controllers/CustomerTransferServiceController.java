package com.mtn.aggregator.controllers;


import brave.baggage.BaggageField;
import com.mtn.aggregator.callback.CustomerTransferCallbackService;
import com.mtn.aggregator.configuration.TransferAggregatorProperties;
import com.mtn.aggregator.enums.RequestType;
import com.mtn.aggregator.enums.SystemName;
import com.mtn.aggregator.enums.TransferType;
import com.mtn.aggregator.exception.CanonicalErrorCodeException;
import com.mtn.aggregator.helpers.AggregatorHelper;
import com.mtn.aggregator.helpers.logging.LoggingHelper;
import com.mtn.aggregator.models.SystemEndpoint;
import com.mtn.aggregator.models.logging.RequestLogItem;
import com.mtn.aggregator.models.request.transfer.CustomerTransferRequest;
import com.mtn.aggregator.models.request.transfer.CustomerTransferRequestWrapper;
import com.mtn.aggregator.models.request.transfer.TransferDsmRequestWrapper;
import com.mtn.aggregator.models.request.transfer.TransferRequest;
import com.mtn.aggregator.models.response.APIResponse;
import com.mtn.aggregator.models.response.CustomerTransferResponse;
import com.mtn.aggregator.models.response.consent.ConsentNotificationResponse;
import com.mtn.aggregator.services.CustomerTransferService;
import com.mtn.madapi.commons.helpers.TransactionIdGenerator;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static com.mtn.aggregator.constants.TransferConstant.NOTIFICATION_PATH;
import static com.mtn.aggregator.constants.TransferConstant.SENDER_MSISDN_PLACEHOLDER;
import static com.mtn.aggregator.enums.response.CanonicalErrorCode.*;
import static com.mtn.aggregator.helpers.MsisdnHelper.getSystemEndpoint;
import static com.mtn.aggregator.helpers.MsisdnHelper.isMsisdnValid;


@Slf4j
@RestController
public class CustomerTransferServiceController {

    private final CustomerTransferService customerTransferService;

    private final LoggingHelper loggingHelper;
    private final TransferAggregatorProperties properties;
    private final AggregatorHelper aggregatorHelper;
    private final BaggageField msisdnTraceField;
    private final BaggageField transactionIdTraceField;
    private final BaggageField partnerNameTraceField;
    private final BaggageField sequenceNoTraceField;
    private final TransactionIdGenerator transactionIdGenerator;
    private final CustomerTransferCallbackService callbackService;

    private int MAX_CHAR = 20;

    @Autowired
    public CustomerTransferServiceController(TransferAggregatorProperties properties,
                                             CustomerTransferService customerTransferService,
                                             AggregatorHelper aggregatorHelper, LoggingHelper loggingHelper,
                                             TransactionIdGenerator transactionIdGenerator,
                                             CustomerTransferCallbackService callbackService,
                                             BaggageField msisdnTraceField,
                                             BaggageField transactionIdTraceField,
                                             BaggageField partnerNameTraceField,
                                             BaggageField sequenceNoTraceField) {
        this.customerTransferService = customerTransferService;
        this.loggingHelper = loggingHelper;
        this.aggregatorHelper = aggregatorHelper;
        this.properties = properties;
        this.msisdnTraceField = msisdnTraceField;
        this.transactionIdTraceField = transactionIdTraceField;
        this.partnerNameTraceField = partnerNameTraceField;
        this.sequenceNoTraceField = sequenceNoTraceField;
        this.transactionIdGenerator = transactionIdGenerator;
        this.callbackService = callbackService;
    }


    @ApiOperation(value = "Customers", notes = "Customers will be able to transfer airtime or data via traditional and digital channel",
            response = CustomerTransferResponse.class)
    @PostMapping(value = "/customers/{senderMsisdn}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<APIResponse> transfer(@PathVariable String senderMsisdn,
                                      @RequestBody @Valid CustomerTransferRequest customerTransferRequest,
                                      @RequestParam(value="network", required = false) String network,
                                      @RequestHeader(value="x-country-code", required = false) String countryCode,
                                      @RequestHeader(value="transactionId", required = false) String providedTransactionId,
                                      @RequestHeader(value="x-origin-channelid", required = false) String partnerName,
                                      HttpServletRequest httpServletRequest) {

        String sequenceNo = generateSequenceNumber();
        String transactionId = StringUtils.isBlank(providedTransactionId) ? sequenceNo
              : providedTransactionId;
        propagateSleuthFields(senderMsisdn, transactionId,partnerName,sequenceNo);

        String xAuthorizationClaims = httpServletRequest.getHeader("x-authorization-claims");

        String channelId = aggregatorHelper.extractChannelId(xAuthorizationClaims);

        RequestLogItem logItem = RequestLogItem.builder()
                .senderMsisdn(senderMsisdn)
                .countryCode(countryCode)
                .customerTransferRequest(customerTransferRequest)
                .partnerName(partnerName)
                .nodeId(channelId)
                .operation(getRequestType(customerTransferRequest.getType()))
                .network(network)
                .transactionId(transactionId)
                .sequenceNo(sequenceNo)
                .partnerName(partnerName)
                .build();

        loggingHelper.logRequestObject(log,logItem);

        String requestPath = String.format("/customers/%s", senderMsisdn);

        Optional<SystemEndpoint> systemEndpoint = getSystemEndpoint(senderMsisdn, properties,
              customerTransferRequest.getTargetSystem());

        Optional<SystemEndpoint> systemEndpointReceiver = getSystemEndpoint(customerTransferRequest.getReceiverMsisdn(),
              properties, customerTransferRequest.getTargetSystem());

        if (systemEndpoint.isEmpty()) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(INVALID_SENDER_COUNTRY_CODE,
                  requestPath, transactionId));
        }

        if (systemEndpointReceiver.isEmpty()) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(INVALID_RECEIVER_COUNTRY_CODE,
                  requestPath, transactionId));
        }

        if (isMsisdnValid(senderMsisdn, systemEndpoint.get().getMsisdnLength())) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(INVALID_SENDER_MSISDN,
                  requestPath, transactionId));
        }

        if (isMsisdnValid(customerTransferRequest.getReceiverMsisdn(), systemEndpoint.get().getMsisdnLength())) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(INVALID_RECEIVER_MSISDN,
                  requestPath, transactionId));
        }

        Link link = Link.of(properties.getExperienceLayer().concat(requestPath), "self");

        if(customerTransferRequest.getType().equalsIgnoreCase(TransferType.AIRTIME.getValue()) &&
              SystemName.COMVIVA.getName().equalsIgnoreCase(customerTransferRequest.getTargetSystem())){

            if(Objects.isNull(customerTransferRequest.getTransferAmount())){
                return Mono.error(CanonicalErrorCodeException.invalidRequestException(INVALID_TRANSFER_AMOUNT,
                        requestPath, transactionId));
            }

            var dsmRequestWrapper = TransferDsmRequestWrapper.builder()
                    .endpoint(systemEndpoint.get().getCustomerAirtimeTransferEndPoint().replace(SENDER_MSISDN_PLACEHOLDER, senderMsisdn))
                    .partnerName(partnerName)
                    .sequenceNo(sequenceNo)
                    .senderMsisdn(senderMsisdn)
                    .transactionId(transactionId)
                    .transferRequest(customerTransferRequest)
                    .requestPath(requestPath)
                    .build();
            return customerTransferService.transferAirTimeComviva(dsmRequestWrapper)
                    .map(response -> {
                        response.add(link);
                        loggingHelper.logResponseObject(log, RequestType.TRANSFER_AIRTIME, response);
                        return response;
                    });
        }

        if(!(("GHA").equals(countryCode) || ("LBR").equals(countryCode)) && StringUtils.isBlank(channelId)) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(MISSING_CHANNEL_ID,
                  requestPath, transactionId));
        }

        if("GHA".equals(countryCode) && customerTransferRequest.getTransferAmount().compareTo(new BigDecimal("0")) < 0) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(INVALID_TRANSFER_AMOUNT,
                  requestPath, transactionId));
        }

        if (customerTransferRequest.getType().equalsIgnoreCase(TransferType.AIRTIME.getValue())) {

            return airtimeTransfer(httpServletRequest, countryCode, network, CustomerTransferRequestWrapper.builder()
                  .systemEndpoint(systemEndpoint.get())
                  .channelId(channelId)
                  .customerTransferRequest(customerTransferRequest)
                  .transactionId(transactionId)
                  .link(link)
                  .senderMsisdn(senderMsisdn)
                  .requestPath(requestPath)
                  .partnerName(partnerName)
                  .transactionId(transactionId)
                  .sequenceNo(sequenceNo)
                  .build());

        } else if (customerTransferRequest.getType().equalsIgnoreCase(TransferType.DATA.getValue())) {

            return dataTransfer(CustomerTransferRequestWrapper.builder()
                        .systemEndpoint(systemEndpoint.get())
                        .channelId(channelId)
                        .customerTransferRequest(customerTransferRequest)
                        .link(link)
                        .senderMsisdn(senderMsisdn)
                        .requestPath(requestPath)
                        .partnerName(partnerName)
                        .transactionId(transactionId)
                        .sequenceNo(sequenceNo)
                        .build(),countryCode);


        } else if (TransferType.SME_DATA.getValue().equalsIgnoreCase(customerTransferRequest.getType())) {

            return smeDataTransfer(CustomerTransferRequestWrapper.builder()
                  .systemEndpoint(systemEndpoint.get())
                  .channelId(channelId)
                  .customerTransferRequest(customerTransferRequest)
                  .link(link)
                  .senderMsisdn(senderMsisdn)
                  .requestPath(requestPath)
                  .partnerName(partnerName)
                  .transactionId(transactionId)
                  .sequenceNo(sequenceNo)
                  .build());
        }

        return Mono.error(CanonicalErrorCodeException.invalidRequestException(INVALID_TRANSFER_TYPE,
              requestPath, transactionId));
    }


    private Mono<APIResponse> airtimeTransfer(HttpServletRequest httpServletRequest, String countryCode, String network,
                                              CustomerTransferRequestWrapper requestWrapper){
        if(Objects.isNull(requestWrapper.getCustomerTransferRequest().getTransferAmount())){
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(INVALID_TRANSFER_AMOUNT,
                   requestWrapper.getRequestPath(), requestWrapper.getTransactionId()));
        }
        var notificationUrl = getNotificationUrl(httpServletRequest);

        String[] channelIds = requestWrapper.getSystemEndpoint().getChannelIds().split(",");

        if("GHA".equalsIgnoreCase(countryCode) && SystemName.PRYMO.getName().equals(
              requestWrapper.getCustomerTransferRequest().getTargetSystem())){

            if(StringUtils.isBlank(requestWrapper.getCustomerTransferRequest().getCallbackUrl())){
                return Mono.error(CanonicalErrorCodeException.invalidRequestException(MISSING_CALLBACK_URL,
                      requestWrapper.getRequestPath(), requestWrapper.getTransactionId()));
            }else{
                callbackService.saveCallbackForTransaction(requestWrapper.getTransactionId(),
                      requestWrapper.getCustomerTransferRequest().getCallbackUrl(), countryCode,
                      requestWrapper.getRequestPath(), requestWrapper.getCustomerTransferRequest().getTargetSystem());
            }
        }

        if (Arrays.stream(channelIds).anyMatch(channel -> channel.equalsIgnoreCase(requestWrapper.getChannelId()))
              || "GHA".equals(countryCode)) {

            var airtimeCisMymtnappTransferRequest = TransferRequest.builder()
                  .nodeId(requestWrapper.getChannelId())
                  .agentId(requestWrapper.getCustomerTransferRequest().getAgentId())
                  .productCode(requestWrapper.getCustomerTransferRequest().getProductCode())
                  .receiverMsisdn(requestWrapper.getCustomerTransferRequest().getReceiverMsisdn())
                  .transferAmount(requestWrapper.getCustomerTransferRequest().getTransferAmount())
                  .pinCode(requestWrapper.getCustomerTransferRequest().getPin())
                  .callBack(requestWrapper.getCustomerTransferRequest().getCallbackUrl())
                  .network(network)
                  .partnerName(requestWrapper.getPartnerName())
                  .sequenceNo(requestWrapper.getSequenceNo())
                  .transactionId(requestWrapper.getTransactionId())
                  .build();

            log.info("SL endpoint : {}", requestWrapper.getSystemEndpoint().getCustomerAirtimeTransferEndPoint());

            return customerTransferService.transfer(requestWrapper.getSystemEndpoint().getCustomerAirtimeTransferEndPoint()
                              .replace(SENDER_MSISDN_PLACEHOLDER, requestWrapper.getSenderMsisdn()), requestWrapper.getRequestPath(),
                        airtimeCisMymtnappTransferRequest, requestWrapper.getPartnerName())
                  .map(response -> {
                      response.add(requestWrapper.getLink());
                      loggingHelper.logResponseObject(log, RequestType.TRANSFER_AIRTIME, response);
                      return response;
                  });
        } else {



            return customerTransferService.sendConsent(requestWrapper,
                        notificationUrl)
                  .map(response -> {
                      response.add(requestWrapper.getLink());
                      loggingHelper.logResponseObject(log, RequestType.TRANSFER_AIRTIME, response);
                      return response;
                  });
        }

    }

    private Mono<APIResponse> dataTransfer(CustomerTransferRequestWrapper requestWrapper, String countryCode){

        var dataEndpoint = requestWrapper.getSystemEndpoint().getCustomerDataTransferEndPoint()
              .replace(SENDER_MSISDN_PLACEHOLDER, requestWrapper.getSenderMsisdn());
        var transferRequest = TransferRequest.builder()
              .nodeId(requestWrapper.getChannelId())
              .productCode(requestWrapper.getCustomerTransferRequest().getProductCode())
              .receiverMsisdn(requestWrapper.getCustomerTransferRequest().getReceiverMsisdn())
              .transferAmount(requestWrapper.getCustomerTransferRequest().getTransferAmount())
              .partnerName(requestWrapper.getPartnerName())
              .sequenceNo(requestWrapper.getSequenceNo())
              .transactionId(requestWrapper.getTransactionId())
              .productId(String.valueOf(requestWrapper.getCustomerTransferRequest().getProductId()))
              .type(requestWrapper.getCustomerTransferRequest().getType())
              .build();
        log.info(String.format("customer Data transfer system layer endpoint: %s", dataEndpoint));
        if (("LBR").equals(countryCode) || requestWrapper.getCustomerTransferRequest().getTargetSystem().equalsIgnoreCase("ACS")){
            return customerTransferService.acsTransfer(dataEndpoint,requestWrapper.getRequestPath(),transferRequest,
                    requestWrapper.getPartnerName())
                    .map(response -> {
                        response.add(requestWrapper.getLink());
                        loggingHelper.logResponseObject(log, RequestType.TRANSFER_DATA, response);
                        return response;
                    });
        }
        return customerTransferService.transfer(dataEndpoint, requestWrapper.getRequestPath(), transferRequest,
                    requestWrapper.getPartnerName() )
              .map(response -> {
                  response.add(requestWrapper.getLink());
                  loggingHelper.logResponseObject(log, RequestType.TRANSFER_DATA, response);
                  return response;
              });
    }

    private Mono<APIResponse> smeDataTransfer(CustomerTransferRequestWrapper requestWrapper){
        var dataEndpoint = requestWrapper.getSystemEndpoint().getCustomerSmeDataEndPoint()
              .replace(SENDER_MSISDN_PLACEHOLDER, requestWrapper.getSenderMsisdn());
        var transferRequest = TransferRequest.builder()
              .nodeId(requestWrapper.getChannelId())
              .productCode(requestWrapper.getCustomerTransferRequest().getProductCode())
                .pinCode(requestWrapper.getCustomerTransferRequest().getPin())
              .receiverMsisdn(requestWrapper.getCustomerTransferRequest().getReceiverMsisdn())
              .transferAmount(requestWrapper.getCustomerTransferRequest().getTransferAmount())
              .partnerName(requestWrapper.getPartnerName())
              .sequenceNo(requestWrapper.getSequenceNo())
              .transactionId(requestWrapper.getTransactionId())
              .build();
        log.info(String.format("customer SME Data system layer endpoint: %s", dataEndpoint));
        return customerTransferService.transfer(dataEndpoint, requestWrapper.getRequestPath(), transferRequest,
                    requestWrapper.getPartnerName())
              .map(response -> {
                  response.add(requestWrapper.getLink());
                  loggingHelper.logResponseObject(log, RequestType.SME_DATA, response);
                  return response;
              });
    }

    @ApiOperation(
            value = "a User Consent Response object",
            notes = "The users consent information posted here as a callback notification."
    )
    @PostMapping(value = NOTIFICATION_PATH)
    @ResponseStatus(value = HttpStatus.CREATED)
    public void handleConsentNotification(@RequestBody ConsentNotificationResponse consentNotificationRequest,
                                          @RequestHeader(value="x-country-code", required = false) String countryCode,
                                          @RequestHeader(value="transactionId", required = false) String providedTransactionId,
                                          @RequestHeader(value="x-origin-channelid", required = false) String partnerName) {

        String sequenceNo = generateSequenceNumber();
        String transactionId = StringUtils.isBlank(providedTransactionId) ? generateTransactionId(providedTransactionId)
                : providedTransactionId;

        propagateSleuthFields(consentNotificationRequest.getMsisdn(), transactionId,partnerName,sequenceNo);

        var logItem =  new RequestLogItem();
        logItem.setOperation(RequestType.AIRTIME_TRANSFER_CONSENT_RESPONSE_NOTIFICATION);
        logItem.setSenderMsisdn(consentNotificationRequest.getMsisdn());
        logItem.setRequestBody(consentNotificationRequest);
        logItem.setPartnerName(partnerName);
        logItem.setSequenceNo(sequenceNo);
        logItem.setTransactionId(transactionId);

        loggingHelper.logRequestObject(log,logItem);

        consentNotificationRequest.setPartnerName(partnerName);
        consentNotificationRequest.setSequenceNo(sequenceNo);
        consentNotificationRequest.setTransactionId(transactionId);

        customerTransferService.handleUserConsentResponse(consentNotificationRequest,partnerName)
                .doOnNext(customerTransferResponse ->
                        loggingHelper.responseObject(log, customerTransferResponse)
                )
                .subscribe();
    }


    @ApiOperation(value = "Customers", notes = "Provides the status of a Transaction to service providers.",
        response = CustomerTransferResponse.class)
    @GetMapping(value = "/customers/transactionStatus", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<APIResponse> transactionStatus(
        @RequestParam(value="transactionId", required = false) String transactionId,
        @RequestParam(value="operation", required = false) String operation,
        @RequestParam(value="network", required = false) String network,
        @RequestHeader(value="x-country-code", required = false) String countryCode,
        @RequestHeader(value="targetSystem", required = false) String targetSystem) throws SSLException {

        String sequenceNo = generateSequenceNumber();
        String tranxId = StringUtils.isBlank(transactionId) ? sequenceNo
            : transactionId;

        RequestLogItem logItem = RequestLogItem.builder()
            .countryCode(countryCode)
            .partnerName(targetSystem)
            .operation(operation)
            .network(network)
            .transactionId(tranxId)
            .build();

        loggingHelper.logRequestObject(log,logItem);

        Optional<SystemEndpoint> systemEndpoint = properties.getSystem().stream()
            .filter(
                sysEndpoint -> sysEndpoint.getSystemId().getName().equalsIgnoreCase(targetSystem)
            ).findFirst();
        var requestPath = String.format("/customers/transactionStatus?transactionId=%s&operation=%s&network=%s", tranxId, operation,network);

        if (systemEndpoint.isEmpty()) {
            return Mono.error(CanonicalErrorCodeException.invalidRequestException(SERVICE_UNAVAILABLE,
                requestPath, transactionId));
        }

        var endpoint = systemEndpoint.get().getDescription().concat(requestPath);

        Link link = Link.of(properties.getExperienceLayer().concat(requestPath), "self");

        return customerTransferService.transactionStatus(endpoint, tranxId, countryCode)
            .map(response -> {
                response.add(link);
                loggingHelper.logResponseObject(log, RequestType.TRANSFER_AIRTIME, response);
                return response;
            });
    }

    private String getNotificationUrl(HttpServletRequest servletRequest) {
        return String.format(
                "%s://%s:%s/notification", servletRequest.getScheme(),
                servletRequest.getServerName(), servletRequest.getServerPort()
        );
    }

    private RequestType getRequestType(String requestType){
        HashMap<String, RequestType> requestTypeMap = new HashMap<>();
        requestTypeMap.put("airtime",RequestType.TRANSFER_AIRTIME);
        requestTypeMap.put("data",RequestType.TRANSFER_DATA);
        requestTypeMap.put("sme_data",RequestType.SME_DATA);

        if(requestType == null || requestType.trim().length() < 1) return  RequestType.UNMAPPED_REQUEST_TYPE;

        RequestType type = requestTypeMap.get(requestType.trim().toLowerCase());
        if(type == null) return RequestType.UNMAPPED_REQUEST_TYPE;
        return type;

    }


    private void propagateSleuthFields(String msisdn, String transactionId, String partnerName, String sequenceNo) {
        msisdnTraceField.updateValue(msisdn);
        transactionIdTraceField.updateValue(transactionId);
        sequenceNoTraceField.updateValue(sequenceNo);
        partnerNameTraceField.updateValue(partnerName);
    }

    public String generateSequenceNumber() {
        String sequenceNo = transactionIdGenerator.generateTransactionId("CustomerTransfer",
                "CustomerTransferSystemLayer")
                .replaceAll("[^\\d.]", "");

        int maxLength = (sequenceNo.length() < MAX_CHAR)?sequenceNo.length():MAX_CHAR;
        sequenceNo = sequenceNo.substring(0, maxLength);
        return sequenceNo;
    }

    public String generateTransactionId(String transactionId) {
        if(StringUtils.isBlank(transactionId)) {
            return generateSequenceNumber();
        }else{
            return transactionId;
        }
    }
}
