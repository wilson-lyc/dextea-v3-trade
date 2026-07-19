package cn.dextea.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCalculateRequest {

    private Long storeId;

    private List<CartItemDTO> cartItems;
}
