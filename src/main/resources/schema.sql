-- 用户表 (多租户基础)
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100),
                       created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 仓库表 (每个用户可以创建多个仓库)
CREATE TABLE warehouse (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           name VARCHAR(100) NOT NULL,
                           description TEXT,
                           location VARCHAR(200),
                           created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 食材表 (全局共享)
CREATE TABLE ingredient (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            category VARCHAR(50),
                            unit VARCHAR(20) NOT NULL,
                            shelf_life_days INTEGER,
                            created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 批次表 (新增：详细的批次管理)
CREATE TABLE batch (
                       id BIGSERIAL PRIMARY KEY,
                       warehouse_id BIGINT NOT NULL,
                       ingredient_id BIGINT NOT NULL,
                       batch_number VARCHAR(100) NOT NULL,
                       initial_quantity NUMERIC(10, 2) NOT NULL,
                       current_quantity NUMERIC(10, 2) NOT NULL,
                       production_date DATE NOT NULL,
                       expiry_date DATE NOT NULL,
                       purchase_price NUMERIC(10, 2),
                       supplier VARCHAR(200),
                       status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, EXPIRED, CONSUMED, DISCARDED
                       created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                       updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                       FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE CASCADE,
                       FOREIGN KEY (ingredient_id) REFERENCES ingredient(id),
                       UNIQUE(warehouse_id, ingredient_id, batch_number)
);
-- 家庭表
CREATE TABLE families (
                          id INT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL COMMENT '家庭名称',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建用户家庭关联表
CREATE TABLE user_families (
                               id SERIAL PRIMARY KEY,
                               user_id INTEGER NOT NULL,
                               family_id INTEGER NOT NULL,
                               role VARCHAR(20) DEFAULT 'MEMBER',
                               joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_user_families_user
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               CONSTRAINT fk_user_families_family
                                   FOREIGN KEY (family_id) REFERENCES families(id) ON DELETE CASCADE,
                               CONSTRAINT unique_user_family
                                   UNIQUE (user_id, family_id),
                               CONSTRAINT chk_user_families_role
                                   CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER'))
);
-- 库存汇总表 (新增：用于快速查询)
CREATE TABLE inventory_summary (
                                   id BIGSERIAL PRIMARY KEY,
                                   warehouse_id BIGINT NOT NULL,
                                   ingredient_id BIGINT NOT NULL,
                                   total_stock NUMERIC(10, 2) DEFAULT 0,
                                   min_stock_alert NUMERIC(10, 2) DEFAULT 0,
                                   created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE CASCADE,
                                   FOREIGN KEY (ingredient_id) REFERENCES ingredient(id),
                                   UNIQUE(warehouse_id, ingredient_id)
);



-- 菜品表 (用户私有或全局共享)
CREATE TABLE dish (
                      id BIGSERIAL PRIMARY KEY,
                      user_id BIGINT,
                      name VARCHAR(100) NOT NULL,
                      description TEXT,
                      is_public BOOLEAN DEFAULT FALSE,
                      created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 配方表 (关联菜品和食材)
CREATE TABLE recipe (
                        id BIGSERIAL PRIMARY KEY,
                        dish_id BIGINT NOT NULL,
                        ingredient_id BIGINT NOT NULL,
                        quantity_required NUMERIC(10, 2) NOT NULL,
                        created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (dish_id) REFERENCES dish(id) ON DELETE CASCADE,
                        FOREIGN KEY (ingredient_id) REFERENCES ingredient(id),
                        UNIQUE(dish_id, ingredient_id)
);

-- 库存操作流水表 (改进：关联具体批次)
CREATE TABLE stock_operation (
                                 id BIGSERIAL PRIMARY KEY,
                                 warehouse_id BIGINT NOT NULL,
                                 ingredient_id BIGINT NOT NULL,
                                 batch_id BIGINT, -- 新增：关联具体批次
                                 operation_type VARCHAR(20) NOT NULL, -- PURCHASE, CONSUME, ADJUST, TRANSFER
                                 quantity NUMERIC(10, 2) NOT NULL,
                                 note TEXT,
                                 created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
                                 FOREIGN KEY (ingredient_id) REFERENCES ingredient(id),
                                 FOREIGN KEY (batch_id) REFERENCES batch(id) ON DELETE SET NULL
);

-- 创建用量统计表，用于计算库存等级
CREATE TABLE usage_statistics (
                                  id BIGSERIAL PRIMARY KEY,
                                  warehouse_id BIGINT NOT NULL,
                                  ingredient_id BIGINT NOT NULL,
                                  average_daily_usage NUMERIC(10, 2) DEFAULT 0, -- 平均日用量（克）
                                  usage_period_days INTEGER DEFAULT 30, -- 统计周期（天）
                                  last_calculated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE CASCADE,
                                  FOREIGN KEY (ingredient_id) REFERENCES ingredient(id),
                                  UNIQUE(warehouse_id, ingredient_id)
);