package org.example.xyawalongserver.model.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoomResponse {
    private String roomId;
    private String roomName;
    private String creatorName;
    private Integer currentPlayers;
    private Integer maxPlayers;
    private String status; // "WAITING", "PLAYING"
    private LocalDateTime createdAt;
}