package cn.dextea.trade.infrastructure.acl.customization;

import java.util.List;

public interface CustomizationQuery {

    CustomizationPO findById(Long id);

    List<CustomizationPO> findByIds(List<Long> ids);
}
