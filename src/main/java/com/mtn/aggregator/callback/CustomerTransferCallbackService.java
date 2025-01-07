package com.mtn.aggregator.callback;


import com.mtn.aggregator.callback.cache.CallbackCacheService;
import com.mtn.aggregator.callback.cache.CallbackTransactionCacheItem;
import com.mtn.aggregator.enums.response.CanonicalErrorCode;
import com.mtn.aggregator.exception.CanonicalErrorCodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerTransferCallbackService {
  private final CallbackCacheService callbackCacheService;

  public void saveCallbackForTransaction(String transactionId, String callbackUrl, String countryCode,
                                         String requestPath, String targetSystem) {

    var cacheItem = callbackCacheService.get(countryCode + targetSystem + transactionId);
    if (cacheItem != null) {
      throw CanonicalErrorCodeException.invalidRequestException(CanonicalErrorCode.DUPLICATE_TRANSACTION_ID,
            requestPath, transactionId);
    }
    var cacheEntry = CallbackTransactionCacheItem.builder()
          .callbackUrl(callbackUrl)
          .transactionId(transactionId)
          .targetSystem(targetSystem)
          .countryCode(countryCode)
          .createdAt(LocalDateTime.now())
          .build();
    callbackCacheService.put(countryCode + targetSystem + transactionId, cacheEntry);

    log.info("new cache entry created: [{} : {}]", countryCode + targetSystem + transactionId, cacheEntry);
  }

  public CallbackTransactionCacheItem getExistingCallbackForTransaction(String transactionId, String countryCode,
                                                                        String targetSystem, String requestPath) {

    var cacheKey = countryCode + targetSystem + transactionId;
    log.info("Get cached callbackEndpoint for key : {}", cacheKey);
    var cacheItem = callbackCacheService.get(cacheKey);
    if (cacheItem == null) {
      throw CanonicalErrorCodeException.invalidRequestException(CanonicalErrorCode.CALLBACK_NOT_FOUND, requestPath,
            transactionId);
    }
    return cacheItem;
  }

}
