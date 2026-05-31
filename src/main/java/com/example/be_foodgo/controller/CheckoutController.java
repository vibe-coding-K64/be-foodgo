package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.CheckoutRequestV2;
import com.example.be_foodgo.dto.CheckoutResponse;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.CheckoutService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Checkout", description = "API dat hang (Checkout) cho phan he Khach hang")
@SecurityRequirement(name = "bearerAuth")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
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
                    responseCode = "404",
                    description = "Khong tim thay dia chi hoac voucher",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Loi he thong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<CheckoutResponse>> thucHienDatHang(
            @Valid
            @RequestBody
            @Parameter(description = "Thong tin dat hang")
            CheckoutRequestV2 request
    ) {
        log.info("Nhan yeu cau dat hang - userId: {}, addressId: {}, paymentMethod: {}, storeId: {}, discountVoucher: {}, shopVoucher: {}, freeshpVoucher: {}, itemCount: {}",
                request.getUserId(), request.getAddressId(), request.getPaymentMethod(), request.getStoreId(),
                request.getDiscountVoucherId(), request.getShopVoucherId(), request.getFreeshpVoucherId(),
                request.getItems() != null ? request.getItems().size() : 0);

        CheckoutResponse response = checkoutService.thucHienDatHang(request);

        log.info("Dat hang thanh cong - orderId: {}, orderCode: {}, tong thanh toan: {} VND",
                response.getOrderId(), response.getOrderCode(), response.getFinalAmount());

        return ResponseEntity.ok(
                ApiResponse.thatSuccess(response, "Dat hang thanh cong, vui long cho cua hang xac nhan.")
        );
    }

    @Schema(name = "CheckoutSuccessSchema", description = "Schema cho CheckoutResponse trong phan hoi thanh cong")
    public static class CheckoutSuccessSchema extends CheckoutResponse {
    }

    @Schema(name = "ApiResponseSchema", description = "Schema co ban cho ApiResponse")
    public static class ApiResponseSchema extends ApiResponse<Void> {
    }
}
