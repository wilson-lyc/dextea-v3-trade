package cn.dextea.trade.health;


public interface HealthService {

    HealthResult checkMysql();

    HealthResult checkRedis();

    HealthResult checkBackend();
}
