package com.mtn.aggregator.helpers;

import com.mtn.aggregator.enums.SystemName;
import com.mtn.aggregator.enums.response.CanonicalErrorCode;
import com.mtn.aggregator.models.SystemEndpoint;
import com.mtn.aggregator.models.request.transfer.CustomerTransferRequest;
import com.mtn.aggregator.models.request.transfer.CustomerTransferRequestWrapper;
import com.mtn.aggregator.models.request.transfer.TransferRequest;
import com.mtn.aggregator.models.response.CustomerTransferResponse;
import com.mtn.aggregator.models.response.acs.*;
import com.mtn.aggregator.models.response.consent.ConsentNotificationResponse;
import com.mtn.aggregator.models.response.data.CustomerTransferResponseData;
import com.mtn.madapi.commons.models.response.APIResponse;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public class TestHelpers {
    public static final String SAMPLE_VALID_SENDER_MSISDN = "2347061555567";
    public static final String SAMPLE_VALID_SENDER_MSISDN_LBR = "231706155556";
    public static final String SAMPLE_VALID_SENDER_MSISDN_GHA = "233706155556";
    public static final String SAMPLE_INVALID_RECEIVER_MSISDN = "23470615553yu";
    public static final String SAMPLE_INVALID_RECEIVER_MSISDN_COUNTRY_CODE = "22470615553yu";
    public static final String SAMPLE_VALID_RECEIVER_MSISDN = "2347061555333";
    public static final String SAMPLE_VALID_GHA_RECEIVER_MSISDN = "233706155533";
    public static final String SAMPLE_INVALID_SENDER_MSISDN = "234706155556u";
    public static final String SAMPLE_INVALID_SENDER_MSISDN_COUNTRY_CODE = "2227061555569";
    public static final String SAMPLE_TRANSACTION_ID = "65676898093469830";
    public static final String NODE_ID = "GOOGLE";
    public static final String NODE_ID_HEADER = "nodeId";
    public static final String SAMPLE_SMS_SHORT_CODE = "89808";
    public static final String TRANSFER_ENDPOINT = "/customers/%s";
    public static final String PRODUCT_CODE = "RACT_DATA_243";
    private static final String SAMPLE_CONSENT_MESSAGE = "Sample confirmation message";
    public static final String SAMPLE_NOTIFICATION_URL = "http://notification.com/notification";

    public static final String AIRTIME = "airtime";
    public static final String DATA = "data";
    public static final String SME_DATA = "sme_data";


    public static final String SAMPLE_CALLBACK_URL = "http://callback.com/callback";

    private static List<String> sampleCustomData = List.of(
            SAMPLE_TRANSACTION_ID, SAMPLE_VALID_RECEIVER_MSISDN,
            PRODUCT_CODE, AIRTIME, "9", "60", SAMPLE_CALLBACK_URL,NODE_ID);


    public static CustomerTransferRequest sampleDataTransferRequest(String msisdn, String type, String callbackUrl) {
        return CustomerTransferRequest.builder()
                .type(type)
                .agentId("myapp")
                .transferAmount(new BigDecimal(900))
                .targetSystem("CIS")
                .callbackUrl(callbackUrl)
                .productCode(PRODUCT_CODE)
                .receiverMsisdn(msisdn)
                .build();
    }

    public static TransferRequest sampleTransferRequest() {
        return TransferRequest.builder()
                .targetSystem(SystemName.CIS.getName())
                .receiverMsisdn(SAMPLE_VALID_RECEIVER_MSISDN)
                .productCode(PRODUCT_CODE)
                .transferAmount(new BigDecimal("900"))
                .pinCode("1234")
                .nodeId(NODE_ID)
                .build();
    }

    public static CustomerTransferRequest sampleTransferRequestGHA() {
        return CustomerTransferRequest.builder()
              .receiverMsisdn(SAMPLE_VALID_GHA_RECEIVER_MSISDN)
              .transferAmount(new BigDecimal("500"))
              .targetSystem("AIR")
              .type("Airtime")
              .build();
    }

    public static CustomerTransferRequest samplePrymoTransferRequestGHA() {
        return CustomerTransferRequest.builder()
              .receiverMsisdn(SAMPLE_VALID_GHA_RECEIVER_MSISDN)
              .transferAmount(new BigDecimal("0.2"))
              .targetSystem("PRYMO")
              .agentId("mobile_app")
              .type("Airtime")
              .callbackUrl("http://localhost:1080/callback")
              .build();
    }

    public static CustomerTransferRequest sampleInvalidAmountTransferRequestGHA() {
        return CustomerTransferRequest.builder()
              .receiverMsisdn(SAMPLE_VALID_GHA_RECEIVER_MSISDN)
              .transferAmount(new BigDecimal("-500"))
              .targetSystem("AIR")
              .type("Airtime")
              .build();
    }


    public static CustomerTransferResponse sampleTransferResponse(CanonicalErrorCode canonicalErrorCode) {
        return CustomerTransferResponse.builder()
                .statusCode(canonicalErrorCode.getCanonicalCode())
                .transactionId(SAMPLE_TRANSACTION_ID)
                .data(dataTransferResponseData())
                .build();
    }

    public static CustomerTransferResponse sampleGHATransferResponse(CanonicalErrorCode canonicalErrorCode) {
        return CustomerTransferResponse.builder()
              .statusCode(canonicalErrorCode.getCanonicalCode())
              .statusMessage(canonicalErrorCode.getMessage())
              .transactionId(SAMPLE_TRANSACTION_ID)
              .build();
    }

    public static CustomerTransferResponse sampleGHAPrymoTransferResponse(CanonicalErrorCode canonicalErrorCode) {
        return CustomerTransferResponse.builder()
              .statusCode(canonicalErrorCode.getCanonicalCode())
              .statusMessage(canonicalErrorCode.getMessage())
              .transactionId(SAMPLE_TRANSACTION_ID)
              .data(CustomerTransferResponseData.builder()
                    .notification("Your Airtime request of GHS 0.2 for 0594814807 is being processed. You will receive an alert or dial *170#, My Account then My Approvals to approve.")
                    .build())
              .build();
    }

    public static CustomerTransferResponse sampleGHAPrymoTransactionStatusResponse(CanonicalErrorCode canonicalErrorCode) {
        return CustomerTransferResponse.builder()
              .statusCode(canonicalErrorCode.getCanonicalCode())
              .statusMessage(canonicalErrorCode.getMessage())
              .transactionId(SAMPLE_TRANSACTION_ID)
              .data(CustomerTransferResponseData.builder()
                    .notification("Your Airtime request of GHS 0.2 for 0594814807 is being processed. You will receive an alert or dial *170#, My Account then My Approvals to approve.")
                    .localTransactionId(SAMPLE_TRANSACTION_ID)
                    .build())
              .build();
    }

    public static CustomerTransferResponseData dataTransferResponseData() {
       return CustomerTransferResponseData.builder()
               .valueCharged(4000L)
               .unit("NGN")
               .notification("You have successfully transferred 100MB Data to 2349062058463")
               .productId(PRODUCT_CODE)
               .productType("Data2Share")
               .productName("Share 100MB")
               .build();

    }


    public static ConsentNotificationResponse sampleGiveConsentNotificationResponse() {
        return ConsentNotificationResponse.builder()
                .statusCode(CanonicalErrorCode.OK.getCanonicalCode())
                .msisdn(SAMPLE_INVALID_SENDER_MSISDN)
                .userResponse(SAMPLE_SMS_SHORT_CODE)
                .customData(sampleCustomData)
                .build();
    }

    public static APIResponse<CustomerTransferResponse> sampleDsmTransferAirTimeResponse(CanonicalErrorCode code) {
        APIResponse<CustomerTransferResponse> apiResponse = new APIResponse<>();
        apiResponse.setStatusCode(code.getCanonicalCode());
        apiResponse.setData(sampleCustomerTransferResponse(code));
        return apiResponse;
    }

    public static CustomerTransferResponse sampleCustomerTransferResponse(CanonicalErrorCode code) {

        return CustomerTransferResponse.builder()
                .data(CustomerTransferResponseData.builder()
                        .notification("test")
                        .productId("ShareNSell")
                        .build())
                .statusCode(code.getCanonicalCode())
                .transactionId(SAMPLE_TRANSACTION_ID)
                .statusMessage(code.getMessage())
                .build();
    }

    public static CustomerTransferRequest sampleTransferAirTimeRequest(){
        return CustomerTransferRequest.builder()
                .agentId("SMS")
                .targetSystem("COMVIVA")
                .productCode("603")
                .receiverMsisdn("2349876543211")
                .pin("12345")
                .type("AirTime")
                .transferAmount(new BigDecimal(50))
                .build();
    }


    public static CustomerTransferRequest sampleTransferSmeDataRequest(){
        return CustomerTransferRequest.builder()
                .agentId("SMS")
                .targetSystem("CIS")
                .productCode("603")
                .receiverMsisdn("2349876543211")
                .pin("12345")
                .type("SME_DATA")
                .transferAmount(new BigDecimal(50))
                .build();
    }

    public static TransferRequest sampleAcsDataTransferRequest(){
        return TransferRequest.builder()
                .nodeId("")
                .channelId("ACS")
                .productCode("1234")
                .receiverMsisdn("231345456567")
                .transferAmount(BigDecimal.valueOf(100))
                .targetSystem("ACS")
                .sequenceNo("2436325")
                .transactionId("143664537564867")
                .productId(String.valueOf(1234))
                .type("DATA")
                .build();
    }

    public static TransferResponse sampleAcsTransferResponse(CanonicalErrorCode canonicalErrorCode) {
        return TransferResponse.builder()
                .success(true)
                .message("Successfully Transfered")
                .statusCode(canonicalErrorCode.getCanonicalCode())
                .transactionId(SAMPLE_TRANSACTION_ID)
                .data(acsDataTransferResponse())
                .build();
    }

    public static TransferResponse sampleAcsExceptionTransferResponse(CanonicalErrorCode canonicalErrorCode) {
        return TransferResponse.builder()
                .success(false)
                .message("Internal Server Errror")
                .statusCode(canonicalErrorCode.getCanonicalCode())
                .transactionId(SAMPLE_TRANSACTION_ID)
                .data(null)
                .build();
    }

    public static TransferResponseData acsDataTransferResponse() {
        return TransferResponseData.builder()
                .sender("231706155556")
                .receiver("231886501595")
                .amount(Double.valueOf(101))
                .packageTransferred(packageTransferred())
                .packageReceived(packageReceived())
                .transferExpiry(new Date())
                .build();

    }

    public static PackageTransferred packageTransferred() {
        return PackageTransferred.builder()
                .productName("")
                .cost(Double.valueOf(10))
                .productId(15)
                .categoryId(345)
                .categoryName("Jumbo")
                .description("You have selected $0.35 - 100MB Package valid 24hrs")
                .validityDays(Double.valueOf(1))
                .type("DATA")
                .validityHours(Double.valueOf(24))
                .CostCurrencyObject(costCurrency()).
                build();

    }

    public static PackageReceived packageReceived() {
        return PackageReceived.builder()
                .productName("$0.35-100MB-1Day")
                .cost(Double.valueOf(10))
                .productId(15)
                .categoryId(345)
                .categoryName("Jumbo")
                .description("You have selected $0.35 - 100MB Package valid 24hrs")
                .validityDays(Double.valueOf(1))
                .type("DATA")
                .validityHours(Double.valueOf(24))
                .costCurrency(null)
                .build();

    }

    public static CostCurrency costCurrency() {
        return CostCurrency.builder()
                .usd("0")
                .lrd("0")
                .build();

    }
    public static CustomerTransferRequestWrapper sampleSendConsentRequest() {
        return CustomerTransferRequestWrapper.builder()
                .channelId("3pp")
                .systemEndpoint(consentSystemEndPoint())
                .customerTransferRequest(sampleCustomerTransferRequest())
                .partnerName("3pp")
                .requestPath("http://localhost:1080/customers/{senderMsisdn}/sendConsent")
                .senderMsisdn("SAMPLE_VALID_SENDER_MSISDN")
                .sequenceNo("2534643")
                .transactionId(SAMPLE_TRANSACTION_ID)
                .build();
    }

    public static SystemEndpoint consentSystemEndPoint() {

        SystemEndpoint systemEndpoint = new SystemEndpoint();

        systemEndpoint.setConsentEndpoint("http://localhost:1080/customers/{senderMsisdn}/sendConsent");
        systemEndpoint.setSmsShortCode("2244");
        return systemEndpoint;

    }

    private static CustomerTransferRequest sampleCustomerTransferRequest() {
        return CustomerTransferRequest.builder()
                .agentId("132547")
                .callbackUrl(SAMPLE_CALLBACK_URL)
                .pin("1234")
                .productCode("123")
                .productId(123)
                .receiverMsisdn(SAMPLE_VALID_RECEIVER_MSISDN)
                .targetSystem("CIS")
                .type("AIRTIME")
                .transferAmount(BigDecimal.valueOf(500))
                .build();
    }


}
