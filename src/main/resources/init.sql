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