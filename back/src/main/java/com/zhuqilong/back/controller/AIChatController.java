package com.zhuqilong.back.controller;

import com.zhuqilong.back.dto.ChatRequest;
import com.zhuqilong.back.dto.ChatResponse;
import com.zhuqilong.back.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-chat")
public class AIChatController {

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = deepSeekService.getChatResponse(request.getMessage());
        return ResponseEntity.ok(response);
    }
}