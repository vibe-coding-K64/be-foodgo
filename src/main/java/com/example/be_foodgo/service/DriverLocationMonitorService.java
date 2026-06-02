package com.example.be_foodgo.service;

import com.example.be_foodgo.repository.WalletRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriverLocationMonitorService {

    private static final Logger log = LoggerFactory.getLogger(DriverLocationMonitorService.class);
    private static final String RDB_ACTIVE_DRIVERS = "active_drivers";
    private static final long LOCATION_TIMEOUT_MS = 60_000L;
    private static final long CHECK_INTERVAL_MS = 30_000L;

    private final DeliveryService deliveryService;
    private final WalletRepository walletRepository;
    private final FirebaseDatabase firebaseDatabase;

    public DriverLocationMonitorService(DeliveryService deliveryService,
                                        WalletRepository walletRepository,
                                        FirebaseDatabase firebaseDatabase) {
        this.deliveryService = deliveryService;
        this.walletRepository = walletRepository;
        this.firebaseDatabase = firebaseDatabase;
    }

    @PostConstruct
    public void init() {
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase chua duoc cau hinh - DriverLocationMonitorService se khong hoat dong.");
        } else {
            log.info("DriverLocationMonitorService da khoi tao. Khoang thoi gian kiem tra: {}ms, Thoi gian cho location: {}ms.",
                    CHECK_INTERVAL_MS, LOCATION_TIMEOUT_MS);
            log.info("Scheduler se chay task kiem tra tai xe lien tuc moi {}ms.", CHECK_INTERVAL_MS);
        }
    }

    @Scheduled(fixedRateString = "${driver.location.check.interval:30000}")
    public void kiemTraTaiXeKhongHoatDong() {
        log.info("[SCHEDULED] Bat dau kiem tra tai xe khong hoat dong...");
        if (firebaseDatabase == null) {
            log.warn("[SCHEDULED] FirebaseDatabase chua duoc cau hinh, bo qua kiem tra location.");
            return;
        }

        DatabaseReference ref = firebaseDatabase.getReference(RDB_ACTIVE_DRIVERS);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    log.debug("Khong co tai xe nao dang online.");
                    return;
                }

                long now = System.currentTimeMillis();
                List<String> driverIdsCanhBao = new ArrayList<>();
                List<String> driverIdsDeactivate = new ArrayList<>();

                for (DataSnapshot driverSnap : snapshot.getChildren()) {
                    String driverId = driverSnap.getKey();
                    Long lastUpdate = driverSnap.child("lastLocationUpdate").getValue(Long.class);
                    Boolean isActive = driverSnap.child("isActive").getValue(Boolean.class);

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
                            snapshot.getChildrenCount(), driverIdsDeactivate.size());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                log.error("Loi khi doc active_drivers tu Realtime Database: {}", error.getMessage());
            }
        });
    }
}
