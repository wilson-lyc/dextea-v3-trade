package cn.dextea.trade.order.interfaces.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetStoreWindowOrdersResponse {

    private List<StoreWindowOrderItem> items;

    private Long total;
}
