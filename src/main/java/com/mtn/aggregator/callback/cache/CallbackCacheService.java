package com.mtn.aggregator.callback.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class CallbackCacheService extends ConcurrentHashMap<String, CallbackTransactionCacheItem> {}
