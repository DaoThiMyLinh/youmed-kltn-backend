package com.youmed.service;

import com.youmed.dto.ai.AiChatRequest;
import com.youmed.dto.ai.AiChatResponse;

public interface AiChatService {
    AiChatResponse processChat(AiChatRequest request);
}
