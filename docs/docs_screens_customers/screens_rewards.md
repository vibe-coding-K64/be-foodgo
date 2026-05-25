# Màn Hình - Ưu Đãi (Rewards)

## 1. Màn Hình Ưu Đãi (RewardsView)

**File:** `lib/features/rewards/views/rewards_view.dart`

**Class:** `RewardsView`

**Mục đích chính:** Màn hình hiển thị điểm thành viên, voucher đổi được, và voucher của người dùng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **AppBar Gradient Xanh:** Tiêu đề "Ưu đãi"
- **Card Điểm Thành Viên (RewardsPointCard):**
  - Số điểm hiện tại (VD: 850 điểm, font lớn)
  - Hạng thành viên hiện tại (VD: "Bạc Đồng", badge)
  - Thanh tiến triển đến hạng tiếp theo (Progress bar)
  - "Còn X điểm nữa sẽ lên hạng"
- **Phần "Đổi Điểm" (Danh sách ngang):**
  - Tiêu đề "Đổi điểm"
  - ListView ngang các SystemVoucherCard, mỗi card chứa:
    - Hình ảnh voucher (160x80)
    - Tên voucher
    - Mô tả phụ (VD: "Freeship 15K")
    - Nút "ĐỔI ĐIỂM" với số điểm (VD: "500 điểm")
- **Phần "Voucher Của Tôi":**
  - Tiêu đề "Voucher của tôi" + nút "Xem tất cả" (text button)
  - ListView dọc các RewardsVoucherCard, mỗi card chứa:
    - Icon voucher (VD: truck, discount)
    - Tên voucher
    - Mã voucher
    - Hạn sử dụng
    - Nút "Dùng ngay"
  - Trạng thái rỗng: "Chưa có ưu đãi nào"
- **Skeleton Loading:** Cho card điểm, danh sách đổi điểm, voucher của tôi

---

**Các hành động của người dùng (User Actions):**

- Bấm card đổi điểm để mở RewardDetailView (ExchangeVoucherModel)
- Bấm "Xem tất cả" voucher để mở MyVouchersView
- Bấm "Dùng ngay" trên voucher để mở VoucherApplicableProductsView
