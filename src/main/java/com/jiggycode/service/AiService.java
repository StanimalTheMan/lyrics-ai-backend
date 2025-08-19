package com.jiggycode.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
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
        String systemMessage = "You are a helpful assistant that explains the meaning of words in the context of song lyrics.";

        String userMessage = String.format(
                "First, identify and understand the song and its lyrics using this context: '%s'.\n" +
                        "Then, explain the meaning of the word '%s' as it is used within those lyrics.\n\n" +
                        "Your response should include:\n" +
                        "- A clear definition of the word\n" +
                        "- Romanization of the selected lyrics\n" +
                        "- A translation if it's a non-English word\n" +
                        "- Cultural or lyrical significance as used in the song",
                context, word
        );

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemMessage),
                new UserMessage(userMessage)
        ));

        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            return "Sorry, I couldn't analyze the word right now. Please try again later.";
        }
    }

}

