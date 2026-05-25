# Màn Hình - Địa Chỉ (Address)

## 1. Màn Hình Quản Lý Địa Chỉ (AddressManagementView)

**File:** `lib/features/address/views/address_management_view.dart`

**Class:** `AddressManagementView`

**Mục đích chính:** Hiển thị danh sách địa chỉ đã lưu, cho phép đặt địa chỉ mặc định, sửa, xóa và thêm địa chỉ mới.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Tiêu đề:** "Địa chỉ của tôi"
- **Danh Sách Địa Chỉ (StreamBuilder từ AddressService.getAddressesStream()):**
  - Mỗi AddressCardWidget chứa:
    - Radio button danh sách (chỉ hiển thị khi isFromCheckout = true)
    - Tên người nhận (in đậm)
    - Loại địa chỉ (badge: "Nhà", "Văn phòng", "Khác")
    - Số điện thoại người nhận
    - Địa chỉ chi tiết
    - Nút "Mặc định" / Radio mặc định
    - Nút "Sửa" (icon edit)
    - Nút "Xóa" (icon delete, icon_thrash)
  - Badge "Mặc định" (nếu là địa chỉ mặc định)
- **Trạng thái rỗng:**
  - Icon location_off (80x80)
  - Tiêu đề: "Địa chỉ của tôi"
  - Mô tả: "Hãy thêm địa chỉ giao hàng"
- **Trạng thái loading:** CircularProgressIndicator
- **Sticky Bottom Bar:**
  - Nếu từ Checkout: Nút "Xác nhận" (màu xanh lá) + Nút "Thêm địa chỉ mới"
  - Nếu bình thường: Nút "Thêm địa chỉ mới" (màu xanh lá)
- **Dialog Xác Nhận Xóa:** "Xóa địa chỉ?" / "Bạn có chắc chắn muốn xóa địa chỉ này?"

---

**Các hành động của người dùng (User Actions):**

- Bấm Radio trên địa chỉ (từ Checkout) để chọn làm địa chỉ giao hàng, bấm "Xác nhận" để trả kết quả về CheckoutView
- Bấm nút "Mặc định" / Radio để đặt làm địa chỉ mặc định: gọi AddressService.setDefaultAddress(addressId)
- Bấm nút "Sửa" để mở AddressFormView với address, cập nhật StreamBuilder tự động khi Firestore thay đổi
- Bấm nút "Xóa" để hiển thị dialog xác nhận, gọi AddressService.deleteAddress() và hiển SnackBar
- Bấm "Thêm địa chỉ mới" để mở AddressFormView (không truyền address), tạo địa chỉ mới
- Bấm Back để quay về màn hình trước

---

## 2. Màn Hình Form Địa Chỉ (AddressFormView)

**File:** `lib/features/address/views/address_form_view.dart`

**Class:** `AddressFormView`

**Mục đích chính:** Form nhập liệu để thêm mới hoặc chỉnh sửa địa chỉ giao hàng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Tiêu đề:** "Thêm địa chỉ mới" hoặc "Chỉnh sửa địa chỉ" (tùy chế độ)
- **Mục 1 - Thông Tin Liên Hệ:**
  - Ô nhập Tên người nhận (bắt buộc)
  - Ô nhập Số điện thoại (bắt buộc, 10 số)
- **Mục 2 - Vị Trí:**
  - Nút "Chọn vị trí trên bản đồ" (nền xanh nhất, icon map_outlined)
  - Ô nhập Số nhà / Tên đường (bắt buộc)
  - Ô nhập Phường / Xã (tùy chọn)
  - Ô nhập Quận / Huyện (bắt buộc)
  - Ô nhập Tỉnh / Thành phố (bắt buộc)
- **Mục 3 - Cài Đặt Bổ Sung:**
  - Chip lựa chọn loại địa chỉ: "Nhà", "Văn phòng", "Khác"
  - Switch "Đặt làm địa chỉ mặc định"
- **Sticky Bottom Bar:** Nút "Lưu địa chỉ" (màu xanh lá)

---

**Các hành động của người dùng (User Actions):**

- Bấm nút "Chọn vị trí trên bản đồ" để mở MapPickerView (hiện tại chỉ log placeholder)
- Nhập các trường liệu (validate: tên, số điện thoại, số nhà, quận, tỉnh bắt buộc)
- Chọn loại địa chỉ (Nhà / Văn phòng / Khác)
- Toggle Switch "Đặt làm địa chỉ mặc định"
- Bấm "Lưu địa chỉ" để validate form, tạo AddressModel, gọi callback onSave(), pop về AddressManagementView
