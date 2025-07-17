package com.jiggycode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiggycode.SpotifyTokenService;
import com.jiggycode.dto.TrackInfo;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SpotifyApiService {

    private final SpotifyTokenService tokenService;
    private final HttpClient httpClient;

    public SpotifyApiService(SpotifyTokenService tokenService) {
        this.tokenService = tokenService;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String searchTracks(String track, String artist) throws IOException, InterruptedException {
        String token = tokenService.getAccessToken();

        // Build the Spotify query string with track and artist filters
        String query = String.format("track:\"%s\" artist:\"%s\"", track, artist);

        // URL-encode the query so spaces and special chars are handled properly
        String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=track&limit=5"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new IOException("Spotify search failed: " + response.body());
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TrackInfo searchExactMatchTrack(String track, String artist) throws IOException, InterruptedException {
        artist = artist.trim().replaceAll("\\r?\\n", "");
        String jsonResponse = searchTracks(track, artist);
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode items = root.path("tracks").path("items");

        for (JsonNode item : items) {
            String trackName = item.path("name").asText();
            JsonNode artists = item.path("artists");
            for (JsonNode artistNode : artists) {
                String artistName = artistNode.path("name").asText();
                if (trackName.equalsIgnoreCase(track) && artistName.equalsIgnoreCase(artist)) {
                    return new TrackInfo(
                            trackName,
                            artistName,
                            item.path("external_urls").path("spotify").asText(),
                            item.path("album").path("images").get(0).path("url").asText()
                    );
                }
            }
        }

        return null;
    }

}

