package cn.dextea.trade.order.interfaces.http.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerMonthOrderItem {

    private Long id;

    private String storeName;

    private LocalDateTime createdAt;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private Integer makingStatus;

    private Integer paymentStatus;

    private List<String> covers;
}
