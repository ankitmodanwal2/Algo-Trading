package com.myorg.trading.controller;

import com.myorg.trading.broker.api.MarketDataTick;
import com.myorg.trading.service.marketdata.MarketDataService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/marketdata")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    /**
     * Subscribe to a server-sent stream of market ticks for an instrument token.
     * The client receives a JSON stream (SSE) of MarketDataTick JSON objects.
     */
    @GetMapping(value = "/stream/{instrumentToken}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MarketDataTick> stream(@PathVariable String instrumentToken) {
        return marketDataService.streamFor(instrumentToken);
    }
}
