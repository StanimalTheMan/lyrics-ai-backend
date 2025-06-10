package com.jiggycode;
import com.jiggycode.dto.SongDetails;
import com.jiggycode.dto.TrackInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SpotifyApiService spotifyApiService;
    private final LyricsService lyricsService;

    public SongController(SpotifyApiService spotifyApiService, LyricsService lyricsService) {
        this.spotifyApiService = spotifyApiService;
        this.lyricsService = lyricsService;
    }

    @GetMapping("/details")
    public ResponseEntity<?> getSongDetails(
            @RequestParam String track,
            @RequestParam String artist
    ) {
        try {
            TrackInfo trackInfo = spotifyApiService.searchExactMatchTrack(track, artist);
            if (trackInfo == null) {
                return ResponseEntity.notFound().build();
            }

            String lyrics = lyricsService.fetchLyricsFromLrclib(artist, track, null, null);
            SongDetails details = new SongDetails(trackInfo, lyrics);

            return ResponseEntity.ok(details);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}

