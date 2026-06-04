package yu.aihackerton.fingerbot.service;

import org.springframework.stereotype.Service;
import yu.aihackerton.fingerbot.dto.CalcRequestDto;
import yu.aihackerton.fingerbot.dto.HeatingResultDto;
import yu.aihackerton.fingerbot.dto.ReservationRequestDto;
import yu.aihackerton.fingerbot.model.ReservationEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReservationService {

    private final List<ReservationEntry> reservations = new ArrayList<>();
    private final HeatingService heatingService;

    private static final int BUFFER_MIN = 10;
    private static final int MIN_RUNTIME = 15;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReservationService(HeatingService heatingService) {
        this.heatingService = heatingService;
    }

    public ReservationEntry create(ReservationRequestDto req) {
        LocalTime arrival = LocalTime.parse(req.getArrivalTime());

        CalcRequestDto calcReq = new CalcRequestDto();
        calcReq.setIndoorTemp(req.getIndoorTemp());
        calcReq.setIndoorHum(req.getIndoorHum() > 0 ? req.getIndoorHum() : 45.0);
        calcReq.setOutdoorTemp(req.getOutdoorTemp());
        calcReq.setArea(req.getArea() > 0 ? req.getArea() : 30);
        calcReq.setInsul(req.getInsul() != null ? req.getInsul() : "2");
        calcReq.setPrefStep(req.getPrefStep() > 0 ? req.getPrefStep() : 3);

        HeatingResultDto result = heatingService.calculate(calcReq, null);
        int runtime = Math.max(MIN_RUNTIME, result.getRuntimeMin());
        LocalTime startTime = arrival.minusMinutes(runtime + BUFFER_MIN);

        ReservationEntry entry = new ReservationEntry();
        entry.setId(UUID.randomUUID().toString().substring(0, 8));
        entry.setLabel(req.getLabel() != null && !req.getLabel().isBlank() ? req.getLabel() : "귀가 예약");
        entry.setArrivalTime(arrival.format(TIME_FMT));
        entry.setStartTime(startTime.format(TIME_FMT));
        entry.setRuntimeMin(runtime);
        entry.setBufferMin(BUFFER_MIN);
        entry.setPrefStep(req.getPrefStep() > 0 ? req.getPrefStep() : 3);
        entry.setTriggered(false);
        entry.setScheduledDate(LocalDate.now().format(DATE_FMT));
        entry.setCreatedAt(LocalDateTime.now().format(DT_FMT));

        synchronized (reservations) {
            reservations.add(0, entry);
        }
        return entry;
    }

    public List<ReservationEntry> getAll() {
        synchronized (reservations) {
            return new ArrayList<>(reservations);
        }
    }

    public boolean delete(String id) {
        synchronized (reservations) {
            return reservations.removeIf(r -> r.getId().equals(id));
        }
    }
}
