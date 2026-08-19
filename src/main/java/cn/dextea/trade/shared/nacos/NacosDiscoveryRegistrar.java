package cn.dextea.trade.shared.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class NacosDiscoveryRegistrar {
    private final NacosDiscoveryProperties properties;
    private NamingService namingService;

    public NacosDiscoveryRegistrar(NacosDiscoveryProperties properties) {
        this.properties = properties;
    }

    public void register() {
        if (!properties.isEnabled() || !properties.isRegisterEnabled()) {
            return;
        }
        try {
            Properties props = new Properties();
            props.setProperty(PropertyKeyConst.SERVER_ADDR, properties.getServerAddr());
            if (properties.getNamespace() != null && !properties.getNamespace().isEmpty()) {
                props.setProperty(PropertyKeyConst.NAMESPACE, properties.getNamespace());
            }
            if (properties.getUsername() != null && !properties.getUsername().isEmpty()) {
                props.setProperty(PropertyKeyConst.USERNAME, properties.getUsername());
                props.setProperty(PropertyKeyConst.PASSWORD, properties.getPassword());
            }
            this.namingService = NamingFactory.createNamingService(props);

            Instance instance = new Instance();
            instance.setServiceName(properties.getService());
            instance.setIp(resolveIp());
            instance.setPort(properties.getPort());
            instance.setClusterName(properties.getClusterName());
            instance.setEphemeral(true);
            namingService.registerInstance(properties.getService(), properties.getGroup(), instance);
            log.info("已向 Nacos 注册服务实例 {} @ {}:{} (namespace={}, group={})",
                    properties.getService(), instance.getIp(), instance.getPort(),
                    properties.getNamespace(), properties.getGroup());
        } catch (NacosException e) {
            log.warn("向 Nacos 注册服务失败，应用继续运行（Nacos 为可选能力）：{}", e.getMessage());
        }
    }

    public void deregister() {
        if (namingService == null || !properties.isEnabled() || !properties.isRegisterEnabled()) {
            return;
        }
        try {
            Instance instance = new Instance();
            instance.setServiceName(properties.getService());
            instance.setIp(resolveIp());
            instance.setPort(properties.getPort());
            instance.setClusterName(properties.getClusterName());
            namingService.deregisterInstance(properties.getService(), properties.getGroup(), instance);
            namingService.shutDown();
        } catch (NacosException e) {
            log.warn("从 Nacos 注销服务失败：{}", e.getMessage());
        }
    }

    private String resolveIp() {
        if (properties.getIp() != null && !properties.getIp().isEmpty()) {
            return properties.getIp();
        }
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}
