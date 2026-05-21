package yu.aihackerton.fingerbot.dto;

/**
 * 프론트엔드가 /api/calc 로 보내는 입력값.
 * 실내 온습도, 외기온, 평수, 단열등급, 선호 단계를 담는다.
 */
public class CalcRequestDto {

    private double indoorTemp;   // 실내 온도(°C)
    private double indoorHum;    // 실내 습도(%)
    private Double outdoorTemp;  // 외기 온도(°C). 날씨 미연동 시 null
    private double outdoorHum;   // 외기 습도(%)
    private double wind;         // 풍속(m/s)
    private int area;            // 평수
    private String insul;        // 단열등급 "1"(불량) "2"(보통) "3"(양호)
    private int prefStep;        // 선호 쾌적 단계 1~5 (3=표준)

    public double getIndoorTemp() { return indoorTemp; }
    public void setIndoorTemp(double indoorTemp) { this.indoorTemp = indoorTemp; }

    public double getIndoorHum() { return indoorHum; }
    public void setIndoorHum(double indoorHum) { this.indoorHum = indoorHum; }

    public Double getOutdoorTemp() { return outdoorTemp; }
    public void setOutdoorTemp(Double outdoorTemp) { this.outdoorTemp = outdoorTemp; }

    public double getOutdoorHum() { return outdoorHum; }
    public void setOutdoorHum(double outdoorHum) { this.outdoorHum = outdoorHum; }

    public double getWind() { return wind; }
    public void setWind(double wind) { this.wind = wind; }

    public int getArea() { return area; }
    public void setArea(int area) { this.area = area; }

    public String getInsul() { return insul; }
    public void setInsul(String insul) { this.insul = insul; }

    public int getPrefStep() { return prefStep; }
    public void setPrefStep(int prefStep) { this.prefStep = prefStep; }
}
