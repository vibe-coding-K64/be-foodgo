package com.example.be_foodgo.service;

import com.example.be_foodgo.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DriverLocationMonitorService {

    private static final Logger log = LoggerFactory.getLogger(DriverLocationMonitorService.class);
    private static final long LOCATION_TIMEOUT_MS = 180_000L;
    private static final long CHECK_INTERVAL_MS = 30_000L;

    private final DeliveryService deliveryService;
    private final WalletRepository walletRepository;

    public DriverLocationMonitorService(DeliveryService deliveryService,
                                        WalletRepository walletRepository) {
        this.deliveryService = deliveryService;
        this.walletRepository = walletRepository;
    }

    @PostConstruct
    public void init() {
        log.info("DriverLocationMonitorService da khoi tao. Khoang thoi gian kiem tra: {}ms, Thoi gian cho location: {}ms.",
                CHECK_INTERVAL_MS, LOCATION_TIMEOUT_MS);
        log.info("Scheduler se chay task kiem tra tai xe lien tuc moi {}ms.", CHECK_INTERVAL_MS);
    }

    @Scheduled(fixedRateString = "${driver.location.check.interval:30000}")
    public void kiemTraTaiXeKhongHoatDong() {
        log.info("[SCHEDULED] Bat dau kiem tra tai xe khong hoat dong...");

        try {
            List<Map<String, Object>> activeDrivers = walletRepository.findActiveDriverProfiles();
            if (activeDrivers.isEmpty()) {
                log.debug("Khong co tai xe nao dang online.");
                return;
            }

            long now = System.currentTimeMillis();
            List<String> driverIdsCanhBao = new ArrayList<>();
            List<String> driverIdsDeactivate = new ArrayList<>();

            for (Map<String, Object> profile : activeDrivers) {
                String driverId = profile.get("userId") != null
                        ? String.valueOf(profile.get("userId"))
                        : profile.get("id") != null ? String.valueOf(profile.get("id")) : null;
                Object lastUpdateValue = profile.get("lastLocationUpdate");
                Long lastUpdate = null;
                if (lastUpdateValue instanceof Number number) {
                    lastUpdate = number.longValue();
                }
                Boolean isActive = profile.get("isActive") instanceof Boolean value ? value : null;

                if (driverId == null || isActive == null || !isActive) {
                    continue;
                }

                if (lastUpdate == null) {
                    log.warn("Tai xe {} khong co lastLocationUpdate, cho deactive", driverId);
                    driverIdsDeactivate.add(driverId);
                    continue;
                }

                long elapsed = now - lastUpdate;
                if (elapsed > LOCATION_TIMEOUT_MS) {
                    log.warn("Tai xe {} chua cap nhat vi tri trong {}ms (>{}ms), se bi deactive",
                            driverId, elapsed, LOCATION_TIMEOUT_MS);
                    driverIdsDeactivate.add(driverId);
                } else if (elapsed > LOCATION_TIMEOUT_MS * 0.6) {
                    log.debug("Tai xe {} chua cap nhat vi tri trong {}ms, canh bao",
                            driverId, elapsed);
                    driverIdsCanhBao.add(driverId);
                }
            }

            for (String driverId : driverIdsCanhBao) {
                if (!driverIdsDeactivate.contains(driverId)) {
                    log.info("Tai xe {} canh bao: chua cap nhat vi tri qua {:.0f}s",
                            driverId, (double) LOCATION_TIMEOUT_MS * 0.6 / 1000);
                }
            }

            for (String driverId : driverIdsDeactivate) {
                try {
                    deliveryService.deactivateDriverDueToTimeout(driverId);
                    log.warn("Da tu dong tat trang thai tai xe {} do khong cap nhat vi tri", driverId);
                } catch (Exception e) {
                    log.error("Loi khi tu dong tat trang thai tai xe {}: {}", driverId, e.getMessage());
                }
            }

            if (!driverIdsDeactivate.isEmpty()) {
                log.info("Da kiem tra {} tai xe, {} bi tu dong tat.",
                        activeDrivers.size(), driverIdsDeactivate.size());
            }
        } catch (Exception e) {
            log.error("Loi khi kiem tra active driver trong Firestore: {}", e.getMessage(), e);
        }
    }
}
