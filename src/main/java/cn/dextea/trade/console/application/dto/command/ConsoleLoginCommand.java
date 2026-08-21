package cn.dextea.trade.console.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsoleLoginCommand {
    private String username;
    private String password;
}
