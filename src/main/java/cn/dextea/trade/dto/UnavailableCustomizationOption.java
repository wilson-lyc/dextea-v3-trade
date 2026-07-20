package cn.dextea.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnavailableCustomizationOption {

    private Long optionId;

    private String optionName;

    private Long productId;

    private String productName;
}
