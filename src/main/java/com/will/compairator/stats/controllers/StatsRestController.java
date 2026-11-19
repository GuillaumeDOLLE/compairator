package com.will.compairator.stats.controllers;

import com.will.compairator.stats.dto.StatsDTO;
import com.will.compairator.stats.services.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.*;

@RestController
@RequestMapping("/stats")
public class StatsRestController {
    private final StatsService stats;
    public StatsRestController(StatsService stats) { this.stats = stats; }

    @GetMapping
    public StatsDTO statistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate end = to == null ? LocalDate.now(ZoneOffset.UTC).plusDays(1) : to;
        return stats.calculate(from == null ? end.minusDays(30) : from, end);
    }
}
