package com.mtn.aggregator.helpers.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtn.aggregator.enums.RequestType;
import com.mtn.aggregator.models.logging.RequestLogItem;
import com.mtn.aggregator.models.response.APIResponse;
import net.logstash.logback.marker.Markers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class LoggingHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${logging.config:#{null}}")
    private String loggingConfig;


    public void logRequestObject(Logger logger,
                                 String senderMsisdn,
                                 RequestType requestType,
                                 Object customerTransferRequest, String nodeId, String partnerName) {
        logger.info("{} Aggregator :: start", requestType.getValue());

        var logItem =  new RequestLogItem();
        logItem.setOperation(requestType);
        logItem.setSenderMsisdn(senderMsisdn);
        logItem.setCustomerTransferRequest(customerTransferRequest);
        logItem.setNodeId(nodeId);
        logItem.setPartnerName(partnerName);

        requestObject(
                logger,
                logItem
        );
    }

    public void logRequestObject(Logger logger,
                                 RequestLogItem logItem) {
        logger.info("{} Aggregator :: start", logItem.getOperation());

        requestObject(
                logger,
                logItem
        );
    }

    public void logResponseObject(Logger logger, RequestType requestType, APIResponse apiResponse) {
        responseObject(logger, apiResponse);
        logger.info("{} Aggregator :: end", requestType.getValue());
    }

    public void responseObject(Logger logger, Object object) {
        String x = "{}";
        try {
            x = objectMapper.writeValueAsString(object);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        if (StringUtils.isBlank(loggingConfig)) {
            logger.info("{ \"responseObject\" : {} }", x);
        } else {
            logger.info(Markers.appendRaw("responseObject", x), null);
        }

    }

    public void requestObject(Logger logger, Object object) {
        String x = "{}";
        try {
            x = objectMapper.writeValueAsString(object);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        if (StringUtils.isBlank(loggingConfig)) {
            logger.info("{ \"requestObject\" : {} }", x);
        } else {
            logger.info(Markers.appendRaw("requestObject", x), null);
        }
    }

}