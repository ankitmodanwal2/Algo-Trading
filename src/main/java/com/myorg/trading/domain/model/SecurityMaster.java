package com.myorg.trading.domain.model;

import lombok.Data;

@Data
public class SecurityMaster {
    private String securityId;
    private String tradingSymbol;
    private String name;
    private String exchangeSegment; // e.g. NSE, BSE
    private String instrumentType;  // e.g. EQUITY, FNO
    private Double tickSize;
    private Integer lotSize;
}