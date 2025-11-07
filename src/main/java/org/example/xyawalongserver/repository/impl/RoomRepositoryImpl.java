package org.example.xyawalongserver.repository.impl;


import org.example.xyawalongserver.model.entity.Room;
import org.example.xyawalongserver.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RoomRepositoryImpl implements RoomRepository {

    private static final String ROOM_KEY_PREFIX = "avalon:room:";
    private static final String ROOM_IDS_KEY = "avalon:room:ids";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(Room room) {
        String roomKey = ROOM_KEY_PREFIX + room.getRoomId();

        // 保存房间数据
        redisTemplate.opsForValue().set(roomKey, room);

        // 将房间ID添加到房间列表
        redisTemplate.opsForSet().add(ROOM_IDS_KEY, room.getRoomId());
    }

    @Override
    public Optional<Room> findById(String roomId) {
        String roomKey = ROOM_KEY_PREFIX + roomId;
        Room room = (Room) redisTemplate.opsForValue().get(roomKey);
        return Optional.ofNullable(room);
    }

    @Override
    public List<Room> findAll() {
        Set<Object> roomIds = redisTemplate.opsForSet().members(ROOM_IDS_KEY);
        if (roomIds == null || roomIds.isEmpty()) {
            return new ArrayList<>();
        }

        return roomIds.stream()
                .map(id -> findById((String) id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted((r1, r2) -> r2.getCreateTime().compareTo(r1.getCreateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String roomId) {
        String roomKey = ROOM_KEY_PREFIX + roomId;
        redisTemplate.delete(roomKey);
        redisTemplate.opsForSet().remove(ROOM_IDS_KEY, roomId);
    }

    @Override
    public boolean existsById(String roomId) {
        String roomKey = ROOM_KEY_PREFIX + roomId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(roomKey));
    }

    @Override
    public boolean addPlayer(String roomId, String playerName) {
        Optional<Room> roomOpt = findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            if (room.getPlayers().contains(playerName)) {
                return true; // 玩家已在房间中
            }
            if (room.getCurrentPlayers() < room.getMaxPlayers()) {
                room.getPlayers().add(playerName);
                room.setCurrentPlayers(room.getCurrentPlayers() + 1);
                save(room);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removePlayer(String roomId, String playerName) {
        Optional<Room> roomOpt = findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            if (room.getPlayers().remove(playerName)) {
                room.setCurrentPlayers(room.getCurrentPlayers() - 1);
                save(room);

                // 如果房间没人了，删除房间
                if (room.getCurrentPlayers() == 0) {
                    deleteById(roomId);
                }
                return true;
            }
        }
        return false;
    }
}