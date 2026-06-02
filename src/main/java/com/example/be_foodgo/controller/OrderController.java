package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.CancelOrderRequest;
import com.example.be_foodgo.dto.CancelOrderResponse;
import com.example.be_foodgo.dto.CheckoutRequestV2;
import com.example.be_foodgo.dto.CheckoutResponse;
import com.example.be_foodgo.dto.OrderDTO;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.service.CheckoutService;
import com.example.be_foodgo.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Don hang", description = "Cac API lien quan den quan ly don hang")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private CheckoutService checkoutService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    @Operation(summary = "Lay danh sach don hang", description = "Lay danh sach tat ca cac don hang")
    public ResponseEntity<List<OrderDTO>> getOrders(@RequestParam String storeId) throws Exception {
        return ResponseEntity.ok(orderService.getOrdersByStoreId(storeId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lay thong tin chi tiet don hang", description = "Lay chi tiet don hang theo ID")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String id) throws Exception {
        OrderDTO order = orderService.getOrderById(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/checkout")
    @Operation(
            summary = "Dat hang (Checkout)",
            description = "Thuc hien dat hang cho khach hang. " +
                    "Quy trinh gom: kiem tra gio hang, kiem tra khoang cach Haversine, " +
                    "tinh tong tien server-side, xu ly voucher, va tao don hang atomically. " +
                    "Sau khi dat hang thanh cong, gio hang se bi xoa."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dat hang thanh cong",
                    content = @Content(schema = @Schema(implementation = CheckoutSuccessSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Yeu cau khong hop le - Gio hang rong, khoang cach vuot gioi han, voucher khong hop le, hoac mon an het hang",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "UserId trong request khong khop voi nguoi dung dang nhap",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay dia chi hoac voucher",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Don hang voi idempotency key da ton tai",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Loi he thong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<CheckoutResponse>> datHang(
            @AuthenticationPrincipal
            @Parameter(description = "User ID tu token xac thuc", hidden = true)
            String authenticatedUserId,

            @Valid
            @RequestBody
            @Parameter(description = "Thong tin dat hang")
            CheckoutRequestV2 request
    ) {
        log.info("Nhan yeu cau dat hang - userId: {}, addressId: {}, paymentMethod: {}, storeId: {}, discountVoucher: {}, shopVoucher: {}, freeshpVoucher: {}, itemCount: {}",
                request.getUserId(), request.getAddressId(), request.getPaymentMethod(), request.getStoreId(),
                request.getDiscountVoucherId(), request.getShopVoucherId(), request.getFreeshpVoucherId(),
                request.getItems() != null ? request.getItems().size() : 0);
        try {
            log.info("Request body: {}", objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.warn("Khong the serialize request body: {}", e.getMessage());
        }

        CheckoutResponse response = checkoutService.thucHienDatHang(request, authenticatedUserId);

        log.info("Dat hang thanh cong - orderId: {}, orderCode: {}, tong thanh toan: {} VND",
                response.getOrderId(), response.getOrderCode(), response.getFinalAmount());

        return ResponseEntity.ok(
                ApiResponse.thatSuccess(response, "Dat hang thanh cong, vui long cho cua hang xac nhan.")
        );
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Cap nhat trang thai don hang", description = "Cap nhat trang thai cua don hang (vi du: dang giao, da giao, v.v.)")
    public ResponseEntity<String> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> body) throws Exception {
        String status = body.get("status");
        String result = orderService.updateOrderStatus(id, status);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Huy don hang",
            description = "Cho phep khach hang huy don hang cua minh. Chi co the huy khi don hang o trang thai [Cho xac nhan] (0). Don hang o trang thai [Dang chuan bi], [Dang giao], [Hoan thanh], hoac [Da huy] khong the huy."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Huy don hang thanh cong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CancelOrderResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Khong the huy don hang - don dang o trang thai khong cho phep huy",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Khach hang khong co quyen huy don hang nay",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay don hang voi ID tuong ung",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @Parameter(description = "ID don hang can huy", required = true)
            @PathVariable("id") String orderId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal String userId,

            @Valid
            @RequestBody
            @Parameter(description = "Ly do huy don hang", example = "{\"reason\": \"Doi y\"}")
            CancelOrderRequest request) throws Exception {

        CancelOrderResponse response = orderService.cancelOrder(orderId, userId, request.getReason());
        return ResponseEntity.ok(ApiResponse.thatSuccess(response, "Huy don hang thanh cong."));
    }

    @Schema(name = "CancelOrderResponseSchema", description = "Schema cho CancelOrderResponse trong phan hoi thanh cong")
    public static class CancelOrderResponseSchema extends CancelOrderResponse {
    }

    @Schema(name = "CheckoutSuccessSchema", description = "Schema cho CheckoutResponse trong phan hoi thanh cong")
    public static class CheckoutSuccessSchema extends CheckoutResponse {
    }

    @Schema(name = "ApiResponseSchema", description = "Schema co ban cho ApiResponse")
    public static class ApiResponseSchema extends ApiResponse<Void> {
    }

    @GetMapping("/admin")
    @Operation(summary = "Lấy tất cả đơn hàng trên hệ thống dành cho Admin", description = "Admin giám sát toàn bộ đơn hàng của sàn FoodGo")
    public ResponseEntity<List<OrderDTO>> getAllAdminOrders() throws Exception {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
