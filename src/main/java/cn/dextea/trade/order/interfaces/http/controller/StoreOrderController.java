package cn.dextea.trade.order.interfaces.http.controller;

import cn.dextea.trade.order.application.dto.command.GetStoreOrderDetailCommand;
import cn.dextea.trade.order.application.dto.command.GetStoreWindowOrdersCommand;
import cn.dextea.trade.order.application.dto.command.MarkOrderReadyCommand;
import cn.dextea.trade.order.application.dto.command.MarkOrderCollectedCommand;
import cn.dextea.trade.order.application.dto.result.GetStoreWindowOrdersResult;
import cn.dextea.trade.order.application.dto.result.StoreOrderDetailResult;
import cn.dextea.trade.order.application.usecase.GetStoreOrderDetailUseCase;
import cn.dextea.trade.order.application.usecase.GetStoreWindowOrdersUseCase;
import cn.dextea.trade.order.application.usecase.MarkOrderReadyUseCase;
import cn.dextea.trade.order.application.usecase.MarkOrderCollectedUseCase;
import cn.dextea.trade.order.interfaces.http.assembler.StoreOrderDetailHttpAssembler;
import cn.dextea.trade.order.interfaces.http.assembler.StoreOrderHttpAssembler;
import cn.dextea.trade.order.interfaces.http.dto.request.GetStoreWindowOrdersRequest;
import cn.dextea.trade.order.interfaces.http.dto.response.GetStoreWindowOrdersResponse;
import cn.dextea.trade.order.interfaces.http.dto.response.StoreOrderDetailResponse;
import cn.dextea.trade.shared.api.APIResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/store/orders")
@Validated
@RequiredArgsConstructor
@Slf4j
public class StoreOrderController {

    private static final String STORE_ID_HEADER = "X-Store-Id";

    private final GetStoreWindowOrdersUseCase getStoreWindowOrdersUseCase;
    private final MarkOrderReadyUseCase markOrderReadyUseCase;
    private final MarkOrderCollectedUseCase markOrderCollectedUseCase;
    private final GetStoreOrderDetailUseCase getStoreOrderDetailUseCase;
    private final StoreOrderHttpAssembler storeOrderHttpAssembler;

    @GetMapping("/window")
    public APIResponse<GetStoreWindowOrdersResponse> getStoreWindowOrders(
            @RequestHeader(STORE_ID_HEADER) @NotNull(message = "storeId 不能为空") Long storeId,
            @Valid GetStoreWindowOrdersRequest request) {
        log.info("查询门店窗口订单请求, storeId={}, hours={}", storeId, request.getHours());
        GetStoreWindowOrdersCommand command = storeOrderHttpAssembler.toCommand(request, storeId);
        GetStoreWindowOrdersResult result = getStoreWindowOrdersUseCase.execute(command);
        log.info("查询门店窗口订单成功, storeId={}, hours={}, orderCount={}, total={}",
                storeId, request.getHours(), result.getItems().size(), result.getTotal());
        return APIResponse.success(storeOrderHttpAssembler.toResponse(result));
    }

    @GetMapping("/{orderId}")
    public APIResponse<StoreOrderDetailResponse> getStoreOrderDetail(
            @RequestHeader(STORE_ID_HEADER) @NotNull(message = "storeId 不能为空") Long storeId,
            @PathVariable("orderId") @NotNull(message = "orderId 不能为空") Long orderId) {
        log.info("查询门店订单详情请求, storeId={}, orderId={}", storeId, orderId);
        GetStoreOrderDetailCommand command = StoreOrderDetailHttpAssembler.toCommand(storeId, orderId);
        StoreOrderDetailResult result = getStoreOrderDetailUseCase.execute(command);
        log.info("查询门店订单详情成功, storeId={}, orderId={}", storeId, orderId);
        return APIResponse.success(StoreOrderDetailHttpAssembler.toResponse(result));
    }

    @PostMapping("/{orderId}/ready")
    public APIResponse<Void> markReady(
            @RequestHeader(STORE_ID_HEADER) @NotNull(message = "storeId 不能为空") Long storeId,
            @PathVariable("orderId") @NotNull(message = "orderId 不能为空") Long orderId) {
        log.info("标记订单制作完成请求, storeId={}, orderId={}", storeId, orderId);
        MarkOrderReadyCommand command = MarkOrderReadyCommand.builder()
                .storeId(storeId)
                .orderId(orderId)
                .build();
        markOrderReadyUseCase.execute(command);
        log.info("标记订单制作完成成功, storeId={}, orderId={}", storeId, orderId);
        return APIResponse.success();
    }

    @PostMapping("/{orderId}/collect")
    public APIResponse<Void> markCollected(
            @RequestHeader(STORE_ID_HEADER) @NotNull(message = "storeId 不能为空") Long storeId,
            @PathVariable("orderId") @NotNull(message = "orderId 不能为空") Long orderId) {
        log.info("标记订单已取餐请求, storeId={}, orderId={}", storeId, orderId);
        MarkOrderCollectedCommand command = MarkOrderCollectedCommand.builder()
                .storeId(storeId)
                .orderId(orderId)
                .build();
        markOrderCollectedUseCase.execute(command);
        log.info("标记订单已取餐成功, storeId={}, orderId={}", storeId, orderId);
        return APIResponse.success();
    }
}
