package com.jiggycode.config;

import com.jiggycode.service.JwtService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, @Lazy UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 0) Always let preflight through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1) Skip JWT processing for public / oauth2 endpoints (prevents clashes with Google login)
        String uri = request.getRequestURI();
        if (isPublic(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) Pull Bearer token (if absent, just continue; auth rules may 401 later)
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // 3) Extract subject; if invalid/expired, handle cleanly
            final String userEmail = jwtService.extractUsername(jwt); // may throw if token bad/expired

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Invalid token: clear context and 401 (or just continue—choose one policy)
                    unauthorized(response, "Invalid JWT");
                    return;
                }
            }

            // 4) Continue the chain
            filterChain.doFilter(request, response);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            unauthorized(response, "JWT expired");
        } catch (io.jsonwebtoken.SignatureException e) {
            unauthorized(response, "Invalid JWT signature");
        } catch (Exception e) {
            // Catch-all for other parsing errors
            unauthorized(response, "Invalid JWT");
        }
    }

    private boolean isPublic(String uri) {
        // Make sure these match your security config's permitAll
        return uri.startsWith("/oauth2/")
                || uri.startsWith("/login")
                || uri.startsWith("/error")
                || uri.startsWith("/actuator/health")
                || uri.startsWith("/public/");
        // Add any other non-protected paths here
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + message + "\"}");
        }
        // Do NOT call filterChain after writing the response
        SecurityContextHolder.clearContext();
    }
}
