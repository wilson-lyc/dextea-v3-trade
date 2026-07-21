package cn.dextea.trade.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 雪花算法 ID 生成器。
 *
 * <p>生成的 64 位 ID 结构（从高位到低位）：</p>
 * <ul>
 *     <li>1 位符号位，恒为 0</li>
 *     <li>41 位时间戳（毫秒级，相对自定义纪元起始时间），可用约 69 年</li>
 *     <li>5 位数据中心 ID（{@code datacenterId}），取值范围 0~31</li>
 *     <li>5 位机器 ID（{@code workerId}），取值范围 0~31</li>
 *     <li>12 位序列号，单机同毫秒内可生成 4096 个 ID</li>
 * </ul>
 *
 * <p>线程安全：{@link #nextId()} 通过 {@code synchronized} 保证同一毫秒内序列号自增的正确性。</p>
 */
@Component
public class SnowflakeIdGenerator {

    /** 自定义纪元起始时间（2024-01-01 00:00:00 UTC，毫秒）。 */
    private static final long EPOCH = 1704067200000L;

    private static final long DATACENTER_ID_BITS = 5L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long datacenterId;
    private final long workerId;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(
            @Value("${snowflake.datacenter-id:1}") long datacenterId,
            @Value("${snowflake.worker-id:1}") long workerId) {
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException(
                    "datacenterId 超出范围 [0," + MAX_DATACENTER_ID + "]: " + datacenterId);
        }
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    "workerId 超出范围 [0," + MAX_WORKER_ID + "]: " + workerId);
        }
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }

    /**
     * 生成下一个全局唯一 ID。线程安全。
     *
     * @return 雪花算法生成的 64 位整数 ID
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp < lastTimestamp) {
            // 时钟回拨：回拨量在容忍范围内则等待，否则直接抛错避免生成重复 ID
            long offset = lastTimestamp - currentTimestamp;
            if (offset <= 5) {
                try {
                    Thread.sleep(offset << 1);
                    currentTimestamp = System.currentTimeMillis();
                    if (currentTimestamp < lastTimestamp) {
                        throw new IllegalStateException("时钟回拨，拒绝生成 ID: " + lastTimestamp);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("等待时钟回拨恢复时被中断", e);
                }
            } else {
                throw new IllegalStateException("时钟回拨超过容忍范围: " + offset + "ms");
            }
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0L) {
                // 同一毫秒序列号耗尽，阻塞到下一毫秒
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;
        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
