package com.mtn.aggregator.callback.events;

import com.mtn.aggregator.callback.cache.CallbackCacheService;
import com.mtn.madapi.commons.webclients.DefaultWebClientHttpService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLException;
import javax.swing.plaf.IconUIResource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@AllArgsConstructor
@Component
public class CallbackSubscriber {

  private final DefaultWebClientHttpService defaultWebClientHttpService;

  private final CallbackCacheService callbackCacheService;

  @Async
  @EventListener
  public void handleAsyncCallbackRegistrationForGHAECW(CallbackPublisherDto event) {

    CompletableFuture.runAsync(() -> {
      try {
        defaultWebClientHttpService.post(event.getCallbackUrl(), event.getRequest(), new HashMap<>(), Map.class, event.getCountryCode())
              .cast(Map.class)
              .doOnNext(response -> log.info("3PP Response for Callback with TransactionId [{}] : {}", event.getTransactionId(), response))
              .onErrorResume(e -> {
                log.error("GHA prymo callback to {} failed for transactionId : {}.", event.getCallbackUrl(), event.getTransactionId(), e);
                return Mono.error(e);
              })
              .doOnNext(result -> callbackCacheService.remove(event.getCountryCode() + event.getTargetSystem() + event.getTransactionId()))
              .block();

      } catch (Exception e) {
        log.error("GHA prymo callback to {} failed for transactionId : {}.", event.getCallbackUrl(), event.getTransactionId(), e);
      }
    });

  }


}
