package yu.aihackerton.fingerbot.service;

import org.springframework.stereotype.Service;
import yu.aihackerton.fingerbot.dto.CalcRequestDto;
import yu.aihackerton.fingerbot.dto.HeatingResultDto;

import java.util.Map;

/**
 * 난방 계산의 두뇌 (v2 알고리즘).
 *
 * 핵심 차별점: 오버슈트 방지(잔열 제어).
 * 보일러는 끈 뒤에도 바닥 잔열로 온도가 더 오르므로(오버슈트),
 * 목표보다 1.0℃ 낮은 '가상 목표치'를 기기가 쫓게 해서 부드럽게 안착시킨다.
 *
 * 습도는 기기가 제어하지 않는다. 체감온도 계산과 사용자 알림에만 사용한다.
 *
 * 체감온도는 6개 가중치 다항식(온습도 시너지항 포함)으로 산출한다:
 *   C = b0 + b1*T + b2*H + b3*(T*H) + b4*E + b5*A
 */
@Service
public class HeatingService {

    // 하드코딩 상수 (1인 가구 30제곱미터 기준)
    private static final double AREA = 30.0;
    private static final double B0 = 1.4;    // 기본 보정
    private static final double B1 = 1.0;    // 실내온도 T
    private static final double B2 = -0.05;  // 습도 H (낮을수록 춥게)
    private static final double B3 = 0.003;  // 온습도 시너지 T*H
    private static final double B4 = 0.1;    // 외부기온 E
    private static final double B5 = 0.02;   // 면적 A 보정

    private static final double OVERSHOOT = 1.0;  // 잔열 오버슈트(도)
    private static final Map<Integer, Double> LEVEL_OFFSET =
            Map.of(1, -2.0, 2, -1.0, 3, 0.0, 4, 1.0, 5, 2.0);
    private static final Map<String, Double> INSUL_MULT =
            Map.of("1", 1.5, "2", 1.0, "3", 0.7);

    /**
     * 다항식 체감온도.
     * 가습기를 켜서 H가 오르면 b3*T*H 시너지로 체감온도가 빠르게 상승한다.
     */
    public double feelsLike(double t, double h, double e, double area) {
        double c = B0 + B1 * t + B2 * h + B3 * (t * h) + B4 * e + B5 * area;
        return round1(c);
    }

    /** Level(1~5)을 반영한 설정 목표온도 C_target */
    public double targetTemp(double base, int level) {
        return round1(base + LEVEL_OFFSET.getOrDefault(level, 0.0));
    }

    /** 오버슈트를 미리 뺀 가상 목표치 C_virtual */
    public double virtualTarget(double target) {
        return round1(target - OVERSHOOT);
    }

    public HeatingResultDto calculate(CalcRequestDto req, Integer lastGas) {
        HeatingResultDto r = new HeatingResultDto();

        double t = req.getIndoorTemp();
        double h = req.getIndoorHum();
        double e = (req.getOutdoorTemp() != null) ? req.getOutdoorTemp() : -2.0;
        double area = (req.getArea() > 0) ? req.getArea() : AREA;

        double base = 22.0; // 쾌적 목표 C_base
        int level = req.getPrefStep() > 0 ? req.getPrefStep() : 3;

        double cTarget = targetTemp(base, level);
        double cVirtual = virtualTarget(cTarget);
        double cCurrent = feelsLike(t, h, e, area);
        double deltaC = round1(cVirtual - cCurrent);

        r.setFeelsLike(cCurrent);
        r.setMyTarget(cTarget);
        r.setVirtualTarget(cVirtual);
        r.setOptimalTemp(cTarget);
        r.setDeltaC(deltaC);
        r.setGap(deltaC);
        if (req.getOutdoorTemp() != null) {
            r.setOutdoorFeels(feelsLike(e, req.getOutdoorHum(), e, area));
        }

        // ΔC 3구간 보일러 제어 + 습도 알림
        if (deltaC >= 1.0) {
            r.setBoilerState("heating");
            r.setBoilerSetTemp(round1(cTarget + 1));
            r.setBoilerCommand(String.format("보일러 설정 %.1f도 — 빠르게 가열", cTarget + 1));
            r.setComfortStep(h < 40 ? 2 : 1);
            if (h < 40) {
                r.setAlertType("humidify");
                r.setAlertMsg("가습기를 켜면 더 빨리 따뜻해져요!");
            }
        } else if (deltaC >= 0.0) {
            r.setBoilerState("approaching");
            r.setBoilerSetTemp(round1(cTarget - 1));
            r.setBoilerCommand(String.format("보일러 설정 %.1f도 — 잔열 브레이크", cTarget - 1));
            r.setComfortStep(3);
        } else {
            r.setBoilerState("reached");
            r.setBoilerSetTemp(null);
            r.setBoilerCommand("보일러 OFF — 잔열로 목표 도달");
            r.setComfortStep(3);
            if (h > 60) {
                r.setAlertType("ventilate");
                r.setAlertMsg("습도가 높습니다. 환기를 권장합니다.");
            }
        }

        r.setComfortLabel(comfortLabel(r.getComfortStep()));
        r.setComfortEmoji(comfortEmoji(r.getComfortStep()));

        int rt = runtime(deltaC, e, area, req.getInsul());
        r.setRuntimeMin(rt);
        if ("heating".equals(r.getBoilerState())) {
            int rate = (int) Math.round(20 + Math.min(15, Math.abs(deltaC) * 2));
            int daily = (int) Math.round(rt * 3.2 * rate / 100.0);
            r.setSaveRate(rate);
            r.setSaveDaily(daily);
            r.setSaveMonthly(lastGas != null
                    ? Math.round(lastGas * (1 - rate / 100.0))
                    : (long) daily * 30);
        } else {
            r.setSaveRate(35);
            r.setSaveDaily(0);
            r.setSaveMonthly(0);
        }
        return r;
    }

    /** 보일러 권장 가동시간(분). ΔC가 양수일 때만 */
    public int runtime(double deltaC, double outdoor, double area, String insul) {
        if (deltaC <= 0) return 0;
        double base = deltaC * 4.5;
        base += Math.max(0, (0 - outdoor) * 0.8);
        double areaFactor = 1 + (area - 10) * 0.015;
        double insulMult = INSUL_MULT.getOrDefault(insul, 1.0);
        return (int) Math.round(Math.max(0, base * insulMult * Math.max(0.8, areaFactor)));
    }

    private String comfortLabel(int step) {
        switch (step) {
            case 1: return "매우 추워요";
            case 2: return "조금 추워요";
            case 3: return "쾌적해요";
            case 4: return "조금 더워요";
            default: return "매우 더워요";
        }
    }

    private String comfortEmoji(int step) {
        switch (step) {
            case 1: return "\uD83E\uDD76";
            case 2: return "\uD83D\uDE30";
            case 3: return "\uD83D\uDE0A";
            case 4: return "\uD83D\uDE05";
            default: return "\uD83E\uDD75";
        }
    }

    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
