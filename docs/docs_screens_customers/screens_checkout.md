# Màn Hình - Thanh Toán (Checkout)

## 1. Màn Hình Thanh Toán (CheckoutView)

**File:** `lib/features/checkout/views/checkout_view.dart`

**Class:** `CheckoutView`

**Mục đích chính:** Màn hình bước cuối của quy trình đặt hàng, cho phép xem lại giỏ hàng, chọn địa chỉ giao hàng, áp dụng voucher, chọn phương thức thanh toán, và đặt hàng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Phần 1 - Thông Tin Giao Hàng (CheckoutDeliveryInfo):**
  - Icon vị trí, tên người nhận (VD: "Nguyễn Văn A")
  - Địa chỉ chi tiết (VD: "123 Nguyễn Huệ, Quận 1, TP.HCM")
  - Thời gian ước tính (VD: "15-20 phút")
  - Nút "Thay đổi địa chỉ" (Outline)

- **Phần 2 - Danh Sách Món Đã Chọn (CheckoutCartItems):**
  - Tiêu đề "Món đã chọn" + số lượng món (VD: "3 món")
  - Danh sách CheckoutCartItem, mỗi item chứa:
    - Hình món (72x72)
    - Tên món
    - Topping đã chọn (VD: "+ Trân châu +5.000")
    - Ghi chú (nếu có)
    - Số lượng + đơn giá
    - Nút "Sửa" (icon edit)
    - Nút "Xóa" (icon delete)

- **Phần 3 - Ưu Đãi Và Thanh Toán (CheckoutPromotions):**
  - Dòng Voucher/Khuyến mãi (icon voucher): hiển thị voucher đã chọn hoặc "Chọn Voucher"
  - Dòng Phương thức thanh toán (icon wallet): hiển thị phương thức đã chọn (VD: "Tiền mặt")
  - Switch "Điểm tích lũy" (toggle bật/tắt)

- **Phần 4 - Chi Tiết Hóa Đơn (CheckoutSummary):**
  - Tạm tính (VD: 105.000 VNĐ)
  - Phí giao hàng (VD: 15.000 VNĐ)
  - Giảm giá (VD: -5.000 VNĐ)
  - Tổng thanh toán (in đậm, màu xanh lá, VD: 115.000 VNĐ)

- **Sticky Bottom Bar:**
  - Tiêu đề "Tổng thanh toán"
  - Số tiền tổng (VD: "115.000 VNĐ", font 18, in đậm)
  - Nút "Đặt hàng" (màu xanh lá)

- **Bottom Sheet Phương Thức Thanh Toán:**
  - 4 tuy chọn: Tiền mặt (money_outlined), MoMo (wallet_outlined), ZaloPay (account_balance_wallet), Thẻ ngân hàng (credit_card)
  - Checkmark bên phải tuy chọn đang chọn

- **Bottom Sheet Voucher (2 Tab):**
  - Tab 1: "Miễn phí giao hàng" (Freeship)
  - Tab 2: "Mã giảm giá" (Discount)
  - Danh sách voucher, mỗi voucher chứa: icon, tên voucher, yêu cầu tối thiểu, ngày hết hạn, nút "Áp dụng" / "Đã áp dụng"

- **Bottom Sheet Sửa Món:**
  - Hình + tên + giá món
  - Danh sách topping checkbox
  - Ô ghi chú (2 dòng, 100 ký tự)
  - Hiển thị giá tạm tính
  - Nút "Cập nhật"

---

**Các hành động của người dùng (User Actions):**

- Bấm "Thay đổi địa chỉ" để mở AddressManagementView (isFromCheckout: true), nhận địa chỉ đã chọn về cập nhật _deliveryAddress
- Bấm nút Sửa trên item để mở BottomSheet sửa món (topping + ghi chú)
- Bấm nút Xóa trên item để xóa món khỏi danh sách
- Tăng/giảm số lượng trên item trong BottomSheet sửa món
- Bấm "Áp dụng" voucher để chọn voucher, cập nhật _selectedVoucher và giảm giá
- Bấm nút phương thức thanh toán để mở BottomSheet chọn phương thức, cập nhật _selectedPaymentMethod
- Toggle Switch "Điểm tích lũy" để bật/tắt điểm tích lũy (_isPointsEnabled)
- Bấm nút "Đặt hàng" để log thông tin đặt hàng (địa chỉ, danh sách món, tạm tính, phí giao hàng, giảm giá, tổng, phương thức thanh toán, điểm tích lũy, voucher)
