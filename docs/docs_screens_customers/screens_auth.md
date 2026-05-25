# Màn Hình - Xác Thực (Auth)

## 1. Màn Hình Đăng Nhập (LoginView)

**File:** `lib/features/auth/views/login_view.dart`

**Class:** `LoginView`

**Mục đích chính:** Màn hình cho phép người dùng đăng nhập vào ứng dụng FoodGo bằng tài khoản hoặc mạng xã hội.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- Logo và icon ứng dụng (biểu tượng hình restaurant_menu)
- Tiêu đề chào mừng: "Chào mừng trở lại"
- Tiêu đề phụ: "Đăng nhập để tiếp tục"
- Ô nhập tài khoản (số điện thoại/email)
- Ô nhập mật khẩu (có icon bật/tắt hiển thị mật khẩu)
- Liên kết "Quên mật khẩu?"
- Nút "Đăng nhập" (màu xanh lá, full-width)
- Vạch chia giữa đăng nhập và mạng xã hội
- 2 nút mạng xã hội: "Đăng nhập Google" và "Đăng nhập Facebook"
- Liên kết footer: "Chưa có tài khoản? Đăng ký ngay"
- Overlay loading khi đang xử lý

---

**Các hành động của người dùng (User Actions):**

- Nhập tài khoản vào ô tài khoản
- Nhập mật khẩu vào ô mật khẩu
- Bấm icon con mắt (bật/tắt hiển thị mật khẩu)
- Bấm nút "Đăng nhập" để gọi AuthService.login() và chuyển sang MainView
- Bấm liên kết "Quên mật khẩu?" để chuyển sang ForgotPasswordView
- Bấm nút "Đăng nhập Google" hiển thị SnackBar thông báo
- Bấm nút "Đăng nhập Facebook" hiển thị SnackBar thông báo
- Bấm "Đăng ký ngay" để chuyển sang RegisterView

---

## 2. Màn Hình Đăng Ký (RegisterView)

**File:** `lib/features/auth/views/register_view.dart`

**Class:** `RegisterView`

**Mục đích chính:** Màn hình tạo tài khoản mới cho người dùng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- Tiêu đề: "Tạo tài khoản"
- Tiêu đề phụ: "Tạo tài khoản để bắt đầu"
- Ô nhập họ và tên (text, viết hoa ký tự đầu)
- Ô nhập số điện thoại (keyboard số)
- Ô nhập mật khẩu (có icon bật/tắt hiển thị)
- Ô xác nhận mật khẩu (có icon bật/tắt hiển thị)
- Nút "Đăng ký" (màu xanh lá, full-width)
- Footer: "Đã có tài khoản? Đăng nhập ngay"

---

**Các hành động của người dùng (User Actions):**

- Nhập họ và tên
- Nhập số điện thoại (9-11 số)
- Nhập mật khẩu (tối thiểu 6 ký tự)
- Xác nhận mật khẩu (phải giống mật khẩu)
- Bấm icon con mắt để bật/tắt hiển thị mật khẩu
- Bấm nút "Đăng ký" để validate form và chuyển sang OtpVerificationView (verifyType = 'register')
- Bấm footer "Đăng nhập ngay" để quay lại LoginView

---

## 3. Màn Hình Xác Thực OTP (OtpVerificationView)

**File:** `lib/features/auth/views/otp_verification_view.dart`

**Class:** `OtpVerificationView`

**Mục đích chính:** Xác thực người dùng bằng mã OTP 6 số gửi qua số điện thoại hoặc email.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- Icon thông báo (message_outlined)
- Tiêu đề: "Xác thực mã OTP"
- Mô tả: "Nhập mã 6 số gửi tới số điện thoại/email của bạn"
- 6 ô nhập OTP (mỗi ô nhận 1 ký tự số)
- Thời gian đếm ngược gửi lại mã (60 giây)
- Nút "Gửi lại mã OTP" (hiển thị sau khi đếm ngược kết thúc)
- Nút "Xác nhận" (sticky bottom)

---

**Các hành động của người dùng (User Actions):**

- Nhập 6 ký tự số vào các ô OTP (tự động nhảy sang ô tiếp theo)
- Bấm nút "Gửi lại mã" sau khi hết thời gian đếm ngược
- Bấm nút "Xác nhận" để xác thực OTP:
  - Nếu verifyType == 'forgot_password': chuyển sang ResetPasswordView
  - Nếu verifyType == 'register': chuyển sang MainView

---

## 4. Màn Hình Quên Mật Khẩu (ForgotPasswordView)

**File:** `lib/features/auth/views/forgot_password_view.dart`

**Class:** `ForgotPasswordView`

**Mục đích chính:** Cho phép người dùng nhập số điện thoại hoặc email để nhận mã OTP đặt lại mật khẩu.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- Icon khóa (lock_outlined)
- Tiêu đề: "Quên mật khẩu"
- Mô tả: "Nhập số điện thoại hoặc email để nhận mã xác nhận"
- Ô nhập số điện thoại/email
- Nút "Gửi mã xác nhận" (màu xanh lá)

---

**Các hành động của người dùng (User Actions):**

- Nhập số điện thoại hoặc email
- Bấm nút "Gửi mã xác nhận" để validate và chuyển sang OtpVerificationView (verifyType = 'forgot_password')

---

## 5. Màn Hình Đặt Lại Mật Khẩu (ResetPasswordView)

**File:** `lib/features/auth/views/reset_password_view.dart`

**Class:** `ResetPasswordView`

**Mục đích chính:** Cho phép người dùng đặt mật khẩu mới sau khi xác thực OTP thành công.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- Icon khóa reset (lock_reset)
- Tiêu đề: "Đặt lại mật khẩu"
- Mô tả: "Nhập mật khẩu mới cho tài khoản của bạn"
- Ô nhập mật khẩu mới (tối thiểu 8 ký tự)
- Ô xác nhận mật khẩu mới
- Nút "Cập nhật mật khẩu" (sticky bottom, có loading)

---

**Các hành động của người dùng (User Actions):**

- Nhập mật khẩu mới
- Xác nhận mật khẩu mới (phải giống nhau)
- Bấm icon con mắt để bật/tắt hiển thị mật khẩu
- Bấm nút "Cập nhật mật khẩu" để validate và lưu mật khẩu mới (giả lập gọi API 1.5s), sau đó quay về màn hình đầu tiên
