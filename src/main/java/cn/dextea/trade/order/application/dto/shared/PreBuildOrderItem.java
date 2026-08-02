package cn.dextea.trade.order.application.dto.shared;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PreBuildOrderItem extends AbstractOrderItem {
}
