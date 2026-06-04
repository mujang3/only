package yu.aihackerton.fingerbot.controller;

import org.springframework.web.bind.annotation.*;
import yu.aihackerton.fingerbot.dto.ReservationRequestDto;
import yu.aihackerton.fingerbot.dto.StatsDto;
import yu.aihackerton.fingerbot.model.HeatingHistory;
import yu.aihackerton.fingerbot.model.ReservationEntry;
import yu.aihackerton.fingerbot.service.HistoryService;
import yu.aihackerton.fingerbot.service.ReservationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;
    private final HistoryService historyService;

    public ReservationController(ReservationService reservationService, HistoryService historyService) {
        this.reservationService = reservationService;
        this.historyService = historyService;
    }

    @PostMapping("/reservation")
    public ReservationEntry createReservation(@RequestBody ReservationRequestDto req) {
        return reservationService.create(req);
    }

    @GetMapping("/reservations")
    public List<ReservationEntry> getReservations() {
        return reservationService.getAll();
    }

    @DeleteMapping("/reservation/{id}")
    public Map<String, Boolean> deleteReservation(@PathVariable String id) {
        return Map.of("deleted", reservationService.delete(id));
    }

    @GetMapping("/stats")
    public StatsDto getStats() {
        return historyService.getStats();
    }

    @GetMapping("/history")
    public List<HeatingHistory> getHistory() {
        return historyService.getRecent(20);
    }
}
