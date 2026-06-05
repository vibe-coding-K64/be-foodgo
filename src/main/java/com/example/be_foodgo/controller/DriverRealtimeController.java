package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.DriverOrderActionResultDTO;
import com.example.be_foodgo.dto.DriverRealtimeEvent;
import com.example.be_foodgo.dto.DriverRealtimeRespondRequest;
import com.example.be_foodgo.dto.DeliveryOrderDTO;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.service.DeliveryOrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

@Controller
public class DriverRealtimeController {

    private static final Logger log = LoggerFactory.getLogger(DriverRealtimeController.class);
    private static final String ORDER_STATUS_DESTINATION = "/queue/order-status";

    private final DeliveryOrderService deliveryOrderService;
    private final SimpMessagingTemplate messagingTemplate;

    public DriverRealtimeController(DeliveryOrderService deliveryOrderService,
                                    SimpMessagingTemplate messagingTemplate) {
        this.deliveryOrderService = deliveryOrderService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/driver/accept")
    public void acceptOrder(@Valid @Payload DriverRealtimeRespondRequest request,
                            @AuthenticationPrincipal String userId) {
        String resolvedUserId = requireAuthenticatedUser(userId);
        String orderId = request.getOrderId();
        String requestId = request.getRequestId();

        log.info("[STOMP DEBUG] ACCEPT request received: principal='{}', orderId='{}', requestId='{}'",
                resolvedUserId, orderId, requestId);

        try {
            DeliveryOrderDTO order = deliveryOrderService.respondAcceptOrder(orderId, resolvedUserId, requestId);
            sendOrderStatusToUser(
                    resolvedUserId,
                    DriverRealtimeEvent.builder()
                            .event("ORDER_ACCEPTED")
                            .message("Nhan don hang thanh cong")
                            .orderId(orderId)
                            .requestId(requestId)
                            .status("SUCCESS")
                            .order(order)
                            .build()
            );
        } catch (BusinessException e) {
            log.warn("Loi business khi realtime accept order {}: {}", orderId, e.getMessage());
            sendOrderStatusToUser(
                    resolvedUserId,
                    DriverRealtimeEvent.builder()
                            .event("ORDER_ACCEPT_FAILED")
                            .message(e.getMessage())
                            .orderId(orderId)
                            .requestId(requestId)
                            .status("ERROR")
                            .actionResult(DriverOrderActionResultDTO.builder()
                                    .orderId(orderId)
                                    .requestId(requestId)
                                    .status("ACCEPT_FAILED")
                                    .build())
                            .build()
            );
        } catch (RuntimeException e) {
            log.error("Loi khi realtime accept order {}: {}", orderId, e.getMessage());
            sendOrderStatusToUser(
                    resolvedUserId,
                    DriverRealtimeEvent.builder()
                            .event("ORDER_ACCEPT_FAILED")
                            .message("Da xay ra loi khong mong muon. Vui long thu lai sau.")
                            .orderId(orderId)
                            .requestId(requestId)
                            .status("ERROR")
                            .actionResult(DriverOrderActionResultDTO.builder()
                                    .orderId(orderId)
                                    .requestId(requestId)
                                    .status("ACCEPT_FAILED")
                                    .build())
                            .build()
            );
        }
    }

    @MessageMapping("/driver/decline")
    public void declineOrder(@Valid @Payload DriverRealtimeRespondRequest request,
                             @AuthenticationPrincipal String userId) {
        String resolvedUserId = requireAuthenticatedUser(userId);
        String orderId = request.getOrderId();
        String requestId = request.getRequestId();

        log.info("[STOMP DEBUG] DECLINE request received: principal='{}', orderId='{}', requestId='{}'",
                resolvedUserId, orderId, requestId);

        try {
            DriverOrderActionResultDTO result = deliveryOrderService.respondDeclineOrder(orderId, resolvedUserId, requestId);
            sendOrderStatusToUser(
                    resolvedUserId,
                    DriverRealtimeEvent.builder()
                            .event("ORDER_DECLINED")
                            .message("Tu choi don hang thanh cong")
                            .orderId(orderId)
                            .requestId(requestId)
                            .status("SUCCESS")
                            .actionResult(result)
                            .build()
            );
        } catch (BusinessException e) {
            log.warn("Loi business khi realtime decline order {}: {}", orderId, e.getMessage());
            sendOrderStatusToUser(
                    resolvedUserId,
                    DriverRealtimeEvent.builder()
                            .event("ORDER_DECLINE_FAILED")
                            .message(e.getMessage())
                            .orderId(orderId)
                            .requestId(requestId)
                            .status("ERROR")
                            .actionResult(DriverOrderActionResultDTO.builder()
                                    .orderId(orderId)
                                    .requestId(requestId)
                                    .status("DECLINE_FAILED")
                                    .build())
                            .build()
            );
        } catch (RuntimeException e) {
            log.error("Loi khi realtime decline order {}: {}", orderId, e.getMessage());
            sendOrderStatusToUser(
                    resolvedUserId,
                    DriverRealtimeEvent.builder()
                            .event("ORDER_DECLINE_FAILED")
                            .message("Da xay ra loi khong mong muon. Vui long thu lai sau.")
                            .orderId(orderId)
                            .requestId(requestId)
                            .status("ERROR")
                            .actionResult(DriverOrderActionResultDTO.builder()
                                    .orderId(orderId)
                                    .requestId(requestId)
                                    .status("DECLINE_FAILED")
                                    .build())
                            .build()
            );
        }
    }

    private void sendOrderStatusToUser(String userId, DriverRealtimeEvent event) {
        log.info("[STOMP DEBUG] Sending order-status to principal='{}', destination='{}', event='{}', orderId='{}', requestId='{}'",
                userId,
                ORDER_STATUS_DESTINATION,
                event.getEvent(),
                event.getOrderId(),
                event.getRequestId());
        messagingTemplate.convertAndSendToUser(userId, ORDER_STATUS_DESTINATION, event);
    }

    private String requireAuthenticatedUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("Chua xac thuc ket noi websocket.");
        }
        return userId;
    }
}
