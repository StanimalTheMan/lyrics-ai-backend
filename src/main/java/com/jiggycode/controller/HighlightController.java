package com.jiggycode.controller;

import com.jiggycode.config.CustomUserDetails;
import com.jiggycode.entity.Highlight;
import com.jiggycode.entity.UserSong;
import com.jiggycode.service.AiService;
import com.jiggycode.service.HighlightService;
import com.jiggycode.service.UserSongService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/songs/{songId}/highlights")
public class HighlightController {

    private final HighlightService highlightService;
    private final UserSongService userSongService;
    private final AiService aiService;

    public HighlightController(HighlightService highlightService, UserSongService userSongService, AiService aiService) {
        this.highlightService = highlightService;
        this.userSongService = userSongService;
        this.aiService = aiService;
    }

    @GetMapping
    public ResponseEntity<?> getHighlights(@PathVariable Long songId, Authentication auth) {
        try {
            Long userId = resolveUserId(auth);

            UserSong userSong = userSongService.findByUserIdAndSongId(userId, songId);
            if (userSong == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "UserSong not found"));
            }

            List<Highlight> highlights = highlightService.findByUserSong(userSong);
            return ResponseEntity.ok(highlights);
        } catch (AccessDeniedException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> addHighlight(@PathVariable Long songId,
                                          @RequestBody Highlight highlight,
                                          Authentication auth) {
        try {
            Long userId = resolveUserId(auth);

            UserSong userSong = userSongService.findByUserIdAndSongId(userId, songId);
            if (userSong == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "UserSong not found"));
            }

            if (highlight.getSelectedText() == null || highlight.getSelectedText().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Selected text is required"));
            }

            highlight.setUserSong(userSong);

            String lyrics = userSongService.getLyricsForUserSong(userId, songId);
            String explanation = aiService.analyzeWord(
                    highlight.getSelectedText(),
                    userSong.getSong().getTitle(),
                    lyrics
            );
            highlight.setExplanation(explanation);

            Highlight saved = highlightService.saveHighlight(highlight);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (AccessDeniedException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process: " + e.getMessage()));
        }
    }


    private Long resolveUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new AccessDeniedException("No authentication principal");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }

        if (principal instanceof OidcUser oidc) {
            String rawEmail = oidc.getEmail();
            if (rawEmail == null || rawEmail.isBlank()) {
                Object v = oidc.getClaims().get("email");
                rawEmail = (v instanceof String s && !s.isBlank()) ? s : null;
            }
            final String email = rawEmail; // effectively final for the lambda

            if (email == null) {
                throw new AccessDeniedException("OAuth2 user email not found");
            }

            return userSongService.findUserIdByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
        }

        if (principal instanceof OAuth2User oauth) {
            Object v = oauth.getAttributes().get("email");
            String email = (v instanceof String s && !s.isBlank()) ? s : null;
            if (email == null) throw new AccessDeniedException("OAuth2 user email not found");

            return userSongService.findUserIdByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
        }

        throw new AccessDeniedException("Unsupported principal type: " + principal.getClass().getName());
    }
}
