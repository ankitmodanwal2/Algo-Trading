package com.myorg.trading.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return method.equalsIgnoreCase("OPTIONS") ||
                path.startsWith("/ws") ||
                path.startsWith("/api/v1/auth") ||
                path.startsWith("/api/v1/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();

        logger.debug("Processing request: {} {}", request.getMethod(), requestURI);

        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                // Extract username from token
                String username = jwtUtil.getUsername(token);
                logger.debug("Extracted username from token: {}", username);

                // Only authenticate if not already authenticated
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Validate token
                    try {
                        jwtUtil.validateToken(token);

                        // Token is valid, set authentication
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        logger.debug("✅ Authentication successful for user: {} on path: {}", username, requestURI);
                    } catch (Exception validationEx) {
                        logger.warn("⚠️ Token validation failed for user {}: {}", username, validationEx.getMessage());
                    }
                }
            } catch (Exception ex) {
                logger.error("❌ Error processing JWT for {}: {}", requestURI, ex.getMessage());
            }
        } else if (!shouldNotFilter(request)) {
            logger.debug("No Authorization header found for protected route: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }
}