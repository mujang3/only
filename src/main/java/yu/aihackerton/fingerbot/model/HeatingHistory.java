package yu.aihackerton.fingerbot.model;

public class HeatingHistory {
    private String id;
    private double feelsLike;
    private double myTarget;
    private String boilerState;
    private int runtimeMin;
    private int saveDaily;
    private String loggedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }
    public double getMyTarget() { return myTarget; }
    public void setMyTarget(double myTarget) { this.myTarget = myTarget; }
    public String getBoilerState() { return boilerState; }
    public void setBoilerState(String boilerState) { this.boilerState = boilerState; }
    public int getRuntimeMin() { return runtimeMin; }
    public void setRuntimeMin(int runtimeMin) { this.runtimeMin = runtimeMin; }
    public int getSaveDaily() { return saveDaily; }
    public void setSaveDaily(int saveDaily) { this.saveDaily = saveDaily; }
    public String getLoggedAt() { return loggedAt; }
    public void setLoggedAt(String loggedAt) { this.loggedAt = loggedAt; }
}
