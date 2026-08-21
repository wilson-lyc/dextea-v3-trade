package cn.dextea.trade.console.application.usecase;

import cn.dextea.trade.console.application.dto.command.ConsoleLoginCommand;
import cn.dextea.trade.console.application.dto.result.ConsoleLoginResult;
import cn.dextea.trade.console.infrastructure.adapter.ConsoleSessionStore;
import cn.dextea.trade.shared.config.AuthConfig;
import cn.dextea.trade.shared.error.AuthErrorCode;
import cn.dextea.trade.shared.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsoleLoginUseCase {

    private final AuthConfig authConfig;
    private final ConsoleSessionStore consoleSessionStore;

    public ConsoleLoginResult execute(ConsoleLoginCommand command) {
        AuthConfig.Console console = authConfig.getConsole();
        if (!console.getUsername().equals(command.getUsername())
                || !console.getPassword().equals(command.getPassword())) {
            throw new BizError(AuthErrorCode.CONSOLE_CREDENTIAL_ERROR);
        }
        String session = consoleSessionStore.create();
        return new ConsoleLoginResult(session);
    }

    public void logout(String session) {
        consoleSessionStore.destroy(session);
    }

    public void requireLogin(String session) {
        if (!consoleSessionStore.valid(session)) {
            throw new BizError(AuthErrorCode.CONSOLE_UNAUTHORIZED);
        }
        consoleSessionStore.refresh(session);
    }
}
