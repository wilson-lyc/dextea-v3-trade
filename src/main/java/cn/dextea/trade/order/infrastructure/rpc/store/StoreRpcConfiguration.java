package cn.dextea.trade.order.infrastructure.rpc.store;

import dextea.store.v1.StoreBusinessServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** Store Service 业务读 RPC 基础设施。 */
@Configuration
@EnableConfigurationProperties(StoreRpcProperties.class)
public class StoreRpcConfiguration {

    @Bean(name = "storeRpcChannel", destroyMethod = "shutdownNow")
    public ManagedChannel storeRpcChannel(
            StoreRpcProperties properties, ObjectProvider<DiscoveryClient> discoveryClients) {
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(resolveTarget(properties, discoveryClients))
                .keepAliveTime(properties.getKeepAliveTimeSeconds(), TimeUnit.SECONDS)
                .keepAliveTimeout(properties.getKeepAliveTimeoutSeconds(), TimeUnit.SECONDS)
                .keepAliveWithoutCalls(properties.isKeepAliveWithoutCalls());
        if (properties.isPlaintext()) {
            builder.usePlaintext();
        }
        return builder.build();
    }

    private String resolveTarget(StoreRpcProperties properties, ObjectProvider<DiscoveryClient> discoveryClients) {
        DiscoveryClient discoveryClient = discoveryClients.getIfAvailable();
        if (discoveryClient == null || properties.getServiceName() == null
                || properties.getServiceName().isBlank()) {
            return properties.getTarget();
        }
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(properties.getServiceName());
            if (instances != null && !instances.isEmpty()) {
                ServiceInstance instance = instances.get(0);
                return instance.getHost() + ":" + instance.getPort();
            }
        } catch (RuntimeException ignored) {
            // Nacos is an optimization for routing; a configured static target remains the fallback.
        }
        return properties.getTarget();
    }

    @Bean
    public StoreBusinessServiceGrpc.StoreBusinessServiceBlockingStub storeBusinessServiceBlockingStub(
            @Qualifier("storeRpcChannel") ManagedChannel storeRpcChannel,
            StoreRpcProperties properties) {
        StoreBusinessServiceGrpc.StoreBusinessServiceBlockingStub stub =
                StoreBusinessServiceGrpc.newBlockingStub(storeRpcChannel);
        String token = properties.getBusinessToken();
        if (token == null || token.isBlank()) {
            return stub;
        }
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("x-service-token", Metadata.ASCII_STRING_MARSHALLER), token);
        return stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    @Bean
    public StoreRpcClient storeRpcClient(
            StoreBusinessServiceGrpc.StoreBusinessServiceBlockingStub storeBusinessServiceBlockingStub,
            StoreRpcProperties properties) {
        return new StoreRpcClient(storeBusinessServiceBlockingStub, properties);
    }
}
