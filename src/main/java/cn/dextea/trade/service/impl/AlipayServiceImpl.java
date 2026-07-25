package cn.dextea.trade.service.impl;

import cn.dextea.trade.model.CreateAlipayTradeRequest;
import cn.dextea.trade.config.AlipaySdkConfig;
import cn.dextea.trade.error.OrderErrorCode;
import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.service.AlipayService;
import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.api.AlipayTradeApi;
import com.alipay.v3.model.AlipayTradeCreateModel;
import com.alipay.v3.model.AlipayTradeCreateResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {

    private final AlipayTradeApi tradeApi;
    private final AlipaySdkConfig alipayConfig;

    public AlipayServiceImpl(ApiClient apiClient, AlipaySdkConfig alipayConfig) {
        this.tradeApi = new AlipayTradeApi(apiClient);
        this.alipayConfig = alipayConfig;
    }

    @Override
    public String createTrade(CreateAlipayTradeRequest request) {
        // 由请求 DTO 转换为支付宝 SDK 所需的 AlipayTradeCreateModel
        AlipayTradeCreateModel model = request.toAlipayTradeCreateModel();
        // 开发/测试环境下，将订单总额强制限制为固定金额，避免产生真实交易金额
        BigDecimal forceAmount = alipayConfig.getForceAmount();
        if (forceAmount != null) {
            log.warn("开发环境：将订单 {} 的交易金额从 {} 覆盖为固定值 {}",
                    request.getOrderNo(), request.getTotalPrice(), forceAmount);
            model.totalAmount(forceAmount.toPlainString());
        }
        // 支付宝异步支付回调地址（notify_url）：非空才设置，空则不传给支付宝
        String notifyUrl = alipayConfig.getNotifyUrl();
        if (notifyUrl != null && !notifyUrl.isBlank()) {
            model.notifyUrl(notifyUrl);
        }
        try {
            AlipayTradeCreateResponseModel resp = tradeApi.create(model);
            if (resp == null || resp.getTradeNo() == null) {
                throw new BizError(OrderErrorCode.ALIPAY_TRADE_CREATE_FAILED,
                        "支付宝创建交易返回为空 outTradeNo=" + request.getOrderNo());
            }
            return resp.getTradeNo();
        } catch (ApiException e) {
            log.error("alipay.trade.create 调用失败 outTradeNo={}", request.getOrderNo(), e);
            throw new BizError(OrderErrorCode.ALIPAY_TRADE_CREATE_FAILED,
                    "支付宝创建交易失败: " + e.getMessage());
        }
    }
}
