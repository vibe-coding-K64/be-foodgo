package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.driver.DriverOrderDTO;
import com.example.be_foodgo.dto.driver.DriverOrderStatusUpdateRequest;
import com.example.be_foodgo.dto.driver.DriverRespondRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.service.DriverOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers/orders")
@Tag(name = "Driver Orders", description = "API quan ly don hang cua tai xe")
public class DriverOrderController extends BaseDriverController {

    private static final Logger log = LoggerFactory.getLogger(DriverOrderController.class);

    private final DriverOrderService driverOrderService;

    public DriverOrderController(DriverOrderService driverOrderService) {
        super(log);
        this.driverOrderService = driverOrderService;
    }

    @GetMapping("/available")
    @Operation(
            summary = "Lay danh sach don hang kha dung",
            description = "Lay danh sach tat ca don hang dang cho tai xe nhan (status == 1, driverId == null)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay danh sach don hang kha dung thanh cong"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chua xac thuc - Token khong hop le hoac chua dang nhap")
    })
    public ResponseEntity<?> getAvailableOrders(HttpServletRequest httpRequest) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            List<DriverOrderDTO> orders = driverOrderService.getAvailableOrders();
            return ResponseEntity.ok(ApiResponse.thatSuccess(orders, "Lay danh sach don hang kha dung thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay don hang kha dung: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
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
            DriverOrderDTO order = driverOrderService.acceptOrder(orderId, holder.userId);
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
            description = "Tai xe tu choi nhan don hang. He thong se gui thong bao type 13 va xoa thong bao type 11 lien quan."
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
            driverOrderService.declineOrder(orderId, holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Tu choi don hang thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi tu choi don hang: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @PostMapping("/{id}/respond")
    @Operation(
            summary = "Tra loi yeu cau nhan don",
            description = "Tai xe tra loi (accept/decline) yeu cau nhan don tu he thong. Chi ap dung khi co thong bao push yeu cau nhan don."
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
            @Valid @RequestBody DriverRespondRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            if ("accept".equals(request.getAction())) {
                DriverOrderDTO order = driverOrderService.respondAcceptOrder(orderId, holder.userId);
                return ResponseEntity.ok(ApiResponse.thatSuccess(order, "Nhan don hang thanh cong."));
            } else {
                driverOrderService.respondDeclineOrder(orderId, holder.userId);
                return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Tu choi don hang thanh cong."));
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
            description = "Tai xe cap nhat trang thai don hang dang giao: 3=Hoan thanh (tao thu nhap), 4=Huy (reset driver)."
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
            @Valid @RequestBody DriverOrderStatusUpdateRequest request) {
        ResponseHolder holder = layUserIdHoacTraLoiLoi(httpRequest);
        if (holder.isAuthError) {
            return ResponseEntity.status(401).body(holder.errorResponse);
        }

        try {
            DriverOrderDTO order = driverOrderService.updateOrderStatus(orderId, holder.userId, request.getStatus());
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

    @GetMapping("/current")
    @Operation(
            summary = "Lay don hien tai",
            description = "Lay don hang dang giao cua tai xe hien tai (driverId == currentUser, status == 2)."
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
            List<DriverOrderDTO> orders = driverOrderService.getCurrentOrder(holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(orders, "Lay don hien tai thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay don hien tai: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }

    @GetMapping("/history")
    @Operation(
            summary = "Lay lich su don hang",
            description = "Lay lich su cac don hang da giao thanh cong cua tai xe (driverId == currentUser, status == 3)."
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
            List<DriverOrderDTO> orders = driverOrderService.getOrderHistory(holder.userId);
            return ResponseEntity.ok(ApiResponse.thatSuccess(orders, "Lay lich su don hang thanh cong."));
        } catch (Exception e) {
            log.error("Loi khi lay lich su don hang: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Da xay ra loi khong mong muon. Vui long thu lai sau."));
        }
    }
}
