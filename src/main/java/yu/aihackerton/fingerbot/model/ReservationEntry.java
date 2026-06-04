package yu.aihackerton.fingerbot.model;

public class ReservationEntry {
    private String id;
    private String label;
    private String arrivalTime;
    private String startTime;
    private int runtimeMin;
    private int bufferMin;
    private int prefStep;
    private boolean triggered;
    private String scheduledDate;
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public int getRuntimeMin() { return runtimeMin; }
    public void setRuntimeMin(int runtimeMin) { this.runtimeMin = runtimeMin; }
    public int getBufferMin() { return bufferMin; }
    public void setBufferMin(int bufferMin) { this.bufferMin = bufferMin; }
    public int getPrefStep() { return prefStep; }
    public void setPrefStep(int prefStep) { this.prefStep = prefStep; }
    public boolean isTriggered() { return triggered; }
    public void setTriggered(boolean triggered) { this.triggered = triggered; }
    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
