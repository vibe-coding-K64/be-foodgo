package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.DeliveryProfileDTO;
import com.example.be_foodgo.dto.DeliveryProfileRequest;
import com.example.be_foodgo.dto.DeliveryStatusRequest;
import com.example.be_foodgo.dto.DeliveryVehicleRequest;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.repository.WalletRepository;
import com.google.cloud.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final WalletRepository walletRepository;

    public DeliveryService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public DeliveryProfileDTO getDriverProfile(String userId) {
        log.info("Bat dau lay ho so tai xe: {}", userId);
        try {
            Map<String, Object> profileData = walletRepository.findDriverProfileById(userId);
            if (profileData == null) {
                log.warn("Khong tim thay ho so tai xe: {}", userId);
                throw BusinessException.taiXeKhongTimThay(userId);
            }

            DeliveryProfileDTO dto = mapToDriverProfileDTO(userId, profileData);

            Map<String, Object> userData = walletRepository.findUserById(userId);
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

    public DeliveryProfileDTO updateDriverProfile(String userId, DeliveryProfileRequest request) {
        log.info("Bat dau cap nhat ho so tai xe: {}", userId);
        try {
            Map<String, Object> profileData = walletRepository.findDriverProfileById(userId);
            if (profileData == null) {
                log.warn("Ho so tai xe chua ton tai: {}", userId);
                throw BusinessException.hoSoTaiXeChuaTonTai(userId);
            }

            Map<String, Object> profileUpdates = buildDriverProfileUpdateMap(request);
            if (!profileUpdates.isEmpty()) {
                profileUpdates.put("updatedAt", Instant.now());
                walletRepository.updateDriverProfileFields(userId, profileUpdates);
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
                walletRepository.updateUserFields(userId, userUpdates);
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

    public DeliveryProfileDTO updateDriverStatus(String userId, Boolean isActive) {
        return updateDriverStatus(userId, isActive, null);
    }

    public DeliveryProfileDTO updateDriverStatus(String userId, Boolean isActive, DeliveryStatusRequest statusRequest) {
        log.info("Bat dau cap nhat trang thai tai xe: {}, isActive={}", userId, isActive);
        try {
            Map<String, Object> profileData = walletRepository.findDriverProfileById(userId);
            if (profileData == null) {
                log.warn("Ho so tai xe chua ton tai: {}", userId);
                throw BusinessException.hoSoTaiXeChuaTonTai(userId);
            }

            if (isActive) {
                log.info("[FIREBASE] updateDriverStatus called with isActive=true for driverId={}, lat={}, lng={}", userId, statusRequest.getLat(), statusRequest.getLng());
                if (statusRequest.getLat() == null || statusRequest.getLng() == null) {
                    log.warn("Tai xe {} bat dau hoat dong nhung khong co vi tri", userId);
                    throw BusinessException.loiDinhVi("Vui lòng gửi vị trí GPS khi bật trạng thái hoạt động.");
                }
                Double lat = statusRequest.getLat();
                Double lng = statusRequest.getLng();
                if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                    throw BusinessException.loiDinhVi("Vị trí GPS không hợp lệ. Vui lòng bật GPS và thử lại.");
                }
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("isActive", isActive);
            updates.put("isAvailable", isActive);
            updates.put("updatedAt", Instant.now());
            walletRepository.updateDriverProfileFields(userId, updates);

            if (!isActive) {
                xoaKhoiDanhSachHoatDong(userId);
            } else {
                log.info("Cap nhat vi tri tai xe hoat dong cho userId={}", userId);
                updateDriverLocation(
                        userId,
                        statusRequest.getLat(),
                        statusRequest.getLng(),
                        statusRequest.getHeading(),
                        statusRequest.getSpeed()
                );
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

    public void deactivateDriverDueToTimeout(String driverId) {
        log.warn("Tai xe {} bi tu dong tat trang thai do khong cap nhat vi tri", driverId);
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("isActive", false);
            updates.put("isAvailable", false);
            updates.put("updatedAt", Instant.now());
            walletRepository.updateDriverProfileFields(driverId, updates);
            xoaKhoiDanhSachHoatDong(driverId);
            log.info("Da tu dong tat trang thai tai xe {} do het thoi gian cap nhat vi tri", driverId);
        } catch (Exception e) {
            log.error("Loi khi tu dong tat trang thai tai xe {}: {}", driverId, e.getMessage());
        }
    }

    public DeliveryProfileDTO updateDriverVehicle(String userId, DeliveryVehicleRequest request) {
        log.info("Bat dau cap nhat phuong tien tai xe: {}", userId);
        try {
            Map<String, Object> profileData = walletRepository.findDriverProfileById(userId);
            if (profileData == null) {
                log.warn("Ho so tai xe chua ton tai: {}", userId);
                throw BusinessException.hoSoTaiXeChuaTonTai(userId);
            }

            Map<String, Object> profileUpdates = new HashMap<>();
            profileUpdates.put("vehiclePlate", request.getVehiclePlate());
            profileUpdates.put("vehicleType", request.getVehicleType());
            profileUpdates.put("driverLicense", request.getDriverLicense());
            profileUpdates.put("updatedAt", Instant.now());
            walletRepository.updateDriverProfileFields(userId, profileUpdates);

            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("vehiclePlate", request.getVehiclePlate());
            userUpdates.put("updatedAt", Instant.now());
            walletRepository.updateUserFields(userId, userUpdates);

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
        Map<String, Object> locationUpdates = new HashMap<>();
        locationUpdates.put("lat", lat);
        locationUpdates.put("lng", lng);
        locationUpdates.put("heading", heading != null ? heading : 0.0);
        locationUpdates.put("speed", speed != null ? speed : 0.0);
        locationUpdates.put("lastLocationUpdate", System.currentTimeMillis());
        locationUpdates.put("isActive", true);
        locationUpdates.put("isAvailable", true);
        locationUpdates.put("updatedAt", Instant.now());

        try {
            walletRepository.updateDriverProfileFields(driverId, locationUpdates);
            log.info("Da cap nhat location tai xe {} trong Firestore - lat={}, lng={}", driverId, lat, lng);
        } catch (Exception e) {
            log.error("Loi khi cap nhat location tai xe {} trong Firestore: {}", driverId, e.getMessage(), e);
        }
    }

    private void xoaKhoiDanhSachHoatDong(String driverId) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("heading", null);
            updates.put("speed", null);
            updates.put("lastLocationUpdate", null);
            updates.put("lat", null);
            updates.put("lng", null);
            updates.put("updatedAt", Instant.now());
            walletRepository.updateDriverProfileFields(driverId, updates);
            log.info("Da xoa location realtime cua tai xe {} khoi Firestore", driverId);
        } catch (Exception e) {
            log.warn("Khong the xoa location realtime cua tai xe {}: {}", driverId, e.getMessage());
        }
    }

    private DeliveryProfileDTO mapToDriverProfileDTO(String docId, Map<String, Object> data) {
        if (data == null) {
            return DeliveryProfileDTO.builder().id(docId).build();
        }
        return DeliveryProfileDTO.builder()
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

    private Map<String, Object> buildDriverProfileUpdateMap(DeliveryProfileRequest request) {
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
