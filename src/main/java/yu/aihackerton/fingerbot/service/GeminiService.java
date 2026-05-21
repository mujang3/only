package yu.aihackerton.fingerbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import yu.aihackerton.fingerbot.dto.ChatRequestDto;
import yu.aihackerton.fingerbot.dto.ChatResponseDto;

import java.util.*;

@Service
public class GeminiService {

    @Value("${factchat.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_URL  = "https://factchat-cloud.mindlogic.ai/v1/gateway/chat/completions";
    private static final String MODEL    = "claude-sonnet-4-6";

    private static final String SYSTEM_PROMPT =
        "당신은 '온리 : ONLY' AI 난방 어시스턴트입니다. 아래 두 가지를 잘 해주세요.\n\n" +
        "1. 현재 집 상태를 바탕으로 몇 단계 설정을 추천할지 알려주세요:\n" +
        "   - 1단계(절약형): 목표 -2°C, 에너지 최대 절약\n" +
        "   - 2단계(약간 절약): 목표 -1°C\n" +
        "   - 3단계(표준): 목표 22°C\n" +
        "   - 4단계(약간 따뜻): 목표 +1°C\n" +
        "   - 5단계(따뜻형): 목표 +2°C\n\n" +
        "2. 다른 사용자들의 실제 절약 사례:\n" +
        "   - 20대 1인 가구: 취침 전 1단계로 낮춰 월 15,000원 절약\n" +
        "   - 직장인: 귀가 30분 전 예열 시작으로 과가열 방지, 20% 절감\n" +
        "   - 재택 근무자: 낮 3단계 유지, 저녁만 4단계로 월 8,000원 절약\n" +
        "   - 습도 관리: 가습기 켜면 체감온도 +1도 효과로 1단계 낮춰도 쾌적\n" +
        "   - 외풍 차단: 단열 개선 시 보일러 가동시간 30% 감소\n\n" +
        "답변 규칙:\n" +
        "- 친근하고 따뜻하게 한국어로 2~3문장\n" +
        "- 단계 추천 시 반드시 숫자 단계 명시 (예: '3단계 추천')\n" +
        "- 절약 팁은 구체적인 금액/비율 포함\n" +
        "- 이모지 1~2개 사용";

    public ChatResponseDto chat(ChatRequestDto req) {
        if (apiKey == null || apiKey.isBlank()) {
            return new ChatResponseDto("API 키가 설정되지 않았어요. application.properties에 factchat.api.key를 입력해주세요.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            String userContent = buildContext(req) + "\n\n사용자 질문: " + req.getMessage();

            List<Map<String, String>> messages = List.of(
                Map.of("role", "system",  "content", SYSTEM_PROMPT),
                Map.of("role", "user",    "content", userContent)
            );

            Map<String, Object> body = new HashMap<>();
            body.put("model",       MODEL);
            body.put("messages",    messages);
            body.put("max_tokens",  400);
            body.put("temperature", 0.8);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String reply = (String) message.get("content");

            return new ChatResponseDto(reply, extractSuggestedStep(reply));

        } catch (Exception e) {
            return new ChatResponseDto("죄송해요, 오류가 발생했어요. (" + e.getMessage() + ")");
        }
    }

    private String buildContext(ChatRequestDto req) {
        StringBuilder sb = new StringBuilder("현재 집 상태:\n");
        if (req.getFeelsLike() != null)   sb.append("- 체감온도: ").append(req.getFeelsLike()).append("°C\n");
        if (req.getTargetTemp() != null)   sb.append("- 목표온도: ").append(req.getTargetTemp()).append("°C\n");
        if (req.getPrefStep() != null)     sb.append("- 현재 설정 단계: ").append(req.getPrefStep()).append("단계\n");
        if (req.getBoilerState() != null)  sb.append("- 보일러: ").append(stateKr(req.getBoilerState())).append("\n");
        if (req.getOutdoorTemp() != null)  sb.append("- 외기온도: ").append(req.getOutdoorTemp()).append("°C\n");
        if (req.getIndoorHum() != null)    sb.append("- 실내 습도: ").append(req.getIndoorHum()).append("%\n");
        if (req.getRuntimeMin() != null && req.getRuntimeMin() > 0)
            sb.append("- 예상 가동시간: ").append(req.getRuntimeMin()).append("분\n");
        if (req.getSaveMonthly() != null && req.getSaveMonthly() > 0)
            sb.append("- 이번 달 예상 절감액: ").append(req.getSaveMonthly()).append("원\n");
        return sb.toString();
    }

    private String stateKr(String state) {
        return switch (state) {
            case "heating"     -> "가열 중";
            case "approaching" -> "목표 도달 임박";
            case "reached"     -> "목표 도달 (OFF)";
            default            -> state;
        };
    }

    private Integer extractSuggestedStep(String reply) {
        for (int s = 1; s <= 5; s++) {
            if (reply.contains(s + "단계")) return s;
        }
        return null;
    }
}
