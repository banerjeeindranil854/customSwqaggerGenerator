package com.mtn.aggregator.helpers;

import com.mtn.aggregator.configuration.TransferAggregatorProperties;
import com.mtn.aggregator.models.SystemEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Slf4j
public class MsisdnHelper {

  private MsisdnHelper() {
  }

  public static boolean isValidCountryNumber(String msisdn, String countryCallingCode) {
    return (!StringUtils.isEmpty(msisdn) && msisdn.startsWith(countryCallingCode));
  }

  public static Optional<SystemEndpoint> getSystemEndpoint(String customerId, TransferAggregatorProperties properties, String targetSystem) {

    if (StringUtils.isNotBlank(targetSystem)) {
      return properties.getSystem().stream()
            .filter(sysEndpoint ->
                  MsisdnHelper.isValidCountryNumber(customerId, sysEndpoint.getCountryCode()) &&
                        sysEndpoint.getSystemId().getName().equalsIgnoreCase(targetSystem)
            ).findFirst();
    } else {
      return properties.getSystem().stream()
            .filter(sysEndpoint ->
                  MsisdnHelper.isValidCountryNumber(customerId, sysEndpoint.getCountryCode())
            ).findFirst();
    }
  }

  public static Optional<SystemEndpoint> getSystemEndpoint(String customerId, TransferAggregatorProperties properties) {

    return properties.getSystem().stream()
          .filter(sysEndpoint ->
                MsisdnHelper.isValidCountryNumber(customerId, sysEndpoint.getCountryCode())
          ).findFirst();
  }

  public static boolean isMsisdnValid(String msisdn, int msisdnLength) {
    return (msisdn.length() != msisdnLength || StringUtils.isEmpty(msisdn) || !StringUtils.isNumeric(msisdn));
  }

}
