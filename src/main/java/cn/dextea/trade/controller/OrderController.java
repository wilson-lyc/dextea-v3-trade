package cn.dextea.trade.controller;

import cn.dextea.trade.common.APIResponse;
import cn.dextea.trade.model.CreateOrderRequest;
import cn.dextea.trade.model.CreateOrderResponse;
import cn.dextea.trade.model.OrderDetailResponse;
import cn.dextea.trade.model.OrderSummary;
import cn.dextea.trade.model.PreBuildOrderRequest;
import cn.dextea.trade.model.PreBuildOrderResponse;
import cn.dextea.trade.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Tag(name = "订单服务")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public APIResponse<CreateOrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse result = orderService.createOrder(request);
        return APIResponse.success(result);
    }

    @PostMapping("/pre-build")
    @Operation(summary = "订单预构建")
    public APIResponse<PreBuildOrderResponse> preBuildOrder(@Valid @RequestBody PreBuildOrderRequest request) {
        PreBuildOrderResponse result = orderService.preBuildOrder(request);
        return APIResponse.success(result);
    }

    @GetMapping
    @Operation(summary = "获取用户近3个月订单")
    public APIResponse<List<OrderSummary>> getOrdersByCustomer(@RequestParam Long customerId) {
        List<OrderSummary> result = orderService.getOrdersByCustomer(customerId);
        return APIResponse.success(result);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情")
    public APIResponse<OrderDetailResponse> getOrderDetail(
            @PathVariable Long orderId,
            @RequestParam @NotNull(message = "customerId 不能为空") Long customerId) {
        OrderDetailResponse result = orderService.getOrderDetail(orderId, customerId);
        return APIResponse.success(result);
    }
}
