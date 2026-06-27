package com.example.smartlead.presentation.controller;

import com.example.smartlead.application.service.LeadAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private LeadAgent leadAgent;

    @PostMapping
    public Mono<String> chat(@RequestBody ChatRequest request) {
        // Runs the LangChain4j agent's chat method asynchronously in WebFlux
        return Mono.fromCallable(() -> leadAgent.chat(request.getMessage()));
    }

    public static class ChatRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
