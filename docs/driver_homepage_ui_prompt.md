# Prompt: Giao diện Trang Chủ Tài Xế (Driver Homepage)

## 1. Tổng quan

Xây dựng giao diện **trang chủ dành cho tài xế (Role 2)** cho ứng dụng giao đồ ăn **FoodGo**, sử dụng **Flutter** cho mobile. Giao diện cần thể hiện rõ vai trò tài xế với các thông tin: trạng thái online/offline, đơn hàng hiện tại, thống kê ngày, ví tài xế, và danh sách đơn hàng gần đây.

---

## 2. Nguồn dữ liệu (Data Source)

Giao diện sử dụng dữ liệu từ các API endpoint của backend Spring Boot đã triển khai:

| Dữ liệu | Nguồn | Mô tả |
|---|---|---|
| DriverProfile | `GET /api/drivers/{driverId}/profile` | Thông tin hồ sơ tài xế |
| Order hiện tại | `GET /api/delivery/orders/current` | Đơn hàng đang giao (nếu có) |
| Danh sách đơn | `GET /api/delivery/orders?driverId={id}&status={status}` | Đơn hàng theo trạng thái |
| Wallet | `GET /api/wallet/{driverId}` | Số dư, thu nhập, rút tiền |
| Notifications | `GET /api/notifications/user/{userId}` | Thông báo tài xế |
| Cập nhật trạng thái | `PUT /api/delivery/orders/{orderId}/status` | Cập nhật trạng thái đơn |
| Cập nhật vị trí | `PUT /api/delivery/driver/location` | Gửi GPS realtime |
| Toggle online/offline | `PUT /api/drivers/{driverId}/status` | Bật/tắt trạng thái nhận đơn |

> **Lưu ý:** `driverId` và `userId` của tài xế là cùng một giá trị, lấy từ JWT token đã đăng nhập.

---

## 3. Thông tin chi tiết các collections Firestore

### 3.1. DriverProfile
| Field | Kiểu | Mô tả |
|---|---|---|
| `userId` | String | ID tài xế |
| `isActive` | boolean | Tài xế đang hoạt động |
| `vehiclePlate` | String | Biển số xe |
| `vehicleType` | String | Loại xe (motorcycle, bicycle, car) |
| `driverLicense` | String | Giấy phép lái xe |
| `fullName` | String | Họ tên đầy đủ |
| `phoneNumber` | String | Số điện thoại |
| `photoUrl` | String | URL ảnh đại diện |
| `rating` | double | Điểm đánh giá (0.0 - 5.0) |
| `totalTrips` | int | Tổng số chuyến đã hoàn thành |

### 3.2. Order (liên quan tài xế)
| Field | Kiểu | Mô tả |
|---|---|---|
| `id` | String | ID đơn hàng |
| `status` | int | 0=Chờ xác nhận, 1=Đang chuẩn bị, 2=Đang giao, 3=Hoàn thành, 4=Đã hủy |
| `driverId` | String | ID tài xế nhận đơn |
| `driverName` | String | Tên tài xế |
| `driverPhone` | String | SĐT tài xế |
| `vehiclePlate` | String | Biển số xe tài xế |
| `deliveryFee` | double | Phí giao hàng |
| `storeName` | String | Tên cửa hàng |
| `deliveryAddress` | String | Địa chỉ giao hàng |
| `receiverName` | String | Tên người nhận |
| `receiverPhone` | String | SĐT người nhận |
| `code` | String | Mã đơn hàng (hiển thị cho tài xế) |
| `note` | String | Ghi chú đơn hàng |

### 3.3. DriverLocation (Realtime Database)
| Field | Kiểu | Mô tả |
|---|---|---|
| `driverId` | String | ID tài xế |
| `lat` | double | Vĩ độ |
| `lng` | double | Kinh độ |
| `heading` | double | Hướng di chuyển (độ) |
| `speed` | double | Tốc độ (km/h) |
| `updatedAt` | long | Timestamp cập nhật |
| `isActive` | boolean | Tài xế đang online |

### 3.4. Wallet
| Field | Kiểu | Mô tả |
|---|---|---|
| `userId` | String | ID tài xế |
| `role` | String | Vai trò (DRIVER) |
| `balance` | double | Số dư khả dụng |
| `totalEarned` | double | Tổng thu nhập |
| `totalWithdrawn` | double | Tổng đã rút |
| `pendingBalance` | double | Số dư chờ xử lý |

