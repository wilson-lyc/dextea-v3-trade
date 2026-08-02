package cn.dextea.trade.order.interfaces.http.dto.shared;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PreBuildOrderItem extends AbstractOrderItem {
}
