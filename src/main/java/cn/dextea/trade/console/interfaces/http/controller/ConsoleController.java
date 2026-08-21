package cn.dextea.trade.console.interfaces.http.controller;

import cn.dextea.trade.console.application.dto.command.ConsoleLoginCommand;
import cn.dextea.trade.console.application.dto.command.SaveApiTokenCommand;
import cn.dextea.trade.console.application.dto.result.ApiTokenResult;
import cn.dextea.trade.console.application.dto.result.ConsoleLoginResult;
import cn.dextea.trade.console.application.usecase.ConsoleLoginUseCase;
import cn.dextea.trade.console.application.usecase.TokenManageUseCase;
import cn.dextea.trade.shared.api.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/console")
@RequiredArgsConstructor
@Tag(name = "控制台", description = "登录与令牌维护")
public class ConsoleController {

    private static final String SESSION_HEADER = "X-Console-Session";

    private final ConsoleLoginUseCase consoleLoginUseCase;
    private final TokenManageUseCase tokenManageUseCase;

    @PostMapping("/login")
    @Operation(summary = "控制台登录")
    public APIResponse<ConsoleLoginResult> login(@Valid @RequestBody ConsoleLoginCommand command) {
        return APIResponse.success(consoleLoginUseCase.execute(command));
    }

    @PostMapping("/logout")
    @Operation(summary = "控制台登出")
    public APIResponse<Void> logout(@RequestHeader(value = SESSION_HEADER, required = false) String session) {
        consoleLoginUseCase.logout(session);
        return APIResponse.success();
    }

    @GetMapping("/tokens")
    @Operation(summary = "令牌列表")
    public APIResponse<List<ApiTokenResult>> listTokens(@RequestHeader(SESSION_HEADER) String session) {
        consoleLoginUseCase.requireLogin(session);
        return APIResponse.success(tokenManageUseCase.list());
    }

    @PostMapping("/tokens")
    @Operation(summary = "创建令牌")
    public APIResponse<ApiTokenResult> createToken(
            @RequestHeader(SESSION_HEADER) String session,
            @Valid @RequestBody SaveApiTokenCommand command) {
        consoleLoginUseCase.requireLogin(session);
        return APIResponse.success(tokenManageUseCase.create(command));
    }

    @PutMapping("/tokens")
    @Operation(summary = "更新令牌")
    public APIResponse<ApiTokenResult> updateToken(
            @RequestHeader(SESSION_HEADER) String session,
            @Valid @RequestBody SaveApiTokenCommand command) {
        consoleLoginUseCase.requireLogin(session);
        return APIResponse.success(tokenManageUseCase.update(command));
    }

    @PutMapping("/tokens/enabled")
    @Operation(summary = "启用/禁用令牌")
    public APIResponse<Void> toggleEnabled(
            @RequestHeader(SESSION_HEADER) String session,
            @RequestParam @NotNull Long id,
            @RequestParam boolean enabled) {
        consoleLoginUseCase.requireLogin(session);
        tokenManageUseCase.toggleEnabled(id, enabled);
        return APIResponse.success();
    }

    @DeleteMapping("/tokens")
    @Operation(summary = "删除令牌")
    public APIResponse<Void> deleteToken(
            @RequestHeader(SESSION_HEADER) String session,
            @RequestParam @NotNull Long id) {
        consoleLoginUseCase.requireLogin(session);
        tokenManageUseCase.delete(id);
        return APIResponse.success();
    }
}
