package com.mtn.aggregator.callback.events;

import com.mtn.aggregator.callback.controller.PrimoCallbackRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallbackPublisherDto {

  private PrimoCallbackRequest request;
  private String transactionId;
  private String countryCode;
  private String callbackUrl;
  private String targetSystem;

}
