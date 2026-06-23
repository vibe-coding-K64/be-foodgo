# FoodGo - Hệ Thống Giao Đồ Ăn

> Hệ thống giao đồ ăn trực tuyến với 4 vai trò: Khách hàng, Tài xế, Người bán, Quản trị viên. Backend bằng Spring Boot + Firebase Firestore, Frontend bằng Flutter.

---

## Công nghệ sử dụng

### Backend (`be-foodgo`)

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Database | Firebase Firestore, Firebase Realtime Database |
| Authentication | Firebase Admin SDK + JWT |
| Storage | Firebase Cloud Storage, Cloudinary |
| API Docs | SpringDoc OpenAPI |
| Build tool | Maven |
| Email | Spring Boot Mail (Gmail SMTP) |
| Real-time | Spring WebSocket (STOMP) |

### Frontend (Flutter)

| Thành phần | Công nghệ |
|---|---|
| Framework | Flutter |
| Ngôn ngữ | Dart |
| State Management | flutter_bloc |
| Database | Firebase Firestore, Firebase Realtime Database |
| Maps | flutter_map (OpenStreetMap) |
| Real-time | STOMP (WebSocket) |
| Push Notification | Firebase Cloud Messaging (FCM) |

---

## Phiên bản

| Thành phần | Phiên bản |
|---|---|
| Flutter SDK | 3.11.1 |
| Dart SDK | 3.11.1 |
| Java | 21 |
| Spring Boot | 4.0.6 |
| Maven | 3.6+ |
| Android Gradle Plugin | 8.x |
| Kotlin | 1.9.x |

Kiểm tra phiên bản Flutter:

```bash
flutter --version
```

---

## Cài đặt và chạy project

### Backend (`be-foodgo`)

**Yêu cầu:** JDK 21+, Maven 3.6+, tài khoản Firebase.

**1. Clone project:**

```bash
git clone https://github.com/vibe-coding-K64/be-foodgo.git
cd be-foodgo
```

**2. Cấu hình Firebase:**

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Kích hoạt **Firestore Database**, **Realtime Database**, **Storage**
4. Vào **Project Settings** → **Service accounts** → **Generate new private key**
5. Tải file JSON về, đổi tên thành `firebase-service-account.json`
6. Đặt file vào `src/main/resources/`

> **Lưu ý bảo mật:** File `firebase-service-account.json` đã được thêm vào `.gitignore`. Không commit file này lên Git.

**3. Cấu hình `application.properties`:**

File `src/main/resources/application.properties` đã có sẵn. Kiểm tra các cấu hình quan trọng:

```properties
server.port=8086
# Firebase
firebase.database.url=https://food-go-17a5d-default-rtdb.asia-southeast1.firebasedatabase.app/
firebase.storage.bucket=food-go-17a5d.appspot.com
# JWT
jwt.secret=FoodGoJwtSecretKey2026Nam3Ki2VeryLongAndSecure256BitSecretKeyForSigningTokens
jwt.expiration=10800000
# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=foldershop83@gmail.com
spring.mail.password=<app-password>
app.email.enabled=true
```

> **Lưu ý:** Để gửi email thực, cần tạo **App Password** từ Google Account → Security → 2-Step Verification → App passwords.

**4. Chạy Backend:**

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run

# Hoặc build JAR
./mvnw clean package -DskipTests
java -jar target/be-foodgo-0.0.1-SNAPSHOT.jar
```

Backend chạy tại: `http://localhost:8086`

Dữ liệu mẫu được seed tự động khi khởi động (`FirebaseDataSeeder`).

---

### Frontend (Flutter)

**Yêu cầu:** Flutter SDK 3.11.1+, Android SDK / Xcode (macOS).

**1. Clone các app Flutter:**

```bash
# Ứng dụng Khách hàng
git clone https://github.com/vibe-coding-K64/fe_foodgo_customer.git

# Ứng dụng Tài xế
git clone https://github.com/vibe-coding-K64/fe_food_go_driver.git

# Ứng dụng Người bán
git clone https://github.com/vibe-coding-K64/fe_food_go_portal.git

# Ứng dụng Quản trị viên
git clone https://github.com/vibe-coding-K64/fe_foodgo_admin.git
```

**2. Cấu hình Firebase cho từng app:**

a) Tạo project Firebase tại [Firebase Console](https://console.firebase.google.com/)

b) **Android:** Tải `google-services.json` → đặt vào `android/app/google-services.json`

