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
public class CreateOrderUnavailable {

    private List<CreateOrderUnavailableProduct> products;
    
    private List<CreateOrderUnavailableOption> customizationOptions;
}
