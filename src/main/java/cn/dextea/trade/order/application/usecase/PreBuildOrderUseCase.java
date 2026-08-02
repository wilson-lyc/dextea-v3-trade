package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreBuildOrderUseCase {

    public PreBuildOrderResult execute(PreBuildOrderCommand command) {
        return PreBuildOrderResult.builder()
                .build();
    }
}
