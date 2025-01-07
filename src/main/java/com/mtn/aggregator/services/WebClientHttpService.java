package com.mtn.aggregator.services;


import brave.baggage.BaggageField;
import com.mtn.aggregator.models.response.APIResponse;
import com.mtn.aggregator.models.response.ConsentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebClientHttpService {

    @Autowired
    private final WebClient webClient;

    private final BaggageField partnerNameTraceField;
    private final BaggageField sequenceNoTraceField;
    private final BaggageField transactionIdTraceField;

    private final String PARTNER_NAME_HEADER_LABEL = "partnerName";
    private final String TRANSACTION_ID_HEADER_LABEL = "transactionId";
    private final String SEQUENCE_NO_HEADER_LABEL = "sequenceNo";

    public Mono<? extends APIResponse> post(String endpoint,Object request, Class<? extends APIResponse> responseClass, Map<String,String> headers) {

        return webClient
                .post()
                .uri(endpoint)
                .body(BodyInserters.fromValue(request))
                .headers(httpHeaders -> {
                    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                    if (null != headers) {
                        headers.keySet().forEach((key) -> {
                            log.info("{} : {}", key,headers.get(key));
                            httpHeaders.add(key, (String) headers.get(key));
                        });
                    }
                })
                .retrieve()
                .bodyToMono(responseClass);
    }

    public Mono<ConsentResponse> post(String endpoint, Object request,Map<String,String> headers) {

        return webClient
                .post()
                .uri(endpoint)
                .body(BodyInserters.fromValue(request))
                .headers(httpHeaders -> {
                    httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    if (null != headers) {
                        headers.keySet().forEach((key) -> {
                            httpHeaders.add(key, (String) headers.get(key));
                        });
                    }
                })
                .retrieve()
                .bodyToMono(ConsentResponse.class);
    }

    Mono<Void> postVoid(String endpoint, Object request,Map<String, String> headers) {

        return webClient
                .post()
                .uri(endpoint)
                .body(BodyInserters.fromValue(request))
                .headers(httpHeaders -> {
                    httpHeaders.add(PARTNER_NAME_HEADER_LABEL,partnerNameTraceField.getValue());
                    httpHeaders.add(TRANSACTION_ID_HEADER_LABEL,transactionIdTraceField.getValue());
                    httpHeaders.add(SEQUENCE_NO_HEADER_LABEL,sequenceNoTraceField.getValue());
                    if (null != headers) {
                        headers.keySet().forEach((key) -> {
                            httpHeaders.add(key, (String) headers.get(key));
                        });
                    }
                })
                .retrieve()
                .bodyToMono(Void.class);
    }
    private Consumer<HttpHeaders> generateHttpHeaders() {
        return consumerHttpHeader -> consumerHttpHeader.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }
}
