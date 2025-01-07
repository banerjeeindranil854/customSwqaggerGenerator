package com.mtn.aggregator.callback.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallbackTransactionCacheItem {


    private String callbackUrl;
    private String transactionId;
    private String targetSystem;
    private String countryCode;
    private LocalDateTime createdAt;
}
