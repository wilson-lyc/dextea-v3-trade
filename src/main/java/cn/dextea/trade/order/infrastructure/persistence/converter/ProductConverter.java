package cn.dextea.trade.order.infrastructure.persistence.converter;

import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.model.ProductCover;
import cn.dextea.trade.order.domain.model.enumeration.ProductGlobalStatus;
import cn.dextea.trade.order.domain.model.enumeration.ProductStoreStatus;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductsPO;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductStoreStatusPO;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;
import cn.dextea.trade.shared.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class ProductConverter {

    public Product toDomain(ProductsPO po, ProductStoreStatusPO storeStatusPO, ProductCover cover) {
        return Product.builder()
                .id(po.getId())
                .name(po.getName())
                .globalStatus(EnumUtils.of(ProductGlobalStatus.class, po.getStatus()))
                .storeStatus(storeStatusOrDefault(storeStatusPO))
                .price(Money.of(po.getPrice()))
                .cover(cover)
                .build();
    }

    public ProductStoreStatus storeStatusOrDefault(ProductStoreStatusPO storeStatusPO) {
        if (storeStatusPO == null) {
            return ProductStoreStatus.SOLD_OUT;
        }
        return EnumUtils.of(ProductStoreStatus.class, storeStatusPO.getStatus());
    }
}
