package org.example.xyawalongserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.xyawalongserver.model.entity.UsageStatistics;
import org.example.xyawalongserver.model.entity.UsageStatisticsUpdateResult;
import org.example.xyawalongserver.service.UsageStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// UsageStatisticsController.java
@RestController
@RequestMapping("/api/usage-statistics")
@Slf4j
public class UsageStatisticsController {

    private final UsageStatisticsService usageStatisticsService;

    public UsageStatisticsController(UsageStatisticsService usageStatisticsService) {
        this.usageStatisticsService = usageStatisticsService;
    }

    @PostMapping("/update/{warehouseId}/{ingredientId}")
    public ResponseEntity<UsageStatisticsUpdateResult> updateUsageStatistics(
            @PathVariable Long warehouseId,
            @PathVariable Long ingredientId) {
        UsageStatisticsUpdateResult result = usageStatisticsService
                .updateUsageStatistics(warehouseId, ingredientId);
        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/batch-update")
    public ResponseEntity<List<UsageStatisticsUpdateResult>> batchUpdateUsageStatistics(
            @RequestParam(required = false) Long warehouseId) {
        List<UsageStatisticsUpdateResult> results = usageStatisticsService
                .batchUpdateUsageStatistics(warehouseId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{warehouseId}/{ingredientId}")
    public ResponseEntity<UsageStatistics> getUsageStatistics(
            @PathVariable Long warehouseId,
            @PathVariable Long ingredientId) {
        Optional<UsageStatistics> stats = usageStatisticsService
                .getUsageStatistics(warehouseId, ingredientId);
        return stats.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}