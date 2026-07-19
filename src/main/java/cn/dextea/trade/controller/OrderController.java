package cn.dextea.trade.controller;

import cn.dextea.trade.common.APIResponse;
import cn.dextea.trade.dto.OrderCalculateRequest;
import cn.dextea.trade.dto.OrderCalculateResult;
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
    public APIResponse<OrderCalculateResult> calculate(@RequestBody OrderCalculateRequest request) {
        OrderCalculateResult result = orderService.calculate(request);
        return APIResponse.success(result);
    }
}
