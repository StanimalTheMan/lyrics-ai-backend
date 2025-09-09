package com.jiggycode.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TopLevelExceptionLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(req, res);
        } catch (Exception ex) {
            // Find the root cause
            Throwable root = ex;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }

            // Log once, with clear details
            System.err.println("[TopLevel] " + req.getMethod() + " " + req.getRequestURI()
                    + " -> " + root.getClass().getSimpleName() + ": " + (root.getMessage() == null ? "" : root.getMessage()));

            // Return a single clean JSON error
            if (!res.isCommitted()) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"" + root.getClass().getSimpleName()
                        + ": " + (root.getMessage() == null ? "" : root.getMessage()) + "\"}");
            }
        }
    }
}
