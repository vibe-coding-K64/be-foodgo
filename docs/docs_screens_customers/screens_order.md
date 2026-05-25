# Màn Hình - Đơn Hàng (Order)

## 1. Màn Hình Hoạt Động / Quản Lý Đơn Hàng (ActivityView)

**File:** `lib/features/activity/views/activity_view.dart`

**Class:** `ActivityView`

**Mục đích chính:** Màn hình chính của tab "Hoạt động", hiển thị danh sách đơn hàng phân theo 3 trạng thái: Đã đặt, Đã nhận, Đã hủy.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **AppBar Gradient Xanh:** Tiêu đề "Hoạt động", icon back
- **TabBar (3 tab):**
  - "Đã đặt" (status 0, 1, 2 - đơn đang xử lý)
  - "Đã nhận" (status 3 - đơn hoàn thành)
  - "Đã hủy" (status 4 - đơn đã hủy)
- **Thanh Tìm Kiếm:** Ô tìm kiếm (placeholder: "Tìm đơn hàng..."), icon tìm kiếm, icon lọc (tune)
- **Danh Sách Đơn Hàng (ActivityOrderCard - StreamBuilder từ OrderService.getMyOrdersStream()):**
  - Hình đại diện quán
  - Tên quán
  - Danh sách món (VD: "Trà Sữa Trân Châu x2, Cà Phê Sữa Đá x1")
  - Tổng tiền (VD: "105.000 VNĐ")
  - Trạng thái đơn hàng (badge): "Đang xử lý", "Đã giao", "Đã hủy"
  - Thời gian đặt hàng
  - Nút "Xem chi tiết", "Đặt lại", "Hủy đơn" (tùy trạng thái)
- **Trạng thái rỗng (3 loại tùy theo tab):**
  - "Chưa có đơn hàng nào đang xử lý"
  - "Chưa có đơn hàng đã nhận"
  - "Chưa có đơn hàng nào bị hủy"
- **Dialog Xác Nhận Hủy:** "Bạn có chắc chắn muốn hủy đơn hàng [id]?"

---

**Các hành động của người dùng (User Actions):**

- Chuyển tab để lọc đơn hàng theo trạng thái
- Nhập từ khóa tìm kiếm đơn hàng (hiện tại chỉ có placeholder)
- Bấm icon lọc (tune) để mở bảng lọc (hiện tại chỉ log placeholder)
- Bấm "Xem chi tiết" để mở OrderDetailView
- Bấm "Đặt lại" để mở CheckoutView với initialOrder
- Bấm "Hủy đơn" để hiển thị dialog xác nhận, nếu đồng ý thì gọi OrderService.cancelOrder(id) và hiển thị SnackBar kết quả

---

## 2. Màn Hình Chi Tiết Đơn Hàng (OrderDetailView)

**File:** `lib/features/activity/views/order_detail_view.dart`

**Class:** `OrderDetailView`

**Mục đích chính:** Hiển thị đầy đủ thông tin chi tiết của một đơn hàng, với các hành động tùy theo trạng thái.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Phần Cố Định:**
  - **Mã Đơn Hàng:** "#ORD001", nút "Sao chép" (icon copy)
  - **Địa Chỉ (Từ -> Đến):**
    - Từ: icon store, tên quán, địa chỉ cửa hàng
    - Đường nối (icon local_shipping)
    - Đến: icon location, tên người nhận, địa chỉ giao hàng
  - **Danh Sách Món:**
    - Tiêu đề "Món đã đặt"
    - Với mỗi item: số lượng (hình vuông, nền xanh), tên món, topping (nếu có), đơn giá
  - **Chi Tiết Thanh Toán:**
    - Tạm tính
    - Phí giao hàng
    - Tổng thanh toán (in đậm, màu xanh lá)
  - **Phương Thức Thanh Toán:** icon + tên (VD: "Thu hộ tiền mặt" hoặc "Ví điện tử")
  - **Nút Trợ Giúp:** OutlinedButton "Bạn cần hỗ trợ?"

- **Phần Động Theo Trạng Thái:**

  *Nếu đơn đang xử lý (status 0, 1, 2):*
  - **Thông Tin Tài Xế:** avatar, tên, biển số xe, số điện thoại, nút "Mở bản đồ", nút "Chat với tài xế"
  - Nếu chưa có tài xế: "Đang tìm tài xế..." (CircularProgressIndicator)
  - **Nút Hủy Đơn Hàng** (màu đỏ)

  *Nếu đơn hoàn thành (status 3):*
  - **Phần Đánh Giá:**
    - Nút "Đánh giá món ăn" (OutlinedButton)
    - Nút "Đánh giá tài xế" (ElevatedButton, nếu có thông tin tài xế)
    - Nút "Đặt lại đơn hàng" (ElevatedButton)

  *Nếu đơn bị hủy (status 4):*
  - **Thông Báo Hủy:** icon cancel (màu đỏ), tiêu đề "Đơn hàng đã bị hủy"
  - Nút "Đặt lại đơn hàng"

- **Sticky Bottom Bar:** Nút "Đóng" (màu xám)

---

**Các hành động của người dùng (User Actions):**

- Bấm nút "Sao chép" để copy mã đơn hàng vào clipboard, hiển thị SnackBar
- Bấm "Mở bản đồ" để mở OrderTrackingMapView với thông tin tài xế
- Bấm "Chat với tài xế" để mở DriverChatView
- Bấm "Hủy đơn" để hiển thị dialog xác nhận, gọi OrderService.cancelOrder(), nếu thành công thì đóng màn hình, hiển thị SnackBar
- Bấm "Đánh giá món ăn" / "Đánh giá tài xế" (hiện tại chỉ log placeholder)
- Bấm "Đặt lại" để mở CheckoutView với initialOrder
- Bấm "Bạn cần hỗ trợ?" để mở SupportView với orderId
- Bấm "Đóng" để quay về màn hình trước
