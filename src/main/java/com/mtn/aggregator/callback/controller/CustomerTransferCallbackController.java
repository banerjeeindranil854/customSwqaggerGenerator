package com.mtn.aggregator.callback.controller;


import com.mtn.aggregator.callback.events.CallbackPublisher;
import com.mtn.aggregator.callback.events.CallbackPublisherDto;
import com.mtn.aggregator.configuration.TransferAggregatorProperties;
import com.mtn.aggregator.enums.RequestType;
import com.mtn.aggregator.enums.SystemName;
import com.mtn.aggregator.helpers.logging.LoggingHelper;
import com.mtn.aggregator.models.logging.RequestLogItem;
import com.mtn.aggregator.models.response.CustomerTransferResponse;
import com.mtn.aggregator.callback.CustomerTransferCallbackService;
import com.mtn.madapi.commons.constants.CanonicalErrorCode;
import com.mtn.madapi.commons.models.response.APIResponse;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import java.util.concurrent.CompletableFuture;

import static com.mtn.aggregator.helpers.MsisdnHelper.getSystemEndpoint;


@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerTransferCallbackController {

  private final CustomerTransferCallbackService callbackService;
  private final LoggingHelper loggingHelper;
  private final TransferAggregatorProperties properties;
  private final CallbackPublisher callbackPublisher;


  @ApiOperation(value = "Customer Airtime Callback",
        notes = "Customers will be able to transfer airtime or data via traditional and digital channel",
        response = CustomerTransferResponse.class)
  @PostMapping(value = "/customers/prymo/callback", produces = {MediaType.APPLICATION_JSON_VALUE})
  public Mono<APIResponse<String>> transfer(@RequestBody @Valid PrimoCallbackRequest request,
                                            @RequestHeader(value = "x-country-code", defaultValue = "GHA") String countryCode) {


    RequestLogItem logItem = RequestLogItem.builder()
          .countryCode(countryCode)
          .requestBody(request)
          .operation(RequestType.PRIMO_CALLBACK)
          .build();

    loggingHelper.logRequestObject(log, logItem);

    String requestPath = properties.getExperienceLayer().concat("/customers/prymo/callback");

    var callbackDetails = callbackService.getExistingCallbackForTransaction(request.getLocalTransactionCode(),
          countryCode, SystemName.PRYMO.getName(), requestPath);

    var apiResponse = new APIResponse<String>();
    apiResponse.setStatusCode(CanonicalErrorCode.OK.getCode());
    apiResponse.setStatusMessage("Callback initiated for transactionId : " + request.getTransactionId() + " to " + callbackDetails.getCallbackUrl());

    return Mono.just(apiResponse)
          .doOnNext(response -> CompletableFuture.runAsync(() ->
                callbackPublisher.publishCallback(CallbackPublisherDto.builder()
                      .request(request)
                      .callbackUrl(callbackDetails.getCallbackUrl())
                      .transactionId(request.getTransactionId())
                      .targetSystem(callbackDetails.getTargetSystem())
                      .countryCode(countryCode)
                      .build())))
          .doOnNext(response -> loggingHelper.responseObject(log, response));
  }

}
