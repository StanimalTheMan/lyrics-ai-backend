package com.jiggycode;

import com.jiggycode.dto.TextRequest;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/speech")
public class SpeechController {

    private final OpenAiAudioSpeechModel speechModel;

    public SpeechController(OpenAiAudioSpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    @PostMapping(value = "/speak", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> speak(@RequestBody TextRequest request) {
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .model("tts-1")
                .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(1.0f)
                .build();

        SpeechPrompt prompt = new SpeechPrompt(request.getText(), speechOptions);
        SpeechResponse response = speechModel.call(prompt);
        byte[] audioBytes = response.getResult().getOutput();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(audioBytes);
    }
}
