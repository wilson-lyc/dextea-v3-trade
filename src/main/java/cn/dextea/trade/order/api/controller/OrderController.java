package cn.dextea.trade.order.api.controller;
import cn.dextea.trade.shared.api.APIResponse;
import cn.dextea.trade.order.api.assembler.OrderApiAssembler;
import cn.dextea.trade.order.api.dto.request.CreateOrderRequest;
import cn.dextea.trade.order.api.dto.request.PreBuildOrderRequest;
import cn.dextea.trade.order.api.dto.response.CreateOrderResponse;
import cn.dextea.trade.order.api.dto.response.OrderDetailResponse;
import cn.dextea.trade.order.api.dto.response.OrderStatusResponse;
import cn.dextea.trade.order.api.dto.response.OrderSummary;
import cn.dextea.trade.order.api.dto.response.PreBuildOrderResponse;
import cn.dextea.trade.order.application.service.OrderApplicationService;
import cn.dextea.trade.order.application.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Tag(name = "订单服务")
@RequiredArgsConstructor
public class OrderController {
    private static final String CUSTOMER_ID_HEADER = "X-Customer-Id";
    private final OrderApplicationService orderApplicationService;
    private final OrderQueryService orderQueryService;

    @PostMapping("/pre-build")
    @Operation(summary = "订单预构建")
    public APIResponse<PreBuildOrderResponse> preBuildOrder(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody PreBuildOrderRequest request) {
        PreBuildOrderResponse result = OrderApiAssembler.toPreBuildResponse(
                orderApplicationService.preBuildOrder(OrderApiAssembler.toPreBuildCommand(request, customerId)));
        return APIResponse.success(result);
    }
    
    @PostMapping
    @Operation(summary = "创建订单")
    public APIResponse<CreateOrderResponse> create(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse result = OrderApiAssembler.toCreateResponse(
                orderApplicationService.createOrder(OrderApiAssembler.toCreateCommand(request, customerId)));
        return APIResponse.success(result);
    }
    @GetMapping
    @Operation(summary = "获取月订单列表")
    public APIResponse<List<OrderSummary>> getOrdersByCustomer(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @RequestParam @NotNull(message = "year 不能为空") @Min(2000) @Max(9999) Integer year,
            @RequestParam @NotNull(message = "month 不能为空") @Min(1) @Max(12) Integer month) {
        List<OrderSummary> result = orderQueryService.getOrdersByCustomer(customerId, year, month).stream()
                .map(OrderApiAssembler::toSummary)
                .toList();
        return APIResponse.success(result);
    }
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情")
    public APIResponse<OrderDetailResponse> getOrderDetail(
            @PathVariable Long orderId,
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId) {
        OrderDetailResponse result = OrderApiAssembler.toDetail(orderQueryService.getOrderDetail(orderId, customerId));
        return APIResponse.success(result);
    }
    @GetMapping("/{orderId}/status")
    @Operation(summary = "获取订单交易状态")
    public APIResponse<OrderStatusResponse> getOrderStatus(
            @PathVariable Long orderId,
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId) {
        OrderStatusResponse result = OrderApiAssembler.toStatus(orderQueryService.getOrderStatus(orderId, customerId));
        return APIResponse.success(result);
    }
}
