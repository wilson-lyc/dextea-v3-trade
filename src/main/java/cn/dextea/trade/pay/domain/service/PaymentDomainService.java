package cn.dextea.trade.pay.domain.service;
import cn.dextea.trade.pay.domain.gateway.PaymentResultSyncGateway;
import cn.dextea.trade.pay.domain.model.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RequiredArgsConstructor
public class PaymentDomainService {
    private final PaymentResultSyncGateway paymentResultSyncGateway;
    public void process(PaymentResult result) {
        if (result.isSuccess()) {
            paymentResultSyncGateway.syncPaid(result.getOrderNo(), result.getTradeNo(),
                    result.getRawStatus(), result.getTraceId());
        } else if (result.isClosed()) {
            paymentResultSyncGateway.syncClosed(result.getOrderNo(), result.getTraceId());
        } else {
            log.info("收到非终态/非支付成功的回单，忽略处理: orderNo={}, tradeStatus={}",
                    result.getOrderNo(), result.getRawStatus());
        }
    }
}
