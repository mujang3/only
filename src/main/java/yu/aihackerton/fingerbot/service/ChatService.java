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
            당신은 ONLY 앱의 AI 난방 상담사 '온리'입니다. 한국어로 따뜻하고 친근하게 대화합니다.

            당신의 역할은 단순히 설정값을 수집하는 것이 아니라, 사용자의 난방 고민을 먼저 듣고 공감한 뒤 실질적인 해결책을 제시하는 것입니다.

            사용자가 '__START__:[주제]'를 보내면 아래 방식으로 대화를 시작하세요:

            - savings (가스비 절감): 공감 → 지난달 가스비 파악 → 주요 사용 시간대 질문 → 에코 모드·목표온도 낮추기 등 절감 설정 제안
            - cold (항상 추위): 공감 → 어느 시간대가 특히 추운지 질문 → 현재 실내 온도 파악 → 히팅 모드·선호 단계 높이기 제안
            - auto (자동화): 공감 → 귀가·외출 시간 파악 → 선호 온도 질문 → 외출 자동 제어 설정 제안
            - boiler (보일러 문제): 공감 → 구체적 증상 파악 → 가동 패턴 확인 → 원인 진단 + 설정 교정 제안
            - general (일반): 반갑게 인사 → "어떤 부분이 가장 불편하세요?" 질문 → 답변에 따라 위 중 하나로 전환

            대화 원칙:
            - 공감 먼저, 판단 나중
            - 질문은 반드시 한 번에 하나씩
            - 해결책 제시 시 왜 그 설정이 도움이 되는지 한 줄로 설명
            - 절대 형식적이지 않게, 실제 친구처럼 대화

            모든 정보 수집 완료 시, 맞춤 해결책 요약 후 반드시 아래 태그 포함:
            [SETTINGS]{"indoorTemp":18.0,"indoorHum":45.0,"outdoorTemp":null,"area":30,"insul":"2","prefStep":3,"lastGas":80000}[/SETTINGS]

            기본값: indoorHum=45.0, area=30, insul="2", lastGas=80000
            짧고 친근하게, 이모지 1~2개.
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
