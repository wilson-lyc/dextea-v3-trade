package cn.dextea.trade.order.interfaces.controller;

import cn.dextea.trade.common.api.APIResponse;
import cn.dextea.trade.order.application.OrderCommandService;
import cn.dextea.trade.order.application.OrderQueryService;
import cn.dextea.trade.order.interfaces.assembler.OrderAssembler;
import cn.dextea.trade.order.interfaces.dto.CreateOrderRequest;
import cn.dextea.trade.order.interfaces.dto.CreateOrderResponse;
import cn.dextea.trade.order.interfaces.dto.OrderDetailResponse;
import cn.dextea.trade.order.interfaces.dto.OrderSummary;
import cn.dextea.trade.order.interfaces.dto.PreBuildOrderRequest;
import cn.dextea.trade.order.interfaces.dto.PreBuildOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Tag(name = "订单服务")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    @PostMapping
    @Operation(summary = "创建订单")
    public APIResponse<CreateOrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse result = OrderAssembler.toCreateResponse(
                orderCommandService.createOrder(OrderAssembler.toCreateCommand(request)));
        return APIResponse.success(result);
    }

    @PostMapping("/pre-build")
    @Operation(summary = "订单预构建")
    public APIResponse<PreBuildOrderResponse> preBuildOrder(@Valid @RequestBody PreBuildOrderRequest request) {
        PreBuildOrderResponse result = OrderAssembler.toPreBuildResponse(
                orderCommandService.preBuildOrder(OrderAssembler.toPreBuildCommand(request)));
        return APIResponse.success(result);
    }

    @GetMapping
    @Operation(summary = "获取用户近3个月订单")
    public APIResponse<List<OrderSummary>> getOrdersByCustomer(@RequestParam Long customerId) {
        List<OrderSummary> result = orderQueryService.getOrdersByCustomer(customerId).stream()
                .map(OrderAssembler::toSummary)
                .toList();
        return APIResponse.success(result);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情")
    public APIResponse<OrderDetailResponse> getOrderDetail(
            @PathVariable Long orderId,
            @RequestParam @NotNull(message = "customerId 不能为空") Long customerId) {
        OrderDetailResponse result = OrderAssembler.toDetail(orderQueryService.getOrderDetail(orderId, customerId));
        return APIResponse.success(result);
    }
}
