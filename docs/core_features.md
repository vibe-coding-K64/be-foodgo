# Chức năng cốt lõi - FoodGo Backend

Tài liệu này mô tả các chức năng cốt lõi của hệ thống FoodGo, được phân theo 4 vai trò (role). Mỗi chức năng gắn với các Firestore collections/fields tương ứng đã được định nghĩa trong [firebase_collections.md](./firebase_collections.md).

---

## Mục lục

1. [Khách hàng (Customer — Role 1)](#1-khách-hàng-customer--role-1)
2. [Người bán (Merchant — Role 3)](#2-người-bán-merchant--role-3)
3. [Tài xế (Driver — Role 2)](#3-tài-xế-driver--role-2)
4. [Quản trị viên (Admin — Role 4)](#4-quản-trị-viên-admin--role-4)

---

## 1. Khách hàng (Customer — Role 1)

Phân hệ có nhiều tương tác nhất, tập trung vào việc tìm kiếm, đặt hàng và theo dõi đơn.

### 1.1. Khám phá & Tìm kiếm

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Xem danh sách quán | `stores` | Lấy toàn bộ quán, hỗ trợ filter theo `categoryIds` |
| Xem quán theo danh mục | `stores` + `system_categories` | Filter quán theo danh mục (Cơm, Phở/Bún, Trà sữa...) |
| Xem chi tiết quán | `stores` + `products` + `reviews` | Thông tin quán, thực đơn, và đánh giá |
| Xem banner quảng cáo | `banners` | Carousel banner trên trang chủ (filter `isActive == true`, sort theo `order`) |
| Xem danh mục trang chủ | `system_categories` | Grid danh mục món ăn/loại quán (sort theo `order`) |
| Lưu lịch sử tìm kiếm | `users/{userId}/search_history` | Ghi lại từ khóa tìm kiếm của khách |

**Realtime Database (bổ sung):**
- `active_drivers` — Lưu tọa độ GPS của tài xế đang hoạt động, phục vụ tracking đơn hàng real-time cho khách.

### 1.2. Quản lý giỏ hàng

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Xem giỏ hàng | `customer_profiles/{userId}/cart` | Lấy toàn bộ item trong giỏ |
| Thêm món vào giỏ | `customer_profiles/{userId}/cart` | Thêm document mới với `storeId`, `foodId`, `quantity`, `price` |
| Chọn size/topping | `cart/{itemId}` → `size`, `sizePrice`, `toppings` | Mỗi sản phẩm có `optionGroups` chứa size và topping, chọn rồi cập nhật vào cart |
| Thay đổi số lượng | `cart/{itemId}` → `quantity` | Tăng/giảm số lượng hoặc xóa item |
| Ghi chú cho quán | `cart/{itemId}` → `note` | Ghi chú riêng cho từng món hoặc toàn bộ đơn |
| Xóa món khỏi giỏ | `customer_profiles/{userId}/cart/{itemId}` | Xóa document khỏi sub-collection |

**Lưu ý:** Giỏ hàng hiện tại là **multi-store** (nhiều quán cùng 1 giỏ). Cần xem xét logic gộp/tách đơn hoặc chỉ cho phép 1 quán/trong giỏ tại một thời điểm.

### 1.3. Mua sắm & Thanh toán

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Thiết lập địa chỉ giao hàng | `customer_profiles/{userId}/addresses` | CRUD danh sách địa chỉ, đánh dấu địa chỉ mặc định (`isDefault`) |
| Chọn phương thức thanh toán | `customer_profiles/{userId}/payment_methods` | Tiền mặt (type=1), Ví điện tử (type=2: MoMo, ZaloPay, VNPay), Thẻ ngân hàng (type=3) |
| Áp dụng mã giảm giá | `customer_profiles/{userId}/my_vouchers` | Kiểm tra voucher khách sở hữu (`code`, `expiryDate`, `minOrderValue`) trước khi đặt |
| Tạo đơn hàng | `orders` | Tạo document mới với danh sách `items`, `totalAmount`, `deliveryFee`, `deliveryAddress`, `paymentMethod` |
| Thanh toán đơn hàng | `orders` → `paymentMethod` | Xử lý theo phương thức đã chọn (COD ghi nhận ngay, ví điện tử cần xác thực) |

### 1.4. Theo dõi đơn hàng

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Xem danh sách đơn hàng | `orders` → filter `userId == currentUser` | Lấy lịch sử đơn hàng |
| Xem chi tiết đơn hàng | `orders/{orderId}` | Trạng thái (`status` 0-4), thông tin tài xế (`driverId`, `driverName`, `driverPhone`, `vehiclePlate`) |
| Theo dõi trạng thái real-time | `orders/{orderId}` (Firestore listener) | Lắng nghe thay đổi `status` từ 0→1→2→3 |
| Theo dõi vị trí tài xế | `active_drivers` (Realtime Database) | Đọc tọa độ GPS của tài xế đang giao đơn đó |
| Nhận thông báo | `customer_profiles/{userId}/notifications` → `type == 2` | Thông báo cập nhật trạng thái đơn hàng |

**Các trạng thái đơn hàng (`status`):**

| Giá trị | Tên | Mô tả |
|---|---|---|
| 0 | Chờ xác nhận | Đơn đang chờ quán xác nhận |
| 1 | Đang chuẩn bị | Quán đã xác nhận, đang chuẩn bị món |
| 2 | Đang giao | Tài xế đã nhận đơn, đang giao |
| 3 | Hoàn thành | Đã giao thành công |
| 4 | Đã hủy | Đơn đã bị hủy |

### 1.5. Đánh giá & Tích điểm

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Viết đánh giá quán | `reviews` | Tạo review với `storeId`, `starRating` (1-5), `comment`, `imageUrls` |
| Xem đánh giá quán | `reviews` → filter `storeId` | Hiển thị danh sách review của quán |
| Tích lũy điểm thưởng | `customer_profiles/{userId}` → `loyaltyPoints` | Cộng điểm sau mỗi đơn hàng thành công |
| Xem thăng hạng thành viên | `customer_profiles/{userId}` → `membershipTier` | Các bậc: 0=Đồng, 1=Bạc, 2=Vàng, 3=Kim Cương |
| Đổi voucher bằng điểm | `vouchers` + `customer_profiles/{userId}/my_vouchers` | Xem voucher có thể đổi (`pointsRequired`), tạo document trong `my_vouchers` sau khi đổi, trừ điểm |

---

## 2. Người bán (Merchant — Role 3)

Phân hệ tập trung vào quản lý cửa hàng, thực đơn và xử lý đơn đầu vào.

### 2.1. Quản lý cửa hàng

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Xem thông tin quán | `stores/{storeId}` | Lấy thông tin chi tiết quán |
| Cập nhật thông tin quán | `stores/{storeId}` | Sửa `name`, `address`, `avtUrl`, `backUrl` |
| Bật/tắt trạng thái mở cửa | `stores/{storeId}` → `isOpen` | Merchant bật/tắt để nhận đơn hoặc tạm ngưng |
| Cập nhật phí giao hàng | `stores/{storeId}` → `deliveryFee` | Thiết lập phí giao riêng cho quán |
| Cập nhật thời gian giao ước tính | `stores/{storeId}` → `deliveryTime` | VD: "20-30 phút" |
| Quản lý danh mục nội bộ | `stores/{storeId}` → `restaurant_categories` (Map) | Thêm/sửa/xóa danh mục menu riêng của quán (Món chính, Món phụ...) |

### 2.2. Quản lý thực đơn

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Xem danh sách món | `products` → filter `storeId` | Lấy toàn bộ sản phẩm thuộc quán |
| Thêm món mới | `products` | Tạo document với `storeId`, `name`, `basePrice`, `categoryId`, `optionGroups` |
| Sửa thông tin món | `products/{productId}` | Cập nhật `name`, `description`, `basePrice`, `imageUrl` |
| Bật/tắt hết hàng | `products/{productId}` → `isOutOfStock` | Tạm thời ẩn món không còn để bán |
| Bật/tắt món nổi bật | `products/{productId}` → `isFeatured` | Hiển thị món nổi bật trên đầu menu |
| Quản lý nhóm tùy chọn | `products/{productId}` → `optionGroups` | Thêm size (name, price), topping (name, price) vào thực đơn |
| Xóa món | `products/{productId}` | Xóa document khỏi Firestore |

### 2.3. Xử lý đơn hàng

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Nhận thông báo đơn mới | `merchant_profiles/{userId}/notifications` → `type == 21` | Thông báo "Đơn hàng mới từ khách hàng" |
| Xem danh sách đơn hàng | `orders` → filter `storeId` | Lấy đơn hàng của quán |
| Xác nhận đơn hàng | `orders/{orderId}` → `status = 1` | Chuyển từ "Chờ xác nhận" sang "Đang chuẩn bị" |
| Cập nhật trạng thái đơn | `orders/{orderId}` → `status` | Theo dõi: 1=Đang chuẩn bị, 3=Hoàn thành, 4=Đã hủy |
| Hủy đơn hàng | `orders/{orderId}` → `status = 4` | Merchant hủy đơn kèm lý do (ghi vào `updatedAt` hoặc field `cancelReason` nếu cần bổ sung) |

### 2.4. Quản lý tài chính

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Xem số dư ví | `wallets` → filter `userId && role == "merchant"` | Kiểm tra `balance`, `totalEarned`, `totalWithdrawn`, `pendingBalance` |
| Xem lịch sử giao dịch | `transactions` → filter `userId` + `type` | Các loại: `order_payment` (thu), `withdrawal` (rút), `refund` (hoàn) |
| Yêu cầu rút tiền | `transactions` + `wallets` | Tạo transaction type `withdrawal`, cập nhật `pendingBalance` trong wallet |

---

## 3. Tài xế (Driver — Role 2)

Phân hệ có luồng dữ liệu ngắn nhất nhưng yêu cầu xử lý thời gian thực cao.

### 3.1. Trạng thái hoạt động

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Bật/tắt trạng thái nhận đơn | `driver_profiles/{userId}` → `isActive` | `true` = sẵn sàng nhận đơn, `false` = offline |
| Cập nhật thông tin phương tiện | `driver_profiles/{userId}` | `vehiclePlate`, `vehicleType`, `driverLicense` |

### 3.2. Tiếp nhận đơn hàng

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Nhận thông báo đơn mới | `driver_profiles/{userId}/notifications` → `type == 11` | Thông báo "Yêu cầu nhận đơn mới" |
| Xác nhận nhận đơn | `orders/{orderId}` → `status = 2` + `driverId`, `driverName`, `driverPhone`, `vehiclePlate` | Chốt nhận đơn, cập nhật thông tin tài xế vào đơn |
| Cập nhật trạng thái giao | `orders/{orderId}` → `status` | 2=Đang giao, 3=Hoàn thành |
| Nhận thông báo liên quan | `driver_profiles/{userId}/notifications` → `type == 12` | Thông báo khác dành cho tài xế |

### 3.3. Định vị GPS

| Chức năng | Data Source | Mô tả |
|---|---|---|
| Phát vị trí liên tục | **Realtime Database** → `active_drivers/{driverId}` | Ghi `lat`, `lng`, `updatedAt` liên tục khi đang giao đơn |
| Đọc vị trí tài xế | **Realtime Database** → `active_drivers/{driverId}` | Khách hàng đọc tọa độ để theo dõi trên bản đồ |

**Lưu ý:** Realtime Database được sử dụng thay vì Firestore cho GPS tracking vì:
- Cần cập nhật tần suất cao (mỗi 3-5 giây)
- Firestore có giới hạn về số lần ghi/đọc mỗi ngày, chi phí cao hơn cho use case này

### 3.4. Quản lý thu nhập

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Xem số dư ví | `wallets` → filter `userId && role == "driver"` | Kiểm tra `balance`, `totalEarned`, `totalWithdrawn`, `pendingBalance` |
| Xem lịch sử thu nhập | `transactions` → filter `userId` + `type == "delivery_income"` | Các khoản cước phí từ đơn hàng đã giao |
| Yêu cầu rút tiền | `transactions` + `wallets` | Tạo transaction type `withdrawal` |

---

## 4. Quản trị viên (Admin — Role 4)

Phân hệ nắm quyền kiểm soát toàn cục, cấu hình hệ thống và đối soát tài chính.

### 4.1. Cấu hình hệ thống

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Thiết lập phí nền tảng | `system_configs` → `platformFeePercentage` | % phí thu từ mỗi đơn hàng |
| Thiết lập phí giao hàng | `system_configs` → `baseDeliveryFee`, `minDeliveryFee`, `maxDeliveryFee` | Phí giao hàng cơ bản và giới hạn |
| Thiết lập hoa hồng | `system_configs` → `driverCommissionPercentage`, `merchantCommissionPercentage` | % hoa hồng cho tài xế và merchant |
| Thiết lập giới hạn ví | `system_configs` → `minWithdrawalAmount`, `maxWithdrawalAmount` | Số dư rút tối thiểu/tối đa |
| Bật/tắt chế độ bảo trì | `system_configs` → `maintenanceMode` | Khóa app khi đang bảo trì |
| Quản lý phiên bản app | `system_configs` → `appVersion` | Thông báo cập nhật cho client |

### 4.2. Quản lý nội dung

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Quản lý danh mục trang chủ | `system_categories` | CRUD danh mục hiển thị trên app (Cơm, Phở/Bún, Trà sữa...), sắp xếp theo `order` |
| Quản lý banner quảng cáo | `banners` | Tạo, sửa, xóa banner, gán cho quán cụ thể (`storeId`) hoặc banner chung |
| Phát hành voucher hệ thống | `system_vouchers` | Tạo voucher cho toàn hệ thống (`title`, `pointsRequired`, `remaining`, `minOrderValue`) |
| Quản lý voucher công khai | `vouchers` | CRUD voucher hiển thị tại trang Ưu đãi |

**Lưu ý:** Hiện tại `system_vouchers` chưa có dữ liệu seed. Cần bổ sung seeder cho collection này khi phát triển chức năng Admin quản lý voucher.

### 4.3. Quản lý kiểm duyệt

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Xem báo cáo tổng quan | Các collections chính | Thống kê số đơn, số quán, số tài xế, doanh thu |
| Khóa/mở khóa tài khoản | `users/{userId}` | Quản lý trạng thái tài khoản (cần bổ sung field `isActive` hoặc xử lý qua Firestore Rules) |
| Duyệt/xóa đánh giá | `reviews/{reviewId}` | Kiểm duyệt nội dung review vi phạm |
| Can thiệp đơn hàng | `orders/{orderId}` | Sửa trạng thái đơn, hủy đơn thay các bên |

**Quyền hạn của Admin** (`admin_profiles/{userId}` → `permissions`):

| Permission | Mô tả |
|---|---|
| `manage_users` | Quản lý tài khoản người dùng |
| `manage_orders` | Can thiệp xử lý đơn hàng |
| `manage_stores` | Duyệt/khóa cửa hàng |
| `view_reports` | Truy cập báo cáo thống kê |

### 4.4. Đối soát tài chính

| Chức năng | Firestore Collections/Fields | Mô tả |
|---|---|---|
| Duyệt yêu cầu rút tiền | `transactions` → filter `type == "withdrawal" && status == "pending"` | Xem và duyệt/từ chối yêu cầu rút tiền từ Merchant/Driver |
| Cộng/trừ tiền thủ công | `transactions` + `wallets` | Điều chỉnh số dư ví (hoàn tiền khách, phạt vi phạm...) |
| Xem lịch sử giao dịch | `transactions` + `wallets` | Kiểm tra toàn bộ dòng tiền trong hệ thống |
| Báo cáo doanh thu | `transactions` + `orders` + `wallets` | Tổng hợp doanh thu theo ngày/tháng |

---

## Tổng kết — Mapping Features ↔ Collections

| Collection | Customer | Merchant | Driver | Admin |
|---|---|---|---|---|
| `users` | Đăng nhập, lịch sử tìm kiếm | Đăng nhập | Đăng nhập | Đăng nhập |
| `customer_profiles` | Profile, điểm, hạng | — | — | — |
| `driver_profiles` | — | — | Profile, trạng thái online | Quản lý tài xế |
| `merchant_profiles` | — | Profile, danh sách cửa hàng | — | Duyệt merchant |
| `admin_profiles` | — | — | — | Profile, quyền hạn |
| `stores` | Xem quán | Quản lý quán | — | Duyệt quán |
| `products` | Xem thực đơn | Quản lý thực đơn | — | — |
| `system_categories` | Xem danh mục | — | — | Quản lý danh mục |
| `banners` | Xem banner | — | — | Quản lý banner |
| `reviews` | Viết/đọc review | — | — | Kiểm duyệt review |
| `orders` | Đặt hàng, theo dõi | Xử lý đơn | Nhận/giao đơn | Can thiệp đơn |
| `vouchers` / `system_vouchers` | Xem/đổi voucher | — | — | Phát hành voucher |
| `customer_profiles/{userId}/addresses` | Quản lý địa chỉ | — | — | — |
| `customer_profiles/{userId}/payment_methods` | Quản lý thanh toán | — | — | — |
| `customer_profiles/{userId}/cart` | Giỏ hàng | — | — | — |
| `customer_profiles/{userId}/my_vouchers` | Ví voucher | — | — | — |
| `customer_profiles/{userId}/notifications` | Thông báo | — | — | — |
| `driver_profiles/{userId}/notifications` | — | — | Thông báo đơn | — |
| `merchant_profiles/{userId}/notifications` | — | Thông báo đơn | — | — |
| `wallets` | — | Quản lý tài chính | Quản lý thu nhập | Đối soát |
| `transactions` | — | Lịch sử thu | Lịch sử thu | Đối soát |
| `system_configs` | — | — | — | Cấu hình hệ thống |
| `active_drivers` (RDB) | Tracking tài xế | — | Gửi GPS | — |

---

## Lịch sử cập nhật

| Ngày | Mô tả |
|---|---|
| 2026-05-22 | Phiên bản đầu tiên — liệt kê đầy đủ chức năng theo 4 vai trò |
