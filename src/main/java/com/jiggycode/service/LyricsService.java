package com.jiggycode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class LyricsService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String fetchLyricsFromLrclib(String artist, String track, String album, Integer duration) {
        String baseUrl = "https://lrclib.net/api/get";

        // rid of extra spaces between words and at front/ends to minimize 404s
        // TODO: look into fuzzy matching so ? is not need for songs e.g. What Is Love? also look into not having to have spaces in whatislove?
        String normalizedArtist = normalizeInput(artist);
        String normalizedTrack = normalizeInput(track);

        String url = String.format("%s?artist_name=%s&track_name=%s", baseUrl,
                URLEncoder.encode(normalizedArtist, StandardCharsets.UTF_8),
                URLEncoder.encode(normalizedTrack, StandardCharsets.UTF_8)
        );

        if (album != null) {
            url += "&album_name=" + URLEncoder.encode(album, StandardCharsets.UTF_8);
        }
        if (duration != null) {
            url += "&duration=" + duration;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                return root.path("plainLyrics").asText();  // return just the lyrics text
            } else {
                throw new RuntimeException("Failed to fetch lyrics: " + response.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error contacting LRC Lib: " + e.getMessage(), e);
        }
    }

    static String normalizeInput(String input) {
        if (input == null) return null;
        input = input.trim();
        input = input.replaceAll("\\s+", " ");
        return input;
    }

}

