package yu.aihackerton.fingerbot.dto;

public class ChatRequestDto {
    private String message;
    private Double feelsLike;
    private Double targetTemp;
    private String boilerState;
    private Double outdoorTemp;
    private Double indoorHum;
    private Integer runtimeMin;
    private Integer prefStep;
    private Long saveMonthly;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(Double feelsLike) { this.feelsLike = feelsLike; }

    public Double getTargetTemp() { return targetTemp; }
    public void setTargetTemp(Double targetTemp) { this.targetTemp = targetTemp; }

    public String getBoilerState() { return boilerState; }
    public void setBoilerState(String boilerState) { this.boilerState = boilerState; }

    public Double getOutdoorTemp() { return outdoorTemp; }
    public void setOutdoorTemp(Double outdoorTemp) { this.outdoorTemp = outdoorTemp; }

    public Double getIndoorHum() { return indoorHum; }
    public void setIndoorHum(Double indoorHum) { this.indoorHum = indoorHum; }

    public Integer getRuntimeMin() { return runtimeMin; }
    public void setRuntimeMin(Integer runtimeMin) { this.runtimeMin = runtimeMin; }

    public Integer getPrefStep() { return prefStep; }
    public void setPrefStep(Integer prefStep) { this.prefStep = prefStep; }

    public Long getSaveMonthly() { return saveMonthly; }
    public void setSaveMonthly(Long saveMonthly) { this.saveMonthly = saveMonthly; }
}
