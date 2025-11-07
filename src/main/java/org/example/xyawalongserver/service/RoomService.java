package org.example.xyawalongserver.service;
import org.example.xyawalongserver.model.dto.request.CreateRoomRequest;
import org.example.xyawalongserver.model.entity.Room;
import org.example.xyawalongserver.model.entity.Player;

import java.util.List;
import java.util.Optional;

public interface  RoomService {
    Room createRoom(CreateRoomRequest request, String creatorUserId);
    List<Room> getRooms();
    /**
     * 用户加入房间
     */
    boolean joinRoom(String roomId, String userId, String userName);

    /**
     * 用户离开房间
     */
    boolean leaveRoom(String roomId, String userId);

    /**
     * 获取房间内的用户列表
     */
    List<Player> getRoomUsers(String roomId);
    /**
     * 根据id找到room
     */
    Optional<Room> findById(String roomId);
}
