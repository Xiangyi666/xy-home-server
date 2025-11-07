package org.example.xyawalongserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.xyawalongserver.model.entity.StockLevelUpdateResult;
import org.example.xyawalongserver.service.StockLevelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// StockLevelController.java
@RestController
@RequestMapping("/api/stock-levels")
@Slf4j
public class StockLevelController {

    private final StockLevelService stockLevelService;

    public StockLevelController(StockLevelService stockLevelService) {
        this.stockLevelService = stockLevelService;
    }

    @PostMapping("/update-all")
    public ResponseEntity<StockLevelUpdateResult> updateAllStockLevels() {
        StockLevelUpdateResult result = stockLevelService.updateAllStockLevels();
        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/update-batches")
    public ResponseEntity<StockLevelUpdateResult> updateBatchStockLevels() {
        StockLevelUpdateResult result = stockLevelService.updateAllStockLevels();
        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/update-summaries")
    public ResponseEntity<StockLevelUpdateResult> updateSummaryStockLevels() {
        StockLevelUpdateResult result = stockLevelService.updateAllSummaryStockLevels();
        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/update-warehouse/{warehouseId}")
    public ResponseEntity<StockLevelUpdateResult> updateStockLevelsByWarehouse(@PathVariable Long warehouseId) {
        StockLevelUpdateResult result = stockLevelService.updateStockLevelsByWarehouse(warehouseId);
        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

//    @GetMapping("/distribution")
//    public ResponseEntity<StockLevelDistribution> getStockLevelDistribution() {
//        StockLevelDistribution distribution = stockLevelService.getStockLevelDistribution();
//        return ResponseEntity.ok(distribution);
//    }
}