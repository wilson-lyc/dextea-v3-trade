package cn.dextea.trade.service;

import cn.dextea.trade.model.HealthResult;

/**
 * 健康检测服务：分别检测 MySQL、Redis 及后端（本服务）的健康状态。
 */
public interface HealthService {

    /**
     * 检测 MySQL 连接健康
     */
    HealthResult checkMysql();

    /**
     * 检测 Redis 连接健康
     */
    HealthResult checkRedis();

    /**
     * 检测后端（本服务）健康
     */
    HealthResult checkBackend();
}
