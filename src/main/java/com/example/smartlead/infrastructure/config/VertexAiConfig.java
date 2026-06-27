package com.example.smartlead.infrastructure.config;

import com.example.smartlead.application.service.LeadAgent;
import com.example.smartlead.application.service.LeadTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Value;

@Configuration
public class VertexAiConfig {

    @Value("${spring.cloud.gcp.project-id}")
    private String project;

    @Value("${gemini.location:us-central1}")
    private String location;

    @Value("${gemini.model.name:gemini-3.5-flash}")
    private String modelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // Uses Vertex AI Gemini via the service account attached to Cloud Run
        return VertexAiGeminiChatModel.builder()
                .project(project)
                .location(location)
                .modelName(modelName)
                .build();
    }

    @Bean
    public LeadAgent leadAgent(ChatLanguageModel chatLanguageModel, LeadTools leadTools) {
        // Construct the LangChain4j AI Service mapping the Agent interface to the tools and memory
        return AiServices.builder(LeadAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(leadTools)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}
