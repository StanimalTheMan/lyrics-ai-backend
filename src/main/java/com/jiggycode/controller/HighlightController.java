package com.jiggycode.controller;

import com.jiggycode.config.CustomUserDetails;
import com.jiggycode.entity.Highlight;
import com.jiggycode.entity.UserSong;
import com.jiggycode.service.AiService;
import com.jiggycode.service.HighlightService;
import com.jiggycode.service.UserSongService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            System.out.println("Authentication object: " + auth);
            System.out.println("Is authenticated? " + (auth != null && auth.isAuthenticated()));
            System.out.println("Principal class: " + (auth != null ? auth.getPrincipal().getClass().getName() : "null"));
            Object principal = auth.getPrincipal();
            if (!(principal instanceof CustomUserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid principal type"));
            }
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            Long userId = userDetails.getId();

            UserSong userSong = userSongService.findByUserIdAndSongId(userId, songId);
            if (userSong == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "UserSong not found"));
            }

            List<Highlight> highlights = highlightService.findByUserSong(userSong);
            return ResponseEntity.ok(highlights);
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
            Object principal = auth.getPrincipal();
            if (!(principal instanceof CustomUserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid principal type"));
            }
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            Long userId = userDetails.getId();

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
                    lyrics
            );
            highlight.setExplanation(explanation);

            Highlight saved = highlightService.saveHighlight(highlight);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process: " + e.getMessage()));
        }
    }

}
