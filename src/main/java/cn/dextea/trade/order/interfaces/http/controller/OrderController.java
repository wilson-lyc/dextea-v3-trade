package cn.dextea.trade.order.interfaces.http.controller;

import cn.dextea.trade.shared.api.APIResponse;
import cn.dextea.trade.order.interfaces.http.assembler.CreateOrderHttpAssembler;
import cn.dextea.trade.order.interfaces.http.assembler.MonthOrderHttpAssembler;
import cn.dextea.trade.order.interfaces.http.dto.request.CreateOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.request.GetMonthOrdersRequest;
import cn.dextea.trade.order.interfaces.http.dto.request.PreBuildOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.CreateOrderResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.GetMonthOrdersResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.PreBuildOrderResponse;
import cn.dextea.trade.order.application.usecase.GetMonthOrdersUseCase;
import cn.dextea.trade.order.application.usecase.PreBuildOrderUseCase;
import cn.dextea.trade.order.application.usecase.CreateOrderUseCase;
import cn.dextea.trade.order.application.usecase.GetOrderDetailUseCase;
import cn.dextea.trade.order.application.dto.command.GetMonthOrdersCommand;
import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.command.CreateOrderCommand;
import cn.dextea.trade.order.application.dto.command.GetOrderDetailCommand;
import cn.dextea.trade.order.application.dto.result.GetMonthOrdersResult;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.application.dto.result.OrderCreateResult;
import cn.dextea.trade.order.application.dto.result.OrderDetailResult;
import cn.dextea.trade.order.interfaces.http.dto.response.OrderDetailResponse;
import cn.dextea.trade.order.interfaces.http.assembler.OrderDetailHttpAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@Slf4j
public class OrderController {

    private static final String CUSTOMER_ID_HEADER = "X-Customer-Id";

    private final PreBuildOrderUseCase preBuildOrderUseCase;
    private final CreateOrderUseCase createOrderUseCase;
    private final GetMonthOrdersUseCase getMonthOrdersUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;

    @PostMapping("/pre-build")
    @Operation(summary = "订单预构建")
    public APIResponse<PreBuildOrderResponse> preBuildOrder(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody PreBuildOrderRequest request) {
        log.info("预构建订单请求, customerId={}, storeId={}, itemCount={}",
                customerId, request.getStoreId(), request.getItems().size());
        PreBuildOrderCommand command = CreateOrderHttpAssembler.toPreBuildCommand(request, customerId);
        PreBuildOrderResult result = preBuildOrderUseCase.execute(command);
        log.info("预构建订单成功, customerId={}, storeId={}, totalPrice={}, totalQuantity={}",
                customerId, request.getStoreId(), result.getTotalPrice(), result.getTotalQuantity());
        return APIResponse.success(CreateOrderHttpAssembler.toPreBuildResponse(result));
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public APIResponse<CreateOrderResponse> create(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("创建订单请求, customerId={}, storeId={}, paymentMethod={}, itemCount={}",
                customerId, request.getStoreId(), request.getPaymentMethod(), request.getItems().size());
        CreateOrderCommand command = CreateOrderHttpAssembler.toCreateCommand(request, customerId);
        OrderCreateResult result = createOrderUseCase.execute(command);
        log.info("创建订单响应, customerId={}, storeId={}, orderNo={}, tradeNo={}",
                customerId, request.getStoreId(), result.getOrderNo(), result.getTradeNo());
        return APIResponse.success(CreateOrderHttpAssembler.toCreateResponse(result));
    }

    @GetMapping("/monthly")
    @Operation(summary = "获取月订单列表")
    public APIResponse<GetMonthOrdersResponse> getMonthOrders(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid GetMonthOrdersRequest request) {
        log.info("查询月订单请求, customerId={}, year={}, month={}",
                customerId, request.getYear(), request.getMonth());
        GetMonthOrdersCommand command = MonthOrderHttpAssembler.toCommand(request, customerId);
        GetMonthOrdersResult result = getMonthOrdersUseCase.execute(command);
        log.info("查询月订单成功, customerId={}, year={}, month={}, orderCount={}, totalAmount={}",
                customerId, request.getYear(), request.getMonth(), result.getOrderCount(), result.getTotalAmount());
        return APIResponse.success(MonthOrderHttpAssembler.toResponse(result));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情")
    public APIResponse<OrderDetailResponse> getOrderDetail(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @PathVariable("orderId") @NotNull(message = "orderId 不能为空") Long orderId) {
        log.info("查询订单详情请求, customerId={}, orderId={}", customerId, orderId);
        GetOrderDetailCommand command = OrderDetailHttpAssembler.toCommand(customerId, orderId);
        OrderDetailResult result = getOrderDetailUseCase.execute(command);
        log.info("查询订单详情成功, customerId={}, orderId={}", customerId, orderId);
        return APIResponse.success(OrderDetailHttpAssembler.toResponse(result));
    }
}
