# FoodGo - He thong Giao Do An

> He thong giao do an truc tuyen voi 4 vai tro: Khach hang, Tai xe, Nguoi ban, Quan tri vien. Backend bang Spring Boot + Firebase Firestore, Frontend bang Flutter.

---

## Muc luc

1. [Tong quan](#1-tong-quan)
2. [Cong nghe su dung](#2-cong-nghe-su-dung)
3. [Phien ban su dung](#3-phien-ban-su-dung)
4. [Cau truc du an](#4-cau-truc-du-an)
5. [Cai dat Backend (be-foodgo)](#5-cai-dat-backend-be-foodgo)
6. [Cai dat Frontend (Flutter)](#6-cai-dat-frontend-flutter)
7. [Cac package dependency](#7-cac-package-dependency)
8. [Tai khoan test](#8-tai-khoan-test)
9. [Cac luu y can thiet](#9-cac-luu-y-can-thiet)
10. [Cau truc API](#10-cau-truc-api)

---

## 1. Tong quan

FoodGo la he thong giao do an truc tuyen gom:

| Module | Mo ta |
|---|---|
| `be-foodgo` | Backend REST API (Spring Boot + Firebase Firestore) |
| `fe_foodgo_admin` | Ung dung quan tri he thong (Flutter) |
| `fe_foodgo_portal` | Ung dung nguoi ban (Flutter) |
| `fe_food_go_driver` | Ung dung tai xe giao hang (Flutter) |

### Vai tro nguoi dung

| Vai tro | Ma | Mo ta |
|---|---|---|
| Khach hang | 1 | Tim kiem quan, dat hang, theo doi don |
| Tai xe | 2 | Nhan don, giao hang, GPS tracking |
| Nguoi ban | 3 | Quan ly cua hang, thuc don, xu ly don |
| Quan tri vien | 4 | Cau hinh he thong, doi soat tai chinh |

---

## 2. Cong nghe su dung

### Backend (`be-foodgo`)

| Thanh phan | Cong nghe |
|---|---|
| Ngon ngu lap trinh | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Ho so du lieu | Firebase Firestore |
| Realtime Database | Firebase Realtime Database |
| Authentication | Firebase Admin SDK + JWT |
| Storage | Firebase Cloud Storage, Cloudinary |
| API Documentation | SpringDoc OpenAPI |
| Build tool | Maven |
| Email | Spring Boot Mail (Gmail SMTP) |
| WebSocket | Spring WebSocket (STOMP) |

### Frontend (`fe_foodgo_*`)

| Thanh phan | Cong nghe |
|---|---|
| Framework | Flutter |
| Ngon ngu | Dart |
| State Management | flutter_bloc |
| Database | Firebase Firestore, Firebase Realtime Database |
| Authentication | Firebase Authentication |
| Maps | flutter_map (OpenStreetMap) |
| Location | Geolocator |
| Real-time | STOMP (WebSocket) |
| Push Notification | Firebase Cloud Messaging (FCM) |
| Dependency Injection | get_it |

---

## 3. Phien ban su dung

### Backend

| Thanh phan | Phien ban |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Maven | 3.6+ |
| Firebase Admin SDK | 9.2.0 |
| google-cloud-storage | 2.47.0 |
| JJWT | 0.12.6 |
| SpringDoc OpenAPI | 2.8.4 |

### Frontend (tat ca 3 app)

| Thanh phan | Phien ban |
|---|---|
| Flutter SDK | 3.11.1 |
| Dart SDK | 3.11.1 |
| Android Gradle Plugin | 8.x |
| Kotlin | 1.9.x |
| iOS Deployment Target | 12.0+ |

Kiem tra phien ban Flutter:

```bash
flutter --version
```

---

## 4. Cau truc du an

```
LTDD_BTL/
|
|-- be-foodgo/                    # Backend Spring Boot
|   |-- src/main/java/com/example/be_foodgo/
|   |   |-- config/               # Cau hinh Firebase, Security, CORS
|   |   |-- controller/           # REST API endpoints
|   |   |-- dto/                  # Data Transfer Object
|   |   |-- model/               # Firestore document mapping
|   |   |-- repository/           # Truy van Firestore
|   |   |-- seeder/              # Khoi tao du lieu mau
|   |   |-- service/             # Logic nghiep vu
|   |   |-- exception/           # Xu ly loi tap trung
|   |   |-- constant/            # Enum va hang so
|   |   |-- websocket/           # Cau hinh WebSocket
|   |-- src/main/resources/
|   |   |-- application.properties
|   |   |-- firebase-service-account.json
|   |-- pom.xml
|
|-- fe_foodgo_admin/              # Ung dung Quan tri vien (Flutter)
|   |-- lib/
|   |-- android/
|   |-- ios/
|   |-- pubspec.yaml
|
|-- fe_food_go_portal/            # Ung dung Nguoi ban (Flutter)
|   |-- lib/
|   |-- android/
|   |-- ios/
|   |-- pubspec.yaml
|
|-- fe_food_go_driver/            # Ung dung Tai xe (Flutter)
|   |-- lib/
|   |-- android/
|   |-- ios/
|   |-- pubspec.yaml
|
|-- docs/                          # Tai lieu du an
```

---

## 5. Cai dat Backend (`be-foodgo`)

### 5.1. Yeu cau

- **JDK 21** tro len
- **Maven 3.6+** (hoac su dung Maven wrapper)
- **Tai khoan Firebase** voi Firestore, Realtime Database, Storage
- **firebase-service-account.json** (lay tu Firebase Console)

### 5.2. Cac buoc cai dat

**1. Clone va di chuyen vao thu muc:**

```bash
git clone <repository-url>
cd be-foodgo
```

**2. Kiem tra Java:**

```bash
java -version
# Dam bao ket qua la Java 21
```

**3. Cai dat Maven dependencies:**

```bash
./mvnw install        # macOS / Linux
mvnw.cmd install      # Windows
```

### 5.3. Cau hinh Firebase

1. Truy cap [Firebase Console](https://console.firebase.google.com/)
2. Tao project moi hoac chon project hien co
3. Kich hoat **Firestore Database** (Test Mode hoac Production)
4. Kich hoat **Realtime Database**
5. Kich hoat **Firebase Storage**
6. Vao **Project Settings** -> **Service accounts** -> **Generate new private key**
7. Tai file JSON ve, doi ten thanh `firebase-service-account.json`
8. Dat file vao `src/main/resources/`

> **Luu y bao mat:** File `firebase-service-account.json` chua credentials nhay cam. **Khong duoc commit len Git.** File nay da duoc them vao `.gitignore`.

### 5.4. Cau hinh `application.properties`

File `src/main/resources/application.properties` da co san. Kiem tra cac cau hinh sau:

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

> **Luu y:** Neu muon gui email thuc su, can tao **App Password** tu tai khoan Google:
> 1. Vao [Google Account](https://myaccount.google.com) -> **Security** -> **2-Step Verification**
> 2. Bat xac minh 2 buoc, sau do vao **App passwords**
> 3. Tao app password moi va thay vao `spring.mail.password`

### 5.5. Chay Backend

```bash
# Chay bang Maven
./mvnw spring-boot:run        # macOS / Linux
mvnw.cmd spring-boot:run       # Windows

# Hoac build JAR roi chay
./mvnw clean package -DskipTests
java -jar target/be-foodgo-0.0.1-SNAPSHOT.jar
```

Khi chay thanh cong, console se hien:

```
Khoi tao Firebase thanh cong.
Firestore bean da duoc tao thanh cong.
```

Backend chay tai: `http://localhost:8086`

### 5.6. Seed du lieu mau

`FirebaseDataSeeder` tu dong chay khi ung dung khoi dong. Neu can seed lai, goi:

```java
DataSeeder.clearAllSeededData();
```

roi khoi dong lai ung dung.

---

## 6. Cai dat Frontend (Flutter)

### 6.1. Yeu cau he thong

- **Flutter SDK:** >= 3.11.1
- **Dart SDK:** >= 3.11.1
- **Android SDK** (neu build Android)
- **Xcode + CocoaPods** (neu build iOS, chi tren macOS)
- **Git**

### 6.2. Cai dat cho tat ca app Flutter

Cac buoc giong nhau cho `fe_foodgo_admin`, `fe_food_go_portal`, `fe_food_go_driver`:

**1. Clone va di chuyen:**

```bash
cd fe_foodgo_admin       # hoac fe_food_go_portal / fe_food_go_driver
```

**2. Cai dat dependencies:**

```bash
flutter pub get
```

**3. Cau hinh Firebase cho tung app:**

a) Tao project Firebase tai [Firebase Console](https://console.firebase.google.com/)

b) **Android:**
- Vao Project Settings -> Your apps -> Android app
- Tai `google-services.json` ve
- Dat vao `android/app/google-services.json`

c) **iOS (chi macOS):**
- Tai `GoogleService-Info.plist` ve
- Dat vao `ios/Runner/GoogleService-Info.plist`

d) Kich hoat trong Firebase Console:
- **Authentication** -> Sign-in method -> bat **Email/Password**
- **Firestore Database** -> Create database
- **Realtime Database** -> Create database
- **Storage** -> Create storage
- **Cloud Messaging** -> Lay Server key de gui push notification

**4. Cau hinh Android (`android/app/build.gradle`):**

```groovy
defaultConfig {
    minSdkVersion 21
    // ...
}
```

**5. Them Google Services plugin:**

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

**6. Them quyen Android (`android/app/src/main/AndroidManifest.xml`):**

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

**7. Chay iOS (chi macOS):**

```bash
cd ios
pod install --repo-update
cd ..
```

**8. Chay ung dung:**

```bash
# Che do debug
flutter run

# Build APK debug
flutter build apk --debug

# Build APK release
flutter build apk --release

# Build iOS
flutter build ios
```

---

## 7. Cac package dependency

### 7.1. Backend (`be-foodgo` / `pom.xml`)

| Package | Phien ban | Muc dich |
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

### 7.2. `fe_foodgo_admin` / `fe_food_go_portal` (`pubspec.yaml`)

| Package | Phien ban | Muc dich |
|---|---|---|
| flutter_bloc | ^8.1.6 | State management |
| fl_chart | ^0.66.0 | Bieu do thong ke |
| intl | ^0.19.0 / ^0.20.2 | Đinh dang ngay thang |
| firebase_core | ^4.9.0 | Firebase core |
| firebase_auth | ^6.5.1 | Xac thuc |
| dio | ^5.9.2 | HTTP client |
| shared_preferences | ^2.5.5 | Luu tru cuc bo |
| image_picker | ^1.1.2 / ^1.2.2 | Chon hinh |
| file_picker | ^8.1.2 | Chon file |
| firebase_storage | ^13.4.2 | Firebase Storage (portal) |
| flutter_map | ^8.3.0 | Ban do (portal) |
| latlong2 | ^0.9.1 | Toa do dia ly |
| geolocator | ^14.0.2 | Lay vi tri |
| geolocator_windows | ^0.2.5 | Geolocator Windows |
| easy_localization | ^3.0.7 | Đa ngon ngu (portal) |
| flutter_lints | ^6.0.0 | Lint rules |

### 7.3. `fe_food_go_driver` (`pubspec.yaml`)

| Package | Phien ban | Muc dich |
|---|---|---|
| flutter_bloc | ^8.1.6 | State management |
| equatable | ^2.0.5 | So sanh doi tuong |
| dartz | ^0.10.1 | Functional programming |
| rxdart | ^0.28.0 | Reactive extensions |
| get_it | ^8.0.3 | Dependency injection |
| shared_preferences | ^2.3.5 | Luu tru cuc bo |
| flutter_secure_storage | ^9.2.4 | Luu tru an toan |
| intl | ^0.20.2 | Đinh dang ngay thang |
| http | ^1.2.0 | HTTP client |
| stomp_dart_client | ^3.0.1 | WebSocket STOMP |
| geolocator | ^13.0.2 | Lay vi tri |
| permission_handler | ^11.3.1 | Xu ly quyen |
| flutter_map | ^7.0.2 | Ban do |
| latlong2 | ^0.9.1 | Toa do dia ly |
| google_fonts | ^6.2.1 | Phong chu |
| shimmer | ^3.0.0 | Hieu ung loading |
| pinput | ^5.0.0 | Nhap OTP |
| url_launcher | ^6.3.1 | Mo URL |
| firebase_core | ^3.12.1 | Firebase core |
| cloud_firestore | ^5.6.6 | Cloud Firestore |
| firebase_database | ^11.3.5 | Realtime Database |
| firebase_auth | ^5.5.3 | Xac thuc |
| firebase_messaging | ^15.2.4 | Push notification |
| flutter_local_notifications | ^21.0.0 | Thong bao cuc bo |

---

## 8. Tai khoan test

### 8.1. Backend (du lieu da duoc seed tu dong)

Khi chay backend, `FirebaseDataSeeder` se tu dong tao cac tai khoan test sau trong Firestore:

| User ID | Email | Mat khau | Vai tro |
|---|---|---|---|
| `user_001` | `khachhang@gmail.com` | `Khoi123@` | Khach hang (1), Tai xe (2), Nguoi ban (3) |
| `user_002` | `admin@foodgo.com` | `Admin123@` | Quan tri vien (4) |
| `user_003` | `taixe@gmail.com` | `Taixe123@` | Tai xe (2) |
| `user_004` | `luudinhnghia30012005@gmail.com` | `Nghia123@` | Nguoi ban (3) |
| `user_005` | `taixe3@gmail.com` | `Taixe123@` | Tai xe (2) |
| `user_006` | `taixe2@gmail.com` | `Taixe123@` | Tai xe (2) |

> **Luu y:** Mat khau duoc luu duoi dang BCrypt hash. Cac mat khau tren la gia tri goc duoc su dung khi tao hash. Neu can xac thuc Firebase Authentication, can tao tay cac tai khoan nay trong Firebase Console.

### 8.2. Firestore Collections da co san (seed data)

Du lieu mau da duoc seed vao cac collection: `users`, `wallets`, `transactions`, `system_configs`, `categories`, `stores`, `products`, `banners`, `vouchers`, `reviews`, `orders`, `customer_profiles`, `driver_profiles`, `merchant_profiles`, `admin_profiles`.

---

## 9. Cac luu y can thiet

### 9.1. Tat ca cac app (Backend + Flutter)

1. **Firebase Configuration bat buoc:** File `google-services.json` (Android) va `GoogleService-Info.plist` (iOS) phai co. Neu khong co, ung dung se loi khi khoi dong.

2. **Dich vu Firebase can bat:** Authentication (Email/Password), Firestore Database, Realtime Database, Storage, Cloud Messaging.

3. **Ket noi Internet:** Tat ca cac app deu can internet de hoat dong (truy cap Firebase, goi API backend, WebSocket).

4. **CORS:** Backend cau hinh cho phep cross-origin tu cac origin Flutter (Android/iOS/Web).

### 9.2. Backend (`be-foodgo`)

1. **Port mac dinh:** `8086`. Neu muon doi port, sua `server.port` trong `application.properties`.

2. **JWT Secret:** Chuoi `jwt.secret` hien tai la chuoi test. Trong moi truong production, can doi thanh chuoi ngau nhien 256-bit.

3. **Firebase Service Account:** Khong commit file `firebase-service-account.json` len Git. Neu lam mat file, vao Firebase Console -> Project Settings -> Service accounts -> Generate new private key.

4. **Email Configuration:** Neu `app.email.enabled=false`, email se chi hien tren console (dev mode). Khi can gui email thuc, tao App Password tu Google va dien vao `spring.mail.password`.

5. **Cloudinary:** Neu mat API secret, tao tai khoan Cloudinary moi va cap nhat trong `application.properties`.

### 9.3. Frontend (Flutter)

1. **Quyen Vi tri:** Tat ca cac app deu can quyen vi tri (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION). Neu khong cap quyen, mot so chuc nang se khong hoat dong.

2. **Android minSdkVersion:** Phai >= 21. Kiem tra trong `android/app/build.gradle`.

3. **iOS Info.plist:** Can them mo ta quyen vi tri vao `ios/Runner/Info.plist` (chi macOS/iOS).

4. **WebSocket/STOMP:** Ung dung driver ket noi den backend qua WebSocket (STOMP) de nhan don giao hang real-time. Dam bao backend da bat WebSocket endpoint.

5. **API URL:** Cau hinh dia chi backend API trong `lib/core/api/api_constants.dart` hoac `lib/injection_container.dart` cua tung app. Mac dinh la `http://10.0.2.2:8086` (Android Emulator) hoac `http://localhost:8086` (iOS Simulator).

6. **Foreground Service (Driver):** Ung dung driver su dung Foreground Service de cap nhat vi tri khi o nen. Dam bao quyen `FOREGROUND_SERVICE` va `FOREGROUND_SERVICE_LOCATION` da duoc khai bao.

7. **Push Notification (Driver):** Khi app o nen, push notification duoc gui qua FCM. Can cau hinh FCM server key phia backend (`firebase.messaging.server-key`) de gui notification.

8. **Thuc muc assets:** Kiem tra thu muc `assets/` co ton tai trong tung app Flutter. Neu thieu, tao thu muc:

```bash
# fe_food_go_driver
mkdir -p assets/lang assets/img

# fe_food_go_portal
mkdir -p assets/translations
```

### 9.4. Cac loi thuong gap

| Loi | Giai phap |
|---|---|
| `Unable to find git` | Cai dat git va them vao PATH |
| `google-services.json` khong hop le | Tai lai tu Firebase Console, kiem tra ten package |
| `minSdkVersion too low` | Tang `minSdkVersion` trong `build.gradle` len 21+ |
| `pod install` that bai (iOS) | Chay `cd ios && pod install --repo-update` |
| Khong gui duoc email | Tao App Password tu Google, kiem tra `app.email.enabled=true` |
| WebSocket khong ket noi | Kiem tra backend da chay, kiem tra URL API trong Flutter |
| Firestore permission denied | Kiem tra rules Firestore trong Firebase Console |

---

## 10. Cau truc API

### 10.1. Base URL

```
http://localhost:8086/api
```

### 10.2. Cau truc chung response

```json
{
  "success": true,
  "message": "Thanh cong",
  "data": { ... }
}
```

### 10.3. Authentication

- **Login:** `POST /api/auth/login` - tra ve JWT token
- **Register:** `POST /api/auth/register`
- **Refresh Token:** `POST /api/auth/refresh`
- **Logout:** `POST /api/auth/logout`

JWT token duoc gui kem trong header:

```
Authorization: Bearer <token>
```

### 10.4. Cac endpoint chinh

| Nhom | Prefix | Mo ta |
|---|---|---|
| Auth | `/api/auth/*` | Đang nhap, dang ky, refresh token |
| User | `/api/users/*` | Thong tin nguoi dung |
| Store | `/api/stores/*` | Quan ly cua hang |
| Product | `/api/products/*` | Quan ly san pham |
| Order | `/api/orders/*` | Xu ly don hang |
| Voucher | `/api/vouchers/*` | Quan ly voucher |
| Payment | `/api/payments/*` | Thanh toan |
| Wallet | `/api/wallets/*` | Vi tien |
| Review | `/api/reviews/*` | Đanh gia |
| Category | `/api/categories/*` | Danh muc |
| Banner | `/api/banners/*` | Banner quang cao |
| Profile | `/api/profiles/*` | Profile nguoi dung |
| Stats | `/api/stats/*` | Thong ke |
| WebSocket | `/ws` | Kenh real-time (STOMP) |

### 10.5. Vai tro va quyen

| Ma | Vai tro | Quyen |
|---|---|---|
| 1 | Khach hang | Tim quan, dat hang, theo doi don, danh gia |
| 2 | Tai xe | Nhan don, giao hang, GPS tracking |
| 3 | Nguoi ban | Quan ly cua hang, thuc don, xu ly don |
| 4 | Quan tri vien | Cau hinh he thong, doi soat tai chinh |

### 10.6. Trang thai don hang

| Gia tri | Trang thai |
|---|---|
| 0 | Cho xac nhan |
| 1 | Dang chuan bi |
| 2 | Dang giao |
| 3 | Hoan thanh |
| 4 | Da huy |

### 10.7. Phuong thuc thanh toan

| Gia tri | Mo ta |
|---|---|
| `cash` | Tien mat (COD) |
| `momo` | Vi MoMo |
| `zalo` | ZaloPay |
| `vnpay` | VNPay |
| `card` | The ngan hang |

---

**Phien ban:** 0.0.1-SNAPSHOT
