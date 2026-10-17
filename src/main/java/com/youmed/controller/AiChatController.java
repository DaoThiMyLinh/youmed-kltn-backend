package com.youmed.controller;

import com.youmed.dto.ai.AiChatRequest;
import com.youmed.dto.ai.AiChatResponse;
import com.youmed.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiChatService.processChat(request));
    }

    @GetMapping("/welcome")
    public ResponseEntity<AiChatResponse> getWelcome() {
        return ResponseEntity.ok(aiChatService.processChat(AiChatRequest.builder().message("chào").build()));
    }
}
