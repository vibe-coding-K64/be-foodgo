package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.OrderDTO;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrders(@RequestParam String storeId) throws Exception {
        return ResponseEntity.ok(orderService.getOrdersByStoreId(storeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String id) throws Exception {
        OrderDTO order = orderService.getOrderById(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderDTO orderDTO) throws Exception {
        String id = orderService.createOrder(orderDTO);
        return ResponseEntity.ok(id);
    }

    @PatchMapping("/{id}/status")
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
