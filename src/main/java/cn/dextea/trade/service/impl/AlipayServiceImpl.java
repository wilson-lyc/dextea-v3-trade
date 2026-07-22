package cn.dextea.trade.service.impl;

import cn.dextea.trade.model.CreateAlipayTradeRequest;
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

@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {

    private final AlipayTradeApi tradeApi;

    public AlipayServiceImpl(ApiClient apiClient) {
        this.tradeApi = new AlipayTradeApi(apiClient);
    }

    @Override
    public String createTrade(CreateAlipayTradeRequest request) {
        // 由请求 DTO 转换为支付宝 SDK 所需的 AlipayTradeCreateModel
        AlipayTradeCreateModel model = request.toAlipayTradeCreateModel();
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
