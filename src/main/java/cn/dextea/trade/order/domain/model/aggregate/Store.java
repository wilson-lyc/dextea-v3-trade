package cn.dextea.trade.order.domain.model.aggregate;

import cn.dextea.trade.order.domain.model.valueobject.StoreStatus;
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
public class Store {
    private Long id;
    private String name;
    private String province;
    private String city;
    private String district;
    private String address;
    private Integer status;
    private String businessHours;
    private String phone;
    private Double longitude;
    private Double latitude;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Optional<StoreStatus> getStoreStatus() {
        return status == null ? Optional.empty() : Optional.of(StoreStatus.of(status));
    }

    public boolean isOpen() {
        return status != null && StoreStatus.OPEN.getCode() == status;
    }
}
