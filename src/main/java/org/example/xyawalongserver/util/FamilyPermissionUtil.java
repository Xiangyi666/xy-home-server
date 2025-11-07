package org.example.xyawalongserver.util;

import org.example.xyawalongserver.model.entity.Warehouse;
import org.example.xyawalongserver.repository.UserFamilyRepository;
import org.example.xyawalongserver.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FamilyPermissionUtil {

    @Autowired
    private UserFamilyRepository userFamilyRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    /**
     * 检查用户是否属于指定家庭
     */
    public void checkUserInFamily(Long userId, Long familyId) {
        if (!userFamilyRepository.existsByUserIdAndFamilyId(userId, familyId)) {
            throw new RuntimeException("用户不属于该家庭，无权访问");
        }
    }

    /**
     * 检查用户是否属于指定家庭（返回boolean）
     */
    public boolean isUserInFamily(Long userId, Long familyId) {
        return userFamilyRepository.existsByUserIdAndFamilyId(userId, familyId);
    }

    /**
     * 通过仓库ID检查用户是否有权限访问
     */
    public void checkUserPermissionByWarehouseId(Long userId, Long warehouseId) {
        // 获取仓库信息
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("仓库不存在"));

        // 检查用户是否属于该仓库所在的家庭
        if (!userFamilyRepository.existsByUserIdAndFamilyId(userId, warehouse.getFamily().getId())) {
            throw new RuntimeException("用户无权访问此仓库");
        }
    }
    /**
     * 通过仓库ID检查用户是否有权限访问（返回boolean）
     */
    public boolean hasUserPermissionByWarehouseId(Long userId, Long warehouseId) {
        try {
            Warehouse warehouse = warehouseRepository.findById(warehouseId).orElse(null);
            if (warehouse == null) {
                return false;
            }
            return userFamilyRepository.existsByUserIdAndFamilyId(userId, warehouse.getFamily().getId());
        } catch (Exception e) {
            return false;
        }
    }
}