### 3.5. Transaction
| Field | Kiểu | Mô tả |
|---|---|---|
| `id` | String | ID giao dịch |
| `userId` | String | ID tài xế |
| `type` | String | Loại: EARNING, WITHDRAWAL, REFUND |
| `amount` | double | Số tiền |
| `createdAt` | Date | Thời gian tạo |

### 3.6. Notification
| Field | Kiểu | Mô tả |
|---|---|---|
| `id` | String | ID thông báo |
| `type` | String | Loại: ORDER_ASSIGNED, ORDER_CANCELLED, NEW_ORDER, PAYMENT_RECEIVED |
| `isRead` | boolean | Đã đọc chưa |
| `message` | String | Nội dung thông báo |
| `createdAt` | Date | Thời gian tạo |

---

## 4. Yêu cầu giao diện (UI/UX)

### 4.1. Header / AppBar
- Ảnh đại diện tài xế (circle avatar, photoUrl)
- Tên tài xế (fullName)
- Badge xếp hạng (rating, hiển thị sao ★)
- Nút toggle **Online / Offline** (nổi bật, dễ bấm)
  - Trạng thái Online: nền xanh lá, icon glow
  - Trạng thái Offline: nền xám, icon mờ

### 4.2. Thẻ thông tin tài xế (Driver Info Card)
- Loại xe + biển số (vehicleType, vehiclePlate)
- Tổng số chuyến (totalTrips)
- Điểm đánh giá (rating)
- Số điện thoại (phoneNumber)

### 4.3. Thẻ đơn hàng hiện tại (Current Order Card)
Chỉ hiển thị khi có đơn đang giao (status = 2):
- Mã đơn hàng (code) - nổi bật, dễ nhìn
- Tên cửa hàng (storeName)
- Địa chỉ giao hàng (deliveryAddress)
- Tên & SĐT người nhận (receiverName, receiverPhone)
- Phí giao hàng (deliveryFee) - highlight tiền
- Ghi chú (note) nếu có
- **Nút hành động**: theo từng trạng thái:
  - Status 2 (Đang giao): "Đã nhận hàng" → chuyển 3, "Đã giao xong" → chuyển 3
  - Status 3 (Hoàn thành): hiển thị trạng thái, không có action
- Nút **Mở Google Maps** (dẫn đến deliveryAddress)
- Nút **Gọi người nhận** (receiverPhone)

### 4.4. Thống kê hôm nay (Today's Stats)
- Tổng số đơn hoàn thành hôm nay
- Thu nhập hôm nay (từ deliveryFee các đơn status=3 trong ngày)
- Số dư ví (balance)

### 4.5. Ví tài xế (Wallet Summary)
- Số dư khả dụng (balance) - số lớn, nổi bật
- Tổng thu nhập (totalEarned)
- Đang chờ (pendingBalance)
- Nút **"Rút tiền"** → điều hướng màn rút tiền
- Nút **"Lịch sử giao dịch"** → điều hướng màn lịch sử

### 4.6. Danh sách đơn hàng gần đây (Recent Orders)
- List hiển thị các đơn: mã đơn, cửa hàng, địa chỉ, phí, thời gian
- Filter tabs: "Tất cả", "Đang giao", "Hoàn thành", "Đã hủy"
- Tap vào item → điều hướng chi tiết đơn

### 4.7. Thông báo (Notifications)
- Icon chuông ở AppBar hoặc bottom nav
- Badge số thông báo chưa đọc
- Danh sách thông báo: ORDER_ASSIGNED, ORDER_CANCELLED, NEW_ORDER, PAYMENT_RECEIVED

### 4.8. Bottom Navigation Bar
Gồm 4 tab:
1. **Trang chủ** (home icon) - màn hiện tại
2. **Đơn hàng** (list icon) - danh sách tất cả đơn
3. **Ví của tôi** (wallet icon) - thông tin tài chính
4. **Hồ sơ** (person icon) - chỉnh sửa profile tài xế

---

## 5. Luồng nghiệp vụ chính

