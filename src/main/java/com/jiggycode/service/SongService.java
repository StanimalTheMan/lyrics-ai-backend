package com.jiggycode.service;

import com.jiggycode.dto.TrackInfo;
import com.jiggycode.entity.Song;
import com.jiggycode.repository.SongRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SongService {

    private final SongRepository songRepository;
    private final SpotifyApiService spotifyApiService;
    private final LyricsService lyricsScraperService;

    public SongService(SongRepository songRepository, SpotifyApiService spotifyApiService, LyricsService lyricsScraperService) {
        this.songRepository = songRepository;
        this.spotifyApiService = spotifyApiService;
        this.lyricsScraperService = lyricsScraperService;
    }

    public Song getOrSaveSong(String title, String artist) throws Exception {
        return songRepository.findByTitleIgnoreCaseAndArtistIgnoreCase(title, artist)
                .orElseGet(() -> {
                    try {
                        TrackInfo trackInfo = spotifyApiService.searchExactMatchTrack(title, artist);
                        String lyrics = lyricsScraperService.fetchLyricsFromLrclib(artist, title, null, null);
                        Song newSong = new Song();
                        newSong.setTitle(trackInfo.getTrack());
                        newSong.setArtist(trackInfo.getArtist());
                        newSong.setSpotifyUrl(trackInfo.getSpotifyUrl());
                        newSong.setImageUrl(trackInfo.getImageUrl());
                        newSong.setLyrics(lyrics);
                        return songRepository.save(newSong); // This must return a Song
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch and save song", e);
                    }
                });
    }
}
