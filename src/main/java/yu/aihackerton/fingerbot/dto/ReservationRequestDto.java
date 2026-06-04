package yu.aihackerton.fingerbot.dto;

public class ReservationRequestDto {
    private String arrivalTime;
    private int prefStep;
    private double indoorTemp;
    private double indoorHum;
    private Double outdoorTemp;
    private int area;
    private String insul;
    private String label;

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public int getPrefStep() { return prefStep; }
    public void setPrefStep(int prefStep) { this.prefStep = prefStep; }
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
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
