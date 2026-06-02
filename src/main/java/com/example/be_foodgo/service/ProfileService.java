package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.ChangePasswordRequest;
import com.example.be_foodgo.dto.UpdateProfileRequest;
import com.example.be_foodgo.dto.UserResponse;
import com.example.be_foodgo.model.User;
import com.example.be_foodgo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse updateProfile(String userId, UpdateProfileRequest request) throws Exception {
        log.info("Bat dau cap nhat ho so cho userId: {}", userId);

        User user = userRepository.timTheoId(userId);
        if (user == null) {
            log.warn("Khong tim thay tai khoan voi userId: {}", userId);
            throw new IllegalArgumentException("Khong tim thay tai khoan voi ID: " + userId);
        }

        userRepository.capNhatThongTinHoSo(userId, request.getFullName(), request.getAvatarUrl());
        log.info("Cap nhat ho so thanh cong cho userId: {}", userId);

        User userCapNhat = userRepository.timTheoId(userId);
        return mapToUserResponse(userCapNhat);
    }

    public UserResponse getProfile(String userId) throws Exception {
        log.info("Bat dau lay ho so cho userId: {}", userId);

        User user = userRepository.timTheoId(userId);
        if (user == null) {
            log.warn("Khong tim thay tai khoan voi userId: {}", userId);
            throw new IllegalArgumentException("Khong tim thay tai khoan voi ID: " + userId);
        }

        log.info("Lay ho so thanh cong cho userId: {}", userId);
        return mapToUserResponse(user);
    }

    public void changePassword(String userId, ChangePasswordRequest request) throws Exception {
        log.info("Bat dau doi mat khau cho userId: {}", userId);

        User user = userRepository.timTheoId(userId);
        if (user == null) {
            log.warn("Khong tim thay tai khoan voi userId: {}", userId);
            throw new IllegalArgumentException("Khong tim thay tai khoan voi ID: " + userId);
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Mat khau cu khong dung cho userId: {}", userId);
            throw new IllegalArgumentException("Mat khau cu khong dung.");
        }

        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        userRepository.capNhatPassword(userId, hashedPassword);
        log.info("Doi mat khau thanh cong cho userId: {}", userId);
    }

    public java.util.List<UserResponse> timTatCaUsers(Integer role) throws Exception {
        log.info("Admin lay danh sach nguoi dung, filter role={}", role);
        java.util.List<com.example.be_foodgo.model.User> users = userRepository.timTatCa(role);
        java.util.List<UserResponse> result = new java.util.ArrayList<>();
        for (com.example.be_foodgo.model.User u : users) {
            result.add(mapToUserResponse(u));
        }
        return result;
    }

    public boolean toggleUserActive(String userId) throws Exception {
        log.info("Admin toggle trang thai active cho userId: {}", userId);
        com.example.be_foodgo.model.User user = userRepository.timTheoId(userId);
        if (user == null) {
            throw new IllegalArgumentException("Khong tim thay nguoi dung voi ID: " + userId);
        }
        boolean currentActive = user.getIsActive() != null ? user.getIsActive() : true;
        boolean newActive = !currentActive;
        userRepository.updateActiveStatus(userId, newActive);
        log.info("Da thay doi active tu {} sang {} cho userId {}", currentActive, newActive, userId);
        return newActive;
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .photoUrl(user.getPhotoUrl())
                .roles(user.getRoles())
                .isActive(user.getIsActive() != null ? user.getIsActive() : true)
                .build();
    }
}
