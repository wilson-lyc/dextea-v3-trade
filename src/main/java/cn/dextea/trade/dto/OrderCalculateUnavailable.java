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
public class OrderCalculateUnavailable {

    private List<UnavailableProduct> products;
    
    private List<UnavailableCustomizationOption> customizationOptions;
}
