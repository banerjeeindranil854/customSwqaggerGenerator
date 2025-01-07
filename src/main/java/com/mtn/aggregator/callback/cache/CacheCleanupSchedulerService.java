package com.mtn.aggregator.callback.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheCleanupSchedulerService {

  private final CallbackCacheService callbackCacheService;

  @Scheduled(fixedDelayString = "21600000")//6 hours
  public void clearRedundantTransactions() {

    if(!callbackCacheService.values().isEmpty()) {
      log.info("======== Clearing Token ========");
      var result = callbackCacheService.values().parallelStream()
            .filter(callbackTransactionCacheItem -> callbackTransactionCacheItem.getCreatedAt().isBefore(LocalDateTime.now().minusHours(6)))
            .map(callbackTransactionCacheItem -> {
              log.info("Removing callback with transactionId : {} from cache due to elapsing 6 hours", callbackTransactionCacheItem.getTransactionId());
              callbackCacheService.remove(callbackTransactionCacheItem.getCountryCode() + callbackTransactionCacheItem.getTargetSystem() + callbackTransactionCacheItem.getTransactionId());
              return true;
            })
            .collect(Collectors.toList());

      log.info("{} total transactions with no callback cleared", result.size());
    }

  }
}
