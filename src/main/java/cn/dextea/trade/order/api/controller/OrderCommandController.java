package cn.dextea.trade.order.api.controller;

import cn.dextea.trade.common.api.APIResponse;
import cn.dextea.trade.order.api.assembler.OrderApiAssembler;
import cn.dextea.trade.order.api.dto.request.CreateOrderRequest;
import cn.dextea.trade.order.api.dto.request.PreBuildOrderRequest;
import cn.dextea.trade.order.api.dto.response.CreateOrderResponse;
import cn.dextea.trade.order.api.dto.response.PreBuildOrderResponse;
import cn.dextea.trade.order.application.service.OrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单命令接口：承载写操作（创建订单、订单预构建）。
 */
@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Tag(name = "订单服务-命令")
@RequiredArgsConstructor
public class OrderCommandController {

    private final OrderApplicationService orderApplicationService;

    @PostMapping
    @Operation(summary = "创建订单")
    public APIResponse<CreateOrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse result = OrderApiAssembler.toCreateResponse(
                orderApplicationService.createOrder(OrderApiAssembler.toCreateCommand(request)));
        return APIResponse.success(result);
    }

    @PostMapping("/pre-build")
    @Operation(summary = "订单预构建")
    public APIResponse<PreBuildOrderResponse> preBuildOrder(@Valid @RequestBody PreBuildOrderRequest request) {
        PreBuildOrderResponse result = OrderApiAssembler.toPreBuildResponse(
                orderApplicationService.preBuildOrder(OrderApiAssembler.toPreBuildCommand(request)));
        return APIResponse.success(result);
    }
}