c) **iOS (macOS):** Tải `GoogleService-Info.plist` → đặt vào `ios/Runner/GoogleService-Info.plist`

d) Kích hoạt trong Firebase Console:
- **Authentication** → Sign-in method → bật **Email/Password**
- **Firestore Database**
- **Realtime Database**
- **Storage**
- **Cloud Messaging**

**3. Cài đặt dependencies:**

```bash
cd <ten-app>
flutter pub get
```

**4. Cấu hình Android (`android/app/build.gradle`):**

```groovy
defaultConfig {
    minSdkVersion 21
}
```

**5. Thêm Google Services plugin:**

`android/build.gradle` (project-level):

```groovy
plugins {
    id 'com.android.application' version '8.1.0' apply false
    id 'com.google.gms.google-services' version '4.4.2' apply false
}
```

`android/app/build.gradle` (app-level):

```groovy
plugins {
    id 'com.google.gms.google-services'
}
```

**6. Thêm quyền Android (`android/app/src/main/AndroidManifest.xml`):**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

**7. Chạy ứng dụng:**

```bash
flutter run                           # Chế độ debug
flutter build apk --debug             # Build APK debug
flutter build apk --release           # Build APK release
```

---

## Các package/dependency cần cài

### Backend (`be-foodgo` / `pom.xml`)

| Package | Phiên bản | Mục đích |
|---|---|---|
| spring-boot-starter-webmvc | (parent) | REST API |
| spring-boot-starter-security | (parent) | Security |
| spring-boot-starter-websocket | (parent) | WebSocket |
| spring-boot-starter-validation | (parent) | Validation |
| spring-boot-starter-mail | (parent) | Email |
| firebase-admin | 9.2.0 | Firebase SDK |
| google-cloud-storage | 2.47.0 | Cloud Storage |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.12.6 | JWT authentication |
| springdoc-openapi-starter-webmvc-ui | 2.8.4 | Swagger UI |
| cloudinary-http5 | 2.3.2 | Cloudinary image hosting |
| commons-csv | 1.10.0 | CSV export |
| lombok | (parent) | Boilerplate reduction |

### Frontend (Customer / Admin / Portal)

| Package | Phiên bản | Mục đích |
|---|---|---|
| flutter_bloc | ^8.1.6 | State management |
| firebase_core | ^4.9.0 | Firebase core |
| firebase_auth | ^6.5.1 | Xác thực |
| dio | ^5.9.2 | HTTP client |
| fl_chart | ^0.66.0 | Biểu đồ thống kê |
| shared_preferences | ^2.5.5 | Lưu trữ cục bộ |
| image_picker | ^1.1.2 | Chọn hình |
| flutter_map | ^8.3.0 | Bản đồ |
| geolocator | ^14.0.2 | Lấy vị trí |
| intl | ^0.20.2 | Định dạng ngày tháng |

### Frontend (Driver)

| Package | Phiên bản | Mục đích |
|---|---|---|
| flutter_bloc | ^8.1.6 | State management |
| firebase_core | ^3.12.1 | Firebase core |
| firebase_auth | ^5.5.3 | Xác thực |
| cloud_firestore | ^5.6.6 | Cloud Firestore |
| firebase_database | ^11.3.5 | Realtime Database |
| firebase_messaging | ^15.2.4 | Push notification |
| stomp_dart_client | ^3.0.1 | WebSocket STOMP |
| geolocator | ^13.0.2 | Lấy vị trí |
| permission_handler | ^11.3.1 | Xử lý quyền |
| flutter_map | ^7.0.2 | Bản đồ |
| get_it | ^8.0.3 | Dependency injection |

---

## Tài khoản test

Khi chạy backend, `FirebaseDataSeeder` tự động tạo các tài khoản test trong Firestore:

| User ID | Email | Mật khẩu | Vai trò |
|---|---|---|---|
| `user_001` | `khachhang@gmail.com` | `Khoi123@` | Khách hàng, Tài xế, Người bán |
| `user_002` | `admin@foodgo.com` | `Admin123@` | Quản trị viên |
| `user_003` | `taixe@gmail.com` | `Taixe123@` | Tài xế |
| `user_004` | `luudinhnghia30012005@gmail.com` | `Nghia123@` | Người bán |
| `user_005` | `taixe3@gmail.com` | `Taixe123@` | Tài xế |
| `user_006` | `taixe2@gmail.com` | `Taixe123@` | Tài xế |

> **Lưu ý:** Mật khẩu được lưu dưới dạng BCrypt hash. Nếu cần xác thực Firebase Authentication, cần tạo tay các tài khoản này trong Firebase Console.

