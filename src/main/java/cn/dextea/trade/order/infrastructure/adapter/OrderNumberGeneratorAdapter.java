package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.port.OrderNumberGenerator;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class OrderNumberGeneratorAdapter implements OrderNumberGenerator {
    private static final String ORDER_ID_GENERATOR = "order";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final IdGeneratorProvider idGeneratorProvider;

    @Override
    public String next() {
        try {
            String date = LocalDate.now().format(DATE_FORMATTER);
            String snowflake = String.valueOf(idGeneratorProvider.getRequired(ORDER_ID_GENERATOR).generate());
            return date + snowflake;
        } catch (RuntimeException e) {
            throw new BizError(OrderErrorCode.ORDER_NO_GENERATE_FAILED, "订单号生成失败", e);
        }
    }
}
