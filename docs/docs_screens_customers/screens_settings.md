# Màn Hình - Cài Đặt & Hỗ Trợ (Settings & Support)

## 1. Màn Hình Cài Đặt (SettingsView)

**File:** `lib/features/settings/views/settings_view.dart`

**Class:** `SettingsView`

**Mục đích chính:** Màn hình cấu hình các tùy chọn cài đặt của ứng dụng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Nhóm "Chung":**
  - **SwitchListTile:** "Thông báo đẩy" / "Push Notifications", có mô tả phụ "Nhận thông báo từ ứng dụng"
  - **SwitchListTile:** "Ngôn ngữ", có mô tả phụ "Tiếng Việt" hoặc "English" (tùy trạng thái hiện tại)
  - **ListTile:** "Đổi mật khẩu", có icon chevron_right
- **Nhóm "Khác":**
  - **ListTile:** "Về chúng tôi", có icon chevron_right
  - **ListTile:** "Phiên bản", hiển thị "1.0.0" bên phải

---

**Các hành động của người dùng (User Actions):**

- Toggle Switch thông báo đẩy để bật/tắt push notifications, hiển thị SnackBar
- Toggle Switch ngôn ngữ để chuyển đổi giữa Tiếng Việt / English qua LocaleProvider, hiển thị SnackBar
- Bấm "Đổi mật khẩu" để mở ChangePasswordView
- Bấm "Về chúng tôi" (hiện tại chỉ log placeholder)

---

## 2. Màn Hình Trợ Giúp (SupportView)

**File:** `lib/features/support/views/support_view.dart`

**Class:** `SupportView`

**Mục đích chính:** Trung tâm trợ giúp với FAQ accordion và các tùy chọn liên hệ.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Thanh Tìm Kiếm:** "Tìm câu hỏi thường gặp..." (placeholder)
- **Danh Sách FAQ (ExpansionTile - Accordion):**
  - 4 câu hỏi FAQ mock:
    1. "Làm sao để hủy đơn hàng?"
    2. "Làm sao để sử dụng mã khuyến mãi?"
    3. "Làm sao để thay đổi địa chỉ giao hàng?"
    4. "Các phương thức thanh toán được chấp nhận?"
  - Mở rộng: hiển thị câu trả lời
  - Đóng: chỉ hiển thị câu hỏi
- **Sticky Bottom Bar:**
  - Dòng "Cần hỗ trợ thêm?"
  - Nút "Chat" (Outline, icon chat_bubble_outline, màu xanh lá)
  - Nút "Gọi tổng đài" (Elevated, icon phone_outlined, màu xanh lá)

---

**Các hành động của người dùng (User Actions):**

- Nhập từ khóa để lọc FAQ theo nội dung câu hỏi và câu trả lời
- Bấm ExpansionTile để mở/đóng câu hỏi FAQ
- Bấm "Chat" để mở SupportChatView (truyền orderId nếu có)
- Bấm "Gọi tổng đài" để hiển thị SnackBar placeholder

---

## 3. Màn Hình Điều Khoản Và Chính Sách (TermsView)

**File:** `lib/features/terms/views/terms_view.dart`

**Class:** `TermsView`

**Mục đích chính:** Hiển thị nội dung văn bản các điều khoản sử dụng và chính sách của ứng dụng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Tiêu đề:** "Điều khoản và chính sách"
- **4 Mục Văn Bản:**
  - **Mục 1:** Tiêu đề (in đậm), nội dung văn bản
  - **Mục 2:** Tiêu đề (in đậm), nội dung văn bản
  - **Mục 3:** Tiêu đề (in đậm), nội dung văn bản
  - **Mục 4:** Tiêu đề (in đậm), nội dung văn bản

---

**Các hành động của người dùng (User Actions):**

- Cuộn danh sách để đọc nội dung
- Bấm Back để quay về

---

## 4. Màn Hình Đăng Ký Đối Tác (PartnerRegistrationView)

**File:** `lib/features/partner/views/partner_registration_view.dart`

**Class:** `PartnerRegistrationView`

**Mục đích chính:** Cho phép người dùng đăng ký trở thành người bán hoặc tài xế giao hàng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Banner Landing Page:**
  - Hình ảnh (200px) từ unsplash (delivery/shopping)
  - Lớp gradient phủ
  - Tiêu đề banner (VD: "Bạn muốn bán hay giao hàng?")
- **Danh Sách Quyền Lợi:**
  - 3 item: icon + tên quyền lợi (VD: "Thu nhập cao", "Lịch linh hoạt", "Hỗ trợ 24/7")
- **Form Đăng Ký:**
  - Lựa chọn loại đối tác: "Người bán" / "Tài xế" (Radio button tùy chọn)
  - Ô nhập Họ và tên (bắt buộc)
  - Ô nhập Số điện thoại (10 số)
  - Ô nhập CCCD (12 số)
  - Ô nhập Khu vực hoạt động (read-only, bắt buộc, icon map)
- **Sticky Bottom Bar:** Nút "Gửi yêu cầu" (màu xanh lá)

---

**Các hành động của người dùng (User Actions):**

