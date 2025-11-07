package org.example.xyawalongserver.controller;


import org.example.xyawalongserver.model.dto.request.CreateRoomRequest;
import org.example.xyawalongserver.model.dto.response.RoomResponse;

import org.example.xyawalongserver.model.entity.Room;
import org.example.xyawalongserver.service.RoomService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    // 使用 @Autowired 显式标注构造函数
    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ApiResponse<Room> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            @RequestHeader("User-Id") String userId // 临时从 Header 取用户ID，后期改为从 Token 解析
    ) {
        try {
            Room newRoom = roomService.createRoom(request, userId);
            return ApiResponse.success(newRoom);
        } catch (Exception e) {
            return ApiResponse.error("创建房间失败: " + e.getMessage());
        }
    }

    @GetMapping("findAll")
    public ApiResponse<List<Room>> getRooms(
    ) {
        try {
            List<Room> rooms = roomService.getRooms();
            return ApiResponse.success(rooms);
        } catch (Exception e) {
            return ApiResponse.error("获取房间: " + e.getMessage());
        }
    }

    /**
     * 用户加入房间
     */
    @PostMapping("/join")
    public ApiResponse<Object> joinRoom(
            @RequestBody Map<String, Object> jsonMap
    ) {
        String roomId = (String) jsonMap.get("roomId");
        String userId = (String) jsonMap.get("userId");
        String userName = (String) jsonMap.get("userName");

        logger.info("接收到加入房间请求: roomId={}, userId={}, userName={}",
                roomId, userId, userName);

        try {
            boolean success = roomService.joinRoom(roomId, userId, userName);
            Object room = roomService.findById(roomId);
            if (success) {
//                JoinRoomResponse response = new JoinRoomResponse(
//                        roomId,
//                        request.getUserId(),
//                        request.getUserName(),
//                        "加入房间成功"
//                );
                logger.info("✅ 加入房间成功: roomId={}, userId={}", roomId, userId);
                return ApiResponse.success(room);
            } else {
                logger.warn("❌ 加入房间失败: roomId={}, userId={}", roomId, userId);
                return ApiResponse.error("加入房间失败");
            }

        } catch (IllegalArgumentException e) {
            logger.warn("加入房间参数错误: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("加入房间业务错误: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("加入房间系统错误: {}", e.getMessage(), e);
        }
        return null;
    }
}
