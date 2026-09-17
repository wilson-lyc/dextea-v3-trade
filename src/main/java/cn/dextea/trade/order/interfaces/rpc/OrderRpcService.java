package cn.dextea.trade.order.interfaces.rpc;

import cn.dextea.trade.order.application.dto.command.GetStoreMakingBoardCommand;
import cn.dextea.trade.order.application.dto.command.GetStoreOrderDetailCommand;
import cn.dextea.trade.order.application.dto.command.GetStoreWindowOrdersCommand;
import cn.dextea.trade.order.application.dto.command.MarkOrderCollectedCommand;
import cn.dextea.trade.order.application.dto.command.MarkOrderReadyCommand;
import cn.dextea.trade.order.application.dto.result.GetStoreMakingBoardResult;
import cn.dextea.trade.order.application.dto.result.GetStoreWindowOrdersResult;
import cn.dextea.trade.order.application.dto.result.StoreOrderDetailItem;
import cn.dextea.trade.order.application.dto.result.StoreOrderDetailResult;
import cn.dextea.trade.order.application.usecase.GetStoreMakingBoardUseCase;
import cn.dextea.trade.order.application.usecase.GetStoreOrderDetailUseCase;
import cn.dextea.trade.order.application.usecase.GetStoreWindowOrdersUseCase;
import cn.dextea.trade.order.application.usecase.MarkOrderCollectedUseCase;
import cn.dextea.trade.order.application.usecase.MarkOrderReadyUseCase;
import dextea.order.v1.Order;
import dextea.order.v1.OrderServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrderRpcService extends OrderServiceGrpc.OrderServiceImplBase {

    private final GetStoreWindowOrdersUseCase getStoreWindowOrdersUseCase;
    private final GetStoreMakingBoardUseCase getStoreMakingBoardUseCase;
    private final GetStoreOrderDetailUseCase getStoreOrderDetailUseCase;
    private final MarkOrderReadyUseCase markOrderReadyUseCase;
    private final MarkOrderCollectedUseCase markOrderCollectedUseCase;

    @Override
    public void getStoreWindowOrders(Order.GetStoreWindowOrdersRequest request,
                                     StreamObserver<Order.GetStoreWindowOrdersResponse> observer) {
        try {
            GetStoreWindowOrdersResult result = getStoreWindowOrdersUseCase.execute(
                    GetStoreWindowOrdersCommand.builder().storeId(request.getStoreId()).hours(request.getHours()).build());
            Order.GetStoreWindowOrdersResponse.Builder response = Order.GetStoreWindowOrdersResponse.newBuilder()
                    .setTotal(result.getTotal());
            result.getItems().forEach(item -> response.addItems(toWindowItem(item)));
            complete(observer, response.build());
        } catch (RuntimeException error) { fail(observer, error); }
    }

    @Override
    public void getStoreMakingBoard(Order.GetStoreMakingBoardRequest request,
                                    StreamObserver<Order.GetStoreMakingBoardResponse> observer) {
        try {
            GetStoreMakingBoardResult result = getStoreMakingBoardUseCase.execute(
                    GetStoreMakingBoardCommand.builder().storeId(request.getStoreId()).build());
            complete(observer, Order.GetStoreMakingBoardResponse.newBuilder()
                    .addAllPreparingPickupCodes(result.getPreparingPickupCodes())
                    .addAllReadyPickupCodes(result.getReadyPickupCodes())
                    .setPreparingOrderCount(result.getPreparingOrderCount())
                    .setPreparingProductQuantity(result.getPreparingProductQuantity().getValue())
                    .build());
        } catch (RuntimeException error) { fail(observer, error); }
    }

    @Override
    public void getStoreOrderDetail(Order.GetStoreOrderDetailRequest request,
                                    StreamObserver<Order.StoreOrderDetail> observer) {
        try {
            StoreOrderDetailResult result = getStoreOrderDetailUseCase.execute(
                    GetStoreOrderDetailCommand.builder().storeId(request.getStoreId()).orderId(request.getOrderId()).build());
            complete(observer, toDetail(result));
        } catch (RuntimeException error) { fail(observer, error); }
    }

    @Override
    public void markOrderReady(Order.MarkOrderRequest request, StreamObserver<Order.Empty> observer) {
        try {
            markOrderReadyUseCase.execute(MarkOrderReadyCommand.builder()
                    .storeId(request.getStoreId()).orderId(request.getOrderId()).build());
            complete(observer, Order.Empty.getDefaultInstance());
        } catch (RuntimeException error) { fail(observer, error); }
    }

    @Override
    public void markOrderCollected(Order.MarkOrderRequest request, StreamObserver<Order.Empty> observer) {
        try {
            markOrderCollectedUseCase.execute(MarkOrderCollectedCommand.builder()
                    .storeId(request.getStoreId()).orderId(request.getOrderId()).build());
            complete(observer, Order.Empty.getDefaultInstance());
        } catch (RuntimeException error) { fail(observer, error); }
    }

    private static Order.StoreWindowOrderItem toWindowItem(GetStoreWindowOrdersResult.StoreWindowOrderItem item) {
        return Order.StoreWindowOrderItem.newBuilder().setOrderId(item.getOrderId()).setOrderNo(item.getOrderNo())
                .setPickupCode(item.getPickupCode()).setTotalPrice(item.getTotalPrice().getValue().doubleValue())
                .setTotalQuantity(item.getTotalQuantity().getValue()).setDiningMethod(item.getDiningMethod().getCode())
                .setMakingStatus(item.getMakingStatus().getCode()).setPaymentStatus(item.getPaymentStatus().getCode())
                .setCreatedAt(String.valueOf(item.getCreatedAt())).build();
    }

    private static Order.StoreOrderDetail toDetail(StoreOrderDetailResult result) {
        Order.StoreOrderDetail.Builder builder = Order.StoreOrderDetail.newBuilder()
                .setId(result.getId()).setOrderNo(result.getOrderNo()).setTradeNo(result.getTradeNo())
                .setStoreId(result.getStoreId()).setNote(nullToEmpty(result.getNote())).setPickupCode(result.getPickupCode())
                .setTotalPrice(result.getTotalPrice().getValue().doubleValue()).setTotalQuantity(result.getTotalQuantity().getValue())
                .setCreatedAt(String.valueOf(result.getCreatedAt())).setUpdatedAt(String.valueOf(result.getUpdatedAt()));
        set(builder, result.getDiningMethod(), (b, v) -> b.setDiningMethod(v));
        set(builder, result.getSource(), (b, v) -> b.setSource(v));
        set(builder, result.getMakingStatus(), (b, v) -> b.setMakingStatus(v));
        set(builder, result.getPaymentMethod(), (b, v) -> b.setPaymentMethod(v));
        set(builder, result.getPaymentStatus(), (b, v) -> b.setPaymentStatus(v));
        if (result.getPaymentExpiredAt() != null) builder.setPaymentExpiredAt(result.getPaymentExpiredAt().toString());
        if (result.getPaymentPaidAt() != null) builder.setPaymentPaidAt(result.getPaymentPaidAt().toString());
        if (result.getPaymentRefundedAt() != null) builder.setPaymentRefundedAt(result.getPaymentRefundedAt().toString());
        if (result.getItems() != null) result.getItems().forEach(item -> builder.addItems(toDetailItem(item)));
        return builder.build();
    }

    private static Order.StoreOrderDetailItem toDetailItem(StoreOrderDetailItem item) {
        return Order.StoreOrderDetailItem.newBuilder().setId(item.getId()).setProductId(item.getProductId())
                .setProductName(nullToEmpty(item.getProductName())).setSkuId(nullToEmpty(item.getSkuId()))
                .setCustomization(nullToEmpty(item.getCustomization())).setCoverUrl(nullToEmpty(item.getCoverUrl()))
                .setQuantity(item.getQuantity().getValue()).setUnitPrice(item.getUnitPrice().getValue().doubleValue())
                .setTotalPrice(item.getTotalPrice().getValue().doubleValue()).setAvailable(Boolean.TRUE.equals(item.getAvailable())).build();
    }

    private interface CodeSetter<T> { void set(T builder, int value); }
    private static <T> void set(T builder, Object value, CodeSetter<T> setter) {
        if (value instanceof cn.dextea.trade.shared.enumeration.CodeEnum code) setter.set(builder, code.getCode());
    }
    private static String nullToEmpty(String value) { return value == null ? "" : value; }
    private static <T> void complete(StreamObserver<T> observer, T response) { observer.onNext(response); observer.onCompleted(); }
    private static void fail(StreamObserver<?> observer, RuntimeException error) {
        observer.onError(Status.INTERNAL.withDescription(error.getMessage()).withCause(error).asRuntimeException());
    }
}
