package org.example.xyawalongserver.test;

import org.example.xyawalongserver.model.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service

public class RedisTestService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RedisTestService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public void run(String... args) throws Exception {
//        logger.info("=== Redis 测试开始 ===");
//        testRedisConnection();
//        testBasicOperations();
//        testRoomOperations();
//        logger.info("=== Redis 测试结束 ===");
    }
    public void testRedisConnection() {
        try {
            redisTemplate.opsForValue().set("connection-test", "success");
            String result = (String) redisTemplate.opsForValue().get("connection-test");

            if ("success".equals(result)) {
                logger.info("✅ Redis 连接测试成功！");
            } else {
                logger.error("❌ Redis 连接测试失败");
            }
            redisTemplate.delete("connection-test");

        } catch (Exception e) {
            logger.error("❌ Redis 连接失败: {}", e.getMessage());
        }
    }
    /**
     * 测试 Redis 基本操作
     */
    public void testBasicOperations() {
        try {
            // 测试字符串操作
            redisTemplate.opsForValue().set("test:string", "Hello Redis");
            String stringValue = (String) redisTemplate.opsForValue().get("test:string");
            logger.info("字符串测试: {}", stringValue);

            // 测试列表操作
            redisTemplate.opsForList().rightPush("test:list", "item1");
            redisTemplate.opsForList().rightPush("test:list", "item2");
            Long listSize = redisTemplate.opsForList().size("test:list");
            logger.info("列表测试: 大小 = {}", listSize);

            // 测试集合操作
            redisTemplate.opsForSet().add("test:set", "member1", "member2");
            Long setSize = redisTemplate.opsForSet().size("test:set");
            logger.info("集合测试: 大小 = {}", setSize);

            // 清理测试数据
            redisTemplate.delete("test:string");
            redisTemplate.delete("test:list");
            redisTemplate.delete("test:set");

            logger.info("✅ Redis 基本操作测试完成");

        } catch (Exception e) {
            logger.error("❌ Redis 操作测试失败: {}", e.getMessage());
        }
    }

    /**
     * 详细打印房间信息
     */
    private void printRoomDetails(Room room, String roomKey) {
        System.out.println("┌─────────────────────────────────────────");
        System.out.println("│ 🏠 房间信息 - Key: " + roomKey);
        System.out.println("├─────────────────────────────────────────");
        System.out.println("│ 房间ID: " + room.getRoomId());
        System.out.println("│ 房间名: " + room.getRoomName());
        System.out.println("│ 创建者: " + room.getCreator());
        System.out.println("│ 人数: " + room.getCurrentPlayers() + "/" + room.getMaxPlayers());
        System.out.println("│ 状态: " + room.getStatus());
        System.out.println("│ 创建时间: " + room.getCreateTime());
        System.out.println("│ 玩家列表: " + String.join(", ", room.getPlayers()));
        System.out.println("│ 是否满员: " + (room.isFull() ? "✅ 是" : "❌ 否"));
        System.out.println("│ 是否可以加入: " + (room.canJoin() ? "✅ 是" : "❌ 否"));
        System.out.println("└─────────────────────────────────────────");
    }

    /**
     * 获取并打印房间信息
     */
    public void getAndPrintRoom(String roomKey) {
        try {
            Object roomData = redisTemplate.opsForValue().get(roomKey);

            if (roomData == null) {
                logger.error("❌ 从Redis获取的数据为null, Key: {}", roomKey);
                return;
            }

            if (roomData instanceof Room) {
                Room savedRoom = (Room) roomData;
                printRoomDetails(savedRoom, roomKey);
            } else {
                logger.error("❌ 数据类型不匹配, 期望: Room, 实际: {}",
                        roomData.getClass().getName());
                logger.info("实际数据内容: {}", roomData);
            }

        } catch (Exception e) {
            logger.error("❌ 获取房间数据失败: {}", e.getMessage());
        }
    }

    /**
     * 测试房间数据操作
     */
    public void testRoomOperations() {
        try {
//            // 模拟房间数据
//            String roomId = "TEST001";
//            String roomKey = "avalon:room:" + roomId;
//            Map<String, Object> roomData = new HashMap<>();
//            roomData.put("roomId", roomId);
//            roomData.put("roomName", "测试房间");
//            roomData.put("creator", "测试玩家");
//            roomData.put("maxPlayers", 8);
//            roomData.put("currentPlayers", 0);
//            roomData.put("status", "WAITING");
            // 测试房间数据存储
//            redisTemplate.opsForValue().set(roomKey, roomData);

            // 测试房间数据读取
            Object keys = redisTemplate.keys("*");
            Object roomData = redisTemplate.opsForValue().get("avalon:room:ROOM_1760671873671");

            logger.info("房间当前keys: {}", keys);
            getAndPrintRoom("avalon:room:ROOM_1760671873671");


            logger.info("✅ 房间数据操作测试完成");

        } catch (Exception e) {
            logger.error("❌ 房间数据操作测试失败: {}", e.getMessage());
        }
    }
}