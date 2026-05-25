# Màn Hình - Trang Chủ (Home)

## 1. Màn Hình Trang Chủ (HomeView)

**File:** `lib/features/home/views/home_view.dart`

**Class:** `HomeView`

**Mục đích chính:** Màn hình chính của ứng dụng, hiển thị tổng hợp nội dung bao gồm địa chỉ, tìm kiếm, danh mục, banner quảng cáo, quán ăn gần đây, món ăn nổi bật, và quán phổ biến.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **Header Banner Địa Chỉ:** Hình ảnh banner xanh gradient, icon vị trí, tên địa chỉ giao hàng hiện tại (VD: "123 Nguyễn Huệ, Quận 1"), nút chỉnh sửa địa chỉ
- **Thanh Tìm Kiếm:** Ô tìm kiếm trắng, placeholder "Tìm kiếm món ăn, cửa hàng...", icon tìm kiếm
- **Danh Mục Món Ăn:** ListView ngang các danh mục từ Firestore Stream (HomeService.getCategoriesStream()), hiển thị icon và tên danh mục (VD: Món chính, Đồ uống, Trà sữa, ...)
- **Banner Quảng Cáo Carousel:** Carousel hình ảnh quảng cáo từ Firestore Stream (HomeService.getBannersStream())
- **Khối "Quán Ngon Gần Đây":** ListView ngang (chiều cao 200px) các card quán ăn, mỗi card chứa: hình ảnh bìa (160x100), tên quán, số sao (VD: 4.8), số lượt đánh giá (VD: (120)), vị trí (VD: 1.2 km), thời gian giao (VD: 15-20 phút), địa chỉ
- **Khối "Món Ăn Nổi Bật":** ListView ngang (chiều cao 240px) các card món ăn, mỗi card chứa: hình món (150x120), tên món, giá tiền (VD: 35.000 VNĐ), số sao, khoảng cách, thời gian giao, nếu hết hàng thì hiển thị overlay "Hết hàng"
- **Khối "Quán Phổ Biến":** ListView dọc (VerticalStoreItem) các quán, mỗi item chứa: hình avatar (90x90), tên quán, địa chỉ, số sao, số đánh giá, trạng thái mở/đóng (badge xanh/đỏ), khoảng cách, thời gian giao
- **FAB Giỏ Hàng:** Nút bấm tròn ở góc phải dưới cùng màn hình, icon giỏ hàng, chuyển sang CartView
- **Skeleton Loading:** Các placeholder loading cho card quán và món ăn khi đang tải dữ liệu

---

**Các hành động của người dùng (User Actions):**

- Bấm nút chỉnh sửa địa chỉ để mở AddressManagementView
- Bấm thanh tìm kiếm để mở SearchView
- Bấm một danh mục để mở SearchResultView với query = tên danh mục
- Bấm "Xem tất cả" của khối quán ngon gần đây, món ăn nổi bật, quán phổ biến
- Bấm một card quán ăn (Horizontal/Vertical) để mở RestaurantDetailView với storeId tương ứng
- Bấm một card món ăn để gọi showProductDetailSheet() (ProductDetailBottomSheet)
- Bấm FAB Giỏ Hàng để mở CartView

---

## 2. Màn Hình Tìm Kiếm (SearchView)

**File:** `lib/features/search/views/search_view.dart`

**Class:** `SearchView`

**Mục đích chính:** Màn hình tìm kiếm chính, cho phép người dùng nhập từ khóa và xem lịch sử tìm kiếm.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- Thanh tìm kiếm ở AppBar (có icon back, ô nhập, icon xóa, icon tìm kiếm)
- Icon giỏ hàng ở góc phải AppBar
- **Lịch sử tìm kiếm:** Danh sách các chip từ khóa đã tìm từ Firestore Stream (SearchService.getSearchHistoryStream()), mỗi chip có icon history và nút xóa
- Nút "Xóa tất cả" lịch sử
- Tiêu đề "Tìm kiếm phổ biến" (Popular Searches)
- Danh sách chip từ khóa phổ biến từ API (ApiSearchService.getPopularKeywords()), mỗi chip có icon theo loại từ khóa (VD: pho -> ramen_dining, bánh mì -> bakery_dining, cà phê -> coffee, trà sữa -> local_cafe)
- Trạng thái rỗng: "Chưa có lịch sử tìm kiếm" (italic)
- Skeleton loading cho popular keywords

---

**Các hành động của người dùng (User Actions):**

