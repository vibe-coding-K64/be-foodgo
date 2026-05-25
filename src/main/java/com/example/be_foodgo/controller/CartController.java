package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.CartRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.model.CartItem;
import com.example.be_foodgo.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "API quản lý giỏ hàng cho phân hệ Khách hàng")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    @Operation(
            summary = "Thêm món vào giỏ hàng",
            description = "Thêm một món ăn vào giỏ hàng của khách hàng. " +
                    "Nếu giỏ hàng đã có món từ cửa hàng khác, hệ thống sẽ trả về lỗi yêu cầu xác nhận xóa giỏ hàng cũ. " +
                    "Giá tiền được tính toán từ phía server dựa trên basePrice, size và toppings từ collection products."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Thêm món vào giỏ hàng thành công",
                    content = @Content(schema = @Schema(implementation = CartItemSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Yêu cầu không hợp lệ - Món ăn hết hàng hoặc vi phạm quy tắc một cửa hàng",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Sản phẩm không tồn tại",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<CartItem>> themMonVaoGio(
            @Valid
            @RequestBody
            @Parameter(description = "Thông tin món ăn cần thêm vào giỏ hàng")
            CartRequest request
    ) {
        log.info("Nhận yêu cầu thêm món vào giỏ hàng - userId: {}, foodId: {}, storeId: {}, số lượng: {}",
                request.getUserId(), request.getFoodId(), request.getStoreId(), request.getQuantity());

        CartItem item = cartService.themMonVaoGio(request);

        log.info("Xử lý thêm món vào giỏ hàng thành công - cartItemId: {}", item.getId());
        return ResponseEntity.ok(ApiResponse.thatSuccess(item, "Đã thêm món vào giỏ hàng thành công."));
    }

    @Schema(name = "CartItemSchema", description = "Schema cho CartItem trong phản hồi thành công")
    public static class CartItemSchema extends CartItem {
    }

    @Schema(name = "ApiResponseSchema", description = "Schema cơ bản cho ApiResponse")
    public static class ApiResponseSchema extends ApiResponse<Void> {
    }
}
