package org.example.xyawalongserver.service.impl;

import  org.example.xyawalongserver.model.dto.request.CreateRoomRequest;
import org.example.xyawalongserver.service.RoomService;
import org.example.xyawalongserver.repository.RoomRepository;
import org.example.xyawalongserver.model.entity.Room;
import org.example.xyawalongserver.model.entity.Player;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    // 后续这里会注入 Repository 和 RedisTemplate
     private final RoomRepository roomRepository;

    @Override
    public Room createRoom(CreateRoomRequest request, String creatorUserId) {
        // 1. 此处是业务逻辑
        // 2. 生成房间ID，设置默认人数等
        // 3. 将房间信息保存到 Redis 或数据库


        String roomId = "ROOM_" + System.currentTimeMillis();
        String roomName = request.getRoomName();
        String creator = creatorUserId;
        Integer maxPlayers = request.getMaxPlayers() != null ? request.getMaxPlayers() : 8;
        Room room = new Room(roomId,roomName,creator,maxPlayers);
        room.addPlayer(creator);
        roomRepository.save(room);
        log.info("用户 {} 创建了房间: {}", creatorUserId, request.getRoomName());

        return room;
    }
    @Override
    public List<Room> getRooms() {
        return roomRepository.findAll();
    }
    @Override
    public boolean joinRoom(String roomId, String userId, String userName) {
        log.info("用户尝试加入房间: roomId={}, userId={}, userName={}", roomId, userId, userName);

        try {
            // 1. 参数验证
            if (roomId == null || roomId.trim().isEmpty()) {
                throw new IllegalArgumentException("房间ID不能为空");
            }
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (userName == null || userName.trim().isEmpty()) {
                throw new IllegalArgumentException("用户名不能为空");
            }

            String cleanedRoomId = roomId.trim();
            String cleanedUserId = userId.trim();
            String cleanedUserName = userName.trim();

            // 2. 检查房间是否存在
            Room room = roomRepository.findById(cleanedRoomId)
                    .orElseThrow(() -> new RuntimeException("房间不存在: " + cleanedRoomId));

            // 3. 检查房间状态
            if (!"WAITING".equals(room.getStatus())) {
                throw new RuntimeException("房间状态不允许加入，当前状态: " + room.getStatus());
            }

            // 4. 检查是否已满
            if (room.isFull()) {
                throw new RuntimeException("房间已满，无法加入");
            }

            // 5. 检查用户是否已在房间中
            if (room.getPlayers().contains(cleanedUserId)) {
                throw new RuntimeException("用户已在房间中");
            }

            // 6. 检查用户是否在其他房间中
            if (isUserInOtherRoom(cleanedUserId, cleanedRoomId)) {
                throw new RuntimeException("用户已在其他房间中");
            }

            // 7. 添加用户到房间
            boolean success = roomRepository.addPlayer(cleanedRoomId, cleanedUserId);

            if (success) {
                log.info("✅ 用户加入房间成功: roomId={}, userId={}, userName={}",
                        cleanedRoomId, cleanedUserId, cleanedUserName);

                // 更新房间用户信息（如果需要存储用户详情）
                updateRoomUserInfo(cleanedRoomId, cleanedUserId, cleanedUserName);
            }

            return success;

        } catch (IllegalArgumentException e) {
            log.warn("加入房间参数错误: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.warn("加入房间业务错误: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("加入房间系统错误: {}", e.getMessage(), e);
            throw new RuntimeException("加入房间失败，请稍后重试");
        }
    }

    @Override
    public boolean leaveRoom(String roomId, String userId) {
        log.info("用户离开房间: roomId={}, userId={}", roomId, userId);

        try {
            if (roomId == null || roomId.trim().isEmpty()) {
                throw new IllegalArgumentException("房间ID不能为空");
            }
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("用户ID不能为空");
            }

            String cleanedRoomId = roomId.trim();
            String cleanedUserId = userId.trim();

            // 从房间移除用户
            boolean success = roomRepository.removePlayer(cleanedRoomId, cleanedUserId);

            if (success) {
                log.info("✅ 用户离开房间成功: roomId={}, userId={}", cleanedRoomId, cleanedUserId);

                // 清理用户信息
                removeRoomUserInfo(cleanedRoomId, cleanedUserId);
            }

            return success;

        } catch (Exception e) {
            log.error("离开房间失败: {}", e.getMessage(), e);
            throw new RuntimeException("离开房间失败: " + e.getMessage());
        }
    }
    @Override
    public Optional<Room> findById(String roomId) {
//        return roomRepository.findById(roomId);
        try {
            Object roomData = roomRepository.findById(roomId);
            if (roomData instanceof Room) {
                Room room = (Room) roomData;
                return Optional.of(room); // 房间存在，返回包装的 Room
            } else {
                return Optional.empty(); // 房间不存在或数据类型不匹配
            }

        } catch (Exception e) {
            log.error("查找房间失败: roomId={}, error={}", roomId, e.getMessage());
            return Optional.empty();
        }
    }
    @Override
    public List<Player> getRoomUsers(String roomId) {
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("房间不存在: " + roomId));

            // 这里可以根据需要返回用户详情
            // 目前只返回用户ID列表，可以扩展为返回完整用户信息
            return room.getPlayers().stream()
                    .map(playerId -> new Player(playerId, "玩家" + playerId)) // 临时实现
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取房间用户列表失败: {}", e.getMessage());
            throw new RuntimeException("获取用户列表失败: " + e.getMessage());
        }
    }
    /**
     * 检查用户是否在其他房间中
     */
    private boolean isUserInOtherRoom(String userId, String currentRoomId) {
        List<Room> allRooms = roomRepository.findAll();
        return allRooms.stream()
                .filter(room -> !room.getRoomId().equals(currentRoomId)) // 排除当前房间
                .anyMatch(room -> room.getPlayers().contains(userId) &&
                        "WAITING".equals(room.getStatus()));
    }
    /**
     * 更新房间用户信息（可选功能）
     */
    private void updateRoomUserInfo(String roomId, String userId, String userName) {
        try {
            // 可以在这里存储用户详细信息到Redis
            String userKey = "avalon:room:" + roomId + ":user:" + userId;
            Player user = new Player(userId, userName);
            // redisTemplate.opsForValue().set(userKey, user);

        } catch (Exception e) {
            log.warn("更新用户信息失败: {}", e.getMessage());
            // 不影响主要功能，只记录日志
        }
    }

    private void removeRoomUserInfo(String roomId, String userId) {
        try {
            String userKey = "avalon:room:" + roomId + ":user:" + userId;
            // redisTemplate.delete(userKey);
        } catch (Exception e) {
            log.warn("清理用户信息失败: {}", e.getMessage());
        }
    }
}