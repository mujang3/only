package yu.aihackerton.fingerbot.service;

import org.springframework.stereotype.Service;
import yu.aihackerton.fingerbot.dto.HeatingResultDto;
import yu.aihackerton.fingerbot.dto.StatsDto;
import yu.aihackerton.fingerbot.model.HeatingHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    private final Deque<HeatingHistory> logs = new ArrayDeque<>();
    private static final int MAX_LOGS = 200;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void record(HeatingResultDto result) {
        HeatingHistory h = new HeatingHistory();
        h.setId(UUID.randomUUID().toString().substring(0, 8));
        h.setFeelsLike(result.getFeelsLike());
        h.setMyTarget(result.getMyTarget());
        h.setBoilerState(result.getBoilerState());
        h.setRuntimeMin(result.getRuntimeMin());
        h.setSaveDaily(result.getSaveDaily());
        h.setLoggedAt(LocalDateTime.now().format(FMT));
        synchronized (logs) {
            logs.addFirst(h);
            while (logs.size() > MAX_LOGS) logs.removeLast();
        }
    }

    public List<HeatingHistory> getRecent(int n) {
        synchronized (logs) {
            return logs.stream().limit(n).collect(Collectors.toList());
        }
    }

    public StatsDto getStats() {
        List<HeatingHistory> all;
        synchronized (logs) {
            all = new ArrayList<>(logs);
        }
        String todayPrefix = LocalDate.now().toString();
        List<HeatingHistory> today = all.stream()
                .filter(h -> h.getLoggedAt().startsWith(todayPrefix))
                .collect(Collectors.toList());

        StatsDto s = new StatsDto();
        s.setTodayLogs(today.size());
        s.setTodayRuntimeMin(today.stream().mapToInt(HeatingHistory::getRuntimeMin).sum());
        s.setTodaySave(today.stream().mapToLong(HeatingHistory::getSaveDaily).sum());
        s.setTotalLogs(all.size());
        s.setTotalRuntimeMin(all.stream().mapToInt(HeatingHistory::getRuntimeMin).sum());
        s.setTotalSave(all.stream().mapToLong(HeatingHistory::getSaveDaily).sum());
        s.setLastBoilerState(all.isEmpty() ? "none" : all.get(0).getBoilerState());
        s.setAvgFeelsLike(
                Math.round(today.stream().mapToDouble(HeatingHistory::getFeelsLike).average().orElse(0.0) * 10) / 10.0
        );
        s.setRecent(all.stream().limit(10).collect(Collectors.toList()));
        return s;
    }
}
