package org.example.xyawalongserver.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.xyawalongserver.controller.WarehouseController;
import org.example.xyawalongserver.model.entity.Batch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

// BatchStockOutRepository.java
@Repository
@Slf4j
public class BatchStockOutRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(BatchStockOutRepository.class);

    public BatchStockOutRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 批量出库 - 核心方法
     */
    public Map<Long, String> batchConsumeStock(Long warehouseId, Map<Long, BigDecimal> consumptions, String note) {
        if (consumptions.isEmpty()) {
            return new HashMap<>();
        }

        String sql = """
            UPDATE batch 
            SET current_quantity = current_quantity - ?,
                updated_time = NOW()
            WHERE id = ? 
            AND warehouse_id = ?
            AND status = 'ACTIVE'
            AND current_quantity >= ?
            """;

        List<Object[]> batchArgs = new ArrayList<>();
        Map<Long, String> results = new HashMap<>();

        // 准备批量参数
        for (Map.Entry<Long, BigDecimal> entry : consumptions.entrySet()) {
            Long batchId = entry.getKey();
            BigDecimal quantity = entry.getValue();

            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                results.put(batchId, "失败: 出库量必须大于0");
                continue;
            }

            batchArgs.add(new Object[]{
                    quantity,
                    batchId,
                    warehouseId,
                    quantity  // 用于检查库存是否充足
            });
        }

        if (batchArgs.isEmpty()) {
            return results;
        }

        // 打印每个参数
        for (int i = 0; i < batchArgs.size(); i++) {
            Object[] args = batchArgs.get(i);
            log.info("参数 {}: quantity={}, batchId={}, warehouseId={}, checkQuantity={}",
                    i, args[0], args[1], args[2], args[3]);
        }
        try {
            // 执行批量更新
            int[] updateCounts = jdbcTemplate.batchUpdate(sql, batchArgs);

            // 3. 打印更新结果
            log.info("updateCounts: {}", Arrays.toString(updateCounts));
            log.info("updateCounts 长度: {}", updateCounts.length);
            // 处理结果
            int index = 0;
            for (Map.Entry<Long, BigDecimal> entry : consumptions.entrySet()) {
                Long batchId = entry.getKey();
                BigDecimal quantity = entry.getValue();

                if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // 已经在前面处理过了
                }

                if (updateCounts[index] > 0) {
                    results.put(batchId, "成功: 出库 " + quantity + "g");
                } else {
                    results.put(batchId, "失败: 库存不足或批次不存在");
                }
                index++;
            }

            log.info("批量出库完成: 仓库ID={}, 处理批次={}, 成功={}",
                    warehouseId, consumptions.size(),
                    results.values().stream().filter(msg -> msg.contains("成功")).count());

        } catch (Exception e) {
            log.error("批量出库执行失败: 仓库ID={}, 错误: {}", warehouseId, e.getMessage(), e);
            // 标记所有批次为失败
            for (Long batchId : consumptions.keySet()) {
                results.put(batchId, "失败: 系统错误 - " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * 详细的批量操作记录（包含食材名称和批次号）
     */
    public void batchRecordDetailedStockOperations(Long warehouseId,
                                                   Map<Long, BigDecimal> successfulConsumptions,
                                                   String note) {
        if (successfulConsumptions.isEmpty()) {
            return;
        }

        try {
            // 获取批次详细信息
            Map<Long, Batch> batchMap = getBatchInfo(successfulConsumptions.keySet(), warehouseId);

            String operationSql = """
                INSERT INTO stock_operation 
                (warehouse_id, ingredient_id, batch_id, operation_type, quantity, note, created_time)
                VALUES (?, ?, ?, 'CONSUME', ?, ?, NOW())
                """;

            List<Object[]> batchArgs = successfulConsumptions.entrySet().stream()
                    .map(entry -> {
                        Long batchId = entry.getKey();
                        BigDecimal quantity = entry.getValue();
                        Batch batch = batchMap.get(batchId);

                        String detailedNote = String.format("%s | 食材: %s | 食材ID: %s | 批次: %s | 数量: %sg",
                                note,
                                batch != null ? batch.getIngredientName() : "未知",
                                batch !=null ? batch.getIngredient().getId() : "xx",
                                batch != null ? batch.getBatchNumber() : "未知",
                                quantity
                        );

                        return new Object[]{
                                warehouseId,
                                batch != null ? batch.getIngredient().getId() : null,
                                batchId,
                                quantity,
                                detailedNote
                        };
                    })
                    .collect(Collectors.toList());
            // 打印每个参数
            for (int i = 0; i < batchArgs.size(); i++) {
                Object[] args = batchArgs.get(i);
                log.info("参数-------3333 {}: warehouseId={}, ingredient_id={}, batchId={}, quantity={}, detailedNote={}",
                        i, args[0], args[1], args[2], args[3], args[4]);
            }
            jdbcTemplate.batchUpdate(operationSql, batchArgs);

            log.info("详细操作记录完成: 仓库ID={}, 记录数量={}", warehouseId, successfulConsumptions.size());

        } catch (Exception e) {
            log.error("详细操作记录失败: 仓库ID={}, 错误: {}", warehouseId, e.getMessage(), e);
            throw new RuntimeException("记录操作流水失败", e);
        }
    }

    /**
     * 获取批次详细信息（用于操作记录）
     */
    public Map<Long, Batch> getBatchInfo(Set<Long> batchIds, Long warehouseId) {
        if (batchIds.isEmpty()) {
            return new HashMap<>();
        }

        String placeholders = batchIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format("""
            SELECT b.id, b.ingredient_id, i.name as ingredient_name, b.batch_number
            FROM batch b
            LEFT JOIN ingredient i ON b.ingredient_id = i.id
            WHERE b.id IN (%s)
            AND b.warehouse_id = ?
            """, placeholders);

        List<Object> params = new ArrayList<>(batchIds);
        params.add(warehouseId);

        try {
            return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
                Batch batch = new Batch();
                batch.setId(rs.getLong("id"));
                batch.setBatchNumber(rs.getString("batch_number"));
                return batch;
            }).stream().collect(Collectors.toMap(Batch::getId, info -> info));
        } catch (Exception e) {
            log.error("获取批次信息失败: 批次IDs={}, 错误: {}", batchIds, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 批量检查库存是否充足
     */
    public Map<Long, Boolean> batchCheckStock(Long warehouseId, Map<Long, BigDecimal> consumptions) {
        if (consumptions.isEmpty()) {
            return new HashMap<>();
        }

        String sql = """
            SELECT b.id, 
                   CASE WHEN b.current_quantity >= ? THEN true ELSE false END as sufficient
            FROM batch b
            WHERE b.id = ?
            AND b.warehouse_id = ?
            AND b.status = 'ACTIVE'
            """;

        Map<Long, Boolean> stockCheckResults = new HashMap<>();

        for (Map.Entry<Long, BigDecimal> entry : consumptions.entrySet()) {
            try {
                Boolean sufficient = jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        entry.getValue(), entry.getKey(), warehouseId
                );
                stockCheckResults.put(entry.getKey(), Boolean.TRUE.equals(sufficient));
            } catch (Exception e) {
                log.warn("检查批次库存失败: 批次ID={}, 错误: {}", entry.getKey(), e.getMessage());
                stockCheckResults.put(entry.getKey(), false);
            }
        }

        return stockCheckResults;
    }

    /**
     * 批量更新库存汇总表
     */
    public void batchUpdateInventorySummary(Long warehouseId, Map<Long, BigDecimal> successfulConsumptions) {
        if (successfulConsumptions.isEmpty()) {
            return;
        }

        try {
            // 获取批次对应的食材ID
            Map<Long, Batch> batchMap = getBatchInfo(successfulConsumptions.keySet(), warehouseId);

            // 按食材ID分组汇总出库量
            Map<Long, BigDecimal> ingredientConsumptions = new HashMap<>();
            for (Map.Entry<Long, BigDecimal> entry : successfulConsumptions.entrySet()) {
                Batch batchInfo = batchMap.get(entry.getKey());
                if (batchInfo != null && batchInfo.getIngredientId() != null) {
                    ingredientConsumptions.merge(batchInfo.getIngredientId(), entry.getValue(), BigDecimal::add);
                }
            }

            if (ingredientConsumptions.isEmpty()) {
                log.warn("没有找到有效的食材ID，跳过库存汇总更新");
                return;
            }

            // 更新库存汇总表
            String updateSummarySql = """
                INSERT INTO inventory_summary 
                (warehouse_id, ingredient_id, total_stock, created_time, updated_time)
                VALUES (?, ?, ?, NOW(), NOW())
                ON CONFLICT (warehouse_id, ingredient_id) 
                DO UPDATE SET 
                    total_stock = inventory_summary.total_stock - EXCLUDED.total_stock,
                    updated_time = NOW()
                """;

            List<Object[]> summaryArgs = ingredientConsumptions.entrySet().stream()
                    .map(entry -> new Object[]{
                            warehouseId,
                            entry.getKey(),
                            entry.getValue()  // 这里应该是减去的数量
                    })
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(updateSummarySql, summaryArgs);

            log.info("库存汇总表更新完成: 仓库ID={}, 更新食材数量={}",
                    warehouseId, ingredientConsumptions.size());

        } catch (Exception e) {
            log.error("更新库存汇总表失败: 仓库ID={}, 错误: {}", warehouseId, e.getMessage(), e);
        }
    }

    /**
     * 简单的批量操作记录
     */
    public void batchRecordStockOperations(Long warehouseId, Map<Long, BigDecimal> successfulConsumptions, String note) {
        if (successfulConsumptions.isEmpty()) {
            return;
        }

        try {
            String operationSql = """
                INSERT INTO stock_operation 
                (warehouse_id, ingredient_id, batch_id, operation_type, quantity, note, created_time)
                SELECT 
                    b.warehouse_id,
                    b.ingredient_id,
                    b.id,
                    'CONSUME',
                    ?,
                    ?,
                    NOW()
                FROM batch b
                WHERE b.id = ?
                AND b.warehouse_id = ?
                """;

            List<Object[]> batchArgs = successfulConsumptions.entrySet().stream()
                    .map(entry -> new Object[]{
                            entry.getValue(),
                            note + " - 批次ID: " + entry.getKey(),
                            entry.getKey(),
                            warehouseId
                    })
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(operationSql, batchArgs);

            log.info("批量记录操作流水完成: 仓库ID={}, 记录数量={}",
                    warehouseId, successfulConsumptions.size());

        } catch (Exception e) {
            log.error("批量记录操作流水失败: 仓库ID={}, 错误: {}", warehouseId, e.getMessage(), e);
            throw new RuntimeException("记录操作流水失败", e);
        }
    }

}