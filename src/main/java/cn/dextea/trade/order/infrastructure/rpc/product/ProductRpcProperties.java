package cn.dextea.trade.order.infrastructure.rpc.product;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 商品 RPC 客户端连接配置。
 *
 * <p>当前只负责构建客户端基础设施，{@code target} 先支持 gRPC 直连地址；
 * 后续接入 Nacos resolver 时可以复用 {@code serviceName}，不需要改订单领域代码。</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "product.rpc")
public class ProductRpcProperties {

    /** gRPC target，例如 127.0.0.1:9090 或 dns:///dextea-product。 */
    private String target = "127.0.0.1:9090";

    /** 商品服务在 Nacos 中的服务名，供后续服务发现接入使用。 */
    private String serviceName = "dextea-product";

    /** 当前商品服务使用明文 gRPC；TLS 接入时再扩展 credentials 配置。 */
    private boolean plaintext = true;

    private long keepAliveTimeSeconds = 30;
    private long keepAliveTimeoutSeconds = 10;
    private boolean keepAliveWithoutCalls;

    private long deadlineMillis = 3000;
}
