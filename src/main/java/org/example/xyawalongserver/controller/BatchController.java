package org.example.xyawalongserver.controller;

import lombok.RequiredArgsConstructor;
import org.example.xyawalongserver.model.entity.IngredientBatch;
import org.example.xyawalongserver.service.BatchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @GetMapping("/expiring-soon")
    public List<IngredientBatch> getExpiringSoon() {
        return batchService.getBatchesExpiringSoon();
    }

    @GetMapping("/ingredient/{ingredientId}")
    public List<IngredientBatch> getBatchesByIngredient(@PathVariable Long ingredientId) {
        return batchService.getAvailableBatches(ingredientId);
    }

    @GetMapping("/search")
    public List<IngredientBatch> searchBatches(
            @RequestParam(required = false) Long ingredientId,
            @RequestParam(required = false) Boolean isConsumed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return batchService.searchBatches(ingredientId, isConsumed, startDate, endDate);
    }
}