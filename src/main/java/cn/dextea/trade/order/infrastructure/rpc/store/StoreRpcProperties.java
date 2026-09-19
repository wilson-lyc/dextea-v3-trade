package cn.dextea.trade.order.infrastructure.rpc.store;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Trade 访问 Store Service 业务读面的连接配置。 */
@Getter
@Setter
@ConfigurationProperties(prefix = "store.rpc")
public class StoreRpcProperties {

    /** Nacos 无可用实例时使用的静态 gRPC 地址。 */
    private String target = "127.0.0.1:9092";

    private String serviceName = "dextea-store-service";

    /** StoreBusinessService 的 x-service-token。 */
    private String businessToken = "";

    private boolean plaintext = true;
    private long keepAliveTimeSeconds = 30;
    private long keepAliveTimeoutSeconds = 10;
    private boolean keepAliveWithoutCalls;
    private long deadlineMillis = 3000;
}
