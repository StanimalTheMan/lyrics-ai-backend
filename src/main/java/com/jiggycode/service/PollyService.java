package com.jiggycode.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.io.InputStream;

@Service
public class PollyService {

    private final PollyClient pollyClient;

    public PollyService(PollyClient pollyClient) {
        this.pollyClient = pollyClient;
    }

    public InputStream synthesizeSpeech(String text, VoiceId voiceId, OutputFormat outputFormat) {
        SynthesizeSpeechRequest synthesizeSpeechRequest = SynthesizeSpeechRequest.builder()
                .text(text)
                .voiceId(voiceId)
                .outputFormat(outputFormat)
                .engine("neural")
                .build();
        ResponseInputStream<SynthesizeSpeechResponse> synthesizeSpeechResponse = pollyClient.synthesizeSpeech(synthesizeSpeechRequest);
        return synthesizeSpeechResponse;
    }
}
