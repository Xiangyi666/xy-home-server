-- 重新创建修正后的函数
CREATE OR REPLACE FUNCTION update_summary_stock_level()
RETURNS TRIGGER AS $$
DECLARE
v_warehouse_user_id BIGINT;
    v_usage_ratio NUMERIC(10,4);
    v_calculated_level VARCHAR(20);
    v_daily_usage NUMERIC(10,2);
BEGIN
    -- 获取仓库的用户ID
SELECT user_id INTO v_warehouse_user_id
FROM warehouse WHERE id = NEW.warehouse_id;

-- 获取日均用量
SELECT average_daily_usage INTO v_daily_usage
FROM usage_statistics
WHERE warehouse_id = NEW.warehouse_id
  AND ingredient_id = NEW.ingredient_id;

-- 处理没有用量记录的情况 - 这是关键修改！
IF v_daily_usage IS NULL OR v_daily_usage <= 0 THEN
    v_calculated_level := '充足';
END IF;
ELSE
        -- 正常计算逻辑
        v_usage_ratio := NEW.total_stock / v_daily_usage;

SELECT level_name INTO v_calculated_level
FROM stock_level_config
WHERE (user_id = v_warehouse_user_id OR user_id IS NULL)
  AND v_usage_ratio >= min_threshold
  AND (max_threshold IS NULL OR v_usage_ratio < max_threshold)
ORDER BY priority DESC
    LIMIT 1;

v_calculated_level := COALESCE(v_calculated_level, '充足');
END IF;

    -- 更新库存汇总的等级
    NEW.stock_level := v_calculated_level;
    NEW.updated_time := CURRENT_TIMESTAMP;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 重新创建触发器
CREATE TRIGGER trigger_update_summary_stock_level
    BEFORE INSERT OR UPDATE OF total_stock ON inventory_summary
    FOR EACH ROW EXECUTE FUNCTION update_summary_stock_level();


-- 添加家庭外键
ALTER TABLE users ADD COLUMN family_id INT;

-- 添加外键约束
ALTER TABLE users ADD CONSTRAINT fk_users_family
    FOREIGN KEY (family_id) REFERENCES families(id) ON DELETE SET NULL;

-- 添加 family_id 列
ALTER TABLE warehouses ADD COLUMN family_id INT;

-- 添加外键约束
ALTER TABLE warehouses ADD CONSTRAINT fk_warehouses_family
    FOREIGN KEY (family_id) REFERENCES families(id) ON DELETE CASCADE;

-- 设置 family_id 为 NOT NULL（在数据迁移完成后）
-- ALTER TABLE warehouses ALTER COLUMN family_id SET NOT NULL;

-- 修改用户表，添加微信相关字段
ALTER TABLE users
    ADD COLUMN wechat_openid VARCHAR(100) UNIQUE,
ADD COLUMN wechat_unionid VARCHAR(100),
ADD COLUMN nickname VARCHAR(100),
ADD COLUMN avatar_url VARCHAR(500),
ADD COLUMN phone_number VARCHAR(20),
ADD COLUMN user_type VARCHAR(20) DEFAULT 'WECHAT'; -- 用户类型: WECHAT, PHONE, EMAIL

-- 创建索引
CREATE INDEX idx_users_wechat_openid ON users(wechat_openid);
CREATE INDEX idx_users_phone_number ON users(phone_number);

-- 删除函数
DROP FUNCTION IF EXISTS maintain_stock_operation();
-- 重新创建函数（使用正确的字段名）
CREATE OR REPLACE FUNCTION maintain_stock_operation()
       RETURNS TRIGGER AS $$
DECLARE
quantity_change NUMERIC;
    operation_type_val TEXT;
    operation_note TEXT;
BEGIN
    RAISE NOTICE '=== 触发器开始执行 ===';
    RAISE NOTICE '表: %, 操作: %', TG_TABLE_NAME, TG_OP;
           -- 处理 DELETE 操作
    IF TG_OP = 'DELETE' THEN
        RAISE NOTICE '处理DELETE操作: id=%, batch_number=%, current_quantity=%', OLD.id, OLD.batch_number, OLD.current_quantity;

        -- 记录批次删除操作
INSERT INTO stock_operation (
    warehouse_id, ingredient_id, batch_id, operation_type,
    quantity,unit, note, created_time
) VALUES (
             OLD.warehouse_id, OLD.ingredient_id, OLD.id, 'DELETE',
             OLD.current_quantity,NEW.unit,
             format('删除批次: %s, 当前数量: %s, 状态: %s', OLD.batch_number, OLD.current_quantity, OLD.status),
             NOW()
         );

