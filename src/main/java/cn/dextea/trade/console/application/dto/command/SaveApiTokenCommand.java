package cn.dextea.trade.console.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveApiTokenCommand {
    private Long id;
    private String name;
    private boolean enabled = true;
    private LocalDateTime expireAt;
}
