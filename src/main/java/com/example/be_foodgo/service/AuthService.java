package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.*;
import com.example.be_foodgo.exception.TokenInvalidException;
import com.example.be_foodgo.model.RefreshToken;
import com.example.be_foodgo.model.User;
import com.example.be_foodgo.repository.UserRepository;
import com.example.be_foodgo.security.JwtTokenProvider;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
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
    private static final long RESEND_COOLDOWN_SECONDS = 60L;

    private final Map<String, Long> lastOtpSentAt = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final Firestore firestore;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final StoreService storeService;

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();
    private final Map<String, PendingDriverRegistration> pendingDriverRegistrations = new ConcurrentHashMap<>();

    public static class PendingRegistration {
        String email;
        String password;
        String fullName;
        String phoneNumber;
        Instant createdAt;

        PendingRegistration(String email, String password, String fullName, String phoneNumber) {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.createdAt = Instant.now();
        }
    }

    public static class PendingDriverRegistration {
        String email;
        String password;
        String fullName;
        String phoneNumber;
        Instant createdAt;

        PendingDriverRegistration(String email, String password, String fullName,
                String phoneNumber) {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.createdAt = Instant.now();
        }
    }

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       Firestore firestore,
                       RefreshTokenService refreshTokenService,
                       EmailService emailService,
                       StoreService storeService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.firestore = firestore;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
        this.storeService = storeService;
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

        if (Boolean.FALSE.equals(user.getIsEmailVerified())) {
            log.warn("Email chua duoc xac thuc cho: {}", request.getEmail());
            throw new IllegalArgumentException("Email chua duoc xac thuc. Vui long xac thuc email truoc khi dang nhap.");
        }

        log.info("Dang nhap thanh cong - UserId: {}", user.getId());
        String token = jwtTokenProvider.taoToken(user.getId());
        RefreshToken refreshToken = refreshTokenService.taoRefreshToken(user.getId(), null);
        UserResponse userResponse = mapToUserResponse(user);

        return AuthResponse.of(token, jwtTokenProvider.getExpirationMs(),
                refreshToken.getToken(), refreshTokenService.getRefreshExpirationMs(), userResponse);
    }

    public OtpSendResponse guiOtp(OtpSendRequest request) throws Exception {
        String emailOrPhone = request.getEmailOrPhone().trim();
        kiemTraCooldown(emailOrPhone);
        log.info("Bat dau gui ma OTP den: {}", emailOrPhone);

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
        capNhatThoiGianGui(emailOrPhone);
        log.info("Ma OTP cho {}: {} (hieu luc {} giay)", emailOrPhone, otp, OTP_TTL_SECONDS);
        System.out.println("========== [DEV MODE] MA OTP ==========");
        System.out.println("Den: " + emailOrPhone);
        System.out.println("Ma OTP: " + otp);
        System.out.println("Het han sau: " + OTP_TTL_SECONDS + " giay");
        System.out.println("======================================");

        if (emailOrPhone.contains("@")) {
            emailService.guiEmailQuenMatKhau(emailOrPhone, otp);
        }

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
                log.warn("Khong tim thay tai khoan theo so dien thoai: {}", phone);
                throw new IllegalArgumentException("Khong tim thay tai khoan theo so dien thoai nay.");
            }
        }

        String tempToken = jwtTokenProvider.taoTempToken(userId, TEMP_TOKEN_TTL_MS);
        otpStore.remove(emailOrPhone);
        log.info("Xac thuc OTP thanh cong - UserId: {}", userId);

        return OtpVerifyResponse.of(tempToken, TEMP_TOKEN_TTL_MS);
    }

    public OtpSendResponse guiOtpXacThucEmail(String email) throws Exception {
        log.info("Bat dau gui OTP xac thuc email: {}", email);

        User user = userRepository.timTheoEmail(email);
        if (user == null) {
            log.warn("Khong tim thay tai khoan voi email: {}", email);
            throw new IllegalArgumentException("Khong tim thay tai khoan voi email nay.");
        }

        if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
            log.warn("Email da duoc xac thuc: {}", email);
            throw new IllegalArgumentException("Email nay da duoc xac thuc.");
        }

        String otp = sinhMaOtp();
        otpStore.put("verify:" + email, new OtpEntry(otp, user.getId(), System.currentTimeMillis() + OTP_TTL_SECONDS * 1000));
        log.info("Ma OTP xac thuc email cho {}: {} (hieu luc {} giay)", email, otp, OTP_TTL_SECONDS);
        System.out.println("========== [DEV MODE] OTP XAC THUC EMAIL ==========");
        System.out.println("Den: " + email);
        System.out.println("Ma OTP: " + otp);
        System.out.println("Het han sau: " + OTP_TTL_SECONDS + " giay");
        System.out.println("===================================================");

        emailService.guiEmailXacThuc(email, otp);

        return OtpSendResponse.builder()
                .emailOrPhone(email)
                .message("Ma OTP xac thuc email da duoc gui. Vui long kiem tra email.")
                .otpCode(otp)
                .expiresInSeconds((int) OTP_TTL_SECONDS)
                .build();
    }

    public void xacThucEmail(String email, String otpCode) throws Exception {
        String key = "verify:" + email;
        log.info("Bat dau xac thuc email: {}", email);

        OtpEntry entry = otpStore.get(key);
        if (entry == null) {
            log.warn("Khong co ma OTP xac thuc email cho: {}", email);
            throw new IllegalArgumentException("Ma OTP khong hop le hoac da het han. Vui long gui lai ma OTP.");
        }

        if (System.currentTimeMillis() > entry.expiresAtMs) {
            log.warn("Ma OTP xac thuc email da het han cho: {}", email);
            otpStore.remove(key);
            throw new IllegalArgumentException("Ma OTP da het han. Vui long gui lai ma OTP.");
        }

        if (!entry.otp.equals(otpCode)) {
            log.warn("Ma OTP xac thuc email khong dung cho: {}", email);
            throw new IllegalArgumentException("Ma OTP khong dung. Vui long thu lai.");
        }

        String userId = entry.userId;
        userRepository.xacThucEmail(userId);
        otpStore.remove(key);
        log.info("Xac thuc email thanh cong - UserId: {}", userId);
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

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws Exception {
        String refreshTokenValue = request.getRefreshToken();
        log.info("Bat dau lam moi access token");

        RefreshToken oldRefreshToken = refreshTokenService.xacThucRefreshToken(refreshTokenValue);
        if (oldRefreshToken == null) {
            log.warn("Refresh token khong hop le hoac da bi thu hoi");
            throw new TokenInvalidException("Refresh token khong hop le hoac da bi thu hoi.");
        }

        String userId = oldRefreshToken.getUserId();
        User user = userRepository.timTheoId(userId);
        if (user == null) {
            log.warn("Khong tim thay tai khoan voi userId: {}", userId);
            throw new TokenInvalidException("Tai khoan khong ton tai.");
        }

        String newAccessToken = jwtTokenProvider.taoToken(userId);

        refreshTokenService.thuHoiRefreshToken(refreshTokenValue);
        refreshTokenService.taoRefreshToken(userId, oldRefreshToken.getDeviceInfo());
        String newRefreshTokenValue = jwtTokenProvider.taoRefreshTokenValue(userId);

        log.info("Lam moi access token thanh cong cho userId: {}", userId);
        return RefreshTokenResponse.of(newAccessToken, jwtTokenProvider.getExpirationMs(),
                newRefreshTokenValue, refreshTokenService.getRefreshExpirationMs());
    }

    public void logout(String authHeader, String refreshToken) throws Exception {
        if (authHeader != null && authHeader.startsWith(jwtTokenProvider.getBearerPrefix() + " ")) {
            String token = jwtTokenProvider.layTokenTuHeader(authHeader);
            if (token != null) {
                String userId = jwtTokenProvider.layUserIdTuToken(token);
                if (userId != null) {
                    refreshTokenService.thuHoiTatCaRefreshTokenCuaUser(userId);
                    log.info("Logout thanh cong - da thu hoi tat ca refresh tokens cua userId: {}", userId);
                }
            }
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                refreshTokenService.thuHoiRefreshToken(refreshToken);
            } catch (Exception e) {
                log.warn("Khong the thu hoi refresh token khi logout: {}", e.getMessage());
            }
        }
    }

    public void updateFcmToken(String userId, FCMTokenRequest request) throws Exception {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Khong xac dinh duoc tai khoan can cap nhat FCM token.");
        }
        if (request == null || request.getFcmToken() == null || request.getFcmToken().isBlank()) {
            throw new IllegalArgumentException("fcmToken khong duoc de trong.");
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", request.getFcmToken().trim());
        updates.put("updatedAt", Instant.now());

        firestore.collection("driver_profiles")
                .document(userId)
                .set(updates, SetOptions.merge())
                .get();

        log.info("Cap nhat FCM token thanh cong cho tai xe: {}", userId);
    }

    public Map<String, Object> registerMerchant(AuthRequestDTO request) throws Exception {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String normalizedPhoneNumber = request.getPhoneNumber().trim();
        log.info("Bat dau dang ky tai khoan nguoi ban - Email: {}", normalizedEmail);

        User existingUser = userRepository.timTheoEmail(normalizedEmail);
        if (existingUser != null && existingUser.getRoles() != null && existingUser.getRoles().contains(3)) {
            log.warn("Email da ton tai voi role merchant: {}", normalizedEmail);
            throw new IllegalArgumentException("Email da ton tai va da co vai tro nguoi ban trong he thong.");
        }

        if (existingUser == null && userRepository.tonTaiPhoneNumber(normalizedPhoneNumber)) {
            log.warn("So dien thoai da ton tai: {}", normalizedPhoneNumber);
            throw new IllegalArgumentException("So dien thoai da duoc su dung. Vui long su dung so dien thoai khac.");
        }

        if (existingUser != null) {
            List<Integer> updatedRoles = userRepository.themRoleNeuChuaCo(existingUser.getId(), 3);

            StoreDTO defaultStore = new StoreDTO();
            defaultStore.setName("Gian hàng của " + existingUser.getFullName());
            defaultStore.setAddress("Chưa cập nhật địa chỉ");
            defaultStore.setAvtUrl("https://placehold.co/150x150/FF6B35/FFFFFF?text=Store");
            defaultStore.setBackUrl("https://placehold.co/800x400/FF6B35/FFFFFF?text=Cover");
            defaultStore.setDeliveryTime("20-30 phút");
            defaultStore.setDeliveryFee(15000.0);
            defaultStore.setIsOpen(false);
            storeService.createMerchantStore(existingUser.getId(), defaultStore);

            log.info("Them role merchant cho user hien co thanh cong - UserId: {}, Roles: {}", existingUser.getId(), updatedRoles);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Da them vai tro nguoi ban cho tai khoan hien co va tao gian hang thanh cong");
            result.put("uid", existingUser.getId());
            result.put("roles", updatedRoles);
            return result;
        }

        // Tao tai khoan tren Firebase Authentication de ho tro dang nhap bang Firebase
        String firebaseUid = null;
        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(normalizedEmail)
                    .setPassword(request.getPassword())
                    .setDisplayName(request.getFullName())
                    .setEmailVerified(false);
            UserRecord userRecord = firebaseAuth.createUser(createRequest);
            firebaseUid = userRecord.getUid();
            log.info("Tao tai khoan Firebase Auth thanh cong - UID: {}", firebaseUid);
        } catch (FirebaseAuthException e) {
            log.error("Loi tao tai khoan Firebase Auth: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("EMAIL_EXISTS")) {
                throw new IllegalArgumentException("Email nay da duoc dang ky trong Firebase. Vui long su dung email khac hoac dang nhap.");
            }
            // Neu Firebase Auth khong kha dung, van tao tai khoan noi bo
            log.warn("Tiep tuc tao tai khoan noi bo du khong tao duoc Firebase Auth");
        }

        String newId = (firebaseUid != null) ? firebaseUid
                : (request.getFirebaseUid() != null && !request.getFirebaseUid().isBlank())
                        ? request.getFirebaseUid()
                        : userRepository.sinhNextUserId();
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .id(newId)
                .email(normalizedEmail)
                .password(hashedPassword)
                .fullName(request.getFullName())
                .phoneNumber(normalizedPhoneNumber)
                .photoUrl(null)
                .roles(List.of(3))
                .createdAt(Instant.now().toString())
                .build();

        userRepository.taoUser(user);
        
        // Tự động tạo gian hàng mặc định cho người bán
        StoreDTO defaultStore = new StoreDTO();
        defaultStore.setName("Gian hàng của " + request.getFullName());
        defaultStore.setAddress("Chưa cập nhật địa chỉ");
        defaultStore.setAvtUrl("https://placehold.co/150x150/FF6B35/FFFFFF?text=Store");
        defaultStore.setBackUrl("https://placehold.co/800x400/FF6B35/FFFFFF?text=Cover");
        defaultStore.setDeliveryTime("20-30 phút");
        defaultStore.setDeliveryFee(15000.0);
        defaultStore.setIsOpen(false);
        storeService.createMerchantStore(newId, defaultStore);

        log.info("Dang ky tai khoan nguoi ban va tao gian hang thanh cong - UserId: {}, Roles: {}", newId, user.getRoles());

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Dang ky tai khoan nguoi ban va tao gian hang thanh cong");
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
                com.google.cloud.firestore.DocumentSnapshot doc = firestore.collection("merchant_profiles").document(uid).get().get();
                if (doc.exists()) {
                    if (doc.contains("storeIds")) {
                        @SuppressWarnings("unchecked")
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

    public UserResponse getCurrentUser(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith(jwtTokenProvider.getBearerPrefix() + " ")) {
            throw new IllegalArgumentException("Token khong hop le.");
        }
        String token = jwtTokenProvider.layTokenTuHeader(authHeader);
        if (token == null || !jwtTokenProvider.xacThucToken(token)) {
            throw new IllegalArgumentException("Token da het han hoac khong hop le.");
        }
        String userId = jwtTokenProvider.layUserIdTuToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Token khong hop le.");
        }
        User user = userRepository.timTheoId(userId);
        if (user == null) {
            throw new IllegalArgumentException("Tai khoan khong ton tai.");
        }
        log.info("Lay thong tin nguoi dung hien tai - UserId: {}", userId);
        return mapToUserResponse(user);
    }

    public Map<String, Object> checkAdminProfile(String uid) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("isAdmin", false);

        User user = userRepository.timTheoId(uid);
        if (user != null && user.getRoles() != null && user.getRoles().contains(4)) {
            result.put("isAdmin", true);
            result.put("fullName", user.getFullName());
            result.put("email", user.getEmail());
            result.put("phoneNumber", user.getPhoneNumber());
            result.put("photoUrl", user.getPhotoUrl());
            log.info("Nguoi dung {} co quyen Admin", uid);
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

    public FirebaseAuthResponse linkFirebaseToken(String idToken, String email, String password) throws Exception {
        String firebaseUid;
        if (idToken != null && !idToken.isBlank()) {
            firebaseUid = layUidTuFirebaseToken(idToken);
            if (firebaseUid == null) {
                throw new IllegalArgumentException("Firebase ID token khong hop le.");
            }
        } else {
            throw new IllegalArgumentException("Firebase ID token la bat buoc.");
        }

        User user = userRepository.timTheoId("firebase:" + firebaseUid);
        if (user != null) {
            String token = jwtTokenProvider.taoToken(user.getId());
            RefreshToken refreshToken = refreshTokenService.taoRefreshToken(user.getId(), null);
            UserResponse userResponse = mapToUserResponse(user);
            log.info("Dang nhap Firebase thanh cong - Firebase UID: {}, Internal ID: {}", firebaseUid, user.getId());
            return FirebaseAuthResponse.of(token, jwtTokenProvider.getExpirationMs(),
                    refreshToken.getToken(), refreshTokenService.getRefreshExpirationMs(), userResponse);
        }

        if (email == null && password == null) {
            FirebaseToken decoded = giaiMaFirebaseToken(idToken);
            email = (decoded != null) ? decoded.getEmail() : null;
        }

        String newId = userRepository.sinhNextUserId();
        String hashedPassword = passwordEncoder.encode("firebase:" + firebaseUid);

        User userNew = User.builder()
                .id("firebase:" + firebaseUid)
                .email(email)
                .password(hashedPassword)
                .fullName(email)
                .phoneNumber(null)
                .photoUrl(null)
                .roles(List.of(1))
                .createdAt(Instant.now().toString())
                .build();

        if (!newId.equals("firebase:" + firebaseUid)) {
            userNew.setId(newId);
        }

        userRepository.taoUser(userNew);
        log.info("Tao tai khoan moi tu Firebase - Firebase UID: {}, Internal ID: {}", firebaseUid, userNew.getId());

        String token = jwtTokenProvider.taoToken(userNew.getId());
        RefreshToken refreshToken = refreshTokenService.taoRefreshToken(userNew.getId(), null);
        UserResponse userResponse = mapToUserResponse(userNew);

        return FirebaseAuthResponse.of(token, jwtTokenProvider.getExpirationMs(),
                refreshToken.getToken(), refreshTokenService.getRefreshExpirationMs(), userResponse);
    }

    private String layUidTuFirebaseToken(String idToken) {
        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            return decodedToken.getUid();
        } catch (FirebaseAuthException e) {
            log.warn("Firebase token khong hop le: {} - {}", e.getErrorCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Loi khi xac thuc Firebase token: {}", e.getMessage());
            return null;
        }
    }

    private FirebaseToken giaiMaFirebaseToken(String idToken) {
        try {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            log.warn("Firebase token khong hop le: {} - {}", e.getErrorCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Loi khi giai ma Firebase token: {}", e.getMessage());
            return null;
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .photoUrl(user.getPhotoUrl())
                .roles(user.getRoles())
                .isEmailVerified(user.getIsEmailVerified())
                .build();
    }

    private String sinhMaOtp() {
        Random random = new Random();
        int otp = random.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }

    private void kiemTraCooldown(String emailOrPhone) {
        Long lastSent = lastOtpSentAt.get(emailOrPhone);
        if (lastSent != null) {
            long elapsed = (System.currentTimeMillis() - lastSent) / 1000;
            if (elapsed < RESEND_COOLDOWN_SECONDS) {
                throw new IllegalStateException(
                        "Vui long cho " + (RESEND_COOLDOWN_SECONDS - elapsed) + " giay truoc khi gui lai OTP.");
            }
        }
    }

    private void capNhatThoiGianGui(String emailOrPhone) {
        lastOtpSentAt.put(emailOrPhone, System.currentTimeMillis());
    }

    public OtpSendResponse guiOtpDangKyEmail(String email, String password, String fullName, String phoneNumber) throws Exception {
        String emailLower = email.toLowerCase().trim();
        String normalizedPhoneNumber = phoneNumber.trim();
        kiemTraCooldown(emailLower);
        log.info("Bat dau gui OTP dang ky email: {}", emailLower);

        User existingUser = userRepository.timTheoEmail(emailLower);
        if (existingUser != null && existingUser.getRoles() != null && existingUser.getRoles().contains(1)) {
            throw new IllegalArgumentException("Email da ton tai va da co vai tro nguoi dung trong he thong.");
        }

        if (existingUser == null && userRepository.tonTaiPhoneNumber(normalizedPhoneNumber)) {
            throw new IllegalArgumentException("So dien thoai da duoc su dung. Vui long su dung so dien thoai khac.");
        }

        String otp = sinhMaOtp();
        String pendingKey = "pending:" + emailLower;
        pendingRegistrations.put(pendingKey, new PendingRegistration(emailLower, password, fullName, normalizedPhoneNumber));
        otpStore.put(pendingKey, new OtpEntry(otp, null, System.currentTimeMillis() + OTP_TTL_SECONDS * 1000));
        capNhatThoiGianGui(emailLower);

        log.info("Ma OTP dang ky email cho {}: {} (hieu luc {} giay)", emailLower, otp, OTP_TTL_SECONDS);
        System.out.println("========== [DEV MODE] OTP DANG KY ==========");
        System.out.println("Den: " + emailLower);
        System.out.println("Ma OTP: " + otp);
        System.out.println("Het han sau: " + OTP_TTL_SECONDS + " giay");
        System.out.println("==========================================");

        emailService.guiEmailXacThuc(emailLower, otp);

        return OtpSendResponse.builder()
                .emailOrPhone(emailLower)
                .message("Ma OTP xac thuc da duoc gui. Vui long kiem tra email.")
                .otpCode(otp)
                .expiresInSeconds((int) OTP_TTL_SECONDS)
                .build();
    }

    public AuthResponse xacThucDangKyEmail(String email, String otpCode) throws Exception {
        String emailLower = email.toLowerCase().trim();
        String pendingKey = "pending:" + emailLower;
        log.info("Bat dau xac thuc dang ky email: {}", emailLower);

        OtpEntry entry = otpStore.get(pendingKey);
        if (entry == null) {
            log.warn("Khong co ma OTP dang ky cho: {}", emailLower);
            throw new IllegalArgumentException("Ma OTP khong hop le hoac da het han. Vui long gui lai.");
        }

        if (System.currentTimeMillis() > entry.expiresAtMs) {
            otpStore.remove(pendingKey);
            pendingRegistrations.remove(pendingKey);
            log.warn("Ma OTP dang ky da het han cho: {}", emailLower);
            throw new IllegalArgumentException("Ma OTP da het han. Vui long gui lai.");
        }

        if (!entry.otp.equals(otpCode)) {
            log.warn("Ma OTP khong dung cho: {}", emailLower);
            throw new IllegalArgumentException("Ma OTP khong dung.");
        }

        PendingRegistration pending = pendingRegistrations.get(pendingKey);
        if (pending == null) {
            throw new IllegalArgumentException("Khong tim thay thong tin dang ky. Vui long thu lai.");
        }

        User existingUser = userRepository.timTheoEmail(emailLower);
        User user;
        String userId;

        if (existingUser != null) {
            if (existingUser.getRoles() != null && existingUser.getRoles().contains(1)) {
                otpStore.remove(pendingKey);
                pendingRegistrations.remove(pendingKey);
                throw new IllegalArgumentException("Email da ton tai va da co vai tro nguoi dung trong he thong.");
            }

            List<Integer> updatedRoles = userRepository.themRoleNeuChuaCo(existingUser.getId(), 1);
            user = userRepository.timTheoId(existingUser.getId());
            userId = existingUser.getId();
            log.info("Them role nguoi dung cho tai khoan hien co thanh cong - UserId: {}, Roles: {}", userId, updatedRoles);
        } else {
            String newId = userRepository.sinhNextUserId();
            String hashedPassword = passwordEncoder.encode(pending.password);

            user = User.builder()
                    .id(newId)
                    .email(pending.email)
                    .password(hashedPassword)
                    .fullName(pending.fullName)
                    .phoneNumber(pending.phoneNumber)
                    .photoUrl(null)
                    .roles(List.of(1))
                    .createdAt(Instant.now().toString())
                    .isEmailVerified(true)
                    .build();

            userRepository.taoUser(user);
            userId = newId;
            log.info("Dang ky tai khoan thanh cong - UserId: {}, Email: {}", newId, pending.email);
        }

        otpStore.remove(pendingKey);
        pendingRegistrations.remove(pendingKey);

        String token = jwtTokenProvider.taoToken(userId);
        RefreshToken refreshToken = refreshTokenService.taoRefreshToken(userId, null);
        UserResponse userResponse = mapToUserResponse(user);

        return AuthResponse.of(token, jwtTokenProvider.getExpirationMs(),
                refreshToken.getToken(), refreshTokenService.getRefreshExpirationMs(), userResponse);
    }

    public OtpSendResponse guiOtpDangKyTaiXe(String email, String password, String fullName,
            String phoneNumber) throws Exception {
        String emailLower = email.toLowerCase().trim();
        String normalizedPhoneNumber = phoneNumber.trim();
        kiemTraCooldown(emailLower);
        log.info("Bat dau gui OTP dang ky tai xe: {}", emailLower);

        User existingUser = userRepository.timTheoEmail(emailLower);
        if (existingUser != null && existingUser.getRoles() != null && existingUser.getRoles().contains(2)) {
            throw new IllegalArgumentException("Email da ton tai va da co vai tro tai xe trong he thong.");
        }
        if (existingUser == null && userRepository.tonTaiPhoneNumber(normalizedPhoneNumber)) {
            throw new IllegalArgumentException("So dien thoai da duoc su dung.");
        }

        String otp = sinhMaOtp();
        String pendingKey = "pendingDriver:" + emailLower;
        pendingDriverRegistrations.put(pendingKey,
                new PendingDriverRegistration(emailLower, password, fullName, normalizedPhoneNumber));
        otpStore.put(pendingKey, new OtpEntry(otp, null, System.currentTimeMillis() + OTP_TTL_SECONDS * 1000));
        capNhatThoiGianGui(emailLower);

        log.info("Ma OTP dang ky tai xe cho {}: {} (hieu luc {} giay)", emailLower, otp, OTP_TTL_SECONDS);
        System.out.println("========== [DEV MODE] OTP DANG KY TAI XE ==========");
        System.out.println("Den: " + emailLower);
        System.out.println("Ma OTP: " + otp);
        System.out.println("Het han sau: " + OTP_TTL_SECONDS + " giay");
        System.out.println("=================================================");

        emailService.guiEmailXacThuc(emailLower, otp);

        return OtpSendResponse.builder()
                .emailOrPhone(emailLower)
                .message("Ma OTP xac thuc da duoc gui. Vui long kiem tra email.")
                .otpCode(otp)
                .expiresInSeconds((int) OTP_TTL_SECONDS)
                .build();
    }

    public AuthResponse xacThucDangKyTaiXe(String email, String otpCode) throws Exception {
        String emailLower = email.toLowerCase().trim();
        String pendingKey = "pendingDriver:" + emailLower;
        log.info("Bat dau xac thuc dang ky tai xe: {}", emailLower);

        OtpEntry entry = otpStore.get(pendingKey);
        if (entry == null) {
            log.warn("Khong co ma OTP dang ky tai xe cho: {}", emailLower);
            throw new IllegalArgumentException("Ma OTP khong hop le hoac da het han. Vui long gui lai.");
        }

        if (System.currentTimeMillis() > entry.expiresAtMs) {
            otpStore.remove(pendingKey);
            pendingDriverRegistrations.remove(pendingKey);
            log.warn("Ma OTP dang ky tai xe da het han cho: {}", emailLower);
            throw new IllegalArgumentException("Ma OTP da het han. Vui long gui lai.");
        }

        if (!entry.otp.equals(otpCode)) {
            log.warn("Ma OTP khong dung cho: {}", emailLower);
            throw new IllegalArgumentException("Ma OTP khong dung.");
        }

        PendingDriverRegistration pending = pendingDriverRegistrations.get(pendingKey);
        if (pending == null) {
            throw new IllegalArgumentException("Khong tim thay thong tin dang ky. Vui long thu lai.");
        }

        User existingUser = userRepository.timTheoEmail(emailLower);
        User user;
        String userId;

        if (existingUser != null) {
            if (existingUser.getRoles() != null && existingUser.getRoles().contains(2)) {
                otpStore.remove(pendingKey);
                pendingDriverRegistrations.remove(pendingKey);
                throw new IllegalArgumentException("Email da ton tai va da co vai tro tai xe trong he thong.");
            }

            List<Integer> updatedRoles = userRepository.themRoleNeuChuaCo(existingUser.getId(), 2);
            storeService.taoDriverProfile(existingUser.getId(), pending.fullName, pending.phoneNumber);
            user = userRepository.timTheoId(existingUser.getId());
            userId = existingUser.getId();
            log.info("Them role tai xe cho tai khoan hien co thanh cong - UserId: {}, Roles: {}", userId, updatedRoles);
        } else {
            String newId = userRepository.sinhNextUserId();
            String hashedPassword = passwordEncoder.encode(pending.password);

            user = User.builder()
                    .id(newId)
                    .email(pending.email)
                    .password(hashedPassword)
                    .fullName(pending.fullName)
                    .phoneNumber(pending.phoneNumber)
                    .photoUrl(null)
                    .roles(List.of(2))
                    .createdAt(Instant.now().toString())
                    .isEmailVerified(true)
                    .build();

            userRepository.taoUser(user);
            userId = newId;
            storeService.taoDriverProfile(newId, pending.fullName, pending.phoneNumber);
            log.info("Dang ky tai xe thanh cong - UserId: {}, Email: {}", newId, pending.email);
        }

        otpStore.remove(pendingKey);
        pendingDriverRegistrations.remove(pendingKey);

        String token = jwtTokenProvider.taoToken(userId);
        RefreshToken refreshToken = refreshTokenService.taoRefreshToken(userId, null);
        UserResponse userResponse = mapToUserResponse(user);

        return AuthResponse.of(token, jwtTokenProvider.getExpirationMs(),
                refreshToken.getToken(), refreshTokenService.getRefreshExpirationMs(), userResponse);
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
