package com.jiggycode.controller;

import com.jiggycode.config.CustomUserDetails;
import com.jiggycode.entity.Highlight;
import com.jiggycode.entity.UserSong;
import com.jiggycode.service.AiService;
import com.jiggycode.service.HighlightService;
import com.jiggycode.service.UserSongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/songs/{songId}/highlights")
public class HighlightController {

    private static final Logger log = LoggerFactory.getLogger(HighlightController.class);

    private final HighlightService highlightService;
    private final UserSongService userSongService;
    private final AiService aiService;

    public HighlightController(HighlightService highlightService, UserSongService userSongService, AiService aiService) {
        this.highlightService = highlightService;
        this.userSongService = userSongService;
        this.aiService = aiService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getHighlights(@PathVariable Long songId, Authentication auth) {
        try {
            Long userId = resolveUserId(auth);

            UserSong userSong = userSongService.findByUserIdAndSongId(userId, songId);
            if (userSong == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "UserSong not found for userId=" + userId + ", songId=" + songId));
            }

            List<Highlight> highlights = highlightService.findByUserSong(userSong);
            return ResponseEntity.ok(highlights);
        } catch (AccessDeniedException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("GET highlights failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addHighlight(@PathVariable Long songId,
                                          @RequestBody Highlight highlight,
                                          Authentication auth) {
        try {
            Long userId = resolveUserId(auth);

            UserSong userSong = userSongService.findByUserIdAndSongId(userId, songId);
            if (userSong == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "UserSong not found for userId=" + userId + ", songId=" + songId));
            }

            if (highlight.getSelectedText() == null || highlight.getSelectedText().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Selected text is required"));
            }

            // Server owns relationships & computed fields
            highlight.setUserSong(userSong);

            String lyrics = userSongService.getLyricsForUserSong(userId, songId);
            if (lyrics == null) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Lyrics unavailable for userId=" + userId + ", songId=" + songId));
            }

            String explanation;
            try {
                explanation = aiService.analyzeWord(
                        highlight.getSelectedText(),
                        userSong.getSong().getTitle(),
                        lyrics
                );
            } catch (Exception aiErr) {
                log.warn("AI analysis failed: {}", aiErr.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "AI analysis failed"));
            }
            highlight.setExplanation(explanation);

            Highlight saved = highlightService.saveHighlight(highlight);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (AccessDeniedException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("POST highlight failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process: " + e.getMessage()));
        }
    }

    /**
     * Resolve current user ID for CustomUserDetails, OIDC, plain OAuth2, or standard Spring UserDetails.
     * Throws 401 (via AccessDeniedException) when identity cannot be established.
     */
    private Long resolveUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new AccessDeniedException("No authentication principal");
        }

        Object principal = auth.getPrincipal();
        log.debug("resolveUserId principal type: {}", principal.getClass().getName());

        // 1) Your own UserDetails that already carries the userId
        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }

        // 2) Standard Spring UserDetails (username is usually email)
        if (principal instanceof UserDetails ud) {
            String email = ud.getUsername();
            return userSongService.findUserIdByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
        }

        // 3) OIDC first (preferred for Google)
        if (principal instanceof OidcUser oidc) {
            String email = safeEmailFromOidc(oidc);
            if (email == null) throw new AccessDeniedException("OAuth2 user email not found");
            return userSongService.findUserIdByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
        }

        // 4) Fallback to generic OAuth2 principal
        if (principal instanceof OAuth2User oauth) {
            String email = safeEmailFromOAuth2(oauth);
            if (email == null) throw new AccessDeniedException("OAuth2 user email not found");
            return userSongService.findUserIdByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
        }

        throw new AccessDeniedException("Unsupported principal type: " + principal.getClass().getName());
    }

    private String safeEmailFromOidc(OidcUser oidc) {
        String email = oidc.getEmail();
        if (email == null || email.isBlank()) {
            Object v = oidc.getClaims().get("email");
            email = (v instanceof String s && !s.isBlank()) ? s : null;
        }
        return email;
    }

    private String safeEmailFromOAuth2(OAuth2User oauth) {
        Object v = oauth.getAttributes().get("email");
        return (v instanceof String s && !s.isBlank()) ? s : null;
    }
}
