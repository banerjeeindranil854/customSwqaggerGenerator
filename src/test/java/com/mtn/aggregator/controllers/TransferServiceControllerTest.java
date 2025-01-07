package com.mtn.aggregator.controllers;


import com.mtn.aggregator.TransferService;
import com.mtn.aggregator.helpers.MockExpectationBuilder;
import com.mtn.aggregator.helpers.TestHelpers;
import com.mtn.aggregator.models.request.transfer.CustomerTransferRequest;
import com.mtn.aggregator.models.request.transfer.TransferDsmRequestWrapper;
import com.mtn.aggregator.models.response.CustomerTransferResponse;
import com.mtn.aggregator.models.response.acs.TransferResponse;
import com.mtn.aggregator.services.CustomerTransferService;
import com.mtn.aggregator.services.WebClientHttpService;
import com.mtn.madapi.commons.models.response.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.mtn.aggregator.enums.response.CanonicalErrorCode.*;
import static com.mtn.aggregator.helpers.TestHelpers.*;
import static org.junit.Assert.assertThrows;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TransferService.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@PropertySource("classpath:test.properties")
@Slf4j
public class TransferServiceControllerTest {

    private static ClientAndServer mockServer;

    @Autowired
    private MockExpectationBuilder mockExpectationBuilder;

    @Autowired
    private WebTestClient webTestClient;

    @InjectMocks
    private CustomerTransferService customerTransferService;


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
    public void testGetDataTransferEndpoint() {
        mockExpectationBuilder.resetMockSever(mockServer);

        var dataTransferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "dataTransfer", sampleTransferRequest(), dataTransferResponse);

        // doReturn(Mono.just(dataTransferResponse)).when(customerTransferService).transfer(anyString(), anyString(), any());

