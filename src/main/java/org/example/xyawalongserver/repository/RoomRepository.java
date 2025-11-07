package org.example.xyawalongserver.repository;

import org.example.xyawalongserver.model.entity.Room;
import java.util.List;
import java.util.Optional;

public interface RoomRepository {

    // 保存房间
    void save(Room room);

    // 根据ID查找房间
    default Optional<Room> findById(String roomId) {
        return null;
    }

    // 获取所有房间
    List<Room> findAll();

    // 删除房间
    void deleteById(String roomId);

    // 房间是否存在
    boolean existsById(String roomId);

    // 加入房间
    boolean addPlayer(String roomId, String playerName);

    // 离开房间
    boolean removePlayer(String roomId, String playerName);
}