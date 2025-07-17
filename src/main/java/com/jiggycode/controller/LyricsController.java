package com.jiggycode.controller;

import com.jiggycode.service.LyricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lyrics")
public class LyricsController {

    private final LyricsService lyricsService;

    public LyricsController(LyricsService lyricsService) {
        this.lyricsService = lyricsService;
    }

    @GetMapping
    public ResponseEntity<String> getLyrics(
            @RequestParam String artist,
            @RequestParam String track,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) Integer duration
    ) {
        String lyrics = lyricsService.fetchLyricsFromLrclib(artist, track, album, duration);
        return ResponseEntity.ok(lyrics);  // returns plain text
    }

}
