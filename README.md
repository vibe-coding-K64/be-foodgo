# FoodGo Backend

> Backend REST API cho ứng dụng giao đồ ăn FoodGo, được xây dựng bằng **Spring Boot** kết nối **Firebase Firestore** làm cơ sở dữ liệu.

---

## Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Kiến trúc hệ thống](#3-kiến-trúc-hệ-thống)
4. [Cấu trúc dự án](#4-cấu-trúc-dự-án)
5. [Cài đặt](#5-cài-đặt)
6. [Cấu hình Firebase](#6-cấu-hình-firebase)
7. [Chạy ứng dụng](#7-chạy-ứng-dụng)
8. [Các collection Firestore](#8-các-collection-firestore)
9. [Vai trò người dùng](#9-vai-trò-người-dùng)
10. [Cấu trúc API](#10-cấu-trúc-api)
11. [Quy tắc phát triển](#11-quy-tắc-phát-triển)
12. [Tài liệu tham khảo](#12-tài-liệu-tham-khảo)

---

## 1. Tổng quan

FoodGo là hệ thống giao đồ ăn trực tuyến với 4 vai trò chính:

| Vai trò         | Mã | Mô tả                                      |
| --------------- | -- | ------------------------------------------ |
| Khách hàng      | 1  | Tìm kiếm quán, đặt hàng, theo dõi đơn     |
| Tài xế          | 2  | Nhận đơn, giao hàng, GPS tracking         |
| Người bán       | 3  | Quản lý cửa hàng, thực đơn, xử lý đơn    |
| Quản trị viên   | 4  | Cấu hình hệ thống, đối soát tài chính     |

Một người dùng có thể sở hữu nhiều vai trò cùng lúc (ví dụ: vừa là khách hàng, vừa là tài xế).

---

## 2. Công nghệ sử dụng

| Thành phần          | Công nghệ / Phiên bản         |
| ------------------ | ---------------------------- |
| Ngôn ngữ lập trình | Java 21                      |
| Framework          | Spring Boot 4.0.6            |
| Cơ sở dữ liệu     | Firebase Firestore           |
| Authentication     | Firebase Admin SDK           |
| Build tool         | Maven                        |
| Validation         | Spring Boot Validation       |
| Security           | Spring Security              |
| API Documentation  | Swagger / SpringDoc (nếu có) |

---

## 3. Kiến trúc hệ thống

Hệ thống sử dụng **Layered Architecture** (Kiến trúc phân lớp):

```
Client (HTTP Request)
       │
       ▼
┌─────────────────┐
│   Controller     │  Nhận request → gọi Service
└────────┬────────┘
         │ call
         ▼
┌─────────────────┐
│    Service      │  Logic nghiệp vụ → gọi Repository
└────────┬────────┘
         │ call
         ▼
┌─────────────────┐
│  Repository     │  Truy vấn Firestore
└────────┬────────┘
         │ CRUD
         ▼
  Firebase Firestore
```

---

## 4. Cấu trúc dự án

```
be-foodgo/
│
├── .mvn/                              # Maven wrapper
├── .vscode/                           # Cấu hình VS Code
├── docs/                              # Tài liệu dự án
│   ├── core_features.md               # Mô tả chức năng cốt lõi
│   ├── firebase_collections.md        # Cấu trúc Firestore
│   └── project_structure.md           # Tài liệu này
├── src/
│   ├── main/
│   │   ├── java/com/example/be_foodgo/
│   │   │   ├── BeFoodgoApplication.java   # Entry point
│   │   │   ├── config/                   # Cấu hình hệ thống
│   │   │   ├── constant/                 # Enum & hằng số
│   │   │   ├── controller/              # REST API endpoints
│   │   │   ├── dto/                     # Data Transfer Object
│   │   │   ├── exception/               # Xử lý lỗi tập trung
│   │   │   ├── model/                   # Firestore document mapping
│   │   │   ├── repository/              # Giao tiếp Firestore
│   │   │   ├── seeder/                  # Khởi tạo dữ liệu mẫu
│   │   │   └── service/                 # Logic nghiệp vụ
│   │   └── resources/
│   │       ├── application.properties    # Cấu hình Spring
│   │       └── firebase-service-account.json  # Firebase credentials
│   └── test/
│       └── java/com/example/be_foodgo/
│
├── pom.xml                            # Maven dependencies
├── mvnw / mvnw.cmd                   # Maven wrapper scripts
├── .gitignore
└── README.md
```

### Chi tiết từng package

| Package      | Chức năng                                                                              |
| ------------ | -------------------------------------------------------------------------------------- |
| `config`     | Cấu hình Firebase SDK, Security, Swagger, CORS...                                      |
| `constant`   | Enum (trạng thái, role...) và hằng số dùng chung                                      |
| `controller` | REST API endpoints. Nhận request, trả response. Không chứa logic nghiệp vụ.          |
| `dto`        | Data Transfer Object. Đóng gói dữ liệu gửi/nhận qua API.                              |
| `exception`  | Global Exception Handler. Xử lý lỗi tập trung.                                        |
| `model`      | Entity/document mapping với Firestore (annotation `@DocumentReference`...).            |
| `repository` | Truy vấn Firestore bằng Firebase Admin SDK.                                          |
| `seeder`     | Khởi tạo dữ liệu mẫu khi ứng dụng lên (chạy 1 lần nếu chưa có dữ liệu).             |
| `service`    | Logic nghiệp vụ. Xử lý các tác vụ phức tạp, gọi repository để truy vấn dữ liệu.    |

---

## 5. Cài đặt

### Yêu cầu

- **Java Development Kit (JDK) 21** trở lên
- **Maven 3.6+** (hoặc sử dụng Maven wrapper có sẵn)
- **Tài khoản Firebase** với Firestore được kích hoạt
- **firebase-service-account.json** (file credentials từ Firebase Console)

### Các bước

1. **Clone dự án**

```bash
git clone <repository-url>
cd be-foodgo
```

2. **Cài đặt Java 21**

Đảm bảo JDK 21 được cài đặt và biến môi trường `JAVA_HOME` trỏ đúng:

```bash
java -version  # Kiểm tra phiên bản Java
```

3. **Cài đặt Maven dependencies**

```bash
./mvnw install      # macOS/Linux
mvnw.cmd install    # Windows
```

---

## 6. Cấu hình Firebase

### 6.1. Tạo project Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/).
2. Tạo project mới hoặc chọn project hiện có.
3. Kích hoạt **Firestore Database** ở chế độ **Test Mode** (hoặc Production với quy tắc phù hợp).
4. (Tùy chọn) Kích hoạt **Realtime Database** cho GPS tracking của tài xế.

### 6.2. Lấy Service Account Credentials

1. Trong Firebase Console, vào **Project Settings** → tab **Service accounts**.
2. Chọn **Generate new private key** → tải file JSON.
3. Đổi tên file thành `firebase-service-account.json`.
4. Đặt file vào thư mục `src/main/resources/`.

> **Lưu ý bảo mật:** File `firebase-service-account.json` chứa credentials nhạy cảm, **không được commit lên Git**. File này đã được thêm vào `.gitignore`.

### 6.3. Cấu hình application.properties

File `src/main/resources/application.properties`:

```properties
spring.application.name=be-foodgo
```

Các cấu hình khác (database URL, Firebase path...) được load tự động từ `firebase-service-account.json` qua `FirebaseConfig.java`.

---

## 7. Chạy ứng dụng

### Chạy bằng Maven

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### Chạy bằng JAR đã build

```bash
./mvnw clean package -DskipTests
java -jar target/be-foodgo-0.0.1-SNAPSHOT.jar
```

### Kiểm tra

Sau khi chạy thành công, ứng dụng sẽ khởi tạo Firebase và tự động seed dữ liệu mẫu vào Firestore (nếu chưa có dữ liệu). Kiểm tra output console để xác nhận:

```
Khoi tao Firebase thanh cong.
Firestore bean da duoc tao thanh cong.
```

Mặc định, ứng dụng chạy tại: `http://localhost:8080`.

### Tài khoản test có sẵn

| User ID    | Email                     | Vai trò                        |
| ---------- | ------------------------- | ------------------------------ |
| `user_001` | `khachhang@gmail.com`     | Khách hàng (1), Tài xế (2), Người bán (3) |
| `user_002` | —                         | Quản trị viên (4)              |

---

## 8. Các collection Firestore

Hệ thống sử dụng các collection chính sau:

### Root Collections

| Collection           | Mô tả                                              |
| -------------------- | -------------------------------------------------- |
| `users`              | Tài khoản người dùng (email, password, roles)     |
| `system_configs`      | Cấu hình hệ thống (phí platform, phí giao hàng...) |
| `wallets`            | Ví tiền của merchant và driver                    |
| `transactions`       | Lịch sử giao dịch                                  |
| `categories`  | Danh mục hiển thị trang chủ (Cơm, Phở...)         |
| `stores`             | Thông tin cửa hàng                                 |
| `products`           | Sản phẩm / món ăn                                  |
| `banners`            | Banner quảng cáo trang chủ                         |
| `vouchers`           | Voucher hệ thống                                   |
| `system_vouchers`    | Voucher đổi bằng điểm thưởng                       |
| `reviews`            | Đánh giá cửa hàng                                  |
| `orders`             | Đơn hàng                                           |
| `customer_profiles`  | Profile khách hàng + sub-collections               |
| `driver_profiles`    | Profile tài xế + sub-collections                   |
| `merchant_profiles`   | Profile người bán + sub-collections                |
| `admin_profiles`      | Profile quản trị viên                              |

### Sub-collections

| Collection                                             | Mô tả                              |
| ----------------------------------------------------- | ---------------------------------- |
| `users/{userId}/search_history`                       | Lịch sử tìm kiếm                   |
| `customer_profiles/{userId}/addresses`                | Địa chỉ giao hàng                  |
| `customer_profiles/{userId}/payment_methods`          | Phương thức thanh toán             |
| `customer_profiles/{userId}/notifications`           | Thông báo khách hàng               |
| `customer_profiles/{userId}/cart`                     | Giỏ hàng                           |
| `customer_profiles/{userId}/my_vouchers`             | Voucher đã sở hữu                  |
| `driver_profiles/{userId}/notifications`             | Thông báo tài xế                   |
| `merchant_profiles/{userId}/notifications`           | Thông báo người bán                |

### Realtime Database

| Đường dẫn                    | Mô tả                              |
| ---------------------------- | ---------------------------------- |
| `active_drivers/{driverId}`  | Tọa độ GPS của tài xế đang hoạt động |

> Chi tiết đầy đủ về cấu trúc fields, kiểu dữ liệu, và dữ liệu mẫu của từng collection, xem [docs/firebase_collections.md](./docs/firebase_collections.md).

---

## 9. Vai trò người dùng

Hệ thống phân quyền theo giá trị `roles` trong `users`:

| Mã vai trò | Tên              | Quyền hạn chính                                             |
| ---------- | ---------------- | ----------------------------------------------------------- |
| 1          | Khách hàng       | Tìm quán, đặt hàng, theo dõi đơn, đánh giá, tích điểm     |
| 2          | Tài xế           | Nhận đơn, giao hàng, GPS tracking, quản lý thu nhập       |
| 3          | Người bán        | Quản lý cửa hàng, thực đơn, xử lý đơn, quản lý tài chính  |
| 4          | Quản trị viên    | Cấu hình hệ thống, kiểm duyệt, đối soát tài chính         |

### Trạng thái đơn hàng

| Giá trị | Tên           | Mô tả                       |
| ------- | ------------- | --------------------------- |
| 0       | Chờ xác nhận  | Đơn hàng chờ quán xác nhận  |
| 1       | Đang chuẩn bị | Quán đang chuẩn bị món      |
| 2       | Đang giao     | Tài xế đang giao hàng       |
| 3       | Hoàn thành   | Đã giao thành công           |
| 4       | Đã hủy        | Đơn hàng đã bị hủy           |

### Hạng thành viên (membershipTier)

| Giá trị | Hạng       |
| ------- | ---------- |
| 0       | Đồng       |
| 1       | Bạc        |
| 2       | Vàng       |
| 3       | Kim Cương  |

---

## 10. Cấu trúc API

Các REST API endpoints được tổ chức theo module. Chi tiết xem trong từng controller.

### Cấu trúc chung response

```json
{
  "success": true,
  "message": "Thành công",
  "data": { ... }
}
```

### Authentication

Hiện tại hệ thống sử dụng xác thực cơ bản qua email/password được lưu trong Firestore (`users` collection). Mật khẩu nên được mã hóa trước khi lưu.

### Phương thức thanh toán

| Giá trị    | Mô tả             |
| ---------- | ----------------- |
| `cash`     | Tiền mặt (COD)    |
| `momo`     | Ví MoMo           |
| `zalo`     | ZaloPay           |
| `vnpay`    | VNPay             |
| `card`     | Thẻ ngân hàng     |

---

## 11. Quy tắc phát triển

### Quy tắc đặt tên

| Loại            | Quy tắc            | Ví dụ                   |
| --------------- | ------------------ | ----------------------- |
| Class Java      | PascalCase         | `UserController.java`   |
| Method          | camelCase          | `getUserById()`         |
| Variable        | camelCase          | `userId`, `isActive`    |
| Package         | lowercase          | `controller`, `dto`     |
| Enum            | PascalCase         | `OrderStatus.java`      |
| Enum constant   | SCREAMING_SNAKE_CASE | `PENDING`, `COMPLETED` |
| Firestore field | camelCase          | `createdAt`, `userId`   |

### Quy tắc commit Git

| Prefix     | Môi trường sử dụng                           |
| ---------- | --------------------------------------------- |
| `feat`     | Thêm chức năng mới                            |
| `fix`      | Sửa lỗi                                       |
| `docs`     | Cập nhật tài liệu                             |
| `refactor` | Tái cấu trúc code, không thay đổi chức năng  |
| `chore`    | Cập nhật phụ thuộc, build script              |

### Data Seeding

`FirebaseDataSeeder` tự động chạy khi ứng dụng khởi động. Nó sẽ seed dữ liệu mẫu nếu collection chưa có dữ liệu. Để xóa toàn bộ dữ liệu seed và seed lại, gọi `DataSeeder.clearAllSeededData()` rồi khởi động lại ứng dụng.

---

## 12. Tài liệu tham khảo

| Tài liệu                    | Mô tả                                     |
| -------------------------- | ----------------------------------------- |
| `docs/project_structure.md` | Cấu trúc dự án chi tiết                  |
| `docs/firebase_collections.md` | Cấu trúc Firestore đầy đủ              |
| `docs/core_features.md`    | Mô tả chức năng cốt lõi theo từng vai trò |
| [Firebase Admin Java SDK](https://firebase.google.com/docs/admin/java) | Tài liệu chính thức Firebase |
| [Spring Boot Documentation](https://spring.io/projects/spring-boot) | Tài liệu Spring Boot |

---

**Phiên bản:** 0.0.1-SNAPSHOT
