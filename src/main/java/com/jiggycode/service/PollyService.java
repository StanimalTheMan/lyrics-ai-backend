package com.jiggycode.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectDominantLanguageRequest;
import software.amazon.awssdk.services.comprehend.model.DetectDominantLanguageResponse;
import software.amazon.awssdk.services.comprehend.model.DominantLanguage;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;

import java.io.InputStream;
import java.util.Optional;

@Service
public class PollyService {

    private final PollyClient pollyClient;
    private final ComprehendClient comprehendClient;

    public PollyService(PollyClient pollyClient, ComprehendClient comprehendClient) {
        this.pollyClient = pollyClient;
        this.comprehendClient = comprehendClient;
    }

    public Optional<String> detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Optional.empty();
        }
        DetectDominantLanguageRequest request = DetectDominantLanguageRequest.builder()
                .text(text)
                .build();
        DetectDominantLanguageResponse response = comprehendClient.detectDominantLanguage(request);
        if (response.hasLanguages() && !response.languages().isEmpty()) {
            DominantLanguage dominantLanguage = response.languages().get(0);
            return Optional.of(dominantLanguage.languageCode());
        }
        return Optional.empty();
    }

    public InputStream synthesizeSpeech(String text, VoiceId voiceId, LanguageCode languageCode, OutputFormat outputFormat) {
        String ssml = "<speak><prosody rate='80%'>" + text + "</prosody></speak>";

        SynthesizeSpeechRequest synthesizeSpeechRequest = SynthesizeSpeechRequest.builder()
                .text(ssml)
                .textType("ssml")
                .voiceId(voiceId)
                .languageCode(languageCode)
                .outputFormat(outputFormat)
                .engine("neural")
                .build();

        ResponseInputStream<SynthesizeSpeechResponse> synthesizeSpeechResponse =
                pollyClient.synthesizeSpeech(synthesizeSpeechRequest);

        return synthesizeSpeechResponse;
    }

    public InputStream synthesizeSpeech(String text, OutputFormat outputFormat) {
        Optional<String> detectedLangCode = detectLanguage(text);

        LanguageCode languageCode = LanguageCode.EN_US;
        VoiceId voiceId = VoiceId.JOANNA;

        if (detectedLangCode.isPresent()) {
            String langCode = detectedLangCode.get();
            if (langCode.equalsIgnoreCase("es")) {
                languageCode = LanguageCode.ES_US;
                voiceId = VoiceId.MIA;
            } else if (langCode.equalsIgnoreCase("ko")) {
                languageCode = LanguageCode.KO_KR;
                voiceId = VoiceId.SEOYEON;
            } else {
                // handle all other languages; TODO: add other else if blocks to handle other supported languages
                System.out.println("Unsupported detected language, using default: " + langCode);
            }
        }

        return synthesizeSpeech(text, voiceId, languageCode, outputFormat);
    }
}
