package com.mtn.aggregator.configuration;

import com.mtn.aggregator.models.SystemEndpoint;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties("aggregator")
@Configuration
public class TransferAggregatorProperties {
    private String experienceLayer;
    private List<SystemEndpoint> system;
}