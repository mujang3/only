package yu.aihackerton.fingerbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import yu.aihackerton.fingerbot.dto.CalcRequestDto;
import yu.aihackerton.fingerbot.dto.HeatingResultDto;
import yu.aihackerton.fingerbot.service.HeatingService;

@Controller
public class MainController {

    private final HeatingService heatingService;

    public MainController(HeatingService heatingService) {
        this.heatingService = heatingService;
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

}
