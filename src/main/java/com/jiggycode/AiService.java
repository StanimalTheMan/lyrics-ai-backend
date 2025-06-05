package com.jiggycode;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiService {

    private final ChatClient chatClient;

    public AiService(ChatClient.Builder builder) {
        chatClient = builder.build();
    }

    public String analyzeWord(String word, String context) {
        String systemMessage = "You are a helpful assistant that explains words in the context of song lyrics.";
        String userMessage = String.format("Explain the word '%s' in the context of this song lyric: '%s'. Provide a definition, translation (if applicable), and cultural context.", word, context);

        Prompt prompt = new Prompt(List.of(
                new UserMessage(systemMessage),
                new UserMessage(userMessage)
        ));

        return chatClient.prompt(prompt).call().content();

    }
}

