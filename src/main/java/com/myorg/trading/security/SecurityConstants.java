package com.myorg.trading.security;

public final class SecurityConstants {
    private SecurityConstants() {}

    public static final String AUTH_BASE = "/api/v1/auth";
    public static final String LOGIN = AUTH_BASE + "/login";
    public static final String REGISTER = AUTH_BASE + "/register";
    public static final String SWAGGER = "/swagger-ui/**";
}
