package cn.dextea.trade.service.impl;

import cn.dextea.trade.config.AlipayProperties;
import cn.dextea.trade.entity.Order;
import cn.dextea.trade.error.OrderErrorCode;
import cn.dextea.trade.common.BizError;
import cn.dextea.trade.service.AlipayPaymentService;
import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.api.AlipayTradeApi;
import com.alipay.v3.model.AlipayTradeCreateModel;
import com.alipay.v3.model.AlipayTradeCreateResponseModel;
import com.alipay.v3.model.AlipayTradeQueryModel;
import com.alipay.v3.model.AlipayTradeQueryResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 基于 alipay-sdk-java-v3 的 alipay.trade.create 实现（JSAPI 小程序支付下单）。
 * 订单号（orderNo）作为商户订单号 out_trade_no，支付宝侧保证其幂等；
 * 创建成功后返回 trade_no 交由前端 my.tradePay 完成支付。
 *
 * <p>幂等说明：alipay.trade.create 对同一个 out_trade_no 重复调用时，支付宝可能返回
 * “交易已存在 / 交易已被支付” 类子错误码（如 ACQ.TRADE_HAS_EXIST / ACQ.TRADE_HAS_SUCCESS）。
 * 本实现会识别此类情况，转用 alipay.trade.query 按 out_trade_no 回查已有 trade_no 复用，
 * 避免下单链路在“创建成功但回填前崩溃 / 网络超时”的重试场景下永久卡死。
 */
@Slf4j
@Service
public class AlipayPaymentServiceImpl implements AlipayPaymentService {

    /** 表示“交易已存在/已支付”，应复用已有交易号的子错误码。 */
    private static final Set<String> TRADE_EXIST_SUB_CODES = Set.of(
            "ACQ.TRADE_HAS_EXIST",
            "ACQ.TRADE_HAS_SUCCESS");

    private final AlipayProperties props;
    private final AlipayTradeApi tradeApi;

    public AlipayPaymentServiceImpl(AlipayProperties props, ApiClient apiClient) {
        this.props = props;
        this.tradeApi = new AlipayTradeApi(apiClient);
    }

    @Override
    public String createTrade(Order order, String buyerOpenId) {
        AlipayTradeCreateModel model = new AlipayTradeCreateModel()
                .outTradeNo(order.getOrderNo())
                .totalAmount(order.getPrice().toPlainString())
                .subject(props.getSubject())
                .buyerOpenId(buyerOpenId)
                .notifyUrl(props.getNotifyUrl());
        if (props.getProductCode() != null && !props.getProductCode().isBlank()) {
            model.setProductCode(props.getProductCode());
        }
        if (props.getOpAppId() != null && !props.getOpAppId().isBlank()) {
            model.setOpAppId(props.getOpAppId());
        }
        if (order.getStoreId() != null) {
            model.setStoreId(String.valueOf(order.getStoreId()));
        }

        try {
            AlipayTradeCreateResponseModel resp = tradeApi.create(model);
            // 正常返回且拿到交易号，直接返回
            if (resp != null && resp.getTradeNo() != null) {
                return resp.getTradeNo();
            }
            // 返回成功但未携带 trade_no（如交易已存在且不返回 trade_no 的子场景）：
            // 按 out_trade_no 回查已有交易号，作为幂等复用
            log.warn("alipay.trade.create 返回成功但未携带 tradeNo，按 outTradeNo 回查复用 outTradeNo={}",
                    order.getOrderNo());
            return queryTradeNo(order.getOrderNo());
        } catch (ApiException e) {
            if (isTradeAlreadyExisted(e)) {
                log.warn("alipay.trade.create 交易已存在，按 outTradeNo 回查复用 outTradeNo={}",
                        order.getOrderNo());
                return queryTradeNo(order.getOrderNo());
            }
            log.error("alipay.trade.create 调用失败 outTradeNo={}", order.getOrderNo(), e);
            throw new BizError(OrderErrorCode.ALIPAY_TRADE_CREATE_FAILED, "支付宝创建交易失败: " + e.getMessage());
        }
    }

    /**
     * 按商户订单号 out_trade_no 回查支付宝交易号。用于“交易已存在/已支付”场景的幂等复用。
     */
    private String queryTradeNo(String outTradeNo) {
        AlipayTradeQueryModel queryModel = new AlipayTradeQueryModel().outTradeNo(outTradeNo);
        try {
            AlipayTradeQueryResponseModel qr = tradeApi.query(queryModel);
            if (qr != null && qr.getTradeNo() != null) {
                return qr.getTradeNo();
            }
        } catch (ApiException qe) {
            log.error("alipay.trade.query 回查已存在交易失败 outTradeNo={}", outTradeNo, qe);
            throw new BizError(OrderErrorCode.ALIPAY_TRADE_CREATE_FAILED,
                    "支付宝交易已存在但查询失败: " + qe.getMessage());
        }
        throw new BizError(OrderErrorCode.ALIPAY_TRADE_CREATE_FAILED,
                "支付宝交易已存在但查询不到交易号 outTradeNo=" + outTradeNo);
    }

    /**
     * 识别“交易已存在/已支付”类子错误码。支付宝业务错误码位于响应体 JSON 的 sub_code 字段，
     * 这里通过匹配响应体字符串判断（兼容 code=10000 带 sub_code 与 code!=10000 抛异常两种形态）。
     */
    private static boolean isTradeAlreadyExisted(ApiException e) {
        String body = e.getResponseBody();
        if (body == null) {
            return false;
        }
        for (String subCode : TRADE_EXIST_SUB_CODES) {
            if (body.contains(subCode)) {
                return true;
            }
        }
        return false;
    }
}
