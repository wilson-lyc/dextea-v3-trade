package cn.dextea.trade.catalog.domain.model;

import cn.dextea.trade.catalog.domain.enums.CustomerStatusEnum;
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

    public Optional<CustomerStatusEnum> getCustomerStatus() {
        return status == null ? Optional.empty() : Optional.of(CustomerStatusEnum.of(status));
    }

    public boolean isActive() {
        return status != null && CustomerStatusEnum.ACTIVE.getCode() == status;
    }
}
