# FoodGo - Hệ Thống Giao Đồ Ăn

> Hệ thống giao đồ ăn trực tuyến với 4 vai trò: Khách hàng, Tài xế, Người bán, Quản trị viên. Backend bằng Spring Boot + Firebase Firestore, Frontend bằng Flutter.

---

## Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Phiên bản sử dụng](#3-phiên-bản-sử-dụng)
4. [Cấu trúc dự án](#4-cấu-trúc-dự-án)
5. [Cài đặt Backend (be-foodgo)](#5-cài-đặt-backend-be-foodgo)
6. [Cài đặt Frontend (Flutter)](#6-cài-đặt-frontend-flutter)
7. [Các package dependency](#7-các-package-dependency)
8. [Tài khoản test](#8-tài-khoản-test)
9. [Các lưu ý cần thiết](#9-các-lưu-ý-cần-thiết)
10. [Cấu trúc API](#10-cấu-trúc-api)

---

## 1. Tổng quan

FoodGo là hệ thống giao đồ ăn trực tuyến gồm:

| Module | Mô tả |
|---|---|
| `be-foodgo` | Backend REST API (Spring Boot + Firebase Firestore) |
| `fe_foodgo_customer` | Ứng dụng khách hàng (Flutter) |
| `fe_food_go_driver` | Ứng dụng tài xế giao hàng (Flutter) |
| `fe_food_go_portal` | Ứng dụng người bán (Flutter) |
| `fe_foodgo_admin` | Ứng dụng quản trị hệ thống (Flutter) |

### Vai trò người dùng

| Vai trò | Mã | Mô tả |
|---|---|---|
| Khách hàng | 1 | Tìm kiếm quán, đặt hàng, theo dõi đơn |
| Tài xế | 2 | Nhận đơn, giao hàng, GPS tracking |
| Người bán | 3 | Quản lý cửa hàng, thực đơn, xử lý đơn |
| Quản trị viên | 4 | Cấu hình hệ thống, đối soát tài chính |

### Giới thiệu giao diện khách hàng

Ứng dụng `fe_foodgo_customer` là giao diện dành cho khách hàng, cung cấp các chức năng chính sau:

#### Màn hình chính (Home)
- **Thanh tìm kiếm**: Tìm kiếm quán ăn, món ăn theo tên.
- **Danh mục (Categories)**: Hiển thị các loại thực phẩm dưới dạng danh mục ngang (ví dụ: Pizza, Bún phở, Cơm, Đồ uống...).
- **Banner quảng cáo**: Carousel banner chạy tự động ở đầu màn hình.
- **Danh sách quán đang hot**: Hiển thị các quán được đánh giá cao hoặc có khuyến mãi.
- **Danh sách tất cả quán**: Danh sách các cửa hàng có sẵn gần đó.

#### Chi tiết quán ăn
- **Thông tin quán**: Tên, địa chỉ, đánh giá (sao), thời gian mở cửa.
- **Hình ảnh quán**: Album ảnh quán.
- **Thực đơn**: Danh sách các món ăn được phân theo danh mục.
- **Thông tin giao hàng**: Phí giao, thời gian ước tính.
- **Đánh giá**: Xem các đánh giá từ khách hàng khác.

#### Giỏ hàng & Đặt hàng
- **Chọn món**: Thêm món vào giỏ với số lượng, ghi chú (vd: không hành, thêm tiêu...).
- **Tùy chọn món**: Chọn size, topping, mức độ cay...
- **Mã voucher**: Nhập mã giảm giá.
- **Thông tin đơn hàng**: Tổng tiền, phí giao, giảm giá, số tiền phải trả.
- **Phương thức thanh toán**: Tiền mặt (COD), MoMo, ZaloPay, VNPay, Thẻ ngân hàng.
- **Địa chỉ giao hàng**: Chọn địa chỉ đã lưu hoặc nhập địa chỉ mới.
- **Xác nhận đơn hàng**: Đặt hàng và nhận thông báo xác nhận.

#### Theo dõi đơn hàng
- **Trạng thái đơn hàng**: Chờ xác nhận → Đang chuẩn bị → Đang giao → Hoàn thành.
- **Theo dõi tài xế**: Hiển thị vị trí tài xế trên bản đồ real-time.
- **Thông tin tài xế**: Tên, số điện thoại, biển số xe.
- **Liên hệ tài xế**: Gọi điện hoặc nhắn tin trực tiếp.

#### Tài khoản & Hồ sơ
- **Thông tin cá nhân**: Tên, số điện thoại, email, ảnh đại diện.
- **Địa chỉ đã lưu**: Quản lý danh sách địa chỉ giao hàng.
- **Lịch sử đơn hàng**: Xem lại các đơn hàng đã đặt.
- **Ví tiền**: Xem số dư, lịch sử giao dịch, nạp tiền.
- **Voucher của tôi**: Danh sách voucher đã lưu và mã voucher.
- **Đánh giá đơn hàng**: Đánh giá sao và viết bình luận sau khi nhận hàng.
- **Thông báo**: Xem các thông báo khuyến mãi, cập nhật đơn hàng.
- **Lịch sử tìm kiếm**: Các từ khóa đã tìm kiếm trước đó.
- **Đăng xuất**.

#### Các màn hình phụ
- **Đăng nhập / Đăng ký**: Đăng nhập bằng email, đăng ký tài khoản mới, quên mật khẩu.
- **Xác thực OTP**: Nhập mã OTP qua email để xác thực tài khoản.
- **Chi tiết voucher**: Xem thông tin chi tiết voucher, điều kiện sử dụng.
- **Chi tiết đơn hàng**: Xem đầy đủ thông tin một đơn hàng cụ thể.
- **Phương thức thanh toán**: Quản lý các phương thức thanh toán đã lưu.

---

## 2. Công nghệ sử dụng

### Backend (`be-foodgo`)

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ lập trình | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Hồ sơ dữ liệu | Firebase Firestore |
| Realtime Database | Firebase Realtime Database |
| Authentication | Firebase Admin SDK + JWT |
| Storage | Firebase Cloud Storage, Cloudinary |
| API Documentation | SpringDoc OpenAPI |
| Build tool | Maven |
| Email | Spring Boot Mail (Gmail SMTP) |
| WebSocket | Spring WebSocket (STOMP) |

### Frontend (`fe_foodgo_*`)

| Thành phần | Công nghệ |
|---|---|
| Framework | Flutter |
| Ngôn ngữ | Dart |
| State Management | flutter_bloc |
| Database | Firebase Firestore, Firebase Realtime Database |
| Authentication | Firebase Authentication |
| Maps | flutter_map (OpenStreetMap) |
| Location | Geolocator |
| Real-time | STOMP (WebSocket) |
| Push Notification | Firebase Cloud Messaging (FCM) |
| Dependency Injection | get_it |

---

## 3. Phiên bản sử dụng

### Backend

| Thành phần | Phiên bản |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Maven | 3.6+ |
| Firebase Admin SDK | 9.2.0 |
| google-cloud-storage | 2.47.0 |
| JJWT | 0.12.6 |
| SpringDoc OpenAPI | 2.8.4 |

### Frontend (tất cả 4 app)

| Thành phần | Phiên bản |
|---|---|
| Flutter SDK | 3.11.1 |
| Dart SDK | 3.11.1 |
| Android Gradle Plugin | 8.x |
| Kotlin | 1.9.x |
| iOS Deployment Target | 12.0+ |

Kiểm tra phiên bản Flutter:

```bash
flutter --version
```

---

## 4. Cấu trúc dự án

```
LTDD_BTL/
|
|-- be-foodgo/                    # Backend Spring Boot
|   |-- src/main/java/com/example/be_foodgo/
|   |   |-- config/               # Cấu hình Firebase, Security, CORS
|   |   |-- controller/           # REST API endpoints
|   |   |-- dto/                  # Data Transfer Object
|   |   |-- model/               # Firestore document mapping
|   |   |-- repository/           # Truy vấn Firestore
|   |   |-- seeder/              # Khởi tạo dữ liệu mẫu
|   |   |-- service/             # Logic nghiệp vụ
|   |   |-- exception/           # Xử lý lỗi tập trung
|   |   |-- constant/            # Enum và hằng số
|   |   |-- websocket/           # Cấu hình WebSocket
|   |-- src/main/resources/
|   |   |-- application.properties
|   |   |-- firebase-service-account.json
|   |-- pom.xml
|
|-- fe_foodgo_customer/           # Ứng dụng Khách hàng (Flutter)
|   |-- lib/
|   |-- android/
|   |-- ios/
|   |-- pubspec.yaml
|
|-- fe_food_go_driver/            # Ứng dụng Tài xế (Flutter)
|   |-- lib/
|   |-- android/
|   |-- ios/
|   |-- pubspec.yaml
|
|-- fe_food_go_portal/            # Ứng dụng Người bán (Flutter)
|   |-- lib/
|   |-- android/
|   |-- ios/
|   |-- pubspec.yaml
|
|-- fe_foodgo_admin/              # Ứng dụng Quản trị viên (Flutter)
|   |-- lib/
|   |-- android/
|   |-- ios/
|   |-- pubspec.yaml
|
|-- docs/                          # Tài liệu dự án
```

---

## 5. Cài đặt Backend (`be-foodgo`)

### 5.1. Yêu cầu

- **JDK 21** trở lên
- **Maven 3.6+** (hoặc sử dụng Maven wrapper)
- **Tài khoản Firebase** với Firestore, Realtime Database, Storage
- **firebase-service-account.json** (lấy từ Firebase Console)

### 5.2. Các bước cài đặt

**1. Clone và di chuyển vào thư mục:**

```bash
git clone <repository-url>
cd be-foodgo
```

**2. Kiểm tra Java:**

```bash
java -version
# Đảm bảo kết quả là Java 21
```

**3. Cài đặt Maven dependencies:**

```bash
./mvnw install        # macOS / Linux
mvnw.cmd install      # Windows
```

### 5.3. Cấu hình Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Kích hoạt **Firestore Database** (Test Mode hoặc Production)
4. Kích hoạt **Realtime Database**
5. Kích hoạt **Firebase Storage**
6. Vào **Project Settings** → **Service accounts** → **Generate new private key**
7. Tải file JSON về, đổi tên thành `firebase-service-account.json`
8. Đặt file vào `src/main/resources/`

> **Lưu ý bảo mật:** File `firebase-service-account.json` chứa credentials nhạy cảm. **Không được commit lên Git.** File này đã được thêm vào `.gitignore`.

### 5.4. Cấu hình `application.properties`

File `src/main/resources/application.properties` đã có sẵn. Kiểm tra các cấu hình sau:

```properties
spring.application.name=be-foodgo
server.port=8086

# Firebase
firebase.database.url=https://food-go-17a5d-default-rtdb.asia-southeast1.firebasedatabase.app/
firebase.storage.bucket=food-go-17a5d.appspot.com

# JWT
jwt.secret=FoodGoJwtSecretKey2026Nam3Ki2VeryLongAndSecure256BitSecretKeyForSigningTokens
jwt.expiration=10800000

# Email (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=foldershop83@gmail.com
spring.mail.password=<app-password>
app.email.enabled=true

# Cloudinary
cloudinary.cloud_name=dd51afnue
cloudinary.api_key=867855984214359
cloudinary.api_secret=<api-secret>

# Upload
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
```

> **Lưu ý:** Nếu muốn gửi email thực sự, cần tạo **App Password** từ tài khoản Google:
> 1. Vào [Google Account](https://myaccount.google.com) → **Security** → **2-Step Verification**
> 2. Bật xác minh 2 bước, sau đó vào **App passwords**
> 3. Tạo app password mới và thay vào `spring.mail.password`

### 5.5. Chạy Backend

```bash
# Chạy bằng Maven
./mvnw spring-boot:run        # macOS / Linux
mvnw.cmd spring-boot:run       # Windows

# Hoặc build JAR rồi chạy
./mvnw clean package -DskipTests
java -jar target/be-foodgo-0.0.1-SNAPSHOT.jar
```

Khi chạy thành công, console sẽ hiển thị:

```
Khởi tạo Firebase thành công.
Firestore bean đã được tạo thành công.
```

Backend chạy tại: `http://localhost:8086`

### 5.6. Seed dữ liệu mẫu

`FirebaseDataSeeder` tự động chạy khi ứng dụng khởi động. Nếu cần seed lại, gọi:

```java
DataSeeder.clearAllSeededData();
```

rồi khởi động lại ứng dụng.

---

## 6. Cài đặt Frontend (Flutter)

### 6.1. Yêu cầu hệ thống

- **Flutter SDK:** >= 3.11.1
- **Dart SDK:** >= 3.11.1
- **Android SDK** (nếu build Android)
- **Xcode + CocoaPods** (nếu build iOS, chỉ trên macOS)
- **Git**

### 6.2. Cài đặt cho tất cả app Flutter

Các bước giống nhau cho `fe_foodgo_customer`, `fe_foodgo_admin`, `fe_food_go_portal`, `fe_food_go_driver`:

**1. Clone và di chuyển:**

```bash
cd fe_foodgo_customer       # hoặc fe_foodgo_admin / fe_food_go_portal / fe_food_go_driver
```

**2. Cài đặt dependencies:**

```bash
flutter pub get
```

**3. Cấu hình Firebase cho từng app:**

a) Tạo project Firebase tại [Firebase Console](https://console.firebase.google.com/)

b) **Android:**
- Vào Project Settings → Your apps → Android app
- Tải `google-services.json` về
- Đặt vào `android/app/google-services.json`

c) **iOS (chỉ macOS):**
- Tải `GoogleService-Info.plist` về
- Đặt vào `ios/Runner/GoogleService-Info.plist`

d) Kích hoạt trong Firebase Console:
- **Authentication** → Sign-in method → bật **Email/Password**
- **Firestore Database** → Create database
- **Realtime Database** → Create database
- **Storage** → Create storage
- **Cloud Messaging** → Lấy Server key để gửi push notification

**4. Cấu hình Android (`android/app/build.gradle`):**

```groovy
defaultConfig {
    minSdkVersion 21
    // ...
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

**7. Chạy iOS (chỉ macOS):**

```bash
cd ios
pod install --repo-update
cd ..
```

**8. Chạy ứng dụng:**

```bash
# Chế độ debug
flutter run

# Build APK debug
flutter build apk --debug

# Build APK release
flutter build apk --release

# Build iOS
flutter build ios
```

---

## 7. Các package dependency

### 7.1. Backend (`be-foodgo` / `pom.xml`)

| Package | Phiên bản | Mục đích |
|---|---|---|
| spring-boot-starter-webmvc | (parent) | REST API |
| spring-boot-starter-security | (parent) | Security |
| spring-boot-starter-websocket | (parent) | WebSocket |
| spring-boot-starter-validation | (parent) | Validation |
| spring-boot-starter-mail | (parent) | Email |
| spring-boot-starter-validation-test | (parent) | Test validation |
| spring-boot-starter-webmvc-test | (parent) | Test MVC |
| spring-boot-devtools | (parent) | Dev tools |
| firebase-admin | 9.2.0 | Firebase SDK |
| google-cloud-storage | 2.47.0 | Cloud Storage |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.12.6 | JWT authentication |
| springdoc-openapi-starter-webmvc-ui | 2.8.4 | Swagger UI |
| cloudinary-http5 | 2.3.2 | Cloudinary image hosting |
| commons-csv | 1.10.0 | CSV export |
| lombok | (parent) | Boilerplate reduction |
| exec-maven-plugin | 3.1.0 | Script execution |

### 7.2. `fe_foodgo_customer` / `fe_foodgo_admin` / `fe_food_go_portal` (`pubspec.yaml`)

| Package | Phiên bản | Mục đích |
|---|---|---|
| flutter_bloc | ^8.1.6 | State management |
| fl_chart | ^0.66.0 | Biểu đồ thống kê |
| intl | ^0.19.0 / ^0.20.2 | Định dạng ngày tháng |
| firebase_core | ^4.9.0 | Firebase core |
| firebase_auth | ^6.5.1 | Xác thực |
| dio | ^5.9.2 | HTTP client |
| shared_preferences | ^2.5.5 | Lưu trữ cục bộ |
| image_picker | ^1.1.2 / ^1.2.2 | Chọn hình |
| file_picker | ^8.1.2 | Chọn file |
| firebase_storage | ^13.4.2 | Firebase Storage (portal) |
| flutter_map | ^8.3.0 | Bản đồ (portal) |
| latlong2 | ^0.9.1 | Tọa độ địa lý |
| geolocator | ^14.0.2 | Lấy vị trí |
| geolocator_windows | ^0.2.5 | Geolocator Windows |
| easy_localization | ^3.0.7 | Đa ngôn ngữ (portal) |
| flutter_lints | ^6.0.0 | Lint rules |

### 7.3. `fe_food_go_driver` (`pubspec.yaml`)

| Package | Phiên bản | Mục đích |
|---|---|---|
| flutter_bloc | ^8.1.6 | State management |
| equatable | ^2.0.5 | So sánh đối tượng |
| dartz | ^0.10.1 | Functional programming |
| rxdart | ^0.28.0 | Reactive extensions |
| get_it | ^8.0.3 | Dependency injection |
| shared_preferences | ^2.3.5 | Lưu trữ cục bộ |
| flutter_secure_storage | ^9.2.4 | Lưu trữ an toàn |
| intl | ^0.20.2 | Định dạng ngày tháng |
| http | ^1.2.0 | HTTP client |
| stomp_dart_client | ^3.0.1 | WebSocket STOMP |
| geolocator | ^13.0.2 | Lấy vị trí |
| permission_handler | ^11.3.1 | Xử lý quyền |
| flutter_map | ^7.0.2 | Bản đồ |
| latlong2 | ^0.9.1 | Tọa độ địa lý |
| google_fonts | ^6.2.1 | Phông chữ |
| shimmer | ^3.0.0 | Hiệu ứng loading |
| pinput | ^5.0.0 | Nhập OTP |
| url_launcher | ^6.3.1 | Mở URL |
| firebase_core | ^3.12.1 | Firebase core |
| cloud_firestore | ^5.6.6 | Cloud Firestore |
| firebase_database | ^11.3.5 | Realtime Database |
| firebase_auth | ^5.5.3 | Xác thực |
| firebase_messaging | ^15.2.4 | Push notification |
| flutter_local_notifications | ^21.0.0 | Thông báo cục bộ |

---

## 8. Tài khoản test

### 8.1. Backend (dữ liệu đã được seed tự động)

Khi chạy backend, `FirebaseDataSeeder` sẽ tự động tạo các tài khoản test sau trong Firestore:

| User ID | Email | Mật khẩu | Vai trò |
|---|---|---|---|
| `user_001` | `khachhang@gmail.com` | `Khoi123@` | Khách hàng (1), Tài xế (2), Người bán (3) |
| `user_002` | `admin@foodgo.com` | `Admin123@` | Quản trị viên (4) |
| `user_003` | `taixe@gmail.com` | `Taixe123@` | Tài xế (2) |
| `user_004` | `luudinhnghia30012005@gmail.com` | `Nghia123@` | Người bán (3) |
| `user_005` | `taixe3@gmail.com` | `Taixe123@` | Tài xế (2) |
| `user_006` | `taixe2@gmail.com` | `Taixe123@` | Tài xế (2) |

> **Lưu ý:** Mật khẩu được lưu dưới dạng BCrypt hash. Các mật khẩu trên là giá trị gốc được sử dụng khi tạo hash. Nếu cần xác thực Firebase Authentication, cần tạo tay các tài khoản này trong Firebase Console.

### 8.2. Firestore Collections đã có sẵn (seed data)

Dữ liệu mẫu đã được seed vào các collection: `users`, `wallets`, `transactions`, `system_configs`, `categories`, `stores`, `products`, `banners`, `vouchers`, `reviews`, `orders`, `customer_profiles`, `driver_profiles`, `merchant_profiles`, `admin_profiles`.

---

## 9. Các lưu ý cần thiết

### 9.1. Tất cả các app (Backend + Flutter)

1. **Firebase Configuration bắt buộc:** File `google-services.json` (Android) và `GoogleService-Info.plist` (iOS) phải có. Nếu không có, ứng dụng sẽ lỗi khi khởi động.

2. **Dịch vụ Firebase cần bật:** Authentication (Email/Password), Firestore Database, Realtime Database, Storage, Cloud Messaging.

3. **Kết nối Internet:** Tất cả các app đều cần internet để hoạt động (truy cập Firebase, gọi API backend, WebSocket).

4. **CORS:** Backend cấu hình cho phép cross-origin từ các origin Flutter (Android/iOS/Web).

### 9.2. Backend (`be-foodgo`)

1. **Port mặc định:** `8086`. Nếu muốn đổi port, sửa `server.port` trong `application.properties`.

2. **JWT Secret:** Chuỗi `jwt.secret` hiện tại là chuỗi test. Trong môi trường production, cần đổi thành chuỗi ngẫu nhiên 256-bit.

3. **Firebase Service Account:** Không commit file `firebase-service-account.json` lên Git. Nếu làm mất file, vào Firebase Console → Project Settings → Service accounts → Generate new private key.

4. **Email Configuration:** Nếu `app.email.enabled=false`, email sẽ chỉ hiển thị trên console (dev mode). Khi cần gửi email thực, tạo App Password từ Google và điền vào `spring.mail.password`.

5. **Cloudinary:** Nếu mất API secret, tạo tài khoản Cloudinary mới và cập nhật trong `application.properties`.

### 9.3. Frontend (Flutter)

1. **Quyền Vị trí:** Tất cả các app đều cần quyền vị trí (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION). Nếu không cấp quyền, một số chức năng sẽ không hoạt động.

2. **Android minSdkVersion:** Phải >= 21. Kiểm tra trong `android/app/build.gradle`.

3. **iOS Info.plist:** Cần thêm mô tả quyền vị trí vào `ios/Runner/Info.plist` (chỉ macOS/iOS).

4. **WebSocket/STOMP:** Ứng dụng driver kết nối đến backend qua WebSocket (STOMP) để nhận đơn giao hàng real-time. Đảm bảo backend đã bật WebSocket endpoint.

5. **API URL:** Cấu hình địa chỉ backend API trong `lib/core/api/api_constants.dart` hoặc `lib/injection_container.dart` của từng app. Mặc định là `http://10.0.2.2:8086` (Android Emulator) hoặc `http://localhost:8086` (iOS Simulator).

6. **Foreground Service (Driver):** Ứng dụng driver sử dụng Foreground Service để cập nhật vị trí khi ở nền. Đảm bảo quyền `FOREGROUND_SERVICE` và `FOREGROUND_SERVICE_LOCATION` đã được khai báo.

7. **Push Notification (Driver):** Khi app ở nền, push notification được gửi qua FCM. Cần cấu hình FCM server key phía backend (`firebase.messaging.server-key`) để gửi notification.

8. **Thư mục assets:** Kiểm tra thư mục `assets/` có tồn tại trong từng app Flutter. Nếu thiếu, tạo thư mục:

```bash
# fe_food_go_driver
mkdir -p assets/lang assets/img

# fe_food_go_portal
mkdir -p assets/translations
```

### 9.4. Các lỗi thường gặp

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

## 10. Cấu trúc API

### 10.1. Base URL

```
http://localhost:8086/api
```

### 10.2. Cấu trúc chung response

```json
{
  "success": true,
  "message": "Thành công",
  "data": { ... }
}
```

### 10.3. Authentication

- **Login:** `POST /api/auth/login` - trả về JWT token
- **Register:** `POST /api/auth/register`
- **Refresh Token:** `POST /api/auth/refresh`
- **Logout:** `POST /api/auth/logout`

JWT token được gửi kèm trong header:

```
Authorization: Bearer <token>
```

### 10.4. Các endpoint chính

| Nhóm | Prefix | Mô tả |
|---|---|---|
| Auth | `/api/auth/*` | Đăng nhập, đăng ký, refresh token |
| User | `/api/users/*` | Thông tin người dùng |
| Store | `/api/stores/*` | Quản lý cửa hàng |
| Product | `/api/products/*` | Quản lý sản phẩm |
| Order | `/api/orders/*` | Xử lý đơn hàng |
| Voucher | `/api/vouchers/*` | Quản lý voucher |
| Payment | `/api/payments/*` | Thanh toán |
| Wallet | `/api/wallets/*` | Ví tiền |
| Review | `/api/reviews/*` | Đánh giá |
| Category | `/api/categories/*` | Danh mục |
| Banner | `/api/banners/*` | Banner quảng cáo |
| Profile | `/api/profiles/*` | Profile người dùng |
| Stats | `/api/stats/*` | Thống kê |
| WebSocket | `/ws` | Kênh real-time (STOMP) |

### 10.5. Vai trò và quyền

| Mã | Vai trò | Quyền |
|---|---|---|
| 1 | Khách hàng | Tìm quán, đặt hàng, theo dõi đơn, đánh giá |
| 2 | Tài xế | Nhận đơn, giao hàng, GPS tracking |
| 3 | Người bán | Quản lý cửa hàng, thực đơn, xử lý đơn |
| 4 | Quản trị viên | Cấu hình hệ thống, đối soát tài chính |

### 10.6. Trạng thái đơn hàng

| Giá trị | Trạng thái |
|---|---|
| 0 | Chờ xác nhận |
| 1 | Đang chuẩn bị |
| 2 | Đang giao |
| 3 | Hoàn thành |
| 4 | Đã hủy |

### 10.7. Phương thức thanh toán

| Giá trị | Mô tả |
|---|---|
| `cash` | Tiền mặt (COD) |
| `momo` | Ví MoMo |
| `zalo` | ZaloPay |
| `vnpay` | VNPay |
| `card` | Thẻ ngân hàng |

---

**Phiên bản:** 0.0.1-SNAPSHOT