---

## Các lưu ý cần thiết

### Chung (Backend + Flutter)

1. **Firebase Configuration bắt buộc:** File `google-services.json` (Android) và `GoogleService-Info.plist` (iOS) phải có, nếu không ứng dụng sẽ lỗi khi khởi động.
2. **Dịch vụ Firebase cần bật:** Authentication (Email/Password), Firestore Database, Realtime Database, Storage, Cloud Messaging.
3. **Kết nối Internet:** Tất cả các app cần internet để hoạt động.
4. **CORS:** Backend đã cấu hình cho phép cross-origin từ Flutter.

### Backend

1. **Port mặc định:** `8086`. Đổi port bằng cách sửa `server.port` trong `application.properties`.
2. **JWT Secret:** Chuỗi hiện tại là test. Trong production, cần đổi thành chuỗi ngẫu nhiên 256-bit.
3. **Firebase Service Account:** Không commit `firebase-service-account.json` lên Git. Nếu mất, vào Firebase Console → Project Settings → Service accounts → Generate new private key.
4. **Email:** Nếu `app.email.enabled=false`, email chỉ hiển thị trên console. Cần tạo App Password từ Google để gửi email thực.
5. **Cloudinary:** Nếu mất API secret, tạo tài khoản Cloudinary mới và cập nhật trong `application.properties`.

### Frontend

1. **Quyền Vị trí:** Tất cả app đều cần quyền vị trí (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION). Không cấp quyền sẽ ảnh hưởng đến một số chức năng.
2. **Android minSdkVersion:** Phải >= 21. Kiểm tra trong `android/app/build.gradle`.
3. **API URL:** Backend mặc định là `http://10.0.2.2:8086` (Android Emulator) hoặc `http://localhost:8086` (iOS Simulator). Cấu hình trong `lib/core/api/api_constants.dart` của từng app.
4. **WebSocket/STOMP:** App driver kết nối backend qua WebSocket (STOMP) để nhận đơn giao hàng real-time.
5. **Foreground Service (Driver):** Sử dụng Foreground Service để cập nhật vị trí khi ở nền.
6. **Push Notification:** Cần cấu hình FCM server key phía backend để gửi notification.

### Các lỗi thường gặp

| Lỗi | Giải pháp |
|---|---|
| `Unable to find git` | Cài đặt git và thêm vào PATH |
| `google-services.json` không hợp lệ | Tải lại từ Firebase Console, kiểm tra tên package |
| `minSdkVersion too low` | Tăng `minSdkVersion` trong `build.gradle` lên 21+ |
| `pod install` thất bại (iOS) | Chạy `cd ios && pod install --repo-update` |
| Không gửi được email | Tạo App Password từ Google, kiểm tra `app.email.enabled=true` |
| WebSocket không kết nối | Kiểm tra backend đã chạy, kiểm tra URL API trong Flutter |
| Firestore permission denied | Kiểm tra rules Firestore trong Firebase Console |

---

## Cấu trúc dự án

```
be-foodgo/                          # Backend Spring Boot
fe_foodgo_customer/                 # Ứng dụng Khách hàng (Flutter)
fe_food_go_driver/                  # Ứng dụng Tài xế (Flutter)
fe_food_go_portal/                  # Ứng dụng Người bán (Flutter)
fe_foodgo_admin/                    # Ứng dụng Quản trị viên (Flutter)
```

### Vai trò người dùng

| Vai trò | Mã | Mô tả |
|---|---|---|
| Khách hàng | 1 | Tìm kiếm quán, đặt hàng, theo dõi đơn |
| Tài xế | 2 | Nhận đơn, giao hàng, GPS tracking |
| Người bán | 3 | Quản lý cửa hàng, thực đơn, xử lý đơn |
| Quản trị viên | 4 | Cấu hình hệ thống, đối soát tài chính |

### Trạng thái đơn hàng

| Giá trị | Trạng thái |
|---|---|
| 0 | Chờ xác nhận |
| 1 | Đang chuẩn bị |
| 2 | Đang giao |
| 3 | Hoàn thành |
| 4 | Đã hủy |

### Phương thức thanh toán

| Giá trị | Mô tả |
|---|---|
| `cash` | Tiền mặt (COD) |
| `momo` | Ví MoMo |
| `zalo` | ZaloPay |
| `vnpay` | VNPay |
| `card` | Thẻ ngân hàng |

---

**Phiên bản:** 0.0.1-SNAPSHOT
