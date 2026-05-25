# Màn Hình - Chi Tiết Món Ăn (Product Detail)

## 1. Bottom Sheet Chi Tiết Món Ăn (ProductDetailBottomSheet)

**File:** `lib/features/product/views/product_detail_bottom_sheet.dart`

**Class:** `ProductDetailBottomSheet`

**Mục đích chính:** Hiển thị chi tiết món ăn khi người dùng bấm vào một món, cho phép chọn topping, nhập số lượng, ghi chú, và thêm vào giỏ hàng.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Ảnh Món Ăn:** Hình chữ nhật trên cùng (30% chiều cao màn hình), có nút X đóng góc phải trên
- **Thông Tin Món:**
  - Tên món ăn (in đậm, font 20)
  - Giá cơ sở (VD: "35.000 VNĐ", màu xanh lá)
  - Mô tả chi tiết (nếu có)
- **Các Nhóm Tùy Chọn (Option Groups):** Dòng theo ProductModel.optionGroups, mỗi nhóm chứa:
  - Tên nhóm (VD: "Kích thước", "Topping")
  - Badge "Bắt buộc" (màu đỏ) / "Tùy chọn" (màu xám)
  - Danh sách tùy chọn: Radio (single-select) hoặc Checkbox (multi-select)
  - Mỗi tùy chọn hiển thị: tên tùy chọn + giá (+VD: "+5.000")
- **Phần Ghi Chú:**
  - Tiêu đề "Ghi chú"
  - Ô nhập text (3 dòng, 100 ký tự)
  - Placeholder: "VD: Không đường, thêm đá"
- **Sticky Bottom Bar:**
  - Bộ đếm số lượng (nút - và +)
  - Nút "Thêm vào giỏ hàng" + giá tổng (VD: "Thêm vào giỏ hàng 35.000")

---

**Các hành động của người dùng (User Actions):**

- Bấm nút X đóng để đóng BottomSheet
- Bấm vào 1 tùy chọn (Radio/Checkbox):
  - Single-select: bỏ chọn tùy chọn cũ, chọn tùy chọn mới
  - Multi-select: toggle chọn / bỏ chọn tùy chọn
- Nhập ghi chú vào ô text
- Tăng/giảm số lượng (nút - và +)
- Bấm nút "Thêm vào giỏ hàng" để gọi CartState.addItem() với các tham số:
  - product, selectedSize, sizePrice, selectedToppings, note, quantity
  - Kiểm tra AuthStorage.getUserId(), nếu chưa đăng nhập thì hiển thị SnackBar yêu cầu đăng nhập
  - Nếu thành công: đóng BottomSheet, hiển thị SnackBar "Đã thêm vào giỏ hàng"
  - Nếu thất bại: hiển thị SnackBar lỗi

---

## 2. Màn Hình Đánh Giá Cửa Hàng (RestaurantReviewsView)

**File:** `lib/features/restaurant/views/restaurant_reviews_view.dart`

**Class:** `RestaurantReviewsView`

**Mục đích chính:** Hiển thị danh sách đánh giá của khách hàng dành cho một quán ăn, với bộ lọc và thống kê.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Thống Kê Tổng Quan (ReviewOverviewSection):**
  - Số sao trung bình (VD: 4.8, font 57, in đậm)
  - Icon sao vàng nhỏ
  - Tổng số đánh giá (VD: "120 đánh giá")
  - Biểu đồ Tròn (Pie Chart) phân bổ số sao 1-5 sao (donut chart, 5 màu)
  - Thanh tiến triển (LinearProgressIndicator) cho từng mức sao
  - Phần trăm cho từng mức sao (VD: 85%)
- **Bộ Lọc (ReviewFilterBar - cuộn ngang):**
  - Nút lọc theo số sao (Dropdown, có icon mũi tên): "Tất cả", "5 sao", "4 sao", "3 sao", "2 sao", "1 sao"
  - FilterChip "Có bình luận" (toggle)
  - FilterChip "Có hình ảnh" (toggle)
- **Danh Sách Đánh Giá (ReviewItemWidget):**
  - Avatar người đánh giá (hình tròn 40px)
  - Tên người đánh giá (in đậm)
  - Thời gian đánh giá (VD: "14:30 - 22/05/2026")
  - 5 icon sao (vàng / xám)
  - Nội dung bình luận (nếu có), nếu không có thì hiển thị "Không có nội dung" (italic)
  - Hình ảnh đính kèm (nếu có, 80x80, 3-4 ảnh cột ngang)
- **Trạng thái rỗng:** Icon rate_review_outlined, "Chưa có đánh giá nào"

---

**Các hành động của người dùng (User Actions):**

- Bấm Bottom Sheet lọc số sao để chọn số sao lọc
- Toggle FilterChip "Có bình luận" để lọc
- Toggle FilterChip "Có hình ảnh" để lọc
- Cuộn danh sách để xem thêm đánh giá
