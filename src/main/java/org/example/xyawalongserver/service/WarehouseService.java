package org.example.xyawalongserver.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.xyawalongserver.model.dto.request.*;
import org.example.xyawalongserver.model.dto.response.BatchStockOutResult;
import org.example.xyawalongserver.model.dto.response.ExpiringIngredientDTO;
import org.example.xyawalongserver.model.dto.response.WarehouseIngredientDTO;
import org.example.xyawalongserver.model.entity.*;
import org.example.xyawalongserver.repository.*;

import org.example.xyawalongserver.util.FamilyPermissionUtil;
import org.example.xyawalongserver.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;

@Service
@Transactional
public class WarehouseService {

    @Autowired
    private InventorySummaryRepository inventorySummaryRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsageStatisticsService usageStatisticsService;
    @Autowired
    private BatchStockOutRepository batchStockOutRepository;

    @Autowired
    private FamilyPermissionUtil familyPermissionUtil;
    @Autowired
    private FamilyRepository familyRepository;

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

    /**
     * 获取用户的所有仓库
     */
    public List<WarehouseDTO> getUserWarehouses(Long userId) {
        List<Warehouse> warehouses = warehouseRepository.findByUser_Id(userId);
        return warehouses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    /*
    * 创建仓库
    * */
    public Warehouse createWarehouse(String name, Long familyId, Long userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证家族是否存在
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("家族不存在"));

        // 检查仓库名是否重复
        if (warehouseRepository.findByUser_IdAndName(userId, name).isPresent()) {
            throw new RuntimeException("该仓库名称已存在");
        }

        // 创建新仓库
        Warehouse warehouse = new Warehouse();
        warehouse.setName(name);
        warehouse.setFamily(family);
        warehouse.setUser(user);
        warehouse.setCreatedTime(LocalDateTime.now());

        return warehouseRepository.save(warehouse);
    }
    /**
     * 获取用户在某个家庭下的所有仓库
     */
    public List<WarehouseDTO> getUserWarehousesInFamily(Long userId, Long familyId) {
        // 先检查用户是否属于该家庭
        familyPermissionUtil.checkUserInFamily(userId, familyId);

        List<Warehouse> warehouses = warehouseRepository.findByFamilyId(familyId);
        return warehouses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    /**
     * 获取仓库详情
     */
    public WarehouseDTO getWarehouseDetail(Long warehouseId) {
        Long userId = UserContext.getCurrentUserId(); // 直接获取
        familyPermissionUtil.checkUserPermissionByWarehouseId(userId, warehouseId);

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("仓库不存在"));
        return convertToDTO(warehouse);
    }
    /**
     * 获取用户的仓库统计信息
     */
    public List<WarehouseWithStatsDTO> getUserWarehousesWithStats(Long userId) {
        List<Object[]> results = warehouseRepository.findWarehousesWithStatsByUser(userId);

        return results.stream()
                .map(result -> {
                    Warehouse warehouse = (Warehouse) result[0];
                    Long ingredientCount = (Long) result[1];
                    BigDecimal totalStock = (BigDecimal) result[2];

                    WarehouseWithStatsDTO dto = new WarehouseWithStatsDTO();
                    dto.setId(warehouse.getId());
                    dto.setName(warehouse.getName());
                    dto.setDescription(warehouse.getDescription());
                    dto.setLocation(warehouse.getLocation());
                    dto.setIngredientCount(ingredientCount);
                    dto.setTotalStock(totalStock);
                    dto.setCreatedTime(warehouse.getCreatedTime());

                    return dto;
                })
                .collect(Collectors.toList());
    }
    /**
     * 更新仓库信息
     */
    public WarehouseDTO updateWarehouse(Long warehouseId, UpdateWarehouseRequest request) {
        Long userId = UserContext.getCurrentUserId(); // 直接获取
        familyPermissionUtil.checkUserPermissionByWarehouseId(userId, warehouseId);
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("仓库不存在"));

        // 如果修改了名称，检查是否重复
        if (!warehouse.getName().equals(request.getName())) {
            Optional<Warehouse> existing = warehouseRepository.findByUser_IdAndName(
                    warehouse.getUser().getId(), request.getName());
            if (existing.isPresent() && !existing.get().getId().equals(warehouseId)) {
                throw new RuntimeException("仓库名称已存在");
            }
        }

