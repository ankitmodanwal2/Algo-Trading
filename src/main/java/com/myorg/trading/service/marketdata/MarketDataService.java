package com.myorg.trading.service.marketdata;

import com.myorg.trading.broker.api.MarketDataTick;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Market data router: adapters can push ticks into the service; consumers can subscribe by token.
 * For production use, integrate adapters' WebSocket streams into this service and keep resource control.
 */
@Service
public class MarketDataService {

    private final Map<String, Sinks.Many<MarketDataTick>> registry = new ConcurrentHashMap<>();

    public Flux<MarketDataTick> streamFor(String instrumentToken) {
        Sinks.Many<MarketDataTick> sink = registry.computeIfAbsent(instrumentToken, k -> Sinks.many().multicast().onBackpressureBuffer());
        return sink.asFlux();
    }

    /**
     * Called by adapter when it receives a tick from broker streaming API.
     */
    public void pushTick(String instrumentToken, MarketDataTick tick) {
        Sinks.Many<MarketDataTick> sink = registry.computeIfAbsent(instrumentToken, k -> Sinks.many().multicast().onBackpressureBuffer());
        sink.tryEmitNext(tick);
    }
}
