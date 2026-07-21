package cn.dextea.trade.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 顾客实体，对应 {@code customers} 表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String password;

    private Integer platform;

    private String weixinOpenId;

    private String alipayOpenId;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
