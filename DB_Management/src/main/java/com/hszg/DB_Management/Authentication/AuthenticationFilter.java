package com.hszg.DB_Management.Authentication;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@Sel
public class AuthenticationFilter extends OncePerRequestFilter {

    private final String serviceAPIToken;

    public AuthenticationFilter(String serviceAPIToken) {
        this.serviceAPIToken = serviceAPIToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestToken = request.getHeader("SERVICE-API-KEY");

        if (serviceAPIToken.equals(requestToken)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("text/plain");
            response.getWriter().write("Invalid or missing API Secret");
        }
    }
}
