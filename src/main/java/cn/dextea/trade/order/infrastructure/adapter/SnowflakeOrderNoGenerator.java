package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.order.domain.port.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.IdGenerator;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class SnowflakeOrderNoGenerator implements OrderNoGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String PROVIDER_NAME = "order";

    private final IdGeneratorProvider idGeneratorProvider;

    @Override
    public String next() {
        IdGenerator idGenerator = idGeneratorProvider.getRequired(PROVIDER_NAME);
        String datePrefix = LocalDate.now().format(DATE_FORMAT);
        return datePrefix + Long.toString(idGenerator.generate());
    }
}
