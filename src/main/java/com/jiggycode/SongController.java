package com.jiggycode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SpotifyApiService spotifyApiService;

    public SongController(SpotifyApiService spotifyApiService) {
        this.spotifyApiService = spotifyApiService;
    }

    @GetMapping("/search")
    public String searchSongs(
            @RequestParam String track,
            @RequestParam String artist
    ) throws Exception {
        return spotifyApiService.searchExactMatchTrack(track, artist);
    }


}
