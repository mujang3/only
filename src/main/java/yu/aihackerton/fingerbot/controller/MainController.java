package yu.aihackerton.fingerbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import yu.aihackerton.fingerbot.dto.CalcRequestDto;
import yu.aihackerton.fingerbot.dto.ChatRequestDto;
import yu.aihackerton.fingerbot.dto.ChatResponseDto;
import yu.aihackerton.fingerbot.dto.HeatingResultDto;
import yu.aihackerton.fingerbot.service.GeminiService;
import yu.aihackerton.fingerbot.service.HeatingService;

/**
 * 화면 라우팅과 계산 API를 담당.
 * - GET  /            → 온보딩(onboarding.html)
 * - GET  /main        → 메인 대시보드(index.html)
 * - GET  /onboarding  → 온보딩(onboarding.html)
 * - POST /api/calc    → 계산 수행 후 결과 JSON 반환
 */
@Controller
public class MainController {

    private final HeatingService heatingService;
    private final GeminiService geminiService;

    public MainController(HeatingService heatingService, GeminiService geminiService) {
        this.heatingService = heatingService;
        this.geminiService = geminiService;
    }

    @GetMapping("/")
    public String index() {
        return "onboarding";
    }

    @GetMapping("/check")
    public String check() {
        return "check";
    }

    @GetMapping("/main")
    public String main() {
        return "index";
    }

    @GetMapping("/onboarding")
    public String onboarding() {
        return "onboarding";
    }

    @PostMapping("/api/calc")
    @ResponseBody
    public HeatingResultDto calc(@RequestBody CalcRequestDto req) {
        return heatingService.calculate(req, null);
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public ChatResponseDto chat(@RequestBody ChatRequestDto req) {
        return geminiService.chat(req);
    }
}
