package com.myorg.trading.service.marketdata;

import com.myorg.trading.domain.model.SecurityMaster;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SecurityMasterService {

    private final List<SecurityMaster> securityList = new ArrayList<>();
    // Dhan Open API Scrip Master URL
    private static final String CSV_URL = "https://images.dhan.co/api-data/api-scrip-master.csv";

    @PostConstruct
    public void init() {
        // Load data in a separate thread to not block startup
        new Thread(this::loadSecurityMaster).start();
    }

    private void loadSecurityMaster() {
        long start = System.currentTimeMillis();
        log.info("Starting Security Master Sync from Dhan...");
        try {
            URL url = new URL(CSV_URL);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                reader.readLine(); // Skip Header

                while ((line = reader.readLine()) != null) {
                    // CSV Format: SEM_EXM_EXCH_ID, SEM_SEGMENT, SEM_SMST_SECURITY_ID, SEM_GMT_INSTRUMENT_TYPE, SEM_INSTRUMENT_NAME, SEM_TRADING_SYMBOL, ...
                    String[] cols = line.split(",");
                    if (cols.length < 6) continue;

                    SecurityMaster sm = new SecurityMaster();
                    sm.setExchangeSegment(cols[0].trim()); // NSE/BSE
                    sm.setSecurityId(cols[2].trim());      // Token
                    sm.setInstrumentType(cols[3].trim());  // EQUITY/OPTIDX
                    sm.setName(cols[4].trim());            // RELIANCE
                    sm.setTradingSymbol(cols[5].trim());   // RELIANCE-EQ

                    // Optional parsing for Lot/Tick
                    if (cols.length > 8) {
                        try { sm.setLotSize(Integer.parseInt(cols[8].trim())); } catch (Exception e) { sm.setLotSize(1); }
                    }

                    // Optimization: Only keep NSE Equity & FNO to save memory/search time
                    if (sm.getExchangeSegment().contains("NSE")) {
                        securityList.add(sm);
                    }
                }
            }
            log.info("Security Master Loaded: {} records in {}ms", securityList.size(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Failed to load Security Master", e);
        }
    }

    public List<SecurityMaster> search(String query) {
        if (query == null || query.length() < 2) return List.of();

        String q = query.toUpperCase();

        return securityList.stream()
                .filter(s -> s.getTradingSymbol().contains(q) || s.getName().contains(q))
                // Logic to prioritize exact matches and shorter symbols (usually the main stock)
                .sorted(Comparator.comparingInt((SecurityMaster s) -> s.getTradingSymbol().startsWith(q) ? 0 : 1)
                        .thenComparingInt(s -> s.getTradingSymbol().length()))
                .limit(20)
                .collect(Collectors.toList());
    }
}