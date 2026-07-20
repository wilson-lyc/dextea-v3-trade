package cn.dextea.trade.controller;

import cn.dextea.trade.common.APIResponse;
import cn.dextea.trade.dto.CreateOrderRequest;
import cn.dextea.trade.dto.CalculateOrderResponse;
import cn.dextea.trade.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
@Tag(name = "订单接口")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/calculate")
    @Operation(summary = "订单计价", description = "计算订单价格，剔除不可用商品")
    public APIResponse<CalculateOrderResponse> calculate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "订单计价请求参数") @Valid @RequestBody CreateOrderRequest request) {
        CalculateOrderResponse result = orderService.calculate(request);
        return APIResponse.success(result);
    }
}
