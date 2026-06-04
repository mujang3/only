package yu.aihackerton.fingerbot.dto;

public class ChatSettingsDto {
    private double indoorTemp;
    private double indoorHum;
    private Double outdoorTemp;
    private int area;
    private String insul;
    private int prefStep;
    private Integer lastGas;

    public double getIndoorTemp() { return indoorTemp; }
    public void setIndoorTemp(double indoorTemp) { this.indoorTemp = indoorTemp; }
    public double getIndoorHum() { return indoorHum; }
    public void setIndoorHum(double indoorHum) { this.indoorHum = indoorHum; }
    public Double getOutdoorTemp() { return outdoorTemp; }
    public void setOutdoorTemp(Double outdoorTemp) { this.outdoorTemp = outdoorTemp; }
    public int getArea() { return area; }
    public void setArea(int area) { this.area = area; }
    public String getInsul() { return insul; }
    public void setInsul(String insul) { this.insul = insul; }
    public int getPrefStep() { return prefStep; }
    public void setPrefStep(int prefStep) { this.prefStep = prefStep; }
    public Integer getLastGas() { return lastGas; }
    public void setLastGas(Integer lastGas) { this.lastGas = lastGas; }
}
