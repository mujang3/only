package yu.aihackerton.fingerbot.controller;

import org.springframework.web.bind.annotation.*;
import yu.aihackerton.fingerbot.dto.ChatRequestDto;
import yu.aihackerton.fingerbot.dto.ChatResponseDto;
import yu.aihackerton.fingerbot.service.ChatService;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponseDto chat(@RequestBody ChatRequestDto req) {
        return chatService.chat(req);
    }
}
