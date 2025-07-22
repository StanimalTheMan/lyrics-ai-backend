package com.jiggycode.controller;
import com.jiggycode.entity.Song;
import com.jiggycode.service.SongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping("/details")
    public ResponseEntity<?> getOrSaveSong(
            @RequestParam String track,
            @RequestParam String artist
    ) {
        try {
            Song song = songService.getOrSaveSong(track, artist);
            return ResponseEntity.ok(song);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSongById(@PathVariable Long id) {
        try {
            Song song = songService.getSongById(id);
            return ResponseEntity.ok(song);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

