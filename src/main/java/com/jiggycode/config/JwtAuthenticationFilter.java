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
        System.out.println("JWT URI=" + request.getRequestURI() + " Auth=" + request.getHeader("Authorization"));

        // 0) Always let preflight through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1) Skip JWT processing for public / oauth2 endpoints (prevents clashes with Google login)
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) ||
                uri.startsWith("/oauth2/") || uri.startsWith("/login") ||
                uri.startsWith("/error")   || uri.startsWith("/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = auth.substring(7);

        try {
            String email = jwtService.extractUsername(jwt); // may throw
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var user = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(jwt, user)) {
                    var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else { unauthorized(response, "Invalid JWT"); return; }
            }
            filterChain.doFilter(request, response);
            return;
        } catch (io.jsonwebtoken.ExpiredJwtException e) { unauthorized(response, "JWT expired"); return;
        } catch (io.jsonwebtoken.SignatureException e) { unauthorized(response, "Invalid JWT signature"); return;
        } catch (Exception e) { unauthorized(response, "Invalid JWT"); return; }
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

    private void unauthorized(HttpServletResponse res, String msg) throws IOException {
        if (!res.isCommitted()) { res.setStatus(401); res.setContentType("application/json"); res.getWriter().write("{\"error\":\""+msg+"\"}"); }
        SecurityContextHolder.clearContext();
    }
}
