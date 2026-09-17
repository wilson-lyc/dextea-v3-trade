package cn.dextea.trade.order.infrastructure.rpc.product;

import dextea.product.v1.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 商品服务 gRPC 客户端基础设施。
 *
 * <p>这里负责创建 channel、stub 和商品领域 RPC 客户端；订单业务通过客户端或
 * 领域 port 访问商品服务，不直接依赖连接细节。</p>
 */
@Configuration
@EnableConfigurationProperties(ProductRpcProperties.class)
public class ProductRpcConfiguration {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel productRpcChannel(ProductRpcProperties properties) {
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(properties.getTarget())
                .keepAliveTime(properties.getKeepAliveTimeSeconds(), TimeUnit.SECONDS)
                .keepAliveTimeout(properties.getKeepAliveTimeoutSeconds(), TimeUnit.SECONDS)
                .keepAliveWithoutCalls(properties.isKeepAliveWithoutCalls());

        if (properties.isPlaintext()) {
            builder.usePlaintext();
        }

        return builder.build();
    }

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub(
            ManagedChannel productRpcChannel) {
        return ProductServiceGrpc.newBlockingStub(productRpcChannel);
    }

    @Bean
    public ProductRpcClient productRpcClient(
            ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub,
            ProductRpcProperties properties) {
        return new ProductRpcClient(productServiceBlockingStub, properties);
    }

    @Bean
    public ProductServiceGrpc.ProductServiceStub productServiceStub(ManagedChannel productRpcChannel) {
        return ProductServiceGrpc.newStub(productRpcChannel);
    }

    @Bean
    public ProductServiceGrpc.ProductServiceFutureStub productServiceFutureStub(
            ManagedChannel productRpcChannel) {
        return ProductServiceGrpc.newFutureStub(productRpcChannel);
    }
}
