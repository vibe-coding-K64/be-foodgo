package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.OrderDTO;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Đơn hàng", description = "Các API liên quan đến quản lý đơn hàng")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    @Operation(summary = "Lấy danh sách đơn hàng", description = "Lấy danh sách tất cả các đơn hàng")
    public ResponseEntity<List<OrderDTO>> getOrders(@RequestParam String storeId) throws Exception {
        return ResponseEntity.ok(orderService.getOrdersByStoreId(storeId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết đơn hàng", description = "Lấy chi tiết đơn hàng theo ID")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String id) throws Exception {
        OrderDTO order = orderService.getOrderById(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Tạo đơn hàng mới", description = "Thêm một đơn hàng mới vào hệ thống")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@RequestBody OrderDTO orderDTO) throws Exception {
        String orderId = orderService.createOrder(orderDTO);
        OrderDTO createdOrder = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.thatSuccess(createdOrder, "Tạo đơn hàng thành công."));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái đơn hàng", description = "Cập nhật trạng thái của đơn hàng (ví dụ: đang giao, đã giao, v.v.)")
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
            summary = "Hủy đơn hàng",
            description = "Cho phép khách hàng hủy đơn hàng của mình. Chỉ có thể hủy khi đơn hàng ở trạng thái [Chờ xác nhận] (0). Đơn hàng ở trạng thái [Đang chuẩn bị], [Đang giao], [Hoàn thành], hoặc [Đã hủy] không thể hủy."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hủy đơn hàng thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Không thể hủy đơn hàng - đơn đang ở trạng thái không cho phép hủy",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Khách hàng không có quyền hủy đơn hàng này",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy đơn hàng với ID tương ứng",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @Parameter(description = "ID đơn hàng cần hủy", required = true)
            @PathVariable("id") String orderId,

            @Parameter(description = "ID người dùng khách hàng (để xác thực quyền sở hữu đơn hàng)", required = true)
            @RequestParam("userId") String userId) throws Exception {

        OrderDTO cancelledOrder = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.thatSuccess(cancelledOrder, "Hủy đơn hàng thành công."));
    }
}
