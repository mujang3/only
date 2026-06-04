package yu.aihackerton.fingerbot.dto;

import yu.aihackerton.fingerbot.model.HeatingHistory;
import java.util.List;

public class StatsDto {
    private int todayRuntimeMin;
    private long todaySave;
    private int todayLogs;
    private int totalRuntimeMin;
    private long totalSave;
    private int totalLogs;
    private String lastBoilerState;
    private double avgFeelsLike;
    private List<HeatingHistory> recent;

    public int getTodayRuntimeMin() { return todayRuntimeMin; }
    public void setTodayRuntimeMin(int todayRuntimeMin) { this.todayRuntimeMin = todayRuntimeMin; }
    public long getTodaySave() { return todaySave; }
    public void setTodaySave(long todaySave) { this.todaySave = todaySave; }
    public int getTodayLogs() { return todayLogs; }
    public void setTodayLogs(int todayLogs) { this.todayLogs = todayLogs; }
    public int getTotalRuntimeMin() { return totalRuntimeMin; }
    public void setTotalRuntimeMin(int totalRuntimeMin) { this.totalRuntimeMin = totalRuntimeMin; }
    public long getTotalSave() { return totalSave; }
    public void setTotalSave(long totalSave) { this.totalSave = totalSave; }
    public int getTotalLogs() { return totalLogs; }
    public void setTotalLogs(int totalLogs) { this.totalLogs = totalLogs; }
    public String getLastBoilerState() { return lastBoilerState; }
    public void setLastBoilerState(String lastBoilerState) { this.lastBoilerState = lastBoilerState; }
    public double getAvgFeelsLike() { return avgFeelsLike; }
    public void setAvgFeelsLike(double avgFeelsLike) { this.avgFeelsLike = avgFeelsLike; }
    public List<HeatingHistory> getRecent() { return recent; }
    public void setRecent(List<HeatingHistory> recent) { this.recent = recent; }
}
