package com.jiggycode;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class SpotifyTokenService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private String accessToken;
    private Instant expiryTime;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostConstruct
    public void printKeys() {
//        System.out.println("clientId = " + clientId);
//        System.out.println("clientSecret = " + clientSecret);
    }

    public String getAccessToken() throws IOException, InterruptedException {
        if (accessToken != null && Instant.now().isBefore(expiryTime)) {
            return accessToken;
        }

        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        String body = "grant_type=client_credentials";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Parse JSON response (using simple parsing here or use Jackson/Gson)
            String responseBody = response.body();
            // Example response:
            // {"access_token":"BQD...","token_type":"Bearer","expires_in":3600}

            // Naive parsing (for demo) — better to use Jackson or Gson:
            String token = extractValue(responseBody, "access_token");
            String expiresInStr = extractValue(responseBody, "expires_in");
            int expiresIn = Integer.parseInt(expiresInStr);

            this.accessToken = token;
            this.expiryTime = Instant.now().plusSeconds(expiresIn - 60); // refresh 1 min early

            return this.accessToken;
        } else {
            throw new IOException("Failed to get Spotify token: " + response.body());
        }
    }

    private String extractValue(String json, String key) {
        // Very naive JSON extraction (not for prod)
        String pattern = "\"" + key + "\":\"?([^\"]+?)\"?(,|})";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
