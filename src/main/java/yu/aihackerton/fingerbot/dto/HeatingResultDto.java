package yu.aihackerton.fingerbot.dto;

/**
 * 서버가 계산해서 프론트로 돌려주는 결과.
 * 체감온도, 목표온도, 보일러 가동시간, 절감액, 쾌적 단계 정보를 담는다.
 */
public class HeatingResultDto {

    private double feelsLike;     // 실내 체감온도(°C)
    private double optimalTemp;   // ASHRAE 적응형 최적온도(°C)
    private double myTarget;      // 선호 단계 반영한 내 목표온도(°C)
    private double gap;           // 목표 - 체감 (양수면 난방 필요)
    private int runtimeMin;       // 권장 보일러 가동시간(분)
    private Double outdoorFeels;  // 외기 체감온도(°C). 미연동 시 null

    private int comfortStep;      // 쾌적 단계 1~5
    private String comfortLabel;  // "쾌적해요" 등
    private String comfortEmoji;  // 이모지

    private int saveRate;         // 절감률(%)
    private int saveDaily;        // 일 절감액(원)
    private long saveMonthly;     // 월 절감액(원)

    private String boilerState;   // "heating" | "approaching" | "reached"

    private double virtualTarget; // 오버슈트 차감한 가상 목표온도(°C)
    private double deltaC;        // 가상목표 - 현재체감 (ΔC)
    private Double boilerSetTemp; // 보일러에 내릴 설정 온도(°C). OFF면 null
    private String boilerCommand; // 사람이 읽는 제어 명령 설명
    private String alertMsg;      // 습도 기반 권장 알림. 없으면 null
    private String alertType;     // "humidify" | "ventilate" | null

    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }

    public double getOptimalTemp() { return optimalTemp; }
    public void setOptimalTemp(double optimalTemp) { this.optimalTemp = optimalTemp; }

    public double getMyTarget() { return myTarget; }
    public void setMyTarget(double myTarget) { this.myTarget = myTarget; }

    public double getGap() { return gap; }
    public void setGap(double gap) { this.gap = gap; }

    public int getRuntimeMin() { return runtimeMin; }
    public void setRuntimeMin(int runtimeMin) { this.runtimeMin = runtimeMin; }

    public Double getOutdoorFeels() { return outdoorFeels; }
    public void setOutdoorFeels(Double outdoorFeels) { this.outdoorFeels = outdoorFeels; }

    public int getComfortStep() { return comfortStep; }
    public void setComfortStep(int comfortStep) { this.comfortStep = comfortStep; }

    public String getComfortLabel() { return comfortLabel; }
    public void setComfortLabel(String comfortLabel) { this.comfortLabel = comfortLabel; }

    public String getComfortEmoji() { return comfortEmoji; }
    public void setComfortEmoji(String comfortEmoji) { this.comfortEmoji = comfortEmoji; }

    public int getSaveRate() { return saveRate; }
    public void setSaveRate(int saveRate) { this.saveRate = saveRate; }

    public int getSaveDaily() { return saveDaily; }
    public void setSaveDaily(int saveDaily) { this.saveDaily = saveDaily; }

    public long getSaveMonthly() { return saveMonthly; }
    public void setSaveMonthly(long saveMonthly) { this.saveMonthly = saveMonthly; }

    public String getBoilerState() { return boilerState; }
    public void setBoilerState(String boilerState) { this.boilerState = boilerState; }

    public double getVirtualTarget() { return virtualTarget; }
    public void setVirtualTarget(double virtualTarget) { this.virtualTarget = virtualTarget; }

    public double getDeltaC() { return deltaC; }
    public void setDeltaC(double deltaC) { this.deltaC = deltaC; }

    public Double getBoilerSetTemp() { return boilerSetTemp; }
    public void setBoilerSetTemp(Double boilerSetTemp) { this.boilerSetTemp = boilerSetTemp; }

    public String getBoilerCommand() { return boilerCommand; }
    public void setBoilerCommand(String boilerCommand) { this.boilerCommand = boilerCommand; }

    public String getAlertMsg() { return alertMsg; }
    public void setAlertMsg(String alertMsg) { this.alertMsg = alertMsg; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
}
