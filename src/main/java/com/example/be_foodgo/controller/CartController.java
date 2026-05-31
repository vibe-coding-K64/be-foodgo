package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.CartRequest;
import com.example.be_foodgo.dto.CartResponse;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.model.CartItem;
import com.example.be_foodgo.service.CartService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "API quản lý giỏ hàng cho phân hệ Khách hàng")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(
            summary = "Lấy giỏ hàng của người dùng",
            description = "Truy xuất toàn bộ giỏ hàng của khách hàng, bao gồm thông tin cửa hàng và danh sách các món đã chọn."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy giỏ hàng thành công",
                    content = @Content(schema = @Schema(implementation = CartResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<CartResponse>> layGioHang(
            @RequestParam
            @Parameter(description = "ID người dùng khách hàng")
            String userId
    ) {
        log.info("Nhận yêu cầu lấy giỏ hàng - userId: {}", userId);

        CartResponse cart = cartService.layGioHang(userId);

        log.info("Trả giỏ hàng cho người dùng [{}] - {} món.", userId,
                cart.getItems() != null ? cart.getItems().size() : 0);
        return ResponseEntity.ok(ApiResponse.thatSuccess(cart, "Lấy giỏ hàng thành công."));
    }

    @Schema(name = "CartResponseSchema", description = "Schema cho CartResponse trong phản hồi thành công")
    public static class CartResponseSchema extends CartResponse {
    }
    @PostMapping("/add")
    @Operation(
            summary = "Thêm món vào giỏ hàng",
            description = "Thêm một món ăn vào giỏ hàng của khách hàng. " +
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
                    description = "Yêu cầu không hợp lệ - Món ăn hết hàng",
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

    @PutMapping("/{itemId}/quantity")
    @Operation(
            summary = "Cập nhật số lượng món trong giỏ hàng",
            description = "Cập nhật số lượng của một món trong giỏ hàng của khách hàng. " +
                    "Số lượng phải lớn hơn 0. Nếu món không tồn tại trong giỏ hàng, trả về lỗi 404."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật số lượng món thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Yêu cầu không hợp lệ - Số lượng <= 0",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Món không tồn tại trong giỏ hàng",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> capNhatSoLuongMon(
            @PathVariable
            @Parameter(description = "ID của món trong giỏ hàng (cartItemId)")
            String itemId,
            @Valid
            @RequestBody
            @Parameter(description = "Thông tin cập nhật số lượng")
            com.example.be_foodgo.dto.CartUpdateQuantityRequest request
    ) {
        log.info("Nhận yêu cầu cập nhật số lượng - itemId: {}, userId: {}, số lượng mới: {}",
                itemId, request.getUserId(), request.getQuantity());

        cartService.capNhatSoLuongMon(request.getUserId(), itemId, request.getQuantity());

        log.info("Cập nhật số lượng món [{}] thành {} thành công.", itemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Cập nhật số lượng món thành công."));
    }

    @DeleteMapping("/{itemId}")
    @Operation(
            summary = "Xóa một món khỏi giỏ hàng",
            description = "Xóa một món ăn khỏi giỏ hàng của khách hàng. " +
                    "Phương thức này là idempotent - trả về thành công kể cả khi món không tồn tại trong giỏ hàng."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xóa món khỏi giỏ hàng thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> xoaMotMon(
            @PathVariable
            @Parameter(description = "ID của món trong giỏ hàng (cartItemId)")
            String itemId,
            @RequestParam
            @Parameter(description = "ID người dùng khách hàng")
            String userId
    ) {
        log.info("Nhận yêu cầu xóa món - itemId: {}, userId: {}", itemId, userId);

        cartService.xoaMotMon(userId, itemId);

        log.info("Xóa món [{}] khỏi giỏ hàng thành công.", itemId);
        return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Đã xóa món khỏi giỏ hàng thành công."));
    }

    @DeleteMapping
    @Operation(
            summary = "Xóa toàn bộ giỏ hàng",
            description = "Xóa tất cả các món trong giỏ hàng của khách hàng. " +
                    "Sử dụng WriteBatch để tối ưu số lần gọi API lên Firebase."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xóa toàn bộ giỏ hàng thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> xoaToanBoGioHang(
            @RequestParam
            @Parameter(description = "ID người dùng khách hàng")
            String userId
    ) {
        log.info("Nhận yêu cầu xóa toàn bộ giỏ hàng - userId: {}", userId);

        cartService.xoaToanBoGioHang(userId);

        log.info("Xóa toàn bộ giỏ hàng của người dùng [{}] thành công.", userId);
        return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Đã xóa toàn bộ giỏ hàng thành công."));
    }

    @Schema(name = "CartItemSchema", description = "Schema cho CartItem trong phản hồi thành công")
    public static class CartItemSchema extends CartItem {
    }

    @Schema(name = "ApiResponseSchema", description = "Schema cơ bản cho ApiResponse")
    public static class ApiResponseSchema extends ApiResponse<Void> {
    }
}
