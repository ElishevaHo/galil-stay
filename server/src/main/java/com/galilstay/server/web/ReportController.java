package com.galilstay.server.web;

import com.galilstay.server.dto.ReportDtos.MonthRow;
import com.galilstay.server.dto.ReportDtos.Overview;
import com.galilstay.server.dto.ReportDtos.UnitRow;
import com.galilstay.server.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @GetMapping("/overview")
    public Overview overview() {
        return service.overview();
    }

    @GetMapping("/months")
    public List<MonthRow> months(@RequestParam(required = false) Integer year) {
        return service.yearlyBreakdown(year == null ? LocalDate.now().getYear() : year);
    }

    @GetMapping("/units")
    public List<UnitRow> units() {
        return service.unitPerformance();
    }
}
