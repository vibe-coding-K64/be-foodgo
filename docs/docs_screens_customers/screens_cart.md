# Màn Hình - Giỏ Hàng (Cart)

## 1. Màn Hình Giỏ Hàng (CartView)

**File:** `lib/features/cart/views/cart_view.dart`

**Class:** `CartView`

**Mục đích chính:** Màn hình hiển thị danh sách món ăn trong giỏ hàng, cho phép chọn món, thay đổi số lượng, xóa món, và chuyển sang màn hình thanh toán.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Tiêu đề:** "Giỏ hàng" (AppBar, căn giữa)
- **Nội dung chính (ListView):**
  - Danh sách CartItemWidget, mỗi item chứa:
    - Checkbox chọn/bỏ chọn món
    - Hình món ăn (72x72)
    - Tên món ăn
    - Topping đã chọn (nếu có, VD: "+ Trân châu, + Thạch cà phê")
    - Ghi chú (nếu có)
    - Đơn giá (VD: "35.000 VNĐ")
    - Nút - và + để thay đổi số lượng
  - Vuốt sang trái để xóa món (Dismissible)
- **CartBottomBar (sticky bottom):**
  - Checkbox "Chọn tất cả" / "Bỏ chọn tất cả"
  - Số lượng đã chọn / tổng số lượng (VD: "3/5 món")
  - Tạm tính (VD: "Tạm tính: 105.000 VNĐ")
  - Nút "Đặt hàng" (màu xanh lá)
- **Trạng thái rỗng (CartEmptyState):**
  - Icon giỏ hàng trống (96x96, hình tròn xám)
  - Tiêu đề: "Giỏ hàng của bạn trống"
  - Mô tả: "Hãy thêm món ăn vào giỏ hàng để đặt hàng"
  - Nút "Tìm thực đơn ngay"
- **Trạng thái loading:** Skeleton placeholder (3 items)
- **Trạng thái lỗi:** Icon error, thông báo lỗi, nút "Thử lại"

---

**Các hành động của người dùng (User Actions):**

- Bấm checkbox trên item để chọn / bỏ chọn món (cập nhật _selectedIds Set)
- Bấm nút + để tăng số lượng: gọi CartState.updateQuantity(userId, itemId, quantity + 1)
- Bấm nút - để giảm số lượng: gọi CartState.updateQuantity(userId, itemId, quantity - 1), không giảm dưới 1
- Vuốt (Dismissible) sang trái trên item để gọi CartState.removeItem() và hiển thị SnackBar xác nhận
- Bấm "Chọn tất cả" / "Bỏ chọn tất cả" để toggle tất cả checkbox
- Bấm nút "Đặt hàng" để chuyển sang CheckoutView
- Bấm "Tìm thực đơn ngay" để quay về trang chủ
- Bấm "Thử lại" khi có lỗi để gọi lại CartState.startListening()
