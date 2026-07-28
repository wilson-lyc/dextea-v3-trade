package cn.dextea.trade.order.domain.model.valueobject;
import cn.dextea.trade.order.domain.enums.StoreStatusEnum;
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
    public Optional<StoreStatusEnum> getStoreStatus() {
        return status == null ? Optional.empty() : Optional.of(StoreStatusEnum.of(status));
    }
    public boolean isOpen() {
        return status != null && StoreStatusEnum.OPEN.getCode() == status;
    }
}
