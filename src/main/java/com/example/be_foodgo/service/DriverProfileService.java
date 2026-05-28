package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.driver.DriverProfileDTO;
import com.example.be_foodgo.dto.driver.DriverUpdateProfileRequest;
import com.example.be_foodgo.dto.driver.DriverUpdateVehicleRequest;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.repository.DriverRepository;
import com.google.cloud.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class DriverProfileService {

    private static final Logger log = LoggerFactory.getLogger(DriverProfileService.class);

    private static final String RDB_ACTIVE_DRIVERS = "active_drivers";

    private final DriverRepository driverRepository;

    public DriverProfileService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public DriverProfileDTO getDriverProfile(String userId) {
        log.info("Bat dau lay ho so tai xe: {}", userId);
        try {
            Map<String, Object> profileData = driverRepository.findDriverProfileById(userId);
            if (profileData == null) {
                log.warn("Khong tim thay ho so tai xe: {}", userId);
                throw BusinessException.taiXeKhongTimThay(userId);
            }

            DriverProfileDTO dto = mapToDriverProfileDTO(userId, profileData);

            Map<String, Object> userData = driverRepository.findUserById(userId);
            if (userData != null) {
                dto.setFullName((String) userData.get("fullName"));
                dto.setPhoneNumber((String) userData.get("phoneNumber"));
                if (dto.getPhotoUrl() == null) {
                    dto.setPhotoUrl((String) userData.get("photoUrl"));
                }
            }

            log.info("Lay ho so tai xe thanh cong: {}", userId);
            return dto;
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay ho so tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi lay ho so tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DriverProfileDTO updateDriverProfile(String userId, DriverUpdateProfileRequest request) {
        log.info("Bat dau cap nhat ho so tai xe: {}", userId);
        try {
            Map<String, Object> profileData = driverRepository.findDriverProfileById(userId);
            if (profileData == null) {
                log.warn("Ho so tai xe chua ton tai: {}", userId);
                throw BusinessException.hoSoTaiXeChuaTonTai(userId);
            }

            Map<String, Object> profileUpdates = buildDriverProfileUpdateMap(request);
            if (!profileUpdates.isEmpty()) {
                profileUpdates.put("updatedAt", Instant.now());
                driverRepository.updateDriverProfileFields(userId, profileUpdates);
            }

            Map<String, Object> userUpdates = new HashMap<>();
            if (request.getFullName() != null) {
                userUpdates.put("fullName", request.getFullName());
            }
            if (request.getPhoneNumber() != null) {
                userUpdates.put("phoneNumber", request.getPhoneNumber());
            }
            if (request.getPhotoUrl() != null) {
                userUpdates.put("photoUrl", request.getPhotoUrl());
            }
            if (!userUpdates.isEmpty()) {
                userUpdates.put("updatedAt", Instant.now());
                driverRepository.updateUserFields(userId, userUpdates);
            }

            log.info("Cap nhat ho so tai xe thanh cong: {}", userId);
            return getDriverProfile(userId);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi cap nhat ho so tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi cap nhat ho so tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DriverProfileDTO updateDriverStatus(String userId, Boolean isActive) {
        log.info("Bat dau cap nhat trang thai tai xe: {}, isActive={}", userId, isActive);
        try {
            Map<String, Object> profileData = driverRepository.findDriverProfileById(userId);
            if (profileData == null) {
                log.warn("Ho so tai xe chua ton tai: {}", userId);
                throw BusinessException.hoSoTaiXeChuaTonTai(userId);
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("isActive", isActive);
            updates.put("isAvailable", isActive);
            updates.put("updatedAt", Instant.now());
            driverRepository.updateDriverProfileFields(userId, updates);

            if (!isActive) {
                xoaKhoiRealtimeDatabase(userId);
            }

            log.info("Cap nhat trang thai tai xe thanh cong: {}", userId);
            return getDriverProfile(userId);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi cap nhat trang thai tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi cap nhat trang thai tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public DriverProfileDTO updateDriverVehicle(String userId, DriverUpdateVehicleRequest request) {
        log.info("Bat dau cap nhat phuong tien tai xe: {}", userId);
        try {
            Map<String, Object> profileData = driverRepository.findDriverProfileById(userId);
            if (profileData == null) {
                log.warn("Ho so tai xe chua ton tai: {}", userId);
                throw BusinessException.hoSoTaiXeChuaTonTai(userId);
            }

            Map<String, Object> profileUpdates = new HashMap<>();
            profileUpdates.put("vehiclePlate", request.getVehiclePlate());
            profileUpdates.put("vehicleType", request.getVehicleType());
            profileUpdates.put("driverLicense", request.getDriverLicense());
            profileUpdates.put("updatedAt", Instant.now());
            driverRepository.updateDriverProfileFields(userId, profileUpdates);

            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("vehiclePlate", request.getVehiclePlate());
            userUpdates.put("updatedAt", Instant.now());
            driverRepository.updateUserFields(userId, userUpdates);

            log.info("Cap nhat phuong tien tai xe thanh cong: {}", userId);
            return getDriverProfile(userId);
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi cap nhat phuong tien tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        } catch (java.util.concurrent.ExecutionException e) {
            log.error("Loi khi cap nhat phuong tien tai xe: {}", e.getMessage());
            throw BusinessException.loiHeThong(e.getMessage());
        }
    }

    public void updateDriverLocation(String driverId, Double lat, Double lng, Double heading, Double speed) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(RDB_ACTIVE_DRIVERS)
                .child(driverId);

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("driverId", driverId);
        locationData.put("lat", lat);
        locationData.put("lng", lng);
        locationData.put("heading", heading != null ? heading : 0.0);
        locationData.put("speed", speed != null ? speed : 0.0);
        locationData.put("updatedAt", System.currentTimeMillis());
        locationData.put("isActive", true);

        ref.setValue(locationData, (error, refIgnored) -> {
            if (error != null) {
                log.warn("Loi ghi location len Realtime Database: {} - {}", driverId, error.getMessage());
            }
        });
    }

    private void xoaKhoiRealtimeDatabase(String driverId) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference(RDB_ACTIVE_DRIVERS)
                    .child(driverId);
            ref.removeValue((error, ref12) -> {
                if (error != null) {
                    log.warn("Loi khi xoa khoi Realtime Database: {}", error.getMessage());
                } else {
                    log.info("Da xoa tai xe {} khoi Realtime Database", driverId);
                }
            });
        } catch (Exception e) {
            log.warn("Khong the xoa khoi Realtime Database: {}", e.getMessage());
        }
    }

    private DriverProfileDTO mapToDriverProfileDTO(String docId, Map<String, Object> data) {
        if (data == null) {
            return DriverProfileDTO.builder().id(docId).build();
        }
        return DriverProfileDTO.builder()
                .id(docId)
                .vehiclePlate((String) data.get("vehiclePlate"))
                .vehicleType((String) data.get("vehicleType"))
                .driverLicense((String) data.get("driverLicense"))
                .isActive((Boolean) data.get("isActive"))
                .isAvailable((Boolean) data.get("isAvailable"))
                .rating(toDouble(data.get("rating")))
                .totalTrips(toLong(data.get("totalTrips")))
                .totalEarnings(toDouble(data.get("totalEarnings")))
                .currentOrderId((String) data.get("currentOrderId"))
                .lat(toDouble(data.get("lat")))
                .lng(toDouble(data.get("lng")))
                .email((String) data.get("email"))
                .photoUrl((String) data.get("photoUrl"))
                .createdAt(toInstant(data.get("createdAt")))
                .updatedAt(toInstant(data.get("updatedAt")))
                .build();
    }

    private Map<String, Object> buildDriverProfileUpdateMap(DriverUpdateProfileRequest request) {
        Map<String, Object> updates = new HashMap<>();
        if (request.getVehiclePlate() != null) updates.put("vehiclePlate", request.getVehiclePlate());
        if (request.getVehicleType() != null) updates.put("vehicleType", request.getVehicleType());
        if (request.getDriverLicense() != null) updates.put("driverLicense", request.getDriverLicense());
        if (request.getPhotoUrl() != null) updates.put("photoUrl", request.getPhotoUrl());
        return updates;
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().toInstant();
        if (value instanceof java.util.Date) return ((java.util.Date) value).toInstant();
        if (value instanceof Long) return Instant.ofEpochMilli((Long) value);
        return null;
    }
}
