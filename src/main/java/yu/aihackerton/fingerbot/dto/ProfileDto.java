package yu.aihackerton.fingerbot.dto;

/**
 * 온보딩에서 수집하는 사용자 프로필.
 * 지난달 가스비가 있으면 절감액을 실제 요금 기준으로 계산할 수 있다.
 */
public class ProfileDto {

    private String type;       // 주거 유형 (원룸/오피스텔/빌라)
    private int area;          // 평수
    private String yearBand;   // 건물 연식대
    private Integer arrive;    // 귀가 시간(시)
    private double targetTemp; // 선호 온도
    private Integer lastGas;   // 지난달 가스비(원). 없으면 null
    private String gasType;    // 가스 요금제 (도시가스/LPG)
    private String ageGroup;   // 연령대 (20대/30대/40대/50대+)
    private Integer savingsGoal; // 월 절약 목표 금액(원). 없으면 null

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getArea() { return area; }
    public void setArea(int area) { this.area = area; }

    public String getYearBand() { return yearBand; }
    public void setYearBand(String yearBand) { this.yearBand = yearBand; }

    public Integer getArrive() { return arrive; }
    public void setArrive(Integer arrive) { this.arrive = arrive; }

    public double getTargetTemp() { return targetTemp; }
    public void setTargetTemp(double targetTemp) { this.targetTemp = targetTemp; }

    public Integer getLastGas() { return lastGas; }
    public void setLastGas(Integer lastGas) { this.lastGas = lastGas; }

    public String getGasType() { return gasType; }
    public void setGasType(String gasType) { this.gasType = gasType; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public Integer getSavingsGoal() { return savingsGoal; }
    public void setSavingsGoal(Integer savingsGoal) { this.savingsGoal = savingsGoal; }
}
