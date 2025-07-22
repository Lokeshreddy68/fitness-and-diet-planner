package com.fitnessplanner.controller;

import com.fitnessplanner.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/query")
    public ResponseEntity<?> handleChatQuery(@RequestBody ChatQueryRequest request, Principal principal) {
        if (principal == null) {
            // This should ideally be handled by Spring Security for /api/** paths
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("reply", "Query cannot be empty."));
        }

        String username = principal.getName();
        try {
            String reply = chatbotService.processQuery(username, request.getQuery());
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (RuntimeException e) {
            // Log e for server-side diagnostics
            return ResponseEntity.internalServerError().body(Map.of("error", "Error processing your query."));
        }
    }

    // Inner class for request payload
    static class ChatQueryRequest {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}
