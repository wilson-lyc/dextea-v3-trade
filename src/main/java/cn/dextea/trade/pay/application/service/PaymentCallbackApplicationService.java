package cn.dextea.trade.pay.application.service;

import cn.dextea.trade.pay.application.dto.PaymentCallbackMessage;
import cn.dextea.trade.pay.domain.exception.PayErrorCode;
import cn.dextea.trade.pay.domain.port.OrderPaidEventPublisher;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.shared.domain.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackApplicationService {

    private static final String TRADE_STATUS_SUCCESS = "TRADE_SUCCESS";

    private static final DateTimeFormatter PAID_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderPaidEventPublisher orderPaidEventPublisher;

    public void handle(PaymentCallbackMessage message) {
        Map<String, String> data = message.data();
        if (data == null || data.isEmpty()) {
            throw new BizError(PayErrorCode.PAY_CALLBACK_MESSAGE_INVALID, "支付回调消息缺少业务数据");
        }

        String orderNo = data.get("out_trade_no");
        String tradeNo = data.get("trade_no");
        if (isBlank(orderNo) || isBlank(tradeNo)) {
            log.error("支付回调消息缺少订单号, messageId={}, platform={}", message.id(), message.platform());
            throw new BizError(PayErrorCode.PAY_CALLBACK_MESSAGE_INVALID, "支付回调消息缺少订单号");
        }

        String tradeStatus = data.get("trade_status");
        if (!TRADE_STATUS_SUCCESS.equals(tradeStatus)) {
            log.info("支付回调状态非成功, 忽略处理, orderNo={}, tradeNo={}, tradeStatus={}",
                    orderNo, tradeNo, tradeStatus);
            return;
        }

        LocalDateTime paidAt = parsePaidAt(data.get("gmt_payment"));
        BigDecimal amount = parseAmount(data.get("total_amount"));
        if (amount == null) {
            throw new RetryableCallbackException("支付回调金额缺失或解析失败, 等待上游数据修复后重试, orderNo=" + orderNo);
        }

        orderPaidEventPublisher.publish(new OrderPaidEvent(
                orderNo, tradeNo, message.platform(), paidAt, amount));

        log.info("支付成功回调处理完成, orderNo={}, tradeNo={}, platform={}, paidAt={}, amount={}",
                orderNo, tradeNo, message.platform(), paidAt, amount);
    }

    private LocalDateTime parsePaidAt(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, PAID_AT_FORMAT);
        } catch (Exception e) {
            throw new RetryableCallbackException("支付回调支付时间解析失败, 等待重试, value=" + value);
        }
    }

    private BigDecimal parseAmount(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new RetryableCallbackException("支付回调金额解析失败, 等待重试, value=" + value);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static final class RetryableCallbackException extends RuntimeException {
        RetryableCallbackException(String message) {
            super(message);
        }
    }
}

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackApplicationService {

    private static final String TRADE_STATUS_SUCCESS = "TRADE_SUCCESS";

    private static final DateTimeFormatter PAID_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderPaidEventPublisher orderPaidEventPublisher;

    public void handle(PaymentCallbackMessage message) {
        Map<String, String> data = message.data();
        if (data == null || data.isEmpty()) {
            throw new BizError(PayErrorCode.PAY_CALLBACK_MESSAGE_INVALID, "支付回调消息缺少业务数据");
        }

        String orderNo = data.get("out_trade_no");
        String tradeNo = data.get("trade_no");
        if (isBlank(orderNo) || isBlank(tradeNo)) {
            log.error("支付回调消息缺少订单号, messageId={}, platform={}", message.id(), message.platform());
            throw new BizError(PayErrorCode.PAY_CALLBACK_MESSAGE_INVALID, "支付回调消息缺少订单号");
        }

        String tradeStatus = data.get("trade_status");
        if (!TRADE_STATUS_SUCCESS.equals(tradeStatus)) {
            log.info("支付回调状态非成功, 忽略处理, orderNo={}, tradeNo={}, tradeStatus={}",
                    orderNo, tradeNo, tradeStatus);
            return;
        }

        LocalDateTime paidAt = parsePaidAt(data.get("gmt_payment"));
        BigDecimal amount = parseAmount(data.get("total_amount"));

        orderPaidEventPublisher.publish(new OrderPaidEvent(
                orderNo, tradeNo, message.platform(), paidAt, amount));

        log.info("支付成功回调处理完成, orderNo={}, tradeNo={}, platform={}, paidAt={}, amount={}",
                orderNo, tradeNo, message.platform(), paidAt, amount);
    }

    private LocalDateTime parsePaidAt(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, PAID_AT_FORMAT);
        } catch (Exception e) {
            log.warn("支付回调支付时间解析失败, value={}", value);
            return null;
        }
    }

    private BigDecimal parseAmount(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("支付回调金额解析失败, value={}", value);
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
