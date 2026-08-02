package cn.dextea.trade.order.interfaces.http.controller;

import cn.dextea.trade.shared.api.APIResponse;
import cn.dextea.trade.order.interfaces.http.assembler.OrderHttpAssembler;
import cn.dextea.trade.order.interfaces.http.dto.request.CreateOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.request.PreBuildOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.CreateOrderResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.PreBuildOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Tag(name = "订单服务")
@RequiredArgsConstructor
public class OrderController {

    @Value("${order.customer-id-header}")
    private String customerIdHeader;

    @PostMapping("/pre-build")
    @Operation(summary = "订单预构建")
    public APIResponse<PreBuildOrderResponse> preBuildOrder(
            @RequestHeader(customerIdHeader) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody PreBuildOrderRequest request) {
        OrderHttpAssembler.toPreBuildCommand(request, customerId);
        throw new UnsupportedOperationException("待实现");
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public APIResponse<CreateOrderResponse> create(
            @RequestHeader(customerIdHeader) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderHttpAssembler.toCreateCommand(request, customerId);
        throw new UnsupportedOperationException("待实现");
    }
}
