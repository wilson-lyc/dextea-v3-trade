package cn.dextea.trade.console.application.usecase;

import cn.dextea.trade.console.application.dto.command.SaveApiTokenCommand;
import cn.dextea.trade.console.application.dto.result.ApiTokenResult;
import cn.dextea.trade.console.infrastructure.adapter.ApiTokenCache;
import cn.dextea.trade.console.infrastructure.persistence.mapper.ApiTokenMapper;
import cn.dextea.trade.console.infrastructure.persistence.po.ApiTokenPO;
import cn.dextea.trade.shared.error.AuthErrorCode;
import cn.dextea.trade.shared.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenManageUseCase {

    private final ApiTokenMapper apiTokenMapper;
    private final ApiTokenCache apiTokenCache;

    public ApiTokenPO verify(String token) {
        if (token == null || token.isBlank()) {
            throw new BizError(AuthErrorCode.TOKEN_INVALID);
        }
        ApiTokenPO cached = apiTokenCache.get(token).orElse(null);
        if (cached == null) {
            ApiTokenPO po = apiTokenMapper.selectByToken(token);
            if (po == null) {
                apiTokenCache.putMissing(token);
                throw new BizError(AuthErrorCode.TOKEN_INVALID);
            }
            apiTokenCache.put(po);
            cached = po;
        }
        if (cached == null || !cached.isEnabled()) {
            throw new BizError(AuthErrorCode.TOKEN_DISABLED);
        }
        if (cached.getExpireAt() != null && cached.getExpireAt().isBefore(LocalDateTime.now())) {
            apiTokenCache.evict(token);
            throw new BizError(AuthErrorCode.TOKEN_EXPIRED);
        }
        return cached;
    }

    public List<ApiTokenResult> list() {
        return apiTokenMapper.list().stream().map(this::toResult).collect(Collectors.toList());
    }

    public ApiTokenResult create(SaveApiTokenCommand command) {
        ApiTokenPO po = new ApiTokenPO();
        String raw = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        po.setToken(raw);
        po.setName(command.getName());
        po.setEnabled(command.isEnabled());
        po.setExpireAt(command.getExpireAt());
        apiTokenMapper.insert(po);
        return new ApiTokenResult(po.getId(), raw, po.getName(), po.isEnabled(),
                po.getExpireAt(), po.getCreatedAt(), po.getUpdatedAt());
    }

    public ApiTokenResult update(SaveApiTokenCommand command) {
        ApiTokenPO existing = apiTokenMapper.selectById(command.getId());
        if (existing == null) {
            throw new BizError(AuthErrorCode.TOKEN_INVALID);
        }
        apiTokenMapper.update(command.getId(), command.getName(), command.getExpireAt());
        apiTokenCache.evict(existing.getToken());
        ApiTokenPO refreshed = apiTokenMapper.selectById(command.getId());
        return toResult(refreshed);
    }

    public void toggleEnabled(Long id, boolean enabled) {
        ApiTokenPO existing = apiTokenMapper.selectById(id);
        if (existing == null) {
            throw new BizError(AuthErrorCode.TOKEN_INVALID);
        }
        apiTokenMapper.updateEnabled(id, enabled);
        apiTokenCache.evict(existing.getToken());
    }

    public void delete(Long id) {
        ApiTokenPO existing = apiTokenMapper.selectById(id);
        if (existing == null) {
            throw new BizError(AuthErrorCode.TOKEN_INVALID);
        }
        apiTokenMapper.deleteById(id);
        apiTokenCache.evict(existing.getToken());
    }

    private ApiTokenResult toResult(ApiTokenPO po) {
        return new ApiTokenResult(po.getId(), null, po.getName(), po.isEnabled(),
                po.getExpireAt(), po.getCreatedAt(), po.getUpdatedAt());
    }
}
