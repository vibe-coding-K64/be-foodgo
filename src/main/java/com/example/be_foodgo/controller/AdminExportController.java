package com.example.be_foodgo.controller;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/export")
@CrossOrigin(origins = "*")
public class AdminExportController {

    private static final Logger log = LoggerFactory.getLogger(AdminExportController.class);

    @Autowired
    private Firestore firestore;

    /**
     * Xuất danh sách đơn hàng ra CSV.
     * GET /api/admin/export/orders?from=2024-01-01&to=2024-12-31
     */
    @GetMapping("/orders")
    public void exportOrders(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            HttpServletResponse response) throws Exception {

        // Tính khoảng thời gian
        ZoneId zone = ZoneId.systemDefault();
        Instant fromInstant;
        Instant toInstant;

        if (from != null && !from.isBlank()) {
            fromInstant = LocalDate.parse(from).atStartOfDay(zone).toInstant();
        } else {
            fromInstant = LocalDate.now().minusDays(30).atStartOfDay(zone).toInstant();
        }
        if (to != null && !to.isBlank()) {
            toInstant = LocalDate.parse(to).plusDays(1).atStartOfDay(zone).toInstant();
        } else {
            toInstant = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant();
        }

        // Đặt headers cho response CSV
        String filename = "orders_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        // BOM để Excel đọc đúng UTF-8
        response.getOutputStream().write(0xEF);
        response.getOutputStream().write(0xBB);
        response.getOutputStream().write(0xBF);

        // Query Firestore
        List<QueryDocumentSnapshot> docs;
        try {
            Query query = firestore.collection("orders");
            docs = query.get().get().getDocuments();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Loi khi lay don hang de xuat CSV: {}", e.getMessage());
            response.setStatus(500);
            return;
        }

        // Lọc theo thời gian
        Instant finalFromInstant = fromInstant;
        Instant finalToInstant = toInstant;
        docs = docs.stream().filter(doc -> {
            Object createdAtObj = doc.get("createdAt");
            if (createdAtObj == null) return false;
            Instant t;
            if (createdAtObj instanceof Timestamp) {
                t = ((Timestamp) createdAtObj).toDate().toInstant();
            } else return false;
            return !t.isBefore(finalFromInstant) && t.isBefore(finalToInstant);
        }).collect(java.util.stream.Collectors.toList());

        // Ghi CSV
        try (PrintWriter writer = response.getWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Mã đơn", "Khách hàng", "Cửa hàng", "Tổng tiền",
                             "Trạng thái", "Thanh toán", "Ngày tạo"))) {

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(zone);

            for (QueryDocumentSnapshot doc : docs) {
                String code = doc.getString("code");
                if (code == null) code = doc.getId().substring(0, 8).toUpperCase();

                String customerName = doc.getString("customerName");
                if (customerName == null) customerName = "";

                String storeName = doc.getString("storeName");
                if (storeName == null) storeName = "";

                Double totalAmount = doc.getDouble("totalAmount");
                if (totalAmount == null) { Double fa = doc.getDouble("finalAmount"); totalAmount = fa != null ? fa : 0.0; }

                Object statusObj = doc.get("status");
                String statusStr = mapStatusToVi(statusObj);

                String paymentMethod = doc.getString("paymentMethod");
                if (paymentMethod == null) paymentMethod = "";

                Object createdAtObj = doc.get("createdAt");
                String createdAtStr = "";
                if (createdAtObj instanceof Timestamp) {
                    createdAtStr = dtf.format(((Timestamp) createdAtObj).toDate().toInstant());
                }

                csvPrinter.printRecord(code, customerName, storeName,
                        String.format("%.0f", totalAmount), statusStr, paymentMethod, createdAtStr);
            }
            csvPrinter.flush();
        }

        log.info("Da xuat {} don hang ra CSV tu {} den {}", docs.size(), from, to);
    }

    private String mapStatusToVi(Object statusObj) {
        if (statusObj == null) return "";
        if (statusObj instanceof Number) {
            int s = ((Number) statusObj).intValue();
            return switch (s) {
                case 0 -> "Chờ xác nhận";
                case 1 -> "Đang chế biến";
                case 2 -> "Đang giao";
                case 3 -> "Hoàn thành";
                case 4 -> "Đã hủy";
                default -> String.valueOf(s);
            };
        }
        return statusObj.toString();
    }
}
