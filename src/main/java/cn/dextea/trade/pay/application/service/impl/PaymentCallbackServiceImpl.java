package cn.dextea.trade.pay.application.service.impl;

import cn.dextea.trade.pay.api.dto.PaymentCallbackData;
import cn.dextea.trade.pay.api.dto.PaymentCallbackMessage;
import cn.dextea.trade.pay.application.service.PaymentCallbackService;
import cn.dextea.trade.pay.domain.model.PaymentResult;
import cn.dextea.trade.pay.domain.service.PaymentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付回单处理应用服务实现：
 * 校验回单消息、解析为渠道无关的 {@link PaymentResult}，交由支付领域服务处理。
 *
 * <p>订单状态流转与幂等判定由订单域的 {@code PaymentResultSyncGateway} 适配器负责，
 * 本类不依赖任何订单域内部类。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackServiceImpl implements PaymentCallbackService {

    private final PaymentDomainService paymentDomainService;

    @Override
    public void handleCallback(PaymentCallbackMessage message) {
        if (message == null || message.getData() == null) {
            log.error("支付回单消息缺失 data 字段，忽略: message={}", message);
            return;
        }
        PaymentCallbackData data = message.getData();
        String orderNo = data.getOutTradeNo();
        if (orderNo == null || orderNo.isBlank()) {
            log.error("支付回单缺少 out_trade_no，忽略: traceId={}", message.getTraceId());
            return;
        }

        PaymentResult result = PaymentResult.evaluate(
                orderNo, data.getTradeNo(), message.getPlatform(),
                data.getTradeStatus(), message.getTraceId());
        paymentDomainService.process(result);
    }
}
