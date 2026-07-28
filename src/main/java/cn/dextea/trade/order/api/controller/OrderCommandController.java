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
 * 订单命令接口
 * 承载写操作（创建订单、订单预构建）
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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "订单预构建请求示例",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                            {
                                "storeId": 1,
                                "customerId": 2,
                                "platform": "alipay",
                                "diningMethod": 0,
                                "note": "",
                                "products": [
                                    {
                                        "skuId": "1#1_1-2_5-3_9-4_11",
                                        "quantity": 1
                                    }
                                ]
                            }
                            """)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "预构建订单正常响应示例",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                            {
                                "code": 0,
                                "message": "成功",
                                "data": {
                                    "unavailable": {
                                        "products": [],
                                        "customization": []
                                    },
                                    "products": [
                                        {
                                            "skuId": "1#1_1-2_5-3_9-4_11",
                                            "quantity": 1,
                                            "productId": 1,
                                            "productName": "青芒酸",
                                            "coverId": 1,
                                            "coverUrl": "https://dextea-1313412108.cos.ap-guangzhou.myqcloud.com/gallery_1784530228002_4OqY4Fg2.png",
                                            "customizationText": "推荐 / 少甜(推荐) / 标准酸 / 标准(含柠檬叶)",
                                            "unitPrice": 22.00,
                                            "subtotal": 22.00
                                        }
                                    ],
                                    "totalQuantity": 1,
                                    "totalPrice": 22.00
                                }
                            }
                            """)))
    public APIResponse<PreBuildOrderResponse> preBuildOrder(@Valid @RequestBody PreBuildOrderRequest request) {
        PreBuildOrderResponse result = OrderApiAssembler.toPreBuildResponse(
                orderApplicationService.preBuildOrder(OrderApiAssembler.toPreBuildCommand(request)));
        return APIResponse.success(result);
    }
}
