package cn.dextea.trade.order.interfaces.http.controller;

import cn.dextea.trade.shared.api.APIResponse;
import cn.dextea.trade.order.interfaces.http.assembler.CustomerCreateOrderHttpAssembler;
import cn.dextea.trade.order.interfaces.http.assembler.CustomerMonthOrderHttpAssembler;
import cn.dextea.trade.order.interfaces.http.dto.request.CustomerCreateOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.request.CustomerGetMonthOrdersRequest;
import cn.dextea.trade.order.interfaces.http.dto.request.CustomerPreBuildOrderRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.CustomerCreateOrderResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.CustomerGetMonthOrdersResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.CustomerPreBuildOrderResponse;
import cn.dextea.trade.order.application.usecase.CustomerGetMonthOrdersUseCase;
import cn.dextea.trade.order.application.usecase.CustomerPreBuildOrderUseCase;
import cn.dextea.trade.order.application.usecase.CustomerCreateOrderUseCase;
import cn.dextea.trade.order.application.usecase.CustomerGetOrderDetailUseCase;
import cn.dextea.trade.order.application.usecase.CustomerGetOrderPaymentStatusUseCase;
import cn.dextea.trade.order.application.dto.command.CustomerGetMonthOrdersCommand;
import cn.dextea.trade.order.application.dto.command.CustomerPreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.command.CustomerCreateOrderCommand;
import cn.dextea.trade.order.application.dto.command.CustomerGetOrderDetailCommand;
import cn.dextea.trade.order.application.dto.command.CustomerGetOrderPaymentStatusCommand;
import cn.dextea.trade.order.application.dto.result.CustomerGetMonthOrdersResult;
import cn.dextea.trade.order.application.dto.result.CustomerPreBuildOrderResult;
import cn.dextea.trade.order.application.dto.result.CustomerOrderCreateResult;
import cn.dextea.trade.order.application.dto.result.CustomerOrderDetailResult;
import cn.dextea.trade.order.application.dto.result.CustomerOrderPaymentStatusResult;
import cn.dextea.trade.order.interfaces.http.dto.response.CustomerOrderDetailResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.CustomerOrderPaymentStatusResponse;
import cn.dextea.trade.order.interfaces.http.assembler.CustomerOrderDetailHttpAssembler;
import cn.dextea.trade.order.interfaces.http.assembler.CustomerOrderPaymentStatusHttpAssembler;
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
@Tag(name = "顾客订单服务")
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderController {

    private static final String CUSTOMER_ID_HEADER = "X-Customer-Id";

    private final CustomerPreBuildOrderUseCase preBuildOrderUseCase;
    private final CustomerCreateOrderUseCase createOrderUseCase;
    private final CustomerGetMonthOrdersUseCase getMonthOrdersUseCase;
    private final CustomerGetOrderDetailUseCase getOrderDetailUseCase;
    private final CustomerGetOrderPaymentStatusUseCase getOrderPaymentStatusUseCase;

    @PostMapping("/pre-build")
    @Operation(summary = "订单预构建")
    public APIResponse<CustomerPreBuildOrderResponse> preBuildOrder(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody CustomerPreBuildOrderRequest request) {
        log.info("预构建订单请求, customerId={}, storeId={}, itemCount={}",
                customerId, request.getStoreId(), request.getItems().size());
        CustomerPreBuildOrderCommand command = CustomerCreateOrderHttpAssembler.toPreBuildCommand(request, customerId);
        CustomerPreBuildOrderResult result = preBuildOrderUseCase.execute(command);
        log.info("预构建订单成功, customerId={}, storeId={}, totalPrice={}, totalQuantity={}",
                customerId, request.getStoreId(), result.getTotalPrice(), result.getTotalQuantity());
        return APIResponse.success(CustomerCreateOrderHttpAssembler.toPreBuildResponse(result));
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public APIResponse<CustomerCreateOrderResponse> create(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody CustomerCreateOrderRequest request) {
        log.info("创建订单请求, customerId={}, storeId={}, paymentMethod={}, itemCount={}",
                customerId, request.getStoreId(), request.getPaymentMethod(), request.getItems().size());
        CustomerCreateOrderCommand command = CustomerCreateOrderHttpAssembler.toCreateCommand(request, customerId);
        CustomerOrderCreateResult result = createOrderUseCase.execute(command);
        log.info("创建订单响应, customerId={}, storeId={}, orderNo={}, tradeNo={}",
                customerId, request.getStoreId(), result.getOrderNo(), result.getTradeNo());
        return APIResponse.success(CustomerCreateOrderHttpAssembler.toCreateResponse(result));
    }

    @GetMapping("/monthly")
    @Operation(summary = "获取月订单列表")
    public APIResponse<CustomerGetMonthOrdersResponse> getMonthOrders(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid CustomerGetMonthOrdersRequest request) {
        log.info("查询月订单请求, customerId={}, year={}, month={}",
                customerId, request.getYear(), request.getMonth());
        CustomerGetMonthOrdersCommand command = CustomerMonthOrderHttpAssembler.toCommand(request, customerId);
        CustomerGetMonthOrdersResult result = getMonthOrdersUseCase.execute(command);
        log.info("查询月订单成功, customerId={}, year={}, month={}, orderCount={}, totalAmount={}",
                customerId, request.getYear(), request.getMonth(), result.getOrderCount(), result.getTotalAmount());
        return APIResponse.success(CustomerMonthOrderHttpAssembler.toResponse(result));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情")
    public APIResponse<CustomerOrderDetailResponse> getOrderDetail(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @PathVariable("orderId") @NotNull(message = "orderId 不能为空") Long orderId) {
        log.info("查询订单详情请求, customerId={}, orderId={}", customerId, orderId);
        CustomerGetOrderDetailCommand command = CustomerOrderDetailHttpAssembler.toCommand(customerId, orderId);
        CustomerOrderDetailResult result = getOrderDetailUseCase.execute(command);
        log.info("查询订单详情成功, customerId={}, orderId={}", customerId, orderId);
        return APIResponse.success(CustomerOrderDetailHttpAssembler.toResponse(result));
    }

    @GetMapping("/{orderId}/payment-status")
    @Operation(summary = "获取订单支付状态", description = "轻量接口，仅返回支付状态；本地为支付中时会主动向支付渠道查询一次并回写")
    public APIResponse<CustomerOrderPaymentStatusResponse> getOrderPaymentStatus(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @PathVariable("orderId") @NotNull(message = "orderId 不能为空") Long orderId) {
        log.info("查询订单支付状态请求, customerId={}, orderId={}", customerId, orderId);
        CustomerGetOrderPaymentStatusCommand command = CustomerOrderPaymentStatusHttpAssembler.toCommand(customerId, orderId);
        CustomerOrderPaymentStatusResult result = getOrderPaymentStatusUseCase.execute(command);
        log.info("查询订单支付状态成功, customerId={}, orderId={}, paymentStatus={}",
                customerId, orderId, result.getPaymentStatus());
        return APIResponse.success(CustomerOrderPaymentStatusHttpAssembler.toResponse(result));
    }
}
