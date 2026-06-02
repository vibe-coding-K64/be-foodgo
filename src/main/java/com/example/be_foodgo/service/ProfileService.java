package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.ChangePasswordRequest;
import com.example.be_foodgo.dto.UpdateProfileMultipartRequest;
import com.example.be_foodgo.dto.UpdateProfileRequest;
import com.example.be_foodgo.dto.UserResponse;
import com.example.be_foodgo.model.User;
import com.example.be_foodgo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public ProfileService(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }

    public UserResponse updateProfile(String userId, UpdateProfileRequest request) throws Exception {
        log.info("Bat dau cap nhat ho so cho userId: {}", userId);

        User user = userRepository.timTheoId(userId);
        if (user == null) {
            log.warn("Khong tim thay tai khoan voi userId: {}", userId);
            throw new IllegalArgumentException("Khong tim thay tai khoan voi ID: " + userId);
        }

        String newAvatarUrl = request.getAvatarUrl();
        String oldAvatarUrl = user.getPhotoUrl();

        if (newAvatarUrl != null && !newAvatarUrl.isBlank()
                && !newAvatarUrl.equals(oldAvatarUrl)) {
            if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()
                    && oldAvatarUrl.contains("cloudinary.com")) {
                cloudinaryService.deleteAvatar(oldAvatarUrl);
            }
        }

        userRepository.capNhatThongTinHoSo(userId, request.getFullName(), newAvatarUrl);
        log.info("Cap nhat ho so thanh cong cho userId: {}", userId);

        User userCapNhat = userRepository.timTheoId(userId);
        return mapToUserResponse(userCapNhat);
    }

    public UserResponse updateProfileMultipart(String userId,
                                               UpdateProfileMultipartRequest request,
                                               MultipartFile avatarFile) throws Exception {
        log.info("Bat dau cap nhat ho so (multipart) cho userId: {}", userId);

        User user = userRepository.timTheoId(userId);
        if (user == null) {
            log.warn("Khong tim thay tai khoan voi userId: {}", userId);
            throw new IllegalArgumentException("Khong tim thay tai khoan voi ID: " + userId);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Mat khau xac thuc khong dung cho userId: {}", userId);
            throw new IllegalArgumentException("Mat khau xac thuc khong dung.");
        }

        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.isBlank()) {
            String normalizedEmail = newEmail.trim().toLowerCase();
            if (!normalizedEmail.equalsIgnoreCase(user.getEmail())) {
                boolean tonTai = userRepository.tonTaiEmail(normalizedEmail);
                if (tonTai) {
                    log.warn("Email da ton tai: {}", normalizedEmail);
                    throw new IllegalArgumentException("Email da duoc su dung boi tai khoan khac.");
                }
            }
        }

        String newPhotoUrl = null;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            newPhotoUrl = cloudinaryService.uploadAvatar(avatarFile, userId);
            String oldPhotoUrl = user.getPhotoUrl();
            if (oldPhotoUrl != null && !oldPhotoUrl.isBlank()) {
                cloudinaryService.deleteAvatar(oldPhotoUrl);
            }
        }

        String fullName = request.getFullName();
        userRepository.capNhatHoSoDayDu(userId, fullName, newEmail, newPhotoUrl);
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

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .photoUrl(user.getPhotoUrl())
                .roles(user.getRoles())
                .build();
    }
}
