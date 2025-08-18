package com.jiggycode.controller;

import com.jiggycode.service.PollyService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.io.InputStream;
import java.io.OutputStream;

@RestController
public class AudioController {

    private final PollyService pollyService;

    public AudioController(PollyService pollyService) {
        this.pollyService = pollyService;
    }

    @GetMapping("/pronunciation")
    public void getPronunciation(@RequestParam String text, HttpServletResponse response) throws Exception {
        InputStream audioStream = pollyService.synthesizeSpeech(text, VoiceId.JOANNA, OutputFormat.MP3);

        response.setContentType("audio/mpeg");
        try (OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = audioStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }
}