        warehouse.setName(request.getName());
        warehouse.setDescription(request.getDescription());
        warehouse.setLocation(request.getLocation());

        Warehouse updated = warehouseRepository.save(warehouse);
        return convertToDTO(updated);
    }

    /**
     * 删除仓库（会级联删除相关的批次和库存汇总）
     */
    public void deleteWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new RuntimeException("仓库不存在");
        }
        warehouseRepository.deleteById(warehouseId);
    }

    /**
     * 验证用户是否拥有该仓库
     */
    public boolean validateWarehouseOwnership(Long warehouseId, Long userId) {
        return warehouseRepository.existsByIdAndUser_Id(warehouseId, userId);
    }
    /**
     * 搜索用户的仓库
     */
    public List<WarehouseDTO> searchWarehouses(Long userId, String keyword) {
        List<Warehouse> warehouses = warehouseRepository.findByUser_IdAndNameContainingIgnoreCase(userId, keyword);
        return warehouses.stream()
                .map(this::convertToWHDTO)
                .collect(Collectors.toList());
    }

    // DTO转换方法
    private WarehouseDTO convertToWHDTO(Warehouse warehouse) {
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
    /**
     * 转换方法
     */
    private FamilyItemDTO convertToFamilyItemDTO(Batch batch) {
        FamilyItemDTO dto = new FamilyItemDTO();
        dto.setId(batch.getId());
        dto.setItemId(batch.getId());
        dto.setItemName(batch.getName());
        dto.setCategory(batch.getCategory());
        dto.setQuantity(batch.getCurrentQuantity());
        dto.setExpiryDate(batch.getExpiryDate());
        dto.setPurchaseDate(batch.getProductionDate());
        dto.setWarehouseId(batch.getWarehouse().getId());
        dto.setWarehouseName(batch.getWarehouse().getName());
        dto.setUnit(batch.getUnit()); // 新增单位

        return dto;
    }
    /**
     * 获取家庭中的所有批次（包含物品信息）
     */
    public List<FamilyItemDTO> getFamilyBatches(Long familyId) {
        Long userId = UserContext.getCurrentUserId();
        familyPermissionUtil.checkUserInFamily(userId, familyId);

        List<Batch> batches = batchRepository.findByFamilyId(familyId);

        return batches.stream()
                .map(this::convertToFamilyItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * 1. 查询某个仓库的所有原料
     */
    public List<WarehouseIngredientDTO> getWarehouseIngredients(Long warehouseId) {
        List<InventorySummary> summaries = inventorySummaryRepository.findByWarehouse_Id(warehouseId);

        return summaries.stream()
                .map(summary -> {
                    WarehouseIngredientDTO dto = new WarehouseIngredientDTO();
                    dto.setIngredientId(summary.getIngredient().getId());
                    dto.setIngredientName(summary.getIngredient().getName());
                    dto.setCategory(summary.getIngredient().getCategory());
                    dto.setTotalStock(summary.getTotalStock());
                    dto.setUnit(summary.getIngredient().getUnit());
                    dto.setMinStockAlert(summary.getMinStockAlert());

                    // 计算库存状态
                    if (summary.getTotalStock().compareTo(BigDecimal.ZERO) == 0) {
                        dto.setStockStatus("缺货");
                    } else if (summary.getTotalStock().compareTo(summary.getMinStockAlert()) <= 0) {
                        dto.setStockStatus("库存不足");
                    } else {
                        dto.setStockStatus("库存充足");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
    private static final Logger logger = LoggerFactory.getLogger(WarehouseService.class);


    /**
     * 2. 查询某个仓库将要过期的原料（N天内）
     */
    public List<ExpiringIngredientDTO> getExpiringIngredients(Long warehouseId, Long days) {
        Long userId = UserContext.getCurrentUserId(); // 直接获取
        familyPermissionUtil.checkUserPermissionByWarehouseId(userId, warehouseId);
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        // 添加调试日志
        logger.info("查询过期物品 - 仓库ID: {}, 开始日期: {}, 结束日期: {}, 天数: {}",
                warehouseId, expiryDate,days);
        List<Batch> batches = batchRepository.findExpiringBatches(warehouseId, expiryDate);
        logger.info("查询到 {} 个即将过期的批次", batches.size());
        // 打印每个批次的信息用于调试
        batches.forEach(batch -> {
            logger.info("批次: {}, 过期日期: {}, 剩余天数: {}",
                    batch.getBatchNumber(),
                    batch.getExpiryDate(),
                    ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate()));
        });

        return batches.stream()
                .map(batch -> {
                    ExpiringIngredientDTO dto = new ExpiringIngredientDTO();
                    dto.setBatchId(batch.getId());
                    dto.setBatchNumber(batch.getBatchNumber());
                    dto.setIngredientName(batch.getIngredient().getName());
                    dto.setCategory(batch.getIngredient().getCategory());
                    dto.setRemainingQuantity(batch.getCurrentQuantity());
                    dto.setUnit(batch.getIngredient().getUnit());
                    dto.setExpiryDate(batch.getExpiryDate());
                    dto.setDaysUntilExpiry(ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 3. 查询某个仓库今天要过期的原料
     */
    public List<ExpiringIngredientDTO> getTodayExpiringIngredients(Long warehouseId) {
        Long userId = UserContext.getCurrentUserId(); // 直接获取
        familyPermissionUtil.checkUserPermissionByWarehouseId(userId, warehouseId);
        List<Batch> batches = batchRepository.findTodayExpiringBatches(warehouseId);

        return batches.stream()
                .map(batch -> {
                    ExpiringIngredientDTO dto = new ExpiringIngredientDTO();
                    dto.setBatchId(batch.getId());
                    dto.setBatchNumber(batch.getBatchNumber());
                    dto.setIngredientName(batch.getIngredient().getName());
                    dto.setCategory(batch.getIngredient().getCategory());
                    dto.setRemainingQuantity(batch.getCurrentQuantity());
                    dto.setUnit(batch.getIngredient().getUnit());
                    dto.setExpiryDate(batch.getExpiryDate());
                    dto.setDaysUntilExpiry(0L); // 今天过期
                    return dto;
                })
                .collect(Collectors.toList());
    }
    /**
     * 查找或创建食材
     */
    private Ingredient findOrCreateIngredient(StockInRequest request) {
        // 首先尝试按名称查找
        Optional<Ingredient> existingIngredient = ingredientRepository.findByName(request.getIngredientName());

        if (existingIngredient.isPresent()) {
            return existingIngredient.get();
        }

        // 如果食材不存在，创建新的食材
        Ingredient newIngredient = new Ingredient();
        newIngredient.setName(request.getIngredientName());
        newIngredient.setCategory(request.getCategory());
        newIngredient.setUnit(request.getUnit());
        newIngredient.setShelfLifeDays(request.getShelfLifeDays());

        Ingredient savedIngredient = ingredientRepository.save(newIngredient);

        // 清除缓存，确保新创建的食材能被后续查询找到
        entityManager.flush();
        entityManager.clear();

        return savedIngredient;
    }
    private Ingredient createIngredient(StockInRequest request) {
        Ingredient newIngredient = new Ingredient();
        newIngredient.setName(request.getIngredientName());
        newIngredient.setCategory(request.getCategory());
        newIngredient.setUnit(request.getUnit());
        newIngredient.setShelfLifeDays(request.getShelfLifeDays());

        Ingredient savedIngredient = ingredientRepository.save(newIngredient);

        // 清除缓存，确保新创建的食材能被后续查询找到
        entityManager.flush();
        entityManager.clear();

        return savedIngredient;
    }

    /**
     * 清除 JPA 缓存
     */
    private void clearJpaCache() {
        entityManager.flush();
        entityManager.clear();
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    /**
     * 生成批次号
     * 格式: 食材拼音首字母-年月日-序号
     * 示例: NJ-20240115-001
     */
    public String generateBatchNumber(String ingredientName, Long warehouseId) {
        // 获取食材拼音首字母
        String prefix = getIngredientPrefix(ingredientName);

        // 当前日期
        String datePart = LocalDate.now().format(DATE_FORMATTER);

        // 查找当天的序号
        int sequence = findTodaySequence(prefix, datePart, warehouseId);

        return String.format("%s-%s-%03d", prefix, datePart, sequence);
    }

    /**
     * 获取食材名称的拼音首字母
     */
    private String getIngredientPrefix(String ingredientName) {
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            return "OT"; // Other
        }

        // 简单实现：取前2个字符的大写
        // 实际项目中可以使用拼音库如 pinyin4j
        String name = ingredientName.trim();
        if (name.length() >= 2) {
            return name.substring(0, 2).toUpperCase();
        } else {
            return (name + "X").toUpperCase(); // 补位
        }
    }

    /**
     * 查找当天的序号
     */
    private int findTodaySequence(String prefix, String datePart, Long warehouseId) {
        // 查询今天该前缀的已有批次数量
        String todayPattern = prefix + "-" + datePart + "-%";
        Long count = batchRepository.countByBatchNumberLikeAndWarehouseId(todayPattern, warehouseId);

        return count.intValue() + 1;
    }
    // 内部类：批次操作记录
    @Data
    @AllArgsConstructor
    public static class BatchOperation {
        private Long batchId;
        private String batchNumber;
        private BigDecimal consumeQuantity;
        private BigDecimal remainingQuantity;
    }

    // 出库结果
    @Data
    @AllArgsConstructor
    public static class StockOutResult {
        private BigDecimal totalQuantity;
        private List<BatchOperation> batchOperations;
        private Boolean success;

        public Integer getBatchCount() {
            return batchOperations.size();
        }
    }
    /**
     * 将指定批次数量清0，标记为 DISCARDED，并记录操作流水
     */
    public void discardBatch(Long warehouseId, Long batchId, String note) {
        // 权限校验：批次属于当前用户家庭
        if (!isBatchInCurrentUserFamily(batchId)) {
            throw new RuntimeException("批次不属于当前用户家庭");
        }

        // 获取并校验批次属于该仓库
        Batch batch = batchRepository.findByIdAndWarehouseId(batchId, warehouseId)
                .orElseThrow(() -> new RuntimeException("批次不存在或不属于该仓库"));

        // 如果已经是 DISCARDED，则直接返回（也可以选择抛异常）
        if ("DISCARDED".equals(batch.getStatus())) {
            logger.info("批次已为 DISCARDED: batchId={}", batchId);
            return;
        }

        // 保存出库前的数量用于流水记录
        BigDecimal beforeQuantity = batch.getCurrentQuantity() == null ? BigDecimal.ZERO : batch.getCurrentQuantity();

        // 将批次数量清0并标记为 DISCARDED
        batch.setCurrentQuantity(BigDecimal.ZERO);
        batch.setStatus("DISCARDED");
        batchRepository.save(batch);

        // 更新库存汇总与用量统计
        try {
            updateInventorySummary(warehouseId, batch.getIngredient().getId());
        } catch (Exception e) {
            logger.warn("更新库存汇总失败: warehouseId={}, batchId={}, err={}", warehouseId, batchId, e.getMessage());
        }

        try {
            usageStatisticsService.batchUpdateUsageStatistics(warehouseId);
        } catch (Exception e) {
            logger.warn("触发用量统计失败: warehouseId={}, err={}", warehouseId, e.getMessage());
        }
    }
    public StockOutResult stockOutByBatch(Long warehouseId, StockOutRequest request) {
        // 验证请求参数
        boolean validate = isBatchInCurrentUserFamily(request.getBatchId());
        if(!validate) {
            throw new RuntimeException("批次不属于当前用户家庭");
        }
        // 获取指定的批次
        Batch batch = batchRepository.findByIdAndWarehouseId(request.getBatchId(), warehouseId)
                .orElseThrow(() -> new RuntimeException("批次不存在或不属于该仓库"));

        // 验证批次状态
        if (!"ACTIVE".equals(batch.getStatus())) {
            throw new RuntimeException("批次状态不可用，无法出库");
        }

        // 验证出库数量不超过当前库存
        if (request.getQuantity().compareTo(batch.getCurrentQuantity()) > 0) {
            throw new RuntimeException(String.format(
                    "出库数量超出批次库存：请求 %.2f %s，实际库存 %.2f %s",
                    request.getQuantity(),
                    getIngredientUnit(batch.getIngredientId()),
                    batch.getCurrentQuantity(),
                    getIngredientUnit(batch.getIngredientId())
            ));
        }


        // 更新批次库存
        batch.setCurrentQuantity(batch.getCurrentQuantity().subtract(request.getQuantity()));

        // 如果批次库存为0，更新状态
        if (batch.getCurrentQuantity().compareTo(BigDecimal.ZERO) == 0) {
            batch.setStatus("CONSUMED");
        }

        batchRepository.save(batch);

        // 创建操作详情
        BatchOperation operation = new BatchOperation(
                batch.getId(),
                batch.getBatchNumber(),
                request.getQuantity(),
                batch.getCurrentQuantity()
        );

        List<BatchOperation> operations = Collections.singletonList(operation);

        // 更新库存汇总
        updateInventorySummary(warehouseId, batch.getIngredientId());

        // 更新使用分析
        usageStatisticsService.batchUpdateUsageStatistics(warehouseId);

        return new StockOutResult(request.getQuantity(), operations, true);
    }
    /**
     * 出库方法 - 支持先进先出(FIFO)
     */
    public StockOutResult stockOut(Long warehouseId, StockOutRequest request) {
        // 验证请求参数
//        validateStockOutRequest(warehouseId, request);

        // 获取该食材的所有活跃批次（按生产日期排序，先进先出）
        List<Batch> activeBatches = batchRepository
                .findActiveBatchesByWarehouseAndIngredientOrderByProductionDate(
                        warehouseId, request.getIngredientId(), "ACTIVE", BigDecimal.ZERO);

        if (activeBatches.isEmpty()) {
            throw new RuntimeException("该食材在仓库中无库存");
        }

        BigDecimal remainingQuantity = request.getQuantity();

        List<BatchOperation> operations = new ArrayList<>();

        // 先进先出：按生产日期最早的先消耗
        for (Batch batch : activeBatches) {
            if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // 计算本次从该批次消耗的数量
            BigDecimal consumeQuantity = batch.getCurrentQuantity().min(remainingQuantity);

            // 更新批次库存
            batch.setCurrentQuantity(batch.getCurrentQuantity().subtract(consumeQuantity));

            // 如果批次库存为0，更新状态
            if (batch.getCurrentQuantity().compareTo(BigDecimal.ZERO) == 0) {
                batch.setStatus("CONSUMED");
            }

            batchRepository.save(batch);

            // 记录操作详情
            BatchOperation operation = new BatchOperation(
                    batch.getId(),
                    batch.getBatchNumber(),
                    consumeQuantity,
                    batch.getCurrentQuantity()
            );
            operations.add(operation);

            remainingQuantity = remainingQuantity.subtract(consumeQuantity);
        }

        // 检查是否完全出库
        if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException(String.format(
                    "库存不足，还需要 %.2f %s",
                    remainingQuantity,
                    getIngredientUnit(request.getIngredientId())
            ));
        }

        // 更新库存汇总
        updateInventorySummary(warehouseId, request.getIngredientId());
        // 更新使用分析
        usageStatisticsService.batchUpdateUsageStatistics(warehouseId);
        // 记录操作流水
        recordStockOperation(warehouseId, request, operations);

        return new StockOutResult(request.getQuantity(), operations, true);
    }
    // 批量出库方法
    @Transactional
    public BatchStockOutResult batchStockOut(BatchStockOutRequest request) {
        try {
            logger.info("开始批量出库: 仓库ID={},批次数量={}",
                    request.getWarehouseId(), request.getBatchConsumptions().size());

            // 1. 执行批量出库
            Map<Long, String> operationResults = batchStockOutRepository.batchConsumeStock(
                    request.getWarehouseId(),
                    request.getBatchConsumptions(),
                    request.getNote()
            );
            logger.info("----operationResults---", operationResults);
            // 2. 筛选成功的出库操作
            Map<Long, BigDecimal> successfulConsumptions = request.getBatchConsumptions().entrySet().stream()
                    .filter(entry -> operationResults.get(entry.getKey()).contains("成功"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (successfulConsumptions.isEmpty()) {
                logger.warn("批量出库没有成功操作: 仓库ID={}", request.getWarehouseId());
                return BatchStockOutResult.failure("所有出库操作都失败");
            }

            // 3. 📝 批量记录库存操作流水（关键修复）
            try {
                batchStockOutRepository.batchRecordDetailedStockOperations(
                        request.getWarehouseId(),
                        successfulConsumptions,
                        request.getNote()
                );
                logger.info("操作流水记录成功: 仓库ID={}, 记录数量={}",
                        request.getWarehouseId(), successfulConsumptions.size());
            } catch (Exception e) {
                logger.error("记录操作流水失败，但出库操作已提交: 仓库ID={}, 错误: {}",
                        request.getWarehouseId(), e.getMessage());
                // 这里可以根据业务需求决定是否回滚
                // 如果操作流水很重要，可以抛出异常让事务回滚
                // throw new RuntimeException("记录操作流水失败，事务回滚", e);
            }

            // 4. 更新库存汇总表 触发器会做吧？
//            try {
//                batchRepository.batchUpdateInventorySummary(
//                        request.getWarehouseId(), successfulConsumptions);
//                logger.info("库存汇总更新成功: 仓库ID={}", request.getWarehouseId());
//            } catch (Exception e) {
//                logger.error("更新库存汇总失败: 仓库ID={}, 错误: {}",
//                        request.getWarehouseId(), e.getMessage());
//            }

            // 5. 更新用量统计（异步）
            usageStatisticsService.batchUpdateUsageStatistics(request.getWarehouseId());

//            updateUsageStatistics(request.getWarehouseId(), successfulConsumptions);

            // 6. 构建返回结果
            BigDecimal totalConsumed = successfulConsumptions.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String message = String.format("批量出库完成: 成功%d/%d个批次, 总消耗%sg",
                    successfulConsumptions.size(),
                    request.getBatchConsumptions().size(),
                    totalConsumed);

            return BatchStockOutResult.success(
                    message,
                    request.getBatchConsumptions().size(),
                    operationResults,
                    totalConsumed
            );

        } catch (Exception e) {
            logger.error("批量出库失败: 仓库ID={}, 错误: {}",
                    request.getWarehouseId(), e.getMessage(), e);
            return BatchStockOutResult.failure("批量出库失败: " + e.getMessage());
        }
    }


    /**
     * 判断指定批次是否属于当前登录用户的家庭（便捷方法）
     */
    public boolean isBatchInCurrentUserFamily(Long batchId) {
        Long userId = UserContext.getCurrentUserId();
        return isBatchInUserFamily(batchId, userId);
    }
    /**
     * 判断指定批次是否属于指定用户的家庭
     */
    public boolean isBatchInUserFamily(Long batchId, Long userId) {
        Optional<Batch> opt = batchRepository.findById(batchId);
        if (!opt.isPresent()) {
            return false;
        }
        Batch batch = opt.get();
        Warehouse warehouse = batch.getWarehouse();
        if (warehouse == null) {
            return false;
        }

        // 假设 Warehouse 有 getFamilyId() 字段（repository 中已有 findByFamilyId 方法）
        Long familyId;
        try {
            familyId = warehouse.getFamily().getId();
        } catch (Exception e) {
            // 如果实体没有该字段或为 null，则视为不属于任何家庭
            return false;
        }
        if (familyId == null) {
            return false;
        }

        try {
            familyPermissionUtil.checkUserInFamily(userId, familyId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * 记录库存操作流水
     */
    private void recordStockOperation(Long warehouseId, StockOutRequest request, List<BatchOperation> operations) {
        for (BatchOperation operation : operations) {
//            StockOperationLog log = new StockOperationLog();
//            log.setWarehouseId(warehouseId);
//            log.setIngredientId(request.getIngredientId());
//            log.setBatchId(operation.getBatchId());
//            log.setOperationType(request.getOperationType());
//            log.setQuantity(operation.getConsumeQuantity().negate()); // 负数表示出库
//            log.setNote(request.getNote());
//            log.setCreatedTime(LocalDateTime.now());

            // 保存到数据库（需要创建对应的Repository）
            // stockOperationRepository.save(log);
        }
    }
    private String getIngredientUnit(Long ingredientId) {
        return ingredientRepository.findById(ingredientId)
                .map(Ingredient::getUnit)
                .orElse("");
    }
    private void updateInventorySummary(Long warehouseId, Long ingredientId) {
        // 计算该食材在该仓库的总库存
        BigDecimal totalStock = batchRepository.sumCurrentQuantityByWarehouseAndIngredient(
                warehouseId, ingredientId);

        // 更新或创建库存汇总记录
        InventorySummary summary = inventorySummaryRepository
                .findByWarehouse_IdAndIngredient_Id(warehouseId, ingredientId)
                .orElse(new InventorySummary());

        if (summary.getId() == null) {
            summary.setWarehouse(warehouseRepository.findById(warehouseId).get());
            summary.setIngredient(ingredientRepository.findById(ingredientId).get());
        }

        summary.setTotalStock(totalStock);
        inventorySummaryRepository.save(summary);
    }

    /**
     * 4. 往某个仓库录入原料
     */
    public Batch stockIn(StockInRequest request) {
        // 验证仓库和食材存在
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("仓库不存在"));
        // 查找或创建食材
        Ingredient ingredient = findOrCreateIngredient(request);

        // 自动生成批次号
        String batchNumber = generateBatchNumber(
                request.getIngredientName(), request.getWarehouseId());

        // 创建并保存批次
        Batch batch = createBatch(warehouse, ingredient, batchNumber, request);
        clearJpaCache();

        return batchRepository.save(batch);

    }
    /**
     * 创建批次对象
     */
    private Batch createBatch(Warehouse warehouse, Ingredient ingredient, String batchNumber, StockInRequest request) {
        Batch batch = new Batch();
        batch.setWarehouse(warehouse);
        batch.setIngredient(ingredient);
        batch.setUnit(request.getUnit());
        batch.setBatchNumber(batchNumber);
        batch.setInitialQuantity(request.getQuantity());
        batch.setCurrentQuantity(request.getQuantity());
        batch.setProductionDate(request.getProductionDate());
        batch.setExpiryDate(request.getExpiryDate()); // 使用自动计算的过期日期
        batch.setPurchasePrice(request.getPurchasePrice());
        batch.setSupplier(request.getSupplier());
        batch.setStatus("ACTIVE");
        return batch;
    }
    /**
     * 批量入库方法 - 使用JPA批量插入（依赖触发器）
     */
    public List<Batch> stockInBatch(Long warehouseId,List<StockInRequest> requests) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("仓库不存在"));
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<Batch> batches = new ArrayList<>();
        Map<String, String> batchNumbers = generateBatchNumbers(requests);

        // 预先获取或创建所有食材
        Map<String, Ingredient> ingredientMap = preloadIngredients(requests);

        // 创建批次对象
        for (StockInRequest request : requests) {
            String batchNumber = batchNumbers.get(request.getIngredientName());
            Ingredient ingredient = ingredientMap.get(request.getIngredientName());

            Batch batch = createBatch(warehouse, ingredient, batchNumber, request);
            batches.add(batch);
        }
        try {
            List<Batch> savedBatches = batchRepository.saveAll(batches);
            // 批量保存 - 触发器会自动更新 inventory_summary

            // 只需要清除缓存，不需要手动更新库存汇总
            entityManager.flush();
            entityManager.clear();

            return savedBatches;
        } catch(Error e) {
            throw new RuntimeException("savedBatches失败");
        }

    }

    /**
     * 预先加载所有食材
     */
    private Map<String, Ingredient> preloadIngredients(List<StockInRequest> requests) {
        try {
            Set<String> ingredientNames = requests.stream()
                    .map(StockInRequest::getIngredientName)
                    .collect(Collectors.toSet());

            // 查找已存在的食材
            List<Ingredient> existingIngredients = ingredientRepository.findByNameIn(ingredientNames);
            Map<String, Ingredient> ingredientMap = existingIngredients.stream()
                    .collect(Collectors.toMap(Ingredient::getName, Function.identity()));

            // 创建不存在的食材
            for (StockInRequest req : requests) {
                if (!ingredientMap.containsKey(req.getIngredientName())) {
                    Ingredient newIngredient = createIngredient(req);
                    ingredientMap.put(req.getIngredientName(), ingredientRepository.save(newIngredient));
                }
            }

            return ingredientMap;
        } catch (Error e) {
            throw new RuntimeException("获取preloadIngredients 失败");
        }
    }
    /**
     * 生成批次号映射
     */
    private Map<String, String> generateBatchNumbers(List<StockInRequest> requests) {
        Map<String, String> batchNumbers = new HashMap<>();
        Map<String, Integer> sequenceMap = new HashMap<>();

        for (StockInRequest request : requests) {
            String ingredientName = request.getIngredientName();
            int sequence = sequenceMap.getOrDefault(ingredientName, 0) + 1;
            sequenceMap.put(ingredientName, sequence);

            String batchNumber = generateBatchNumber(
                    ingredientName, request.getWarehouseId());
            batchNumbers.put(ingredientName, batchNumber);
        }

        return batchNumbers;
    }
}
