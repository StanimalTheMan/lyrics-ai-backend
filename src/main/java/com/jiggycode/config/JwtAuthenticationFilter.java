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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // bypass preflight & public/auth endpoints
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/login")
                || uri.startsWith("/error")
                || uri.startsWith("/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        try {
            String userEmail = jwtService.extractUsername(jwt); // may throw
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    unauthorized(response, "Invalid JWT"); return;
                }
            }
            filterChain.doFilter(request, response);
            return;
        } catch (io.jsonwebtoken.ExpiredJwtException e)   { unauthorized(response, "JWT expired"); return;
        } catch (io.jsonwebtoken.SignatureException e)     { unauthorized(response, "Invalid JWT signature"); return;
        } catch (Exception e)                              { unauthorized(response, "Invalid JWT"); return; }
    }

    private boolean isPublic(String uri) {
        return uri.startsWith("/oauth2/")
                || uri.startsWith("/login")
                || uri.startsWith("/error")
                || uri.startsWith("/actuator/health")
                || uri.startsWith("/public/");
    }

    private void unauthorized(HttpServletResponse res, String msg) throws IOException {
        if (!res.isCommitted()) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"" + msg + "\"}");
        }
        SecurityContextHolder.clearContext();
    }
}
