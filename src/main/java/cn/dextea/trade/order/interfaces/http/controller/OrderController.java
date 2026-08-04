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
import cn.dextea.trade.order.application.dto.command.GetMonthOrdersCommand;
import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.command.CreateOrderCommand;
import cn.dextea.trade.order.application.dto.result.GetMonthOrdersResult;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.application.dto.result.OrderCreateResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * 获取顾客ID的请求头名称，直接写死，不再支持环境变量配置。
     */
    private static final String CUSTOMER_ID_HEADER = "X-Customer-Id";

    private final PreBuildOrderUseCase preBuildOrderUseCase;
    private final CreateOrderUseCase createOrderUseCase;
    private final GetMonthOrdersUseCase getMonthOrdersUseCase;

    @PostMapping("/pre-build")
    @Operation(summary = "订单预构建")
    public APIResponse<PreBuildOrderResponse> preBuildOrder(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody PreBuildOrderRequest request) {
        PreBuildOrderCommand command = CreateOrderHttpAssembler.toPreBuildCommand(request, customerId);
        PreBuildOrderResult result = preBuildOrderUseCase.execute(command);
        return APIResponse.success(CreateOrderHttpAssembler.toPreBuildResponse(result));
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public APIResponse<CreateOrderResponse> create(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = CreateOrderHttpAssembler.toCreateCommand(request, customerId);
        OrderCreateResult result = createOrderUseCase.execute(command);
        return APIResponse.success(CreateOrderHttpAssembler.toCreateResponse(result));
    }

    @GetMapping("/monthly")
    @Operation(summary = "获取月订单列表")
    public APIResponse<GetMonthOrdersResponse> getMonthOrders(
            @RequestHeader(CUSTOMER_ID_HEADER) @NotNull(message = "customerId 不能为空") Long customerId,
            @Valid GetMonthOrdersRequest request) {
        GetMonthOrdersCommand command = MonthOrderHttpAssembler.toCommand(request, customerId);
        GetMonthOrdersResult result = getMonthOrdersUseCase.execute(command);
        return APIResponse.success(MonthOrderHttpAssembler.toResponse(result));
    }
}
