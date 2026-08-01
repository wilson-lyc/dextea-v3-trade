package cn.dextea.trade.order.domain.model.aggregate;

import cn.dextea.trade.order.domain.model.valueobject.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String alipayOpenId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Optional<CustomerStatus> getCustomerStatus() {
        return status == null ? Optional.empty() : Optional.of(CustomerStatus.of(status));
    }

    public boolean isActive() {
        return status != null && CustomerStatus.ACTIVE.getCode() == status;
    }
}
