package cn.dextea.trade.order.api.controller;

import cn.dextea.trade.common.api.APIResponse;
import cn.dextea.trade.order.api.assembler.OrderApiAssembler;
import cn.dextea.trade.order.api.dto.response.OrderDetailResponse;
import cn.dextea.trade.order.api.dto.response.OrderStatusResponse;
import cn.dextea.trade.order.api.dto.response.OrderSummary;
import cn.dextea.trade.order.application.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单查询接口：承载读操作（订单列表、订单详情、订单交易状态）。
 *
 * <p>顾客 ID 统一从上游鉴权层写入的 {@code X-Customer-Id} Header 提取。</p>
 */
@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Tag(name = "订单服务-查询")
@RequiredArgsConstructor
public class OrderQueryController {

    private static final String CUSTOMER_ID_HEADER = "X-Customer-Id";

    private final OrderQueryService orderQueryService;

    @GetMapping
    @Operation(summary = "获取用户近3个月订单")
    public APIResponse<List<OrderSummary>> getOrdersByCustomer(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId) {
        List<OrderSummary> result = orderQueryService.getOrdersByCustomer(customerId).stream()
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
    @Operation(summary = "获取订单交易状态（前端轮询交易结果）")
    public APIResponse<OrderStatusResponse> getOrderStatus(
            @PathVariable Long orderId,
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId) {
        OrderStatusResponse result = OrderApiAssembler.toStatus(orderQueryService.getOrderStatus(orderId, customerId));
        return APIResponse.success(result);
    }
}
