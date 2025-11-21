package org.example.xyawalongserver.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.xyawalongserver.model.dto.request.*;
import org.example.xyawalongserver.model.dto.response.ExpiringIngredientDTO;
import org.example.xyawalongserver.model.dto.response.*;
import org.example.xyawalongserver.model.entity.Batch;
import org.example.xyawalongserver.model.entity.FamilyItemDTO;
import org.example.xyawalongserver.model.entity.IngredientBatch;
import org.example.xyawalongserver.model.entity.Warehouse;
import org.example.xyawalongserver.service.BatchService;
import org.example.xyawalongserver.service.UserService;
import org.example.xyawalongserver.service.WarehouseService;
import org.example.xyawalongserver.util.FamilyPermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;
    private static final Logger logger = LoggerFactory.getLogger(WarehouseController.class);



    private final ObjectMapper objectMapper;
    @Autowired
    private UserService userService;

    public WarehouseController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Data
    public static class DiscardBatchRequest {
        private Long warehouseId;
        private Long batchId;
        private String note;
    }
    /**
     * 创建仓库
     * POST /api/warehouses
     * fix 不应该在这里
     */
    @PostMapping("/create")
    public ApiResponse<WarehouseDTO> createWarehouse(@RequestBody CreateWarehouseRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        Warehouse warehouse = userService.createWarehouse(userId,request.getName(),
                request.getDescription(),
                request.getFamilyId());
        WarehouseDTO dto = convertToDTO(warehouse);
        return ApiResponse.success(dto);
    }

    /**
     * 获取用户的所有仓库
     * GET /api/warehouses/user/
     */
    @GetMapping("/getAll")
    public ApiResponse<List<WarehouseDTO>> getUserWarehouses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<WarehouseDTO> warehouses = warehouseService.getUserWarehouses(userId);
        return ApiResponse.success(warehouses);
    }

    /**
     * 获取用户的所有仓库
     * GET /api/warehouses/user/
     */
    @PostMapping("/getAllByFamily")
    public ApiResponse<List<WarehouseDTO>> getUserWarehousesByFamily(@RequestBody CreateWarehouseRequest cRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<WarehouseDTO> warehouses = warehouseService.getUserWarehousesInFamily(userId, cRequest.getFamilyId());
        return ApiResponse.success(warehouses);
    }


    /**
     * 获取用户的仓库统计信息
     * GET /api/warehouses/user/with-stats
     */
    @GetMapping("/user/with-stats")
    public ResponseEntity<List<WarehouseWithStatsDTO>> getUserWarehousesWithStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<WarehouseWithStatsDTO> warehouses = warehouseService.getUserWarehousesWithStats(userId);
        return ResponseEntity.ok(warehouses);
    }

    /**
     * 获取仓库详情
     * GET /api/warehouses/{warehouseId}
     */
    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseDTO> getWarehouseDetail(@PathVariable Long warehouseId) {
        WarehouseDTO warehouse = warehouseService.getWarehouseDetail(warehouseId);
        return ResponseEntity.ok(warehouse);
    }

    /**
     * 获取家庭所有物品
     */
    @PostMapping("/family/getAllGoods")
    public ApiResponse<List<FamilyItemDTO>> getFamilyItems(@RequestBody Map<String, Object> request) {
        Object familyIdObj = request.get("familyId");
        Long familyId = Long.valueOf(familyIdObj.toString());

        List<FamilyItemDTO> items = warehouseService.getFamilyBatches(familyId);
        return ApiResponse.success(items, "获取物品列表成功");
    }

    /**
     * 获取仓库中所有物品
     * 如果有参数查询指定仓库
     * GET /api/warehouses/getAllGoods/{warehouseId}
     */
    @PostMapping("getAllGoodsInWarehouse")
    public ApiResponse<List<WarehouseIngredientDTO>> getAllGoodsInWarehouse(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Long warehouseId = (Long) request.get("warehouseId");
        List<WarehouseIngredientDTO> goodsList = warehouseService.getWarehouseIngredients(warehouseId);
        return ApiResponse.success(goodsList);///
    }

    /**
     * 获取仓库中所有即将过期的物品
     * GET /api/warehouses/getAllGoods/{warehouseId}
     */
    @PostMapping("/all-expiring-items")
    public ResponseEntity<List<ExpiringIngredientDTO>> getAllExpiringItems(
            @RequestBody ExpiringItemsRequest request) {
        Long warehouseId = request.getWarehouseId();
        // 参数验证
        if (request.getDays() != null && request.getDays() <= 0) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<ExpiringIngredientDTO> expiringItems = warehouseService.getExpiringIngredients(request.getWarehouseId(), request.getDays());
        return ResponseEntity.ok(expiringItems);
    }

    /**
     * 更新仓库信息
     * PUT /api/warehouses/{warehouseId}
     */
    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseDTO> updateWarehouse(
            @PathVariable Long warehouseId,
            @RequestBody UpdateWarehouseRequest request) {
        WarehouseDTO warehouse = warehouseService.updateWarehouse(warehouseId, request);
        return ResponseEntity.ok(warehouse);
    }
    /**
     * 4. 往某个仓库录入原料
     * POST /api/warehouses/{warehouseId}/stock-in
     */
    @PostMapping("/stock-in")
    public ApiResponse<Batch> stockIn(
            @RequestBody StockInRequest request) {
        Long warehouseId = request.getWarehouseId();
        // 设置仓库ID
        request.setWarehouseId(warehouseId);

        Batch batch = warehouseService.stockIn(request);
        return ApiResponse.success(batch);
    }
    // java
    @PostMapping("/discard-batch")
    public ApiResponse<Void> discardBatch(@RequestBody DiscardBatchRequest request) {
        Long warehouseId = request.getWarehouseId();
        Long batchId = request.getBatchId();

        if (warehouseId == null || batchId == null) {
            return ApiResponse.error("warehouseId 和 batchId 不能为空");
        }

        try {
            warehouseService.discardBatch(warehouseId, batchId, request.getNote());
            return ApiResponse.success(null, "批次已弃置");
        } catch (Exception e) {
            logger.error("弃置批次失败: warehouseId={}, batchId={}, err={}", warehouseId, batchId, e.getMessage(), e);
            return ApiResponse.error("弃置失败: " + e.getMessage());
        }
    }
    /**
     * . 往某个仓库减少材料
     * POST /api/warehouses/{warehouseId}/stock-in
     */
    @PostMapping("/stock-out")
    public ApiResponse<WarehouseService.StockOutResult> stockOut(
            @RequestBody StockOutRequest request) {
        Long warehouseId = request.getWarehouseId();
        try {
            WarehouseService.StockOutResult result = warehouseService.stockOutByBatch(warehouseId, request);
            return ApiResponse.success(result);
        } catch (IllegalArgumentException e) {
            logger.warn("出库参数错误: warehouseId={}, err={}", warehouseId, e.getMessage());
            return ApiResponse.error("出库失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("出库处理异常: warehouseId={}, err={}", warehouseId, e.getMessage(), e);
            return ApiResponse.error("出库失败: " + (e.getMessage() != null ? e.getMessage() : "系统错误"));
        }
//        return ApiResponse.success(warehouseService.stockOutByBatch(warehouseId,request));
    }

    /**
     * 带预检查的批量出库
     */
    @PostMapping("/stock-out-batch")
    public ApiResponse<BatchStockOutResult> batchStockOutWithValidation(@Valid @RequestBody BatchStockOutRequest request) {

        logger.info("接收带预检查的批量出库请求: 仓库ID={}, 批次数量={}",
                request.getWarehouseId(), request.getBatchConsumptions().size());

        try {
            // 参数验证
            if (request.getWarehouseId() == null) {
                return ApiResponse.error("仓库ID不能为空");
            }

            if (request.getBatchConsumptions() == null || request.getBatchConsumptions().isEmpty()) {
                return ApiResponse.error("出库批次不能为空");
            }

            // 执行带预检查的批量出库
            BatchStockOutResult result = warehouseService.batchStockOut(request);

            if (result.isSuccess()) {
                logger.info("带预检查批量出库成功: 仓库ID={}, 成功批次={}/{}",
                        request.getWarehouseId(),
                        result.getSuccessfulBatches(),
                        result.getTotalBatches());
                return ApiResponse.success(result);
            } else {
                logger.warn("带预检查批量出库失败: 仓库ID={}, 原因: {}",
                        request.getWarehouseId(), result.getMessage());
                return ApiResponse.error(result.getMessage()+result.toString());
            }

        } catch (Exception e) {
            logger.error("带预检查批量出库系统错误: 仓库ID={}, 错误: {}",
                    request.getWarehouseId(), e.getMessage(), e);
            return ApiResponse.error("系统错误: " + e.getMessage());
        }
    }


    // 内部类：批次操作记录
    @Data
    @AllArgsConstructor
    public static class stockBatchDto{
        private Long warehouseId;
        private List<StockInRequest> stockInRequests;
    }
    /**
     * 往某个仓库批量录入原料
     * POST /api/warehouses/{warehouseId}/stock-in
     */
    @PostMapping("/stock-in-batch")
    public ApiResponse<List<Batch>> stockInBatch(
            @RequestBody stockBatchDto request) {
        List<Batch> batches = warehouseService.stockInBatch(request.getWarehouseId(),request.getStockInRequests());
        return ApiResponse.success(batches);
    }

    /**
     * 删除仓库
     * DELETE /api/warehouses/{warehouseId}
     */
    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long warehouseId) {
        warehouseService.deleteWarehouse(warehouseId);
        return ResponseEntity.ok().build();
    }

    /**
     * 搜索仓库
     * GET /api/warehouses/user/search?keyword=冰箱
     */
    @GetMapping("/user/search")
    public ApiResponse<List<WarehouseDTO>> searchWarehouses(
            @RequestParam String keyword, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<WarehouseDTO> warehouses = warehouseService.searchWarehouses(userId, keyword);
        return ApiResponse.success(warehouses);
    }

    /**
     * 验证仓库所有权
     * GET /api/warehouses/{warehouseId}/validate-ownership?userId=1
     */
    @GetMapping("/{warehouseId}/validate-ownership")
    public ResponseEntity<Boolean> validateOwnership(
            @PathVariable Long warehouseId,
            @RequestParam Long userId) {
        boolean isValid = warehouseService.validateWarehouseOwnership(warehouseId, userId);
        return ResponseEntity.ok(isValid);
    }

    // 辅助方法：转换实体为DTO
    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setDescription(warehouse.getDescription());
        dto.setLocation(warehouse.getLocation());
        dto.setUserId(warehouse.getUser().getId());
        dto.setUserName(warehouse.getUser().getUsername());
        dto.setCreatedTime(warehouse.getCreatedTime());
        return dto;
    }
}