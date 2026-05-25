# Màn Hình - Hồ Sơ (Profile)

## 1. Màn Hình Hồ Sơ (ProfileView)

**File:** `lib/features/profile/views/profile_view.dart`

**Class:** `ProfileView`

**Mục đích chính:** Màn hình quản lý tài khoản người dùng, hiển thị thông tin cá nhân, avatar và danh sách các tùy chọn quản lý tài khoản.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Header Gradient Xanh:**
  - Avatar người dùng (hình tròn 100px, border trắng)
  - Họ tên người dùng (in đậm, font 22, màu trắng)
  - Số điện thoại (màu trắng)
  - Nút "Chỉnh sửa hồ sơ" (OutlinedButton, border trắng)
- **Danh Sách Menu (ProfileMenuList):**
  - **Quản lý chi tiêu:** icon account_balance_wallet, "Quản lý chi tiêu"
  - **Địa chỉ mặc định:** icon location_on, "Địa chỉ mặc định"
  - **Thanh toán:** icon payment, "Thanh toán"
  - **Trở thành người bán:** icon content_paste, "Trở thành người bán / tài xế"
  - **Hỗ trợ:** icon support_agent, "Hỗ trợ"
  - **Cài đặt:** icon settings, "Cài đặt"
  - **Điều khoản và chính sách:** icon description, "Điều khoản và chính sách"
  - **Đăng xuất:** icon logout, "Đăng xuất" (màu đỏ)
- **Dialog Xác Nhận Đăng Xuất:** "Bạn có chắc chắn muốn đăng xuất?"
- **Trạng thái loading:** Skeleton placeholder cho header

---

**Các hành động của người dùng (User Actions):**

- Bấm "Chỉnh sửa hồ sơ" để mở EditProfileView
- Bấm "Quản lý chi tiêu" để mở ExpenseManagementView
- Bấm "Địa chỉ mặc định" để mở AddressManagementView
- Bấm "Thanh toán" để mở PaymentMethodsView
- Bấm "Trở thành người bán" để mở PartnerRegistrationView (PartnerRole.seller)
- Bấm "Hỗ trợ" để mở SupportView
- Bấm "Cài đặt" để mở SettingsView
- Bấm "Điều khoản và chính sách" để mở TermsView
- Bấm "Đăng xuất" để hiển thị dialog xác nhận, nếu đồng ý thì xóa lịch sử màn hình và chuyển sang LoginView

---

## 2. Màn Hình Chỉnh Sửa Hồ Sơ (EditProfileView)

**File:** `lib/features/profile/views/edit_profile_view.dart`

**Class:** `EditProfileView`

**Mục đích chính:** Cho phép người dùng chỉnh sửa thông tin hồ sơ cá nhân (họ tên, email, avatar).

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Avatar:** Hình tròn 96px, icon camera ở góc phải dưới (phần ghép)
- **Form Nhập Liệu:**
  - Label "Họ tên", ô nhập họ tên (pre-filled nếu có)
  - Label "Email", ô nhập email (pre-filled nếu có)
  - Label "Số điện thoại", ô chỉ đọc (pre-filled, có icon khóa)
- **Sticky Bottom Bar:** Nút "Lưu thay đổi" (màu xanh lá)
- **Dialog Mật Khẩu (Step 1):**
  - Tiêu đề: "Xác thực mật khẩu"
  - Ô nhập mật khẩu (có icon con mắt bật/tắt)
  - Nút "Hủy" và "Tiếp tục"
- **Dialog OTP (Step 2):**
  - Tiêu đề: "Nhập mã xác minh"
  - Mô tả: "Mã 6 số gửi tới số điện thoại của bạn"
  - Ô nhập OTP (6 ký tự số, center, font 24, letter-spacing 8)
  - Nút "Hủy" và "Xác nhận"
- **SnackBar:** Thành công / Thất bại

---

**Các hành động của người dùng (User Actions):**

- Bấm icon camera để đổi avatar (hiện tại chỉ hiển SnackBar placeholder)
- Nhập họ tên và email
- Bấm "Lưu thay đổi" để bắt đầu quy trình xác thực kép:
  - Step 1: Nhập mật khẩu trong dialog, bấm "Tiếp tục"
  - Step 2: Nhập mã OTP 6 số, bấm "Xác nhận"
  - Step 3: Gọi ProfileService.updateProfile(fullName, email)
  - Nếu thành công: hiển SnackBar, quay về ProfileView
  - Nếu thất bại: hiển SnackBar lỗi
