package cn.dextea.trade.infrastructure.acl.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPO {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String alipayOpenId;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
