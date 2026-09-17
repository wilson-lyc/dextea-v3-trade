package cn.dextea.trade.order.interfaces.rpc;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(OrderRpcConfiguration.Properties.class)
@RequiredArgsConstructor
public class OrderRpcConfiguration {
    private final Properties properties;
    private final OrderRpcService orderRpcService;

    @Bean(destroyMethod = "shutdownNow")
    public Server orderRpcServer() throws IOException {
        Server server = NettyServerBuilder.forPort(properties.getPort()).addService(orderRpcService).build();
        server.start();
        return server;
    }

    @ConfigurationProperties(prefix = "order.rpc")
    public static class Properties {
        private int port = 9091;
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }
}
