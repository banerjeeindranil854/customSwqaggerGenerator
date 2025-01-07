package com.mtn.aggregator.callback.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Component
public class CallbackPublisher {

  private final ApplicationEventPublisher publisher;

  public CallbackPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public void publishCallback(final CallbackPublisherDto publisherDto) {
    publisher.publishEvent(publisherDto);
  }


}
