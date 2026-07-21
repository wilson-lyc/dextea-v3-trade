package cn.dextea.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝开放平台配置。
 * 密钥等敏感信息（appId、privateKey、alipayPublicKey、notifyUrl、opAppId）统一在
 * Nacos 配置中心（dataId: dextea-trade.yaml）以 alipay.* 维护，运行时由 Nacos 属性源覆盖。
 * 本地 application.yaml 仅保留 serverUrl / subject / productCode 等非敏感默认值。
 */
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /** 支付宝网关地址 */
    private String serverUrl = "https://openapi.alipay.com";

    /** 应用 AppId */
    private String appId;

    /** 应用私钥（PKCS8） */
    private String privateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 异步通知地址（alipay.trade.create 支付结果回调） */
    private String notifyUrl;

    /** 订单标题前缀 */
    private String subject = "德贤茶庄订单";

    /**
     * 产品码。基于买家 openid 的当面付/小程序场景常用 JSAPI_PAY；
     * App 支付常用 QUICK_MSECURITY_PAY，具体以实际签约产品为准。
     */
    private String productCode = "JSAPI_PAY";

    /** 小程序 AppId（product_code=JSAPI_PAY 时必填） */
    private String opAppId;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getAlipayPublicKey() {
        return alipayPublicKey;
    }

    public void setAlipayPublicKey(String alipayPublicKey) {
        this.alipayPublicKey = alipayPublicKey;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getOpAppId() {
        return opAppId;
    }

    public void setOpAppId(String opAppId) {
        this.opAppId = opAppId;
    }
}
