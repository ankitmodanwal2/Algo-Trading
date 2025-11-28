package com.myorg.trading.domain.entity;

public enum OrderStatus {
    PENDING,
    PLACED,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    FAILED,
    REJECTED,
    EXECUTED
}
