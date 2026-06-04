package yu.aihackerton.fingerbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import yu.aihackerton.fingerbot.dto.ChatRequestDto;
import yu.aihackerton.fingerbot.dto.ChatResponseDto;
import yu.aihackerton.fingerbot.dto.ChatSettingsDto;
import yu.aihackerton.fingerbot.dto.MessageDto;

import java.util.*;

@Service
public class ChatService {

    private static final String API_URL =
            "https://factchat-cloud.mindlogic.ai/v1/gateway/chat/completions/";

    private static final String MODEL = "claude-sonnet-4-6";

    private static final String SYSTEM_PROMPT = """
            당신은 ONLY 앱의 AI 난방 어시스턴트입니다. 한국어로 친근하게 대화합니다.

            사용자가 '__START__'를 보내면: 반갑게 인사하고 첫 번째 질문(현재 실내 온도)을 하세요.

            수집 순서 (반드시 한 번에 하나씩 질문):
            1. 현재 실내 온도 (°C) — 자연어로 말해도 파악
            2. 원하는 따뜻함 수준:
               1단계(❄️ 절약형) / 2단계(🌿 약간 절약) / 3단계(✨ 표준 22°C) / 4단계(☀️ 약간 따뜻) / 5단계(🔥 아주 따뜻)
            3. 지난달 가스비 (원) — 모른다면 기본값 사용하고 넘어가기

            모든 정보 수집 완료 시, 짧게 설정 확인 후 마지막에 반드시 아래 태그 포함:
            [SETTINGS]{"indoorTemp":18.0,"indoorHum":45.0,"outdoorTemp":null,"area":30,"insul":"2","prefStep":3,"lastGas":80000}[/SETTINGS]

            기본값: indoorHum=45.0, area=30, insul="2", lastGas=80000
            짧고 친근하게, 이모지 1~2개 사용.
            """;

    @Value("${factchat.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ChatService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ChatResponseDto chat(ChatRequestDto req) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        for (MessageDto m : req.getMessages()) {
            messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
        }
        messages.add(Map.of("role", "user", "content", req.getNewMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("messages", messages);
        body.put("max_tokens", 500);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST,
                    new HttpEntity<>(body, headers), Map.class);
            String content = extractContent(response.getBody());
            return buildResponse(content);
        } catch (Exception e) {
            ChatResponseDto err = new ChatResponseDto();
            err.setReply("죄송해요, AI 연결에 문제가 생겼어요. 잠시 후 다시 시도해주세요 😅");
            return err;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<?, ?> response) {
        List<Map<?, ?>> choices = (List<Map<?, ?>>) response.get("choices");
        Map<?, ?> message = (Map<?, ?>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private ChatResponseDto buildResponse(String content) {
        ChatResponseDto result = new ChatResponseDto();
        int start = content.indexOf("[SETTINGS]");
        int end = content.indexOf("[/SETTINGS]");
        if (start != -1 && end != -1) {
            String json = content.substring(start + 10, end).trim();
            String reply = content.substring(0, start).trim();
            result.setReply(reply);
            try {
                result.setSettings(objectMapper.readValue(json, ChatSettingsDto.class));
                result.setSettingsReady(true);
            } catch (Exception ignored) {
                result.setReply(content.trim());
            }
        } else {
            result.setReply(content.trim());
        }
        return result;
    }
}
