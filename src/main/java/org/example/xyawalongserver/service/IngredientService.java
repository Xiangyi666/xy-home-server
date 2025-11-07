package org.example.xyawalongserver.service;


import jakarta.persistence.EntityManager;
import org.example.xyawalongserver.model.dto.request.BatchCreateRequest;
import  org.example.xyawalongserver.model.entity.Ingredient;
import  org.example.xyawalongserver.model.entity.IngredientBatch;
import org.example.xyawalongserver.repository.IngredientRepository;
import org.example.xyawalongserver.repository.IngredientBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientBatchRepository batchRepository;
    private final EntityManager entityManager;  // 注入 EntityManager

    /**
     * 获取所有原料
     */
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }
    /**
     * 获取所有存在的原料
     */
    public List<Ingredient> getAllExistIngredients() {
        return ingredientRepository.findAll();
    }

    /**
     * 根据名称搜索原料（模糊搜索）
     */
    public List<Ingredient> findIngredientsByName(String name) {
        return batchRepository.findByNameContainingIgnoreCase(name);
    }
    /**
     * 根据ID查询原料
     */
    public Ingredient getIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("原料不存在，ID: " + id));
    }

    /**
     * 创建原料及批次（事务管理）
     */
    @Transactional
    public IngredientBatch createIngredientWithBatch(BatchCreateRequest request) {
        // 1. 处理原料（存在则更新，不存在则创建）
        Ingredient ingredient = ingredientRepository.findByName(request.getIngredientName())
                .orElseGet(() -> {
                    // 创建新原料
                    Ingredient newIngredient = new Ingredient();
                    newIngredient.setName(request.getIngredientName());
                    newIngredient.setUnit(request.getUnit());
                    newIngredient.setAlertStock(request.getAlertStock());
                    return ingredientRepository.save(newIngredient);
                });

        // 如果原料已存在，更新单位等信息
        if (ingredient.getId() != null) {
            ingredient.setUnit(request.getUnit());
            ingredient.setAlertStock(request.getAlertStock());
            ingredient = ingredientRepository.save(ingredient);
        }

        // 2. 创建批次记录
        IngredientBatch batch = new IngredientBatch();
        batch.setIngredient(ingredient);
        batch.setBatchNumber(request.generateBatchNumber());
        batch.setQuantity(request.getQuantity());
        batch.setProductionDate(request.getProductionDate());
        batch.setExpiryDate(request.calculateExpiryDate());
        batch.setSupplierInfo(request.getSupplierInfo());
        batch.setIsConsumed(false);

        IngredientBatch savedBatch = batchRepository.save(batch);

        // 3. 更新原料总库存
//        ingredientRepository.updateStock(ingredient.getId(), request.getQuantity());

        // 4. 使用原生 SQL 查询获取最新数据
        Ingredient updatedIngredient = ingredientRepository.findIngredientById(ingredient.getId());

        // 5. 设置到返回对象中
        savedBatch.setIngredient(updatedIngredient);

        log.info("原料及批次创建成功: {} - 数量: {} - 批次号: {}",
                request.getIngredientName(), request.getQuantity(), batch.getBatchNumber());

        return savedBatch;
    }
}