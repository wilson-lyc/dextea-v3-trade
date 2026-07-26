package cn.dextea.trade.infrastructure.acl.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorePO {

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
}
