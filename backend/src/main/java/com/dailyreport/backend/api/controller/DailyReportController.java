package com.dailyreport.backend.api.controller;

import com.dailyreport.backend.api.dto.DailyReportRequest;
import com.dailyreport.backend.api.dto.DailyReportResponse;
import com.dailyreport.backend.service.DailyReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;

    @GetMapping
    public ResponseEntity<List<DailyReportResponse>> getMyReports(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyReportService.getMyReports(userDetails.getUsername()));
    }

    @GetMapping("/public")
    public ResponseEntity<List<DailyReportResponse>> getPublicReports() {
        return ResponseEntity.ok(dailyReportService.getPublicReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DailyReportResponse> getMyReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyReportService.getMyReport(id, userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<DailyReportResponse> create(
            @RequestBody @Valid DailyReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        DailyReportResponse response = dailyReportService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DailyReportResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid DailyReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dailyReportService.update(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        dailyReportService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
