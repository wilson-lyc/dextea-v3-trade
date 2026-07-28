package cn.dextea.trade.order.domain.model.valueobject;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
@Getter
@Builder
@Jacksonized
public class UnavailableProduct {
    private Long id;
    private String name;
}
