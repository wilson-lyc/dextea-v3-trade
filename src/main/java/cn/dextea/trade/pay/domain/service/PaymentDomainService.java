package cn.dextea.trade.pay.domain.service;

import cn.dextea.trade.pay.domain.gateway.PaymentResultSyncGateway;
import cn.dextea.trade.pay.domain.model.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付领域服务：根据支付结果的终态判定，驱动支付结果同步网关。
 *
 * <p>订单事件映射与幂等判定属于订单域知识，由网关的订单域适配器负责。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDomainService {

    private final PaymentResultSyncGateway paymentResultSyncGateway;

    /**
     * 处理一笔支付结果：支付成功 / 交易关闭走同步网关，非终态仅记录。
     */
    public void process(PaymentResult result) {
        if (result.isSuccess()) {
            paymentResultSyncGateway.syncPaid(result.getOrderNo(), result.getTradeNo(),
                    result.getRawStatus(), result.getTraceId());
        } else if (result.isClosed()) {
            paymentResultSyncGateway.syncClosed(result.getOrderNo(), result.getTraceId());
        } else {
            // 非终态（如 WAIT_BUYER_PAY）或非支付成功状态，仅记录不更新，避免阻塞重试
            log.info("收到非终态/非支付成功的回单，忽略处理: orderNo={}, tradeStatus={}",
                    result.getOrderNo(), result.getRawStatus());
        }
    }
}
