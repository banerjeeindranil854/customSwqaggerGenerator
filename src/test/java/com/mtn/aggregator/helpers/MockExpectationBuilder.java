package com.mtn.aggregator.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mtn.aggregator.models.MockExpectation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.mockserver.client.ForwardChainExpectation;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockserver.model.HttpError.error;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.XmlBody.xml;

@Service
@Slf4j
public class MockExpectationBuilder {

    private static List<MockExpectation> mockExpectations;
    private final ResourceLoader resourceLoader;
    private final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    @Autowired
    public MockExpectationBuilder(@Qualifier("webApplicationContext") ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        parseExpectationsFile();
    }

    private void parseExpectationsFile() {
        try {
            Resource resource = resourceLoader.getResource("classpath:MockExpectations.json");
            String expectationsJson = IOUtils.toString(resource.getInputStream(), "UTF-8");
            ObjectMapper objectMapper = new ObjectMapper();
            CollectionType expectationsListType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, MockExpectation.class);
            mockExpectations = objectMapper.readValue(expectationsJson, expectationsListType);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private String loadFile(String filename) {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + filename);
            return IOUtils.toString(resource.getInputStream(), "UTF-8");
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return "";
    }

    public void resetMockSever(ClientAndServer mockServer) {
        mockServer.reset();
    }

    public <T> boolean addExpectation(ClientAndServer mockServer, String testName, T responseObject) {
        try {
            Optional<MockExpectation> foundMockExpectation = mockExpectations.stream().filter(exp -> exp.getTestName().equalsIgnoreCase(testName)).findFirst();

            if (foundMockExpectation.isPresent()) {
                MockExpectation mockExpectation = foundMockExpectation.get();

                String contentType = mockExpectation.getContentType();
                String responseContentType = "";

                BodyWithContentType responseContent = null;
                switch (contentType.toLowerCase()) {
                    case "json":
                        responseContent = new JsonBody(new ObjectMapper().writeValueAsString(responseObject), MatchType.STRICT);
                        responseContentType = "application/json";
                        break;
                    case "soap":
                        responseContent = new XmlBody(new XmlMapper().writeValueAsString(responseObject));
                        responseContentType = "text/xml";
                }

                mockServer.when(
                        request()
                                .withPath(mockExpectation.getPath())
                                .withMethod(mockExpectation.getMethod().toUpperCase())
                )
                        .respond(
                                response()
                                        .withDelay(TimeUnit.MILLISECONDS, mockExpectation.getDelay())
                                        .withStatusCode(mockExpectation.getStatus())
                                        .withBody(responseContent)
                                        .withHeader("Content-Type", responseContentType)
                        );
                return true;
            }
        } catch (Exception ex) {

        }

        return false;
    }

    public <R, T> boolean addExpectation(
            ClientAndServer mockServer, String testName, R requestObject, T responseObject) {
        try {
            Optional<MockExpectation> foundMockExpectation =
                    mockExpectations.stream()
                            .filter(exp -> exp.getTestName().equalsIgnoreCase(testName))
                            .findFirst();

            if (foundMockExpectation.isPresent()) {
                MockExpectation mockExpectation = foundMockExpectation.get();

                String contentType = mockExpectation.getContentType();
                String responseContentType = "";
                String requestContentType = "";
                Body responseContent = null;
                Body requestContent = null;
                switch (contentType.toLowerCase()) {
                    case "json":
                        responseContent =
                                json(new ObjectMapper().writeValueAsString(responseObject), MatchType.STRICT);
                        responseContentType = "application/json";
                        break;
                    case "form-urlencoded":
                        responseContent =
                                json(new ObjectMapper().writeValueAsString(responseObject), MatchType.STRICT);
                        responseContentType = "application/json";
                        requestContentType = "application/x-www-form-urlencoded;charset=UTF-8";
                        break;
                    case "soap":
                        responseContent = xml(new XmlMapper().writeValueAsString(responseObject));
                        responseContentType = "text/html";
                        break;
                    case "xml-rpc":
                        StringBuilder bodyContent = new StringBuilder(XML_HEADER);
                        bodyContent.append(loadFile(requestObject.toString()).replaceAll("\\s+", ""));
                        requestContent = xml(bodyContent.toString());
                        requestContentType = "text/xml";

                        if (responseObject != null) {
                            bodyContent = new StringBuilder(XML_HEADER);
                            bodyContent.append(loadFile(responseObject.toString()).replaceAll("\\s+", ""));
                            responseContent = xml(bodyContent.toString());
                            responseContentType = "text/xml";
                        }
                }

                if (contentType.toLowerCase().equals("xml-rpc")) {
                    ForwardChainExpectation expectation =
                            mockServer.when(
                                    request()
                                            .withPath(mockExpectation.getPath())
                                            .withMethod(mockExpectation.getMethod().toUpperCase())
                                            .withBody(requestContent)
                                            .withHeader("Content-Type", requestContentType));
                    if (responseContent != null) {
                        expectation.respond(
                                response()
                                        .withDelay(TimeUnit.MILLISECONDS, mockExpectation.getDelay())
                                        .withStatusCode(mockExpectation.getStatus())
                                        .withBody(responseContent.toString())
                                        .withHeader("Content-Type", responseContentType));
                    } else {
                        expectation.error(error().withDropConnection(true));
                    }
                }
                else if (contentType.toLowerCase().equals("form-urlencoded")) {
                    List<Parameter> parameters = new ArrayList<>();
                    for(Object key : ((Map)requestObject).keySet()){
                        Parameter parameter = param(key.toString(), ((Map)requestObject).get(key).toString());
                        parameters.add(parameter);
                    }
                    ForwardChainExpectation expectation =
                            mockServer.when(
                                    request()
                                            .withPath(mockExpectation.getPath())
                                            .withMethod(mockExpectation.getMethod().toUpperCase())
                                            .withBody(
                                                    params(
                                                            parameters
                                                    )
                                            )
                                            .withHeader("Content-Type", requestContentType));
                    if (responseContent != null) {
                        expectation.respond(
                                response()
                                        .withDelay(TimeUnit.MILLISECONDS, mockExpectation.getDelay())
                                        .withStatusCode(mockExpectation.getStatus())
                                        .withBody(responseContent.toString())
                                        .withHeader("Content-Type", responseContentType));
                    } else {
                        expectation.error(error().withDropConnection(true));
                    }
                }
                else {
                    mockServer
                            .when(
                                    request()
                                            .withPath(mockExpectation.getPath())
                                            .withMethod(mockExpectation.getMethod().toUpperCase()))
                            .respond(
                                    response()
                                            .withDelay(TimeUnit.MILLISECONDS, mockExpectation.getDelay())
                                            .withStatusCode(mockExpectation.getStatus())
                                            .withBody(responseContent.toString())
                                            .withHeader("Content-Type", responseContentType));
                }

                return true;
            }
        } catch (Exception ex) {

        }

        return false;
    }

}