### 5.1. Nhận đơn hàng mới
1. Tài xế Online → hệ thống gửi thông báo `NEW_ORDER` hoặc `ORDER_ASSIGNED`
2. Tài xế nhấn "Nhận đơn" → `PUT /api/delivery/orders/{id}/status` status=2
3. Giao diện cập nhật → hiện Current Order Card
4. Gửi GPS realtime liên tục → `PUT /api/delivery/driver/location`

### 5.2. Cập nhật trạng thái đơn
1. Tài xế nhấn "Đã nhận hàng từ cửa hàng" → status=2 (đang giao)
2. Tài xế nhấn "Đã giao xong" → status=3 (hoàn thành)
3. Nếu khách hủy → thông báo `ORDER_CANCELLED`, cập nhật UI

### 5.3. Toggle Online/Offline
1. Tài xế bật/tắt toggle
2. Gọi `PUT /api/drivers/{driverId}/status`
3. Cập nhật DriverLocation.isActive trong Realtime Database
4. UI thay đổi màu sắc, ẩn/hiện nút nhận đơn

---

## 6. Ràng buộc kỹ thuật

- **Framework**: Flutter (không dùng code generation phức tạp, ưu tiên StatelessWidget + StatefulWidget thuần)
- **State Management**: Provider hoặc BLoC (tùy chọn, ưu tiên Provider cho đơn giản)
- **HTTP Client**: http package hoặc dio (có interceptor JWT)
- **Maps**: url_launcher để mở Google Maps / Google Maps Flutter
- **GPS**: geolocator + permission_handler
- **Local Storage**: shared_preferences để lưu driverId, token
- **Pull-to-refresh**: RefreshIndicator cho mọi danh sách
- **Error handling**: Hiển thị snackbar hoặc dialog khi API lỗi
- **Loading state**: Shimmer hoặc CircularProgressIndicator khi đang fetch data
- **Empty state**: Hiển thị placeholder khi không có đơn hàng
- **Responsive**: Hỗ trợ cả Android và iOS, kiểm tra trên nhiều kích thước màn hình

---

## 7. Cấu trúc thư mục đề xuất (Flutter)

```
lib/
├── main.dart
├── config/
│   ├── api_config.dart          # Base URL, headers
│   └── app_theme.dart            # Colors, typography
├── models/
│   ├── driver_profile.dart
│   ├── order.dart
│   ├── wallet.dart
│   ├── transaction.dart
│   └── notification.dart
├── services/
│   ├── api_service.dart         # HTTP calls + JWT interceptor
│   ├── auth_service.dart        # Token management
│   └── location_service.dart    # GPS tracking
├── providers/
│   ├── driver_provider.dart
│   ├── order_provider.dart
│   └── wallet_provider.dart
└── screens/
    └── driver/
        ├── driver_home_screen.dart
        ├── order_list_screen.dart
        ├── order_detail_screen.dart
        ├── wallet_screen.dart
        ├── transaction_history_screen.dart
        ├── withdraw_screen.dart
        ├── notification_screen.dart
        └── profile_screen.dart
    └── widgets/
        ├── driver_info_card.dart
        ├── current_order_card.dart
        ├── today_stats_card.dart
        ├── wallet_summary_card.dart
        ├── order_list_item.dart
        └── online_toggle_button.dart
```

---

## 8. Một số lưu ý quan trọng

- **Security**: Không hardcode token. Luôn lấy token từ SharedPreferences sau khi đăng nhập.
- **Realtime GPS**: Khi tài xế Online, cần chạy service GPS ở background (dùng flutter_background_service hoặc workmanager) và cập nhật lên Realtime Database mỗi 5-10 giây.
- **Push Notifications**: Cần tích hợp Firebase Cloud Messaging (FCM) để nhận thông báo đơn hàng mới khi app ở background.
- **Mã đơn hàng**: Hiển thị mã ngắn (6 ký tự) để tài xế dễ đọc, dễ xác nhận với cửa hàng và khách hàng.
- **Phone number**: Dùng url_launcher để gọi điện trực tiếp từ app (`tel:` scheme).
- **Maps**: Khi tap vào địa chỉ giao hàng hoặc nút "Chỉ đường", mở Google Maps với địa chỉ hoặc tọa độ GPS.
