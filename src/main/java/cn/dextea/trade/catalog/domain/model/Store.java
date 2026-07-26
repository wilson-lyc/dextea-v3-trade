package cn.dextea.trade.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门店实体，对应 {@code stores} 表。
 */
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

    private String account;

    private String password;

    private String email;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
