package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.*;
import com.example.be_foodgo.model.User;
import com.example.be_foodgo.repository.UserRepository;
import com.example.be_foodgo.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_SECONDS = 300L;
    private static final long TEMP_TOKEN_TTL_MS = 300000L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse register(RegisterRequest request) throws Exception {
        log.info("Bat dau dang ky tai khoan moi - Email: {}", request.getEmail());

        if (userRepository.tonTaiEmail(request.getEmail())) {
            log.warn("Email da ton tai: {}", request.getEmail());
            throw new IllegalArgumentException("Email da ton tai trong he thong. Vui long su dung email khac.");
        }

        if (userRepository.tonTaiPhoneNumber(request.getPhoneNumber())) {
            log.warn("So dien thoai da ton tai: {}", request.getPhoneNumber());
            throw new IllegalArgumentException("So dien thoai da duoc su dung. Vui long su dung so dien thoai khac.");
        }

        String newId = userRepository.sinhNextUserId();
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .id(newId)
                .email(request.getEmail())
                .password(hashedPassword)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .photoUrl(null)
                .roles(List.of(1))
                .createdAt(Instant.now().toString())
                .build();

        userRepository.taoUser(user);
        log.info("Dang ky tai khoan thanh cong - UserId: {}, Roles: {}", newId, user.getRoles());

        String token = jwtTokenProvider.taoToken(newId);
        UserResponse userResponse = mapToUserResponse(user);

        return AuthResponse.of(token, jwtTokenProvider.getExpirationMs(), userResponse);
    }

    public AuthResponse login(LoginRequest request) throws Exception {
        log.info("Bat dau dang nhap - Email: {}", request.getEmail());

        User user = userRepository.timTheoEmail(request.getEmail());
        if (user == null) {
            log.warn("Khong tim thay tai khoan voi email: {}", request.getEmail());
            throw new IllegalArgumentException("Email hoac mat khau khong dung.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Mat khau khong dung cho email: {}", request.getEmail());
            throw new IllegalArgumentException("Email hoac mat khau khong dung.");
        }

        log.info("Dang nhap thanh cong - UserId: {}", user.getId());
        String token = jwtTokenProvider.taoToken(user.getId());
        UserResponse userResponse = mapToUserResponse(user);

        return AuthResponse.of(token, jwtTokenProvider.getExpirationMs(), userResponse);
    }

    public OtpSendResponse guiOtp(OtpSendRequest request) throws Exception {
        String emailOrPhone = request.getEmailOrPhone().trim();
        log.info("Bat dau gui ma OTP den: {}", emailOrPhone);

        String timKiem = emailOrPhone.contains("@") ? emailOrPhone : emailOrPhone;
        User user = emailOrPhone.contains("@")
                ? userRepository.timTheoEmail(emailOrPhone)
                : null;

        if (user == null) {
            boolean phoneExists = !emailOrPhone.contains("@")
                    && userRepository.tonTaiPhoneNumber(emailOrPhone);
            if (!phoneExists) {
                log.warn("Khong tim thay tai khoan voi email hoac so dien thoai: {}", emailOrPhone);
                throw new IllegalArgumentException("Khong tim thay tai khoan voi email hoac so dien thoai nay.");
            }
        }

        String otp = sinhMaOtp();
        String userId = (user != null) ? user.getId() : ("phone:" + emailOrPhone);

        otpStore.put(emailOrPhone, new OtpEntry(otp, userId, System.currentTimeMillis() + OTP_TTL_SECONDS * 1000));
        log.info("Ma OTP cho {}: {} (hieu luc {} giay)", emailOrPhone, otp, OTP_TTL_SECONDS);
        System.out.println("========== [DEV MODE] MA OTP ==========");
        System.out.println("Den: " + emailOrPhone);
        System.out.println("Ma OTP: " + otp);
        System.out.println("Het han sau: " + OTP_TTL_SECONDS + " giay");
        System.out.println("======================================");

        return OtpSendResponse.builder()
                .emailOrPhone(emailOrPhone)
                .message("Ma OTP da duoc gui. Vui long kiem tra email/so dien thoai.")
                .otpCode(otp)
                .expiresInSeconds((int) OTP_TTL_SECONDS)
                .build();
    }

    public OtpVerifyResponse xacThucOtp(OtpVerifyRequest request) throws Exception {
        String emailOrPhone = request.getEmailOrPhone().trim();
        String otpCode = request.getOtpCode().trim();
        log.info("Bat dau xac thuc OTP cho: {}", emailOrPhone);

        OtpEntry entry = otpStore.get(emailOrPhone);
        if (entry == null) {
            log.warn("Khong co ma OTP nao duoc gui toi: {}", emailOrPhone);
            throw new IllegalArgumentException("Ma OTP khong hop le hoac da het han. Vui long gui lai ma OTP.");
        }

        if (System.currentTimeMillis() > entry.expiresAtMs) {
            log.warn("Ma OTP da het han cho: {}", emailOrPhone);
            otpStore.remove(emailOrPhone);
            throw new IllegalArgumentException("Ma OTP da het han. Vui long gui lai ma OTP.");
        }

        if (!entry.otp.equals(otpCode)) {
            log.warn("Ma OTP khong dung cho: {}", emailOrPhone);
            throw new IllegalArgumentException("Ma OTP khong dung. Vui long thu lai.");
        }

        String userId = entry.userId;
        if (userId.startsWith("phone:")) {
            String phone = userId.substring(6);
            User userByPhone = userRepository.timTheoEmail(phone);
            if (userByPhone != null) {
                userId = userByPhone.getId();
            } else {
                List<User> allUsers = null;
                log.warn("Khong tim thay tai khoan theo so dien thoai: {}", phone);
                throw new IllegalArgumentException("Khong tim thay tai khoan theo so dien thoai nay.");
            }
        }

        String tempToken = jwtTokenProvider.taoTempToken(userId, TEMP_TOKEN_TTL_MS);
        otpStore.remove(emailOrPhone);
        log.info("Xac thuc OTP thanh cong - UserId: {}", userId);

        return OtpVerifyResponse.of(tempToken, TEMP_TOKEN_TTL_MS);
    }

    public void datLaiMatKhau(ResetPasswordRequest request) throws Exception {
        String tempToken = request.getTempToken();
        String newPassword = request.getNewPassword();
        log.info("Bat dau dat lai mat khau voi token tam thoi");

        if (!jwtTokenProvider.xacThucToken(tempToken)) {
            log.warn("Token tam thoi khong hop le hoac da het han");
            throw new IllegalArgumentException("Token khong hop le hoac da het han. Vui long gui lai ma OTP.");
        }

        String userId = jwtTokenProvider.layUserIdTuToken(tempToken);
        if (userId == null) {
            log.warn("Khong the trich xuat userId tu token tam thoi");
            throw new IllegalArgumentException("Token khong hop le. Vui long gui lai ma OTP.");
        }

        User user = userRepository.timTheoId(userId);
        if (user == null) {
            log.warn("Khong tim thay tai khoan voi userId: {}", userId);
            throw new IllegalArgumentException("Tai khoan khong ton tai.");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        userRepository.capNhatPassword(userId, hashedPassword);
        log.info("Dat lai mat khau thanh cong - UserId: {}", userId);
    }

    public Map<String, Object> registerMerchant(AuthRequestDTO request) throws Exception {
        log.info("Bat dau dang ky tai khoan nguoi ban - Email: {}", request.getEmail());

        if (userRepository.tonTaiEmail(request.getEmail())) {
            log.warn("Email da ton tai: {}", request.getEmail());
            throw new IllegalArgumentException("Email da ton tai trong he thong. Vui long su dung email khac.");
        }

        if (userRepository.tonTaiPhoneNumber(request.getPhoneNumber())) {
            log.warn("So dien thoai da ton tai: {}", request.getPhoneNumber());
            throw new IllegalArgumentException("So dien thoai da duoc su dung. Vui long su dung so dien thoai khac.");
        }

        String newId = userRepository.sinhNextUserId();
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .id(newId)
                .email(request.getEmail())
                .password(hashedPassword)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .photoUrl(null)
                .roles(List.of(3))
                .createdAt(Instant.now().toString())
                .build();

        userRepository.taoUser(user);
        log.info("Dang ky tai khoan nguoi ban thanh cong - UserId: {}, Roles: {}", newId, user.getRoles());

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Dang ky tai khoan nguoi ban thanh cong");
        result.put("uid", newId);
        return result;
    }

    public Map<String, Object> checkMerchantProfile(String uid) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("isMerchant", false);
        result.put("storeId", null);

        User user = userRepository.timTheoId(uid);
        if (user != null && user.getRoles() != null && user.getRoles().contains(3)) {
            result.put("isMerchant", true);
            result.put("fullName", user.getFullName());
            result.put("email", user.getEmail());
            result.put("phoneNumber", user.getPhoneNumber());
            result.put("photoUrl", user.getPhotoUrl());
            log.info("Nguoi dung {} co quyen nguoi ban", uid);
            
            try {
                com.google.cloud.firestore.Firestore db = com.google.firebase.cloud.FirestoreClient.getFirestore();
                com.google.cloud.firestore.DocumentSnapshot doc = db.collection("merchant_profiles").document(uid).get().get();
                if (doc.exists()) {
                    if (doc.contains("storeIds")) {
                        java.util.List<String> storeIds = (java.util.List<String>) doc.get("storeIds");
                        if (storeIds != null && !storeIds.isEmpty()) {
                            result.put("storeId", storeIds.get(0));
                        }
                    }
                    if (doc.contains("taxCode")) {
                        result.put("taxCode", doc.getString("taxCode"));
                    }
                }
            } catch (Exception e) {
                log.error("Lỗi lấy storeId cho merchant {}: {}", uid, e.getMessage());
            }
        }
        return result;
    }

    public String layUserIdHienTai(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(jwtTokenProvider.getBearerPrefix() + " ")) {
            return null;
        }
        String token = jwtTokenProvider.layTokenTuHeader(authHeader);
        if (token == null) {
            return null;
        }
        return jwtTokenProvider.layUserIdTuToken(token);
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

    private String sinhMaOtp() {
        Random random = new Random();
        int otp = random.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }

    private static class OtpEntry {
        String otp;
        String userId;
        long expiresAtMs;

        OtpEntry(String otp, String userId, long expiresAtMs) {
            this.otp = otp;
            this.userId = userId;
            this.expiresAtMs = expiresAtMs;
        }
    }
}
