package com.myorg.trading.controller;

import com.myorg.trading.broker.api.MarketDataTick;
import com.myorg.trading.domain.model.SecurityMaster;
import com.myorg.trading.service.marketdata.MarketDataService;
import com.myorg.trading.service.marketdata.SecurityMasterService; // <--- NEW
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity; // <--- NEW
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List; // <--- NEW

@RestController
@RequestMapping("/api/v1/marketdata")
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final SecurityMasterService securityMasterService; // <--- NEW

    public MarketDataController(MarketDataService marketDataService,
                                SecurityMasterService securityMasterService) {
        this.marketDataService = marketDataService;
        this.securityMasterService = securityMasterService;
    }

    // --- NEW SEARCH ENDPOINT ---
    @GetMapping("/search")
    public ResponseEntity<List<SecurityMaster>> searchSymbols(@RequestParam String query) {
        return ResponseEntity.ok(securityMasterService.search(query));
    }

    @GetMapping(value = "/stream/{instrumentToken}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MarketDataTick> stream(@PathVariable String instrumentToken) {
        return marketDataService.streamFor(instrumentToken);
    }
}