- Lựa chọn loại đối tác (Người bán / Tài xế)
- Nhập các trường trong form (validate: tất cả bắt buộc)
- Bấm ô nhập "Khu vực hoạt động" để mở MapPickerView, lấy địa chỉ trả về
- Bấm "Gửi yêu cầu" để validate form, hiển thị SnackBar thành công, pop về

---

## 5. Màn Hình Quản Lý Chi Tiêu (ExpenseManagementView)

**File:** `lib/features/expense/views/expense_management_view.dart`

**Class:** `ExpenseManagementView`

**Mục đích chính:** Hiển thị tổng hợp chi tiêu theo tháng với biểu đồ và lịch sử giao dịch.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Tiêu đề:** "Quản lý chi tiêu"
- **Thanh Lọc Tháng:** ChoiceChip ngang (Tháng 1 -> Tháng 12)
- **ExpenseSummaryCard:**
  - Tổng chi tiêu tháng (VD: "1.250.000 VNĐ", font 36)
  - Tháng / Năm (VD: "Tháng 5 - 2026")
- **ExpenseChartWidget (Pie Chart donut):**
  - Biểu đồ tròn phân bổ chi tiêu theo danh mục (5 màu)
  - Chú thích từng danh mục (màu + tên + số tiền + %)
- **Lịch Sử Giao Dịch (ExpenseTransactionItem):**
  - Icon danh mục (màu sắc tương ứng)
  - Tên danh mục
  - Ngày giao dịch
  - Số tiền (VD: "-35.000 VNĐ")
- **Trạng thái rỗng:** "Chưa có giao dịch nào"

---

**Các hành động của người dùng (User Actions):**

- Bấm ChoiceChip tháng để lọc dữ liệu chi tiêu theo tháng tương ứng
- Cuộn danh sách để xem lịch sử giao dịch

---

## 6. Màn Hình Quản Lý Thanh Toán (PaymentMethodsView)

**File:** `lib/features/payment/views/payment_methods_view.dart`

**Class:** `PaymentMethodsView`

**Mục đích chính:** Quản lý các phương thức thanh toán của người dùng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Tiêu đề:** "Phương thức thanh toán"
- **Danh Sách Phương Thức (StreamBuilder từ PaymentService.getPaymentMethodsStream()), phân theo 3 nhóm:**
  - **Tiền mặt (COD):** icon tiền, tên phương thức, badge "Mặc định" (nếu có)
  - **Thẻ (Card):** icon thẻ, tên thẻ, nút xóa
  - **Ví điện tử (Wallet):** icon wallet, tên ví, nút xóa
- **Trạng thái rỗng:** Icon payment (80x80), "Phương thức thanh toán", "Hãy thêm phương thức thanh toán"
- **Trạng thái loading:** CircularProgressIndicator
- **Sticky Bottom Bar:** Nút "Thêm phương thức thanh toán" (icon add, màu xanh lá)
- **Dialog Xác Nhận Xóa:** "Xóa phương thức thanh toán?" / "Bạn có chắc chắn muốn xóa?"

---

**Các hành động của người dùng (User Actions):**

- Bấm phương thức thanh toán (không phải COD) để đặt làm mặc định: gọi PaymentService.setDefaultPayment(id)
- Bấm nút xóa trên phương thức (không phải COD) để hiển thị dialog xác nhận, gọi PaymentService.deletePayment(id)
- Bấm "Thêm phương thức thanh toán" để mở AddPaymentMethodView
- Bấm Back để quay về

---

## 7. Màn Hình Chat Với Tài Xế (DriverChatView)

**File:** `lib/features/activity/views/driver_chat_view.dart`

**Class:** `DriverChatView`

**Mục đích chính:** Giao diện chat giữa khách hàng và tài xế giao hàng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **AppBar:** Avatar tài xế, tên tài xế, biển số xe, nút gọi điện (icon phone_outlined)
- **Danh Sách Tin Nhắn:**
  - Bong bóng bên trái (tài xế, nền xám): avatar + nội dung + thời gian
  - Bong bóng bên phải (khách, nền xanh lá): nội dung + thời gian
  - Tin nhắn vị trí: icon vị trí + địa chỉ
  - Tin nhắn hình ảnh: hình 180x140
- **Quick Replies (cuộn ngang):** 3 chip: "Đang xuống", "Bạn ở đâu?", "Gọi khi đến"
- **Vùng Nhập Tin Nhắn:**
  - Nút đính kèm (icon attach_file): hiển thị bottom sheet tùy chọn
  - Ô nhập text (border tròn)
  - Nút gửi (icon send, màu xanh lá)

---

**Các hành động của người dùng (User Actions):**

- Bấm icon gọi điện trên AppBar (chỉ log placeholder)
- Bấm chip Quick Reply để gửi tin nhắn nhanh
- Nhập text và bấm icon gửi để gửi tin nhắn
- Bấm icon đính kèm để mở bottom sheet:
  - "Gửi vị trí hiện tại" -> gửi tin nhắn vị trí
  - "Gửi hình ảnh" -> chỉ log placeholder
- Bấm Back để quay về OrderDetailView
