package org.example.xyawalongserver.controller;

import org.example.xyawalongserver.model.dto.request.BatchCreateRequest;
import org.example.xyawalongserver.model.entity.Ingredient;
import org.example.xyawalongserver.model.entity.IngredientBatch;
import org.example.xyawalongserver.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final IngredientService ingredientService;

    /**
     * 创建原料及批次
     * 如果原料已经存在，仅创建批次
     */
    @PostMapping("/createIngredientAndBatch")
    public ResponseEntity<ApiResponse<IngredientBatch>> createIngredientWithBatch(
            @Valid @RequestBody BatchCreateRequest request) {
        try {
            IngredientBatch batch = ingredientService.createIngredientWithBatch(request);
            return ResponseEntity.ok(ApiResponse.success(batch, "原料及批次创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建失败: " + e.getMessage()));
        }
    }
    /**
     * 查询所有原料
     */
    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<Ingredient>>> getAllIngredients() {
        try {
            List<Ingredient> ingredients = ingredientService.getAllIngredients();
            return ResponseEntity.ok(ApiResponse.success( ingredients, "查询成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }
    /**
     * 查询所有存在的原料
     */
    @GetMapping("/getAllExistIngredients")
    public ResponseEntity<ApiResponse<List<Ingredient>>> getAllExistIngredients() {
        try {
            List<Ingredient> ingredients = ingredientService.getAllExistIngredients();
            return ResponseEntity.ok(ApiResponse.success( ingredients, "查询成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }
}