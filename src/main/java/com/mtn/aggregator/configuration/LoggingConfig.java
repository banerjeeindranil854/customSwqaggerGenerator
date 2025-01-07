package com.mtn.aggregator.configuration;

import brave.baggage.BaggageField;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import ch.qos.logback.access.tomcat.LogbackValve;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LoggingConfig {

    private final BaggageField msisdnTraceField;
    private final BaggageField transactionIdTraceField;
    private final BaggageField partnerNameTraceField;
    private final BaggageField sequenceNoTraceField;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> accessLogsCustomizer() {
        return factory -> {
            var logbackValve = new LogbackValve();
            logbackValve.setFilename("logback-access.xml");
            logbackValve.setAsyncSupported(true);
            logbackValve.setQuiet(true);
            factory.addContextValves(logbackValve);
        };
    }

    @Bean
    public CurrentTraceContext.ScopeDecorator mdcScopeDecorator() {
        return MDCScopeDecorator.newBuilder()
                .clear()
                .add(
                        CorrelationScopeConfig.SingleCorrelationField.newBuilder(transactionIdTraceField)
                                .flushOnUpdate()
                                .build())
                .add(
                        CorrelationScopeConfig.SingleCorrelationField.newBuilder(msisdnTraceField)
                                .flushOnUpdate()
                                .build())
                .add(
                        CorrelationScopeConfig.SingleCorrelationField.newBuilder(partnerNameTraceField)
                                .flushOnUpdate()
                                .build())
                .add(
                        CorrelationScopeConfig.SingleCorrelationField.newBuilder(sequenceNoTraceField)
                                .flushOnUpdate()
                                .build())
                .build();
    }
}
