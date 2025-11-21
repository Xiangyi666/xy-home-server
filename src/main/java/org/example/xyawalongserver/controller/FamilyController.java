package org.example.xyawalongserver.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.example.xyawalongserver.model.dto.request.AddMemberRequest;
import org.example.xyawalongserver.model.entity.*;
import org.example.xyawalongserver.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/api/families")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    // 创建家庭
    @PostMapping("/createFamily")
    public ApiResponse<?> createFamily(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String familyName = (String) request.get("name");
            Long userId = (Long) httpRequest.getAttribute("userId");

            Family family = familyService.createFamily(familyName, userId);
            return ApiResponse.success(family);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "创建家庭失败: " + e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{familyId}")
    public ResponseEntity<?> getFamily(@PathVariable Long familyId) {
        return familyService.getFamilyById(familyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 获取用户的所有家庭 - 通俗写法
    @GetMapping("/getAll")
    public ApiResponse<List<Family>> getUserFamilies(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            System.out.println((userId));
            // 调用service方法，获取用户的所有家庭列表
            List<Family> families = familyService.getUserFamilies(userId);

            // 直接返回家庭列表
            return ApiResponse.success(families);

        } catch (Exception e) {
            // 如果出错，返回错误信息
            return ApiResponse.error( "");
        }
    }

    // 添加用户到家庭
    @PostMapping("/join")
    public ApiResponse<?> JoinFamily(
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest httpRequest) {
        Long familyId = Long.valueOf(requestBody.get("familyId").toString());
        Long userId = (Long) httpRequest.getAttribute("userId");
        System.out.println(familyId + " ," +  userId);
        try {
            boolean success = familyService.addUserToFamily(
                    familyId,
                    userId,
                    UserFamily.FamilyRole.valueOf("MEMBER")
            );

            if (success) {
                return ApiResponse.success("加入家庭成功");
            } else {
                return ApiResponse.error("添加成员失败");
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ApiResponse.error( e.getMessage());
        }
    }

    // 添加用户到家庭
    @PostMapping("/{familyId}/members")
    public ResponseEntity<?> addMember(
            @PathVariable Long familyId,
            @RequestBody AddMemberRequest request) {
        try {
            boolean success = familyService.addUserToFamily(
                    familyId,
                    request.getUserId(),
                    request.getRole()
            );

            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("添加成员失败");
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 从家庭移除用户
    @DeleteMapping("/{familyId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long familyId, @PathVariable Long userId) {
        boolean success = familyService.removeUserFromFamily(familyId, userId);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("移除成员失败");
        }
    }

    // 获取家庭成员列表
    @GetMapping("/{familyId}/members")
    public ResponseEntity<List<MemberDTO>> getMembers(@PathVariable Long familyId) {
        List<MemberDTO> members = familyService.getFamilyMemberDTOs(familyId);
        return ResponseEntity.ok(members);
    }

    // 获取家庭仓库统计信息
    @GetMapping("/{familyId}/warehouses/statistics")
    public ResponseEntity<?> getWarehouseStatistics(@PathVariable Long familyId) {
        try {
            Map<String, Object> statistics = familyService.getWarehouseStatistics(familyId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取仓库统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 获取家庭仓库列表
    @GetMapping("/{familyId}/warehouses")
    public ResponseEntity<List<Warehouse>> getWarehouses(@PathVariable Long familyId) {
        List<Warehouse> warehouses = familyService.getFamilyWarehouses(familyId);
        return ResponseEntity.ok(warehouses);
    }

    // 检查用户是否可以创建仓库
    @GetMapping("/user/{userId}/can-create-warehouse")
    public ResponseEntity<Map<String, Boolean>> canCreateWarehouse(@PathVariable Long userId) {
        boolean canCreate = familyService.canUserCreateWarehouse(userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canCreate", canCreate);
        return ResponseEntity.ok(response);
    }
}