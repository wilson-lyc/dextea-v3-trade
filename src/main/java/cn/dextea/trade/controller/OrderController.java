package cn.dextea.trade.controller;

import cn.dextea.trade.common.APIResponse;
import cn.dextea.trade.dto.CreateOrderRequest;
import cn.dextea.trade.dto.CalculateOrderResponse;
import cn.dextea.trade.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/calculate")
    public APIResponse<CalculateOrderResponse> calculate(@RequestBody CreateOrderRequest request) {
        CalculateOrderResponse result = orderService.calculate(request);
        return APIResponse.success(result);
    }
}