        webTestClient
            .post()
            .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN))
            .body(BodyInserters.fromValue(TestHelpers.sampleDataTransferRequest(SAMPLE_VALID_RECEIVER_MSISDN, DATA, null)))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .header("x-authorization-claims",
                "eyJhdWQiOiJtaWNyb2dhdGV3YXkiLCJuYmYiOjE2MTY0MTk4NzQsImRldmVsb3Blcl9lbWFpbCI6Iml5ZW5lbWkudHlnZXIuZXh0ZXJuYWxAYXRvcy5uZXQiLCJpc3MiOiJodHRwczovL210bi1wcm9kLXByb2QuYXBpZ2VlLm5ldC9lZGdlbWljcm8tY3VzdG9tLWF1dGgvdmVyaWZ5QXBpS2V5IiwiYXBwX2N1c3RvbV9hdHRyaWJ1dGVzIjp7IkRpc3BsYXlOYW1lIjoiUGxhbnMgdjIgVHlnZXItTkciLCJOb3RlcyI6IiAiLCJjaGFubmVsSWQiOiJteU10bkFwcCJ9LCJqdGkiOiI4MmEwYTdmOS1hYWViLTQ3MDMtYWJjZi02NDUzNDRkMTM1ZWUifQ==")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CustomerTransferResponse.class)
            .value(response -> {
                Assert.assertNotNull(response);
            });
    }

    @Test
    public void testGetAirtimeTransferEndpoint() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var dataTransferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "dataTransfer", sampleTransferRequest(), dataTransferResponse);

        webTestClient
            .post()
            .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN))
            .body(BodyInserters.fromValue(TestHelpers.sampleDataTransferRequest(SAMPLE_VALID_RECEIVER_MSISDN, AIRTIME, SAMPLE_CALLBACK_URL)))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .header(NODE_ID_HEADER, NODE_ID)
            .exchange()
            .expectStatus()
            .is4xxClientError();


    }

    @Test
    public void testGetAirtimeTransferEndpointWithClaims() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var airtimeTransferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "airtimeTransfer", sampleTransferRequest(), airtimeTransferResponse);

        webTestClient
            .post()
            .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN))
            .body(BodyInserters.fromValue(TestHelpers.sampleDataTransferRequest(SAMPLE_VALID_RECEIVER_MSISDN, AIRTIME, null)))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .header("x-authorization-claims",
                "eyJhdWQiOiJtaWNyb2dhdGV3YXkiLCJuYmYiOjE2MTY0MTk4NzQsImRldmVsb3Blcl9lbWFpbCI6Iml5ZW5lbWkudHlnZXIuZXh0ZXJuYWxAYXRvcy5uZXQiLCJpc3MiOiJodHRwczovL210bi1wcm9kLXByb2QuYXBpZ2VlLm5ldC9lZGdlbWljcm8tY3VzdG9tLWF1dGgvdmVyaWZ5QXBpS2V5IiwiYXBwX2N1c3RvbV9hdHRyaWJ1dGVzIjp7IkRpc3BsYXlOYW1lIjoiUGxhbnMgdjIgVHlnZXItTkciLCJOb3RlcyI6IiAiLCJjaGFubmVsSWQiOiJteU10bkFwcCJ9LCJqdGkiOiI4MmEwYTdmOS1hYWViLTQ3MDMtYWJjZi02NDUzNDRkMTM1ZWUifQ==")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CustomerTransferResponse.class)
            .value(response -> {
                Assert.assertNotNull(response);
            });
    }

    @Test
    public void testGetDataWithInvalidSenderCountryCode() {
        mockExpectationBuilder.resetMockSever(mockServer);

        var dataTransferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "dataTransfer", sampleTransferRequest(), dataTransferResponse);

        webTestClient
            .post()
            .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_INVALID_SENDER_MSISDN_COUNTRY_CODE))
            .body(BodyInserters.fromValue(TestHelpers.sampleDataTransferRequest(SAMPLE_VALID_RECEIVER_MSISDN, DATA, null)))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .header(NODE_ID_HEADER, NODE_ID)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testGetDataTransferInvalidSenderMsisdn() {
        mockExpectationBuilder.resetMockSever(mockServer);

        var dataTransferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "dataTransfer", sampleTransferRequest(), dataTransferResponse);

        webTestClient
            .post()
            .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_INVALID_SENDER_MSISDN))
            .body(BodyInserters.fromValue(TestHelpers.sampleDataTransferRequest(SAMPLE_VALID_RECEIVER_MSISDN, DATA, null)))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .header(NODE_ID_HEADER, NODE_ID)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testGetDataTransferInvalidReceiverMsisdn() {
        mockExpectationBuilder.resetMockSever(mockServer);

        var dataTransferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "dataTransfer", sampleTransferRequest(), dataTransferResponse);

        webTestClient
            .post()
            .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN))
            .body(BodyInserters.fromValue(TestHelpers.sampleDataTransferRequest(SAMPLE_INVALID_RECEIVER_MSISDN, DATA, null)))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .header(NODE_ID_HEADER, NODE_ID)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testGetDataTransferInvalidReceiverCountryCode() {
        mockExpectationBuilder.resetMockSever(mockServer);

        var dataTransferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "dataTransfer", sampleTransferRequest(), dataTransferResponse);

        webTestClient
            .post()
            .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN))
            .body(BodyInserters.fromValue(TestHelpers.sampleDataTransferRequest(SAMPLE_INVALID_RECEIVER_MSISDN_COUNTRY_CODE, DATA, null)))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .header(NODE_ID_HEADER, NODE_ID)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    public void testGetDataTransferInValidType() {
        mockExpectationBuilder.resetMockSever(mockServer);

        var dataTransferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "dataTransfer", sampleTransferRequest(), dataTransferResponse);

        webTestClient
            .post()
            .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN))
            .body(BodyInserters.fromValue(TestHelpers.sampleDataTransferRequest(SAMPLE_VALID_RECEIVER_MSISDN, "invalid", null)))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .header(NODE_ID_HEADER, NODE_ID)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }


    @Test
    public void testHandleConsentNotification() {
        mockExpectationBuilder.resetMockSever(mockServer);

        var transferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "dataTransfer", transferResponse);


        webTestClient
            .post()
            .uri("/notification")
            .body(BodyInserters.fromValue(sampleGiveConsentNotificationResponse()))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .exchange()
            .expectStatus()
            .isCreated();
    }

    @Test
    public void testGetSMEDataEndpoint() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var dataTransferResponse = sampleTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "smeData", sampleTransferRequest(), dataTransferResponse);

        webTestClient
                .post()
                .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN))
                .body(BodyInserters.fromValue(TestHelpers.sampleDataTransferRequest(SAMPLE_VALID_RECEIVER_MSISDN, SME_DATA, SAMPLE_CALLBACK_URL)))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .header(NODE_ID_HEADER, NODE_ID)
                .exchange()
                .expectStatus()
                .is4xxClientError();


    }

    @Test
    public void testDsmAirtimeTransferEndpoint() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var airtimeTransferResponse = sampleDsmTransferAirTimeResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "airtimeTransfer", sampleTransferAirTimeRequest(), airtimeTransferResponse);

        webTestClient
                .post()
                .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN))
                .body(BodyInserters.fromValue(sampleTransferAirTimeRequest()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .header("TransactionId", SAMPLE_TRANSACTION_ID)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(APIResponse.class)
                .value(Assert::assertNotNull);


    }

    @Test
    public void testDataTransferEndpoint() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var airtimeTransferResponse = sampleDsmTransferAirTimeResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "smeData", sampleTransferSmeDataRequest(), airtimeTransferResponse);
        mockExpectationBuilder.addExpectation(mockServer, "smeData", sampleTransferSmeDataRequest(), airtimeTransferResponse);

        webTestClient
                .post()
                .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN))
                .body(BodyInserters.fromValue(sampleTransferSmeDataRequest()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .header("TransactionId", SAMPLE_TRANSACTION_ID)
                .header("x-authorization-claims",
                        "eyJhdWQiOiJtaWNyb2dhdGV3YXkiLCJuYmYiOjE2MTY0MTk4NzQsImRldmVsb3Blcl9lbWFpbCI6Iml5ZW5lbWkudHlnZXIuZXh0ZXJuYWxAYXRvcy5uZXQiLCJpc3MiOiJodHRwczovL210bi1wcm9kLXByb2QuYXBpZ2VlLm5ldC9lZGdlbWljcm8tY3VzdG9tLWF1dGgvdmVyaWZ5QXBpS2V5IiwiYXBwX2N1c3RvbV9hdHRyaWJ1dGVzIjp7IkRpc3BsYXlOYW1lIjoiUGxhbnMgdjIgVHlnZXItTkciLCJOb3RlcyI6IiAiLCJjaGFubmVsSWQiOiJteU10bkFwcCJ9LCJqdGkiOiI4MmEwYTdmOS1hYWViLTQ3MDMtYWJjZi02NDUzNDRkMTM1ZWUifQ==")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(APIResponse.class)
                .value(Assert::assertNotNull);


    }

    @Test
    public void testAcsDataTransferEndpoint() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var acsDataTransferResponse = sampleAcsTransferResponse(OK);

        mockExpectationBuilder.addExpectation(mockServer, "acsTransfer", sampleAcsDataTransferRequest(), acsDataTransferResponse);

        webTestClient
                .post()
                .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN_LBR))
                .body(BodyInserters.fromValue(sampleAcsDataTransferRequest()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .header("TransactionId", SAMPLE_TRANSACTION_ID)
                .header("x-country-code","LBR")
                .header("x-origin-channelid","3pp")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TransferResponse.class)
                .value(Assert::assertNotNull);


    }


    @Test
    public void testAcsDataTransferExceptionEndpoint() {

        mockExpectationBuilder.resetMockSever(mockServer);

        var acsDataTransferResponse = sampleAcsExceptionTransferResponse(BAD_REQUEST);

        mockExpectationBuilder.addExpectation(mockServer, "acsTransfer", sampleAcsDataTransferRequest(), acsDataTransferResponse);

        webTestClient
                .post()
                .uri(String.format(TRANSFER_ENDPOINT, SAMPLE_VALID_SENDER_MSISDN_LBR))
                .body(BodyInserters.fromValue(sampleAcsDataTransferRequest()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .header("TransactionId", SAMPLE_TRANSACTION_ID)
                .header("x-country-code","LBR")
                .header("x-origin-channelid","3pp")
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(TransferResponse.class)
                .value(Assert::assertNotNull);


    }

    @Test
    public void testSendConsent() {

        assertThrows(NullPointerException.class, () -> customerTransferService.sendConsent(TestHelpers.sampleSendConsentRequest(), TestHelpers.SAMPLE_NOTIFICATION_URL));

    }

    @Test
    public  void testNotSendConsentResponse(){

        CustomerTransferResponse customerTransferResponse = customerTransferService.consentNotSentResponse("341324657648");
        Assert.assertNotNull(customerTransferResponse);
    }

    @Test
    public void testAgentIdExceptionComviva() {

        Mono<CustomerTransferResponse> customerTransferResponseMono = customerTransferService.transferAirTimeComviva(TransferDsmRequestWrapper
                .builder()
                .transferRequest(CustomerTransferRequest.builder().agentId(" ").build())
                .build());
        Assert.assertNotNull(customerTransferResponseMono);
    }

    @Test
    public void testPinExceptionComviva() {

        Mono<CustomerTransferResponse> customerTransferResponseMono = customerTransferService.transferAirTimeComviva(TransferDsmRequestWrapper
                .builder()
                .transferRequest(CustomerTransferRequest.builder().agentId("346537467")
                        .pin(" ").build())
                .build());
        Assert.assertNotNull(customerTransferResponseMono);
    }

    @Test
    public void testTransferAmountExceptionComviva() {

        Mono<CustomerTransferResponse> customerTransferResponseMono = customerTransferService.transferAirTimeComviva(TransferDsmRequestWrapper
                .builder()
                .transferRequest(CustomerTransferRequest.builder().agentId("346537467")
                        .pin(" 1234")
                        .transferAmount(BigDecimal.valueOf(-1.0))
                        .build())
                .build());
        Assert.assertNotNull(customerTransferResponseMono);
    }

    @Test
    public void testTransactionIdExceptionComviva() {

        Mono<CustomerTransferResponse> customerTransferResponseMono = customerTransferService.transferAirTimeComviva(TransferDsmRequestWrapper
                .builder()
                .transferRequest(CustomerTransferRequest.builder().agentId("346537467")
                        .pin(" 1234")
                        .transferAmount(BigDecimal.valueOf(100))
                        .build())
                        .transactionId("257575328587686597261965186540816280160601845162")
                .build());
        Assert.assertNotNull(customerTransferResponseMono);
    }

    @Test
    public void testTransactionIdNumericExceptionComviva() {

        Mono<CustomerTransferResponse> customerTransferResponseMono = customerTransferService.transferAirTimeComviva(TransferDsmRequestWrapper
                .builder()
                .transferRequest(CustomerTransferRequest.builder().agentId("346537467")
                        .pin(" 1234")
                        .transferAmount(BigDecimal.valueOf(100))
                        .build())
                .transactionId("hsfjhdhfakdjabd")
                .build());
        Assert.assertNotNull(customerTransferResponseMono);
    }


}
