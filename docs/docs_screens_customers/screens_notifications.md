# Màn Hình - Thông Báo (Notifications)

## 1. Màn Hình Thông Báo (NotificationsView)

**File:** `lib/features/notifications/views/notifications_view.dart`

**Class:** `NotificationsView`

**Mục đích chính:** Màn hình hiển thị danh sách thông báo của người dùng với dữ liệu thời gian thực từ Firestore.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **AppBar Gradient Xanh:** Tiêu đề "Thông báo", nút "Đánh dấu đã đọc" (text button)
- **Danh Sách Thông Báo (StreamBuilder từ NotificationService.getNotificationsStream()):**
  - Mỗi NotificationCard chứa:
    - Icon theo loại (settings_outlined / local_offer / receipt_long)
    - Màu icon tùy loại (xám / xanh lá / tím)
    - Tiêu đề thông báo
    - Nội dung tóm tắt (1 dòng)
    - Thời gian nhận (VD: "2 giờ trước")
    - Chấm đỏ nhỏ nếu chưa đọc (trạng thái isRead = false)
- **Trạng thái rỗng:**
  - Icon notifications_none (64x64)
  - "Chưa có thông báo nào"
- **Trạng thái loading:** CircularProgressIndicator
- **Trạng thái lỗi:** Icon error, thông báo lỗi, nút "Thử lại"

---

**Các hành động của người dùng (User Actions):**

- Bấm "Đánh dấu đã đọc" để gọi NotificationService.markAllAsRead()
- Bấm một thông báo:
  - Nếu chưa đọc: gọi NotificationService.markAsRead(id)
  - Mở NotificationDetailView với notification tương ứng
- Bấm "Thử lại" để tải lại Stream

---

## 2. Màn Hình Chi Tiết Thông Báo (NotificationDetailView)

**File:** `lib/features/notifications/views/notification_detail_view.dart`

**Class:** `NotificationDetailView`

**Mục đích chính:** Hiển thị đầy đủ nội dung chi tiết của một thông báo.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Banner Hình (nếu là thông báo khuyến mãi, type = 1):**
  - Hình chữ nhật 200px (unsplash, food image)
  - Lớp gradient phủ trên
  - Icon local_offer góc trái trên (nếu là khuyến mãi)
- **Tiêu Đề:** Icon + tên loại thông báo (in đậm)
- **Thời Gian:** "2 giờ trước - 22/05/2026" (màu xám)
- **Đường kẻ ngăn cách**
- **Nội Dung Chi Tiết:** Văn bản mô tả đầy đủ
- **Sticky Bottom Bar:**
  - Nút hành động thay đổi theo loại:
    - Thông báo hệ thống: "Về trang chủ"
    - Thông báo đơn hàng: "Xem đơn hàng"
    - Thông báo khuyến mãi: "Sử dụng voucher"

---

**Các hành động của người dùng (User Actions):**

- Bấm Back để quay về NotificationsView
- Bấm nút hành động:
  - "Về trang chủ": Navigator.popUntil về trang đầu tiên
  - "Xem đơn hàng": hiển thị SnackBar placeholder
  - "Sử dụng voucher": hiển thị SnackBar placeholder
