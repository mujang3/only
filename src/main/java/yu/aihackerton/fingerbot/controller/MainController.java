package yu.aihackerton.fingerbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import yu.aihackerton.fingerbot.dto.CalcRequestDto;
import yu.aihackerton.fingerbot.dto.HeatingResultDto;
import yu.aihackerton.fingerbot.service.HeatingService;
import yu.aihackerton.fingerbot.service.HistoryService;

@Controller
public class MainController {

    private final HeatingService heatingService;
    private final HistoryService historyService;

    public MainController(HeatingService heatingService, HistoryService historyService) {
        this.heatingService = heatingService;
        this.historyService = historyService;
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

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("/presentation")
    public String presentation() {
        return "presentation";
    }

    @PostMapping("/api/calc")
    @ResponseBody
    public HeatingResultDto calc(@RequestBody CalcRequestDto req) {
        HeatingResultDto result = heatingService.calculate(req, null);
        historyService.record(result);
        return result;
    }

}
