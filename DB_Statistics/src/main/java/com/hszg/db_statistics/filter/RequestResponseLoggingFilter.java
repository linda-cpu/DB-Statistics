package com.hszg.db_statistics.filter; // Dein Package anpassen

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j // 1. Erstellt automatisch eine "log" Variable für dich [cite: 78]
@Order(1) // 2. Legt die Reihenfolge fest (ganz am Anfang) [cite: 79]
@Component // 3. Macht es zu einer Spring Bean, damit es automatisch läuft [cite: 80]
public class RequestResponseLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        log.info("Logging Request   : {} {}", req.getMethod(), req.getRequestURI());
        chain.doFilter(request, response);
        log.info("Logging Response  : {} ({})", res.getContentType(), res.getStatus());
    }
}