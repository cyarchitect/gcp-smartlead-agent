package com.example.smartlead.infrastructure.config;

import com.example.smartlead.application.service.LeadAgent;
import com.example.smartlead.application.service.LeadTools;
import com.example.smartlead.application.service.LeadService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "google-ai")
    public ChatLanguageModel googleAiChatLanguageModel(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.model.name:gemini-1.5-flash}") String modelName) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "vertex-ai")
    public ChatLanguageModel vertexAiChatLanguageModel(
            @Value("${spring.cloud.gcp.project-id}") String project,
            @Value("${gemini.location:us-central1}") String location,
            @Value("${gemini.model.name:gemini-3.5-flash}") String modelName) {
        return VertexAiGeminiChatModel.builder()
                .project(project)
                .location(location)
                .modelName(modelName)
                .build();
    }

    @Bean
    @ConditionalOnBean(ChatLanguageModel.class)
    public LeadAgent langchainLeadAgent(ChatLanguageModel chatLanguageModel, LeadTools leadTools) {
        return AiServices.builder(LeadAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(leadTools)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
    public LeadAgent mockLeadAgent(LeadService leadService) {
        return new MockLeadAgent(leadService);
    }
}
