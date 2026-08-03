package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.order.domain.port.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.snowflake.SnowflakeId;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class SnowflakeOrderNoGenerator implements OrderNoGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SnowflakeId snowflakeId;

    @Override
    public String next() {
        String datePrefix = LocalDate.now().format(DATE_FORMAT);
        return datePrefix + snowflakeId.generateAsString();
    }
}
