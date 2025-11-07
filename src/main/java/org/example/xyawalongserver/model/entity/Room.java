package org.example.xyawalongserver.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // ← 关键注解：忽略未知字段
@JsonInclude(JsonInclude.Include.NON_NULL)  // ← 可选：不序列化null字段
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomId;
    private String roomName;
    private String creator;
    private Integer maxPlayers;
    private Integer currentPlayers = 0;
    private String status = "WAITING"; // WAITING, PLAYING, FINISHED

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();

    private List<String> players = new ArrayList<>();

    // 业务构造函数
    public Room(String roomId, String roomName, String creator, Integer maxPlayers) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.creator = creator;
        this.maxPlayers = maxPlayers;
    }

    // 业务方法
    public boolean isFull() {
        return currentPlayers >= maxPlayers;
    }

    public boolean canJoin() {
        return "WAITING".equals(status) && !isFull();
    }

    public void addPlayer(String playerName) {
        if (!players.contains(playerName)) {
            players.add(playerName);
            currentPlayers = players.size();
        }
    }

    public void removePlayer(String playerName) {
        if (players.remove(playerName)) {
            currentPlayers = players.size();
        }
    }
}