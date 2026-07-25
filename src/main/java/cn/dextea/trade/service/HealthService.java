package cn.dextea.trade.service;

import cn.dextea.trade.model.HealthResult;

public interface HealthService {

    HealthResult checkMysql();

    HealthResult checkRedis();

    HealthResult checkBackend();
}
