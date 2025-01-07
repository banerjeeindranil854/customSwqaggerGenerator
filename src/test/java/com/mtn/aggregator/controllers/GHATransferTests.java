package com.mtn.aggregator.controllers;


import com.mtn.aggregator.TransferService;
import com.mtn.aggregator.callback.controller.PrimoCallbackRequest;
import com.mtn.aggregator.helpers.MockExpectationBuilder;
import com.mtn.aggregator.helpers.TestHelpers;
import com.mtn.aggregator.models.response.CustomerTransferResponse;
import com.mtn.madapi.commons.models.response.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.HashMap;
import java.util.Map;

import static com.mtn.aggregator.enums.response.CanonicalErrorCode.BAD_REQUEST;
import static com.mtn.aggregator.enums.response.CanonicalErrorCode.OK;
import static com.mtn.aggregator.helpers.TestHelpers.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TransferService.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@PropertySource("classpath:test.properties")
@Slf4j
public class GHATransferTests {

    private static ClientAndServer mockServer;

    @Autowired
    private MockExpectationBuilder mockExpectationBuilder;

    @Autowired
    private WebTestClient webTestClient;


    @BeforeClass
    public static void startServer() {
        try {
            mockServer = startClientAndServer(1080);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @AfterClass
    public static void stopServer() {
        if (null != mockServer)
            mockServer.stop();
    }




    @Test
    public void testGetAirtimeTransferAirSystemEndpointGHA() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var airtimeTransferResponse = sampleGHATransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "airtimeTransferGHA", sampleTransferRequestGHA(), airtimeTransferResponse);

        webTestClient
              .post()
              .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN_GHA))
              .body(BodyInserters.fromValue(TestHelpers.sampleTransferRequestGHA()))
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
              .header("x-country-code", "GHA")
              .exchange()
              .expectStatus()
              .is2xxSuccessful();


    }


    @Test
    public void testGetAirtimeTransferAirSystemEndpointGHAWithInvalidAmount() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var airtimeTransferResponse = sampleGHATransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "airtimeTransferGHA", sampleInvalidAmountTransferRequestGHA(), airtimeTransferResponse);

        webTestClient
              .post()
              .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN_GHA))
              .body(BodyInserters.fromValue(TestHelpers.sampleInvalidAmountTransferRequestGHA()))
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
              .header("x-country-code", "GHA")
              .exchange()
              .expectStatus()
              .is4xxClientError();


    }


    @Test
    public void testGetAirtimeTransferPrymoSystemEndpointGHA() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var airtimeTransferResponse = sampleGHAPrymoTransferResponse(BAD_REQUEST);

        var airtimeRequest = TestHelpers.samplePrymoTransferRequestGHA();
        var transactionId = "23456789";

        var callbackRequest = PrimoCallbackRequest.builder()
              .transactionId(transactionId)
              .status("OK")
              .localTransactionCode("23456789")
              .message("Transaction state is COMPLETED")
              .transactionState("COMPLETED")
              .statusCode("00")
              .build();


        mockExpectationBuilder.addExpectation(mockServer, "airtimeTransferGHA", airtimeRequest, airtimeTransferResponse);
        Map<String, String> callbackMockResponse = new HashMap<>();
        callbackMockResponse.put("status", "Callback received successfully");
        mockExpectationBuilder.addExpectation(mockServer, "airtimeTransferGHACallback", callbackRequest, callbackMockResponse);

        webTestClient
              .post()
              .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN_GHA))
              .body(BodyInserters.fromValue(airtimeRequest))
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
              .header("x-country-code", "GHA")
              .header("transactionId", transactionId)
              .exchange()
              .expectStatus()
              .is4xxClientError();


        webTestClient
              .post()
              .uri("/customers/prymo/callback")
              .body(BodyInserters.fromValue(callbackRequest))
              .accept(MediaType.APPLICATION_JSON)
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
              .header("x-country-code", "GHA")
              .exchange()
              .expectStatus()
              .is2xxSuccessful();

    }

    @Test
    public void testGetAirtimeTransferPrymoSystemEndpointInvalidCallbackGHA() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var airtimeTransferResponse = sampleGHAPrymoTransferResponse(OK);

        var airtimeRequest = TestHelpers.samplePrymoTransferRequestGHA();
        var transactionId = "23456789";
        airtimeRequest.setCallbackUrl(null);

        mockExpectationBuilder.addExpectation(mockServer, "airtimeTransferGHA", airtimeRequest, airtimeTransferResponse);

        webTestClient
              .post()
              .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN_GHA))
              .body(BodyInserters.fromValue(airtimeRequest))
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
              .header("x-country-code", "GHA")
              .header("transactionId", transactionId)
              .exchange()
              .expectStatus()
              .is4xxClientError();


    }

    @Test
    public void testGetAirtimeTransferTransactionStatus() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var airtimeTransferResponse = sampleGHAPrymoTransactionStatusResponse(OK);

        var transactionId = "1234567890";

        mockExpectationBuilder.addExpectation(mockServer, "transactionStatus", null, airtimeTransferResponse);

        webTestClient
              .get()
              .uri("/customers/transactionStatus?transactionId=1234567890&operation=Momo&network=MTN")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
              .header("targetSystem", "PRYMO")
              .header("x-country-code", "GHA")
              .header("transactionId", transactionId)
              .exchange()
              .expectStatus()
              .isOk();

    }

    @Test
    public void testGetAirtimeTransferTransactionStatusNo_SystemEndpoint() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var airtimeTransferResponse = sampleGHAPrymoTransactionStatusResponse(OK);

        var transactionId = "1234567890";

        mockExpectationBuilder.addExpectation(mockServer, "transactionStatus", null, airtimeTransferResponse);

        webTestClient
              .get()
              .uri("/customers/transactionStatus?transactionId=1234567890&operation=Momo&network=MTN&targetSystem=PR")
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
              .header("x-country-code", "GHA")
              .header("transactionId", transactionId)
              .exchange()
              .expectStatus()
              .is5xxServerError();

    }

}
