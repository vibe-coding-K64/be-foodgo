package com.example.be_foodgo.controller;

import com.example.be_foodgo.constant.DeliveryOrderStatus;
import com.example.be_foodgo.dto.DeliveryOrderDTO;
import com.example.be_foodgo.dto.DeliveryOrderStatusRequest;
import com.example.be_foodgo.dto.DeliveryRespondRequest;
import com.example.be_foodgo.dto.DriverOrderActionResultDTO;
import com.example.be_foodgo.dto.DriverRealtimeEvent;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.service.DeliveryOrderService;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers/orders")
@Tag(name = "Delivery Orders", description = "API quan ly don hang cua tai xe")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryOrderController extends BaseController {

    private static final String ORDER_STATUS_DESTINATION = "/queue/order-status";

    private final DeliveryOrderService deliveryOrderService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Firestore firestore;

    public DeliveryOrderController(DeliveryOrderService deliveryOrderService,
                                  SimpMessagingTemplate messagingTemplate,
                                  Firestore firestore) {
        super(LoggerFactory.getLogger(DeliveryOrderController.class));
        this.deliveryOrderService = deliveryOrderService;
        this.messagingTemplate = messagingTemplate;
        this.firestore = firestore;
    }

    private void broadcastToDriver(String driverId, DriverRealtimeEvent event) {
        if (driverId != null && !driverId.isBlank()) {
            messagingTemplate.convertAndSendToUser(driverId, ORDER_STATUS_DESTINATION, event);
            log.info("[WS] Broadcast {} to driver {}: orderId={}", event.getEvent(), driverId, event.getOrderId());
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lay chi tiet don hang cho tai xe",
            description = "Lay chi tiet mot don hang theo ID de FE driver dung lam fallback sau khi nhan FCM."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay chi tiet don hang thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay don hang")
    })
    public ResponseEntity<?> getOrderDetail(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID don hang", required = true)
            @PathVariable("id") String orderId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryOrderDTO order = deliveryOrderService.getOrderDetail(orderId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(order, "Lay chi tiet don hang thanh cong."));
        } catch (BusinessException e) {
            log.warn("Loi business khi lay chi tiet don hang: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi lay chi tiet don hang: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/available")
    @Operation(
            summary = "Lay danh sach don hang kha dung",
            description = "Lay danh sach tat ca don hang dang cho tai xe nhan (status == " + DeliveryOrderStatus.WAITING_DRIVER + ", driverId == null)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay danh sach don hang kha dung thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc")
    })
    public ResponseEntity<?> getAvailableOrders(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            List<DeliveryOrderDTO> orders = deliveryOrderService.getAvailableOrders();
            return ResponseEntity.ok(ApiResponse.thatSuccess(orders, "Lay danh sach don hang kha dung thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay don hang kha dung: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/debug-all")
    public ResponseEntity<?> debugAllOrders(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            QuerySnapshot snap = firestore.collection("orders").get().get();
            for (QueryDocumentSnapshot doc : snap.getDocuments()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", doc.getId());
                m.put("status", doc.get("status"));
                m.put("statusClass", doc.get("status") != null ? doc.get("status").getClass().getName() : "null");
                m.put("driverId", doc.get("driverId"));
                m.put("driverIdClass", doc.get("driverId") != null ? doc.get("driverId").getClass().getName() : "null");
                m.put("code", doc.get("code"));
                result.add(m);
            }
            return ResponseEntity.ok(ApiResponse.thatSuccess(result, "Debug orders count=" + result.size()));
        } catch (Exception e) {
            log.error("Debug error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    @GetMapping("/debug-available")
    public ResponseEntity<?> debugAvailableOrders(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            QuerySnapshot snap = firestore.collection("orders")
                    .whereEqualTo("status", 1)
                    .get()
                    .get();
            for (QueryDocumentSnapshot doc : snap.getDocuments()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", doc.getId());
                m.put("status", doc.get("status"));
                m.put("statusClass", doc.get("status") != null ? doc.get("status").getClass().getName() : "null");
                m.put("driverId", doc.get("driverId"));
                m.put("driverIdClass", doc.get("driverId") != null ? doc.get("driverId").getClass().getName() : "null");
                result.add(m);
            }
            return ResponseEntity.ok(ApiResponse.thatSuccess(result, "status=1 count=" + result.size()));
        } catch (Exception e) {
            log.error("Debug available error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    @PostMapping("/create-test-order")
    public ResponseEntity<?> createTestOrder(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }
        try {
            String orderId = "test_order_" + System.currentTimeMillis();
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("id", orderId);
            orderData.put("userId", "user_001");
            orderData.put("storeId", "store_001");
            orderData.put("storeName", "Cơm Tấm Phúc Lộc Thọ");
            orderData.put("code", "TEST001");
            orderData.put("items", new ArrayList<>());
            orderData.put("totalAmount", 90000.0);
            orderData.put("deliveryFee", 15000.0);
            orderData.put("discountAmount", 0.0);
            orderData.put("finalAmount", 105000.0);
            orderData.put("status", 1);
            orderData.put("deliveryStep", "WAITING_DRIVER");
            orderData.put("deliveryAddress", "Ký túc xá UTC2, Quận 9, TP.HCM");
            orderData.put("deliveryLat", 10.8446);
            orderData.put("deliveryLng", 106.7975);
            orderData.put("receiverName", "Test Khach Hang");
            orderData.put("receiverPhone", "0123456789");
            orderData.put("paymentMethod", 1);
            orderData.put("paymentStatus", 2);
            orderData.put("createdAt", new Date());
            orderData.put("updatedAt", new Date());

            firestore.collection("orders").document(orderId).set(orderData).get();
            log.info("Da tao don hang test: {}", orderId);

            return ResponseEntity.ok(ApiResponse.thatSuccess(
                    Map.of("orderId", orderId), "Tao don hang test thanh cong."));
        } catch (Exception e) {
            log.error("Loi tao don hang test: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.thatError(500, e.getMessage()));
        }
    }

    @PostMapping("/{id}/accept")
    @Operation(
            summary = "Xac nhan nhan don hang",
            description = "Tai xe xac nhan nhan mot don hang. Su dung Firestore Transaction de dam bao khong co 2 tai xe cung nhan 1 don."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Nhan don hang thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay don hang"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Don hang da duoc tai xe khac nhan")
    })
    public ResponseEntity<?> acceptOrder(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID don hang", required = true)
            @PathVariable("id") String orderId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryOrderDTO order = deliveryOrderService.acceptOrder(orderId, holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(order, "Nhan don hang thanh cong."));
        } catch (BusinessException e) {
            log.warn("Loi business khi nhan don: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi nhan don hang: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PostMapping("/{id}/decline")
    @Operation(
            summary = "Tu choi don hang",
            description = "Tai xe tu choi nhan don hang."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tu choi don hang thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc")
    })
    public ResponseEntity<?> declineOrder(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID don hang", required = true)
            @PathVariable("id") String orderId) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DriverOrderActionResultDTO result = deliveryOrderService.declineOrder(orderId, holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(result, "Tu choi don hang thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi tu choi don hang: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PostMapping("/{id}/respond")
    @Operation(
            summary = "Tra loi yeu cau nhan don",
            description = "Tai xe tra loi (accept/decline) yeu cau nhan don tu he thong."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tra loi thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Yeu cau khong hop le hoac da het han"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Tai xe khong nam trong danh sach yeu cau")
    })
    public ResponseEntity<?> respondToOrder(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID don hang", required = true)
            @PathVariable("id") String orderId,
            @Valid @RequestBody DeliveryRespondRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            if ("accept".equals(request.getAction())) {
                DeliveryOrderDTO order = deliveryOrderService.respondAcceptOrder(orderId, holder.userId, request.getRequestId());
                return ResponseEntity.ok(ApiResponse.thatSuccess(order, "Nhan don hang thanh cong."));
            } else {
                DriverOrderActionResultDTO result = deliveryOrderService.respondDeclineOrder(orderId, holder.userId, request.getRequestId());
                return ResponseEntity.ok(ApiResponse.thatSuccess(result, "Tu choi don hang thanh cong."));
            }
        } catch (BusinessException e) {
            log.warn("Loi business khi tra loi don: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi tra loi don hang: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PutMapping("/{id}/status")
    @Operation(
            summary = "Cap nhat trang thai don hang",
            description = "Tai xe cap nhat trang thai don hang dang giao: "
                    + DeliveryOrderStatus.COMPLETED + "=Hoan thanh, "
                    + DeliveryOrderStatus.CANCELLED + "=Huy/reset ve cho tai xe khac nhan."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cap nhat trang thai thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Trang thai khong hop le"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Tai xe khong phai chu don hang nay"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay don hang")
    })
    public ResponseEntity<?> updateOrderStatus(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID don hang", required = true)
            @PathVariable("id") String orderId,
            @Valid @RequestBody DeliveryOrderStatusRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryOrderDTO order = deliveryOrderService.updateOrderStatus(orderId, holder.userId, request.getStatus());
            int newStatus = request.getStatus();

            if (newStatus == DeliveryOrderStatus.DELIVERING) {
                broadcastToDriver(holder.userId, DriverRealtimeEvent.builder()
                        .event("ORDER_PICKED_UP")
                        .message("Da xac nhan lay hang thanh cong")
                        .orderId(orderId)
                        .status("SUCCESS")
                        .order(order)
                        .build());
            } else if (newStatus == DeliveryOrderStatus.COMPLETED) {
                broadcastToDriver(holder.userId, DriverRealtimeEvent.builder()
                        .event("ORDER_COMPLETED")
                        .message("Giao hang thanh cong")
                        .orderId(orderId)
                        .status("SUCCESS")
                        .order(order)
                        .build());
            } else if (newStatus == DeliveryOrderStatus.WAITING_DRIVER) {
                broadcastToDriver(holder.userId, DriverRealtimeEvent.builder()
                        .event("ORDER_CANCELLED")
                        .message("Don hang da bi huy")
                        .orderId(orderId)
                        .status("SUCCESS")
                        .order(order)
                        .build());
            }

            return ResponseEntity.ok(ApiResponse.thatSuccess(order, "Cap nhat trang thai don hang thanh cong."));
        } catch (BusinessException e) {
            log.warn("Loi business khi cap nhat trang thai: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi cap nhat trang thai don hang: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PostMapping(value = "/{id}/deliver-with-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Xac nhan giao hang voi anh",
            description = "Tai xe gui anh xac nhan giao hang de hoan thanh don. Anh se duoc upload len Cloudinary va luu URL vao Firestore."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xac nhan giao hang thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Anh khong hop le"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Tai xe khong phai chu don hang nay"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay don hang")
    })
    public ResponseEntity<?> confirmDeliveryWithPhoto(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID don hang", required = true)
            @PathVariable("id") String orderId,
            @Parameter(description = "Anh xac nhan giao hang", required = true)
            @RequestParam("photo") MultipartFile photo) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, "Anh xac nhan giao hang khong duoc de trong."));
        }

        try {
            DeliveryOrderDTO order = deliveryOrderService.confirmDeliveryWithPhoto(
                    orderId, holder.userId, photo);

            broadcastToDriver(holder.userId, DriverRealtimeEvent.builder()
                    .event("ORDER_COMPLETED")
                    .message("Giao hang thanh cong")
                    .orderId(orderId)
                    .status("SUCCESS")
                    .order(order)
                    .build());

            return ResponseEntity.ok(ApiResponse.thatSuccess(order, "Xac nhan giao hang thanh cong."));
        } catch (BusinessException e) {
            log.warn("Loi business khi xac nhan giao hang: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Loi khi upload anh giao hang: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi xac nhan giao hang: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/current")
    @Operation(
            summary = "Lay don hien tai",
            description = "Lay don hang dang giao cua tai xe hien tai (driverId == currentUser, status == " + DeliveryOrderStatus.DELIVERING + ")."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay don hien tai thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc")
    })
    public ResponseEntity<?> getCurrentOrder(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DeliveryOrderDTO order = deliveryOrderService.getCurrentOrder(holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(order, "Lay don hien tai thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay don hien tai: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/active")
    @Operation(
            summary = "Lay danh sach don hang dang hoat dong",
            description = "Lay tat ca don hang dang hoat dong cua tai xe hien tai (status == " + DeliveryOrderStatus.DELIVERING + ", driverId == currentUser)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay danh sach don hang active thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc")
    })
    public ResponseEntity<?> getActiveOrders(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            List<DeliveryOrderDTO> orders = deliveryOrderService.getActiveOrders(holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(orders, "Lay danh sach don hang hoat dong thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay don hang hoat dong: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PostMapping("/{id}/report-issue")
    @Operation(
            summary = "Bao cao su co don hang",
            description = "Tai xe gui bao cao su co khi gap van de trong qua trinh giao hang."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Gui bao cao thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu khong hop le"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Tai xe khong phai chu don hang nay"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay don hang")
    })
    public ResponseEntity<?> reportOrderIssue(
            HttpServletRequest httpRequest,
            @Parameter(description = "ID don hang", required = true)
            @PathVariable("id") String orderId,
            @Valid @RequestBody Map<String, String> body) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        String reason = body.get("reason");
        String additionalNote = body.get("additionalNote");
        if (reason == null || reason.isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, "Ly do bao cao khong duoc de trong."));
        }

        try {
            Map<String, Object> report = deliveryOrderService.reportIssue(
                    orderId, holder.userId, reason, additionalNote);
            return ResponseEntity.ok(ApiResponse.thatSuccess(report, "Gui bao cao thanh cong."));
        } catch (BusinessException e) {
            log.warn("Loi business khi gui bao cao: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus().value()).body(
                    ApiResponse.thatError(e.getStatus().value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Loi khi gui bao cao su co: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/history")
    @Operation(
            summary = "Lay lich su don hang",
            description = "Lay lich su cac don hang da giao thanh cong cua tai xe (driverId == currentUser, status == " + DeliveryOrderStatus.COMPLETED + ")."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay lich su don hang thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc")
    })
    public ResponseEntity<?> getOrderHistory(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            List<DeliveryOrderDTO> orders = deliveryOrderService.getOrderHistory(holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(orders, "Lay lich su don hang thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay lich su don hang: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
