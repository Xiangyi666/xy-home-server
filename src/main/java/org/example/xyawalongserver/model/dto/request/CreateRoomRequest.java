package org.example.xyawalongserver.model.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data; // 推荐使用 Lombok 简化代码

@Data
public class CreateRoomRequest {
    @NotBlank(message = "房间名不能为空")
    private String roomName;

    private Integer maxPlayers; // 可选，默认值可以在 Service 中设置
}