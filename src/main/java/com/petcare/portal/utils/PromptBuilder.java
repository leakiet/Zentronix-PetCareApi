package com.petcare.portal.utils;

import com.petcare.portal.entities.ChatMessage;
import java.util.List;
import java.util.stream.Collectors;

public class PromptBuilder {
    public static String buildPrompt(List<ChatMessage> messages) {
        return messages.stream()
                .map(m -> (m.getIsFromAI() ? "AI: " : "User: ") + m.getContent())
                .collect(Collectors.joining("\n"));
    }
}