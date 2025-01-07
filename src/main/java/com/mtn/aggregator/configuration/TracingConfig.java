package com.mtn.aggregator.configuration;

import brave.baggage.BaggageField;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean(name = "transactionIdTraceField")
    public BaggageField transactionIdTraceField() {
        return BaggageField.create("transactionId");
    }

    @Bean(name = "msisdnTraceField")
    public BaggageField msisdnTraceField() {
        return BaggageField.create("msisdn");
    }

    @Bean(name = "partnerNameTraceField")
    public BaggageField partnerNameTraceField() {
        return BaggageField.create("partnerName");
    }

    @Bean(name = "sequenceNoTraceField")
    public BaggageField sequenceNoTraceField() {
        return BaggageField.create("sequenceNo");
    }
}