- Nhập từ khóa vào ô tìm kiếm, bấm Enter hoặc icon tìm kiếm để gọi SearchService.addSearchKeyword() và chuyển sang SearchResultView với query
- Bấm icon xóa trong ô nhập để xóa nội dung
- Bấm một chip lịch sử tìm kiếm để điền lại từ khóa và gọi tìm kiếm
- Bấm nút xóa trên chip lịch sử để gọi SearchService.deleteSearchHistory(id)
- Bấm "Xóa tất cả" để gọi SearchService.clearAllHistory() (có xác nhận dialog)
- Bấm chip từ khóa phổ biến để chuyển sang SearchResultView
- Bấm icon giỏ hàng để mở CartView

---

## 3. Màn Hình Kết Quả Tìm Kiếm (SearchResultView)

**File:** `lib/features/search/views/search_result_view.dart`

**Class:** `SearchResultView`

**Mục đích chính:** Hiển thị kết quả tìm kiếm với bộ lọc và sắp xếp.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- Header: nút Back, thanh tìm kiếm hiển thị từ khóa đã tìm (clickable để quay về SearchView), icon giỏ hàng
- **Thanh Filter Bar (ngang):** Các FilterChip lọc: "Giá thấp dần", "Giá cao dần", "Đánh giá cao", "Tất cả"
- Danh sách kết quả từ API (ApiSearchService.fetchSearchResults(query)), mỗi item chứa:
  - Hình sản phẩm (80x80)
  - Tên sản phẩm
  - Tên cửa hàng
  - Giá tiền (màu xanh lá)
  - Số sao + số đánh giá
  - Nút "Thêm" (icon +)
- Trạng thái rỗng: icon search_off, "Không có kết quả", "Thử từ khóa khác"
- Trạng thái lỗi: icon error, thông báo lỗi, nút "Thử lại"
- Skeleton card khi loading (5 placeholder cards)

---

**Các hành động của người dùng (User Actions):**

- Bấm FilterChip để sắp xếp kết quả (SearchSortType.priceAsc, priceDesc, ratingDesc, none)
- Bấm một item kết quả để mở chi tiết sản phẩm
- Bấm nút "Thêm" trên item để gọi SnackBar thông báo "Đã được thêm"
- Bấm icon giỏ hàng để mở CartView
- Bấm thanh tìm kiếm header để quay về SearchView
- Bấm "Thử lại" khi có lỗi để gọi lại API

---

## 4. Màn Hình Chi Tiết Cửa Hàng (RestaurantDetailView)

**File:** `lib/features/restaurant/views/restaurant_detail_view.dart`

**Class:** `RestaurantDetailView`

**Mục đích chính:** Hiển thị chi tiết một quán ăn, bao gồm thông tin cơ bản, danh sách danh mục món ăn, và danh sách món theo danh mục.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **SliverAppBar:** Hình bìa mở rộng (260px), tiêu đề tên quán phụ để AppBar, nút yêu thích, nút chia sẻ
- **Thông Tin Cơ Bản:**
  - Avatar quán (hình tròn, border trắng, shadow)
  - Tên quán (in đậm, font 24)
  - Khoảng cách + thời gian giao (VD: "Cách 1.2 km - Giao trong 15-20 phút")
  - Phí giao hàng (VD: "Phí giao hàng: 15.000 VNĐ")
  - Khối đánh giá (clickable): số sao + số đánh giá (VD: "4.8 sao - 120 đánh giá")
- **Sticky Tab Danh Mục:** ListView ngang các tab danh mục (Tất cả, Đồ uống, Fast food, Món Việt, Ăn vặt, Tráng miệng, Sáng trưa, Hải sản), tab được chọn có nền xanh lá
- **Danh Sách Món Ăn:** StreamBuilder theo danh mục từ RestaurantService.getProductsByCategoryStream(), mỗi item chứa:
  - Hình món ăn (90x90), nếu hết hàng thì overlay "Hết hàng"
  - Tên món ăn (in đậm)
  - Mô tả ngắn
  - Giá tiền (màu xanh lá, in đậm)
  - Nút (+) thêm vào giỏ (nếu còn hàng)

---

**Các hành động của người dùng (User Actions):**

- Bấm tab danh mục để lọc danh sách món theo danh mục tương ứng
- Bấm khối đánh giá để mở RestaurantReviewsView
- Bấm nút (+) thêm món để gọi SnackBar thông báo "Đã thêm vào giỏ hàng"
- Bấm icon yêu thích / chia sẻ (chỉ hiển thị SnackBar placeholder)
- Cuộn xuống xem thêm món ăn trong danh mục
