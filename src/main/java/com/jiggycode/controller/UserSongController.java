package com.jiggycode.controller;

import com.jiggycode.entity.Song;
import com.jiggycode.service.UserSongService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usersongs")
public class UserSongController {

    private final UserSongService userSongService;

    public UserSongController(UserSongService userSongService) {
        this.userSongService = userSongService;
    }

    // Save song for authenticated user
    @PostMapping("/{songId}")
    public ResponseEntity<?> saveSongForUser(
            @PathVariable Long songId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            userSongService.saveSongForUser(userDetails.getUsername(), songId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all saved songs for authenticated user
    @GetMapping
    public ResponseEntity<?> getUserSongs(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<Song> songs = userSongService.getSavedSongsForUser(userDetails.getUsername());
            System.out.println("songs" + songs);
            return ResponseEntity.ok(songs);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch songs.");
        }
    }

    // Remove a song from user's saved list
    @DeleteMapping("/{songId}")
    public ResponseEntity<?> deleteUserSong(
            @PathVariable Long songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            userSongService.removeSongForUser(userDetails.getUsername(), songId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