RAISE NOTICE '删除批次操作记录插入完成';
END IF;
    -- 处理 INSERT 操作（新增批次）
    IF TG_OP = 'INSERT' THEN
        RAISE NOTICE '处理INSERT操作: id=%, batch_number=%, current_quantity=%', NEW.id, NEW.batch_number, NEW.current_quantity;

            -- 插入新增批次的操作记录
INSERT INTO stock_operation (
    warehouse_id, ingredient_id, batch_id, operation_type,
    quantity,unit, note, created_time
) VALUES (
             NEW.warehouse_id, NEW.ingredient_id, NEW.id, 'INBOUND',
             NEW.initial_quantity, NEW.unit,
             format('新增批次: %s, 初始数量: %s', NEW.batch_number,NEW.initial_quantity),
             NOW()
         );

RAISE NOTICE '新增批次操作记录插入完成';
END IF;
    -- 显示新旧值（使用条件判断避免 NULL 错误）
    IF TG_OP = 'UPDATE' THEN
        RAISE NOTICE 'OLD: id=%, current_quantity=%, status=%', OLD.id, OLD.current_quantity, OLD.status;
        RAISE NOTICE 'NEW: id=%, current_quantity=%, status=%', NEW.id, NEW.current_quantity, NEW.status;

        -- 计算数量变化
        quantity_change := NEW.current_quantity - OLD.current_quantity;
        RAISE NOTICE '数量变化: %', quantity_change;

        -- 检查数量变化
        IF quantity_change != 0 THEN
            RAISE NOTICE '检测到数量变化，准备插入操作记录';

            -- 确定操作类型
            IF quantity_change > 0 THEN
                operation_type_val := 'INBOUND';
                operation_note := format('库存增加: %s → %s', OLD.current_quantity, NEW.current_quantity);
ELSE
                operation_type_val := 'OUTBOUND';
                operation_note := format('库存减少: %s → %s', OLD.current_quantity, NEW.current_quantity);
END IF;

            RAISE NOTICE '操作类型: %, 数量: %, 备注: %', operation_type_val, ABS(quantity_change), operation_note;

            -- 插入库存操作记录
INSERT INTO stock_operation (
    warehouse_id, ingredient_id, batch_id, operation_type,
    quantity, unit, note, created_time
) VALUES (
             NEW.warehouse_id, NEW.ingredient_id, NEW.id, operation_type_val,
             ABS(quantity_change), NEW.unit,operation_note, NOW()
         );

RAISE NOTICE '操作记录插入完成';
ELSE
            RAISE NOTICE '没有数量变化，跳过操作记录插入';
END IF;

        -- 检查批次状态变化
        IF OLD.status != NEW.status THEN
            RAISE NOTICE '检测到状态变化: % → %', OLD.status, NEW.status;

CASE NEW.status
                WHEN 'DISCARDED' THEN
                    RAISE NOTICE '插入废弃状态操作记录';
INSERT INTO stock_operation (
    warehouse_id, ingredient_id, batch_id, operation_type,
    quantity, unit,note, created_time
) VALUES (
             NEW.warehouse_id, NEW.ingredient_id, NEW.id, 'DISCARD',
             OLD.current_quantity, NEW.unit,format('批次废弃: %s', NEW.batch_number), NOW()
         );
WHEN 'EXPIRED' THEN
                    RAISE NOTICE '插入过期状态操作记录';
INSERT INTO stock_operation (
    warehouse_id, ingredient_id, batch_id, operation_type,
    quantity,  unit,note, created_time
) VALUES (
             NEW.warehouse_id, NEW.ingredient_id, NEW.id, 'EXPIRE',
             OLD.current_quantity, NEW.unit,format('批次过期: %s, 过期日期: %s', NEW.batch_number, NEW.expiry_date), NOW()
         );
WHEN 'CONSUMED' THEN
                    RAISE NOTICE '插入消耗状态操作记录';
INSERT INTO stock_operation (
    warehouse_id, ingredient_id, batch_id, operation_type,
    quantity,unit, note, created_time
) VALUES (
             NEW.warehouse_id, NEW.ingredient_id, NEW.id, 'CONSUME',
             OLD.current_quantity, NEW.unit,format('批次完全消耗: %s', NEW.batch_number), NOW()
         );
ELSE
                    RAISE NOTICE '未知状态变化: %', NEW.status;
END CASE;
ELSE
            RAISE NOTICE '没有状态变化';
END IF;
END IF;

    RAISE NOTICE '=== 触发器执行完成 ===';
RETURN NEW;
END;
$$ LANGUAGE plpgsql;