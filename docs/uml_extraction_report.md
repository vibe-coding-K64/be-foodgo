# Tài liệu trích xuất từ mã nguồn Spring Boot để vẽ UML

## 1. Cấu trúc các thực thể / model cốt lõi

> Ghi chú:
> - Hệ thống dùng Firestore nên quan hệ chủ yếu thể hiện qua các trường tham chiếu như `userId`, `storeId`, `orderId`, `foodId`, `addressId`, `walletId`.
> - Bên dưới chỉ liệt kê **thuộc tính dữ liệu**, không liệt kê getter/setter/hàm xử lý.

---

### Class: `User`
- `id: String`
- `email: String`
- `password: String`
- `fullName: String`
- `phoneNumber: String`
- `photoUrl: String`
- `roles: List<Integer>`
- `createdAt: String`
- `updatedAt: String`
- `isEmailVerified: Boolean`
- `isActive: Boolean`

**Trường liên kết / quan hệ**
- `id`: khóa nhận diện người dùng, được các thực thể khác tham chiếu qua `userId`.

---

### Class: `Store`
- `id: String`
- `name: String`
- `description: String`
- `address: String`
- `rating: Double`
- `reviewCount: Integer`
- `avtUrl: String`
- `backUrl: String`
- `isOpen: boolean`
- `approvalStatus: String`
- `rejectReason: String`
- `adminLockedReason: String`
- `deliveryTime: String`
- `deliveryFee: Double`
- `categoryIds: List<String>`
- `restaurant_categories: Object`
- `lat: Double`
- `lng: Double`
- `createdAt: Date`
- `updatedAt: Date`

**Trường liên kết / quan hệ**
- `id`: được `Product.storeId`, `Order.storeId`, `Review.storeId`, `Voucher.storeId` tham chiếu.
- `categoryIds`: liên kết logic tới danh mục món ăn.
- `lat`, `lng`: phục vụ logic giao hàng và gán tài xế.

---

### Class: `Product`
- `id: String`
- `storeId: String`
- `categoryId: String`
- `categoryName: String`
- `name: String`
- `description: String`
- `basePrice: double`
- `imageUrl: String`
- `isOutOfStock: Boolean`
- `isFeatured: Boolean`
- `optionGroups: List<ProductOptionGroup>`
- `rating: Double`
- `reviewCount: Integer`
- `createdAt: Timestamp`
- `updatedAt: Timestamp`

**Trường liên kết / quan hệ**
- `storeId`: liên kết logic tới `Store.id`.
- `categoryId`: liên kết logic tới category.
- `id`: được các dòng món trong đơn hàng tham chiếu qua `foodId`.
- `optionGroups`: thành phần cấu hình biến thể/topping của sản phẩm.

#### Nested class: `ProductOptionGroup`
- `name: String`
- `isSingleSelect: Boolean`
- `options: List<ProductOption>`

#### Nested class: `ProductOption`
- `name: String`
- `price: double`

---

### Class: `Order`
- `id: String`
- `userId: String`
- `storeId: String`
- `storeName: String`
- `code: String`
- `deliveryAddress: String`
- `addressId: String`
- `receiverName: String`
- `receiverPhone: String`
- `deliveryFee: double`
- `driverName: String`
- `driverPhone: String`
- `items: List<OrderItem>`
- `totalAmount: double`
- `discountAmount: double`
- `shopDiscountAmount: double`
- `freeshipDiscountAmount: double`
- `finalAmount: double`
- `paymentMethod: Object`
- `status: Object`
- `createdAt: Date`
- `updatedAt: Date`
- `deletedAt: Date`
- `deliveryHeading: Double`
- `deliveryLat: Double`
- `deliveryLng: Double`
- `note: String`
- `paymentStatus: Integer`

**Trường liên kết / quan hệ**
- `userId`: liên kết logic tới `User.id`.
- `storeId`: liên kết logic tới `Store.id`.
- `addressId`: liên kết logic tới `Address.id` của khách hàng.
- `items`: danh sách chi tiết món trong đơn.
- `deliveryLat`, `deliveryLng`, `deliveryHeading`: phục vụ logic định vị giao hàng và gán tài xế.
- `driverName`, `driverPhone`: lưu snapshot thông tin tài xế khi nhận đơn, nhưng không thấy field `driverId` trực tiếp trong class model này.
- `paymentMethod`: liên hệ tới phương thức thanh toán của khách.
- `paymentStatus`: trạng thái thanh toán.

---

### Class: `OrderItem`
- `foodId: String`
- `imageUrl: String`
- `name: String`
- `size: String`
- `options: Object`
- `quantity: int`
- `price: double`

**Trường liên kết / quan hệ**
- `foodId`: liên kết logic tới `Product.id`.
- `options`: thông tin topping / lựa chọn thêm của sản phẩm.

---

### Class: `Review`
- `id: String`
- `orderId: String`
- `itemId: String`
- `foodId: String`
- `storeId: String`
- `userId: String`
- `userName: String`
- `userAvatarUrl: String`
- `starRating: Integer`
- `comment: String`
- `imageUrls: List<String>`
- `createdAt: Date`
- `updatedAt: Date`
- `replyComment: String`
- `repliedAt: Date`

**Trường liên kết / quan hệ**
- `orderId`: liên kết logic tới `Order.id`.
- `itemId`: liên kết tới item cụ thể trong đơn.
- `foodId`: liên kết logic tới `Product.id`.
- `storeId`: liên kết logic tới `Store.id`.
- `userId`: liên kết logic tới `User.id`.

---

## 2. Các model liên quan mạnh đến nghiệp vụ thanh toán / ví / voucher

> Trong code không thấy class model tên đúng là `Wallet` hoặc `Transaction`, nhưng hệ thống có collection Firestore `wallets` và `transactions`, đồng thời được thao tác rõ trong `WalletRepository` và `WalletService`. Vì vậy có thể coi đây là **thực thể logic** để vẽ UML mức phân tích dữ liệu.

---

### Thực thể logic: `Wallet` (suy ra từ Firestore collection `wallets`)
**Các field được ghi/đọc trong code**
- `id: String`
- `userId: String`
- `role: Object` (`"driver"` hoặc `1` cho merchant tùy luồng)
- `balance: double`
- `totalEarned: double`
- `totalWithdrawn: double`
- `pendingBalance: double`
- `createdAt: Timestamp`
- `updatedAt: Timestamp`
- `bankName: String` *(được cập nhật trong luồng rút tiền merchant)*
- `bankAccountNumber: String` *(được cập nhật trong luồng rút tiền merchant)*
- `bankAccountName: String` *(được cập nhật trong luồng rút tiền merchant)*

**Trường liên kết / quan hệ**
- `userId`: liên kết logic tới `User.id` hoặc chủ ví tương ứng.
- `role`: phân biệt ví tài xế / ví cửa hàng.
- `id`: được `Transaction.walletId` tham chiếu.

---

### Thực thể logic: `Transaction` (suy ra từ Firestore collection `transactions`)
**Các field được ghi/đọc trong code**
- `id: String`
- `walletId: String`
- `userId: String`
- `type: int`
- `amount: double`
- `fee: double`
- `netAmount: double`
- `description: String`
- `orderId: String`
- `status: int`
- `createdAt: Timestamp`

**Ý nghĩa type thấy trong code**
- `1`: giao dịch doanh thu merchant
- `2`: giao dịch thu nhập tài xế
- `3`: yêu cầu rút tiền
- `5`: COD debit / khấu trừ ví tài xế với đơn tiền mặt

**Trường liên kết / quan hệ**
- `walletId`: liên kết logic tới `Wallet.id`.
- `userId`: liên kết logic tới chủ ví / người tạo giao dịch.
- `orderId`: liên kết logic tới `Order.id`.

---

### Class: `PaymentMethod`
- `id: String`
- `name: String`
- `type: int`
- `details: String`
- `isDefault: Boolean`
- `cardBrand: String`
- `last4Digits: String`
- `walletBrand: String`
- `isLinked: Boolean`
- `createdAt: Instant`
- `updatedAt: Instant`

**Trường liên kết / quan hệ**
- `id`: được `Order.paymentMethod` tham chiếu gián tiếp trong lúc checkout.
- Thực tế được lưu dưới subcollection `customer_profiles/{userId}/payment_methods`.

---

### Class: `Voucher`
- `id: String`
- `storeId: String`
- `title: String`
- `subtitle: String`
- `code: String`
- `type: int`
- `value: double`
- `pointsRequired: int`
- `imageUrl: String`
- `remaining: int`
- `terms: String`
- `minOrderValue: double`
- `limitCount: int`
- `usedCount: int`
- `expiryDate: Date`
- `isActive: boolean`
- `validityDays: int`
- `isFreeship: boolean`
- `createdAt: Date`
- `updatedAt: Date`

**Trường liên kết / quan hệ**
- `storeId`: liên kết logic tới `Store.id` nếu voucher thuộc shop.
- `id`: được dùng trong checkout để áp dụng voucher hệ thống.
- `remaining`: bị giảm khi đặt hàng thành công.

---

### Class: `MyVoucher`
- `id: String`
- `title: String`
- `subtitle: String`
- `code: String`
- `description: String`
- `type: int`
- `value: double`
- `imageUrl: String`
- `terms: String`
- `minOrderValue: double`
- `expiryDate: Date`
- `isActive: boolean`
- `isFreeship: boolean`
- `createdAt: Date`
- `updatedAt: Date`

**Trường liên kết / quan hệ**
- `id`: mã voucher cá nhân của người dùng.
- Được lưu dưới `customer_profiles/{userId}/my_vouchers`.
- Liên kết logic với khách hàng qua `userId` ở đường dẫn Firestore.

---

### Class: `Address`
- `id: String`
- `name: String`
- `address: String`
- `receiverName: String`
- `receiverPhone: String`
- `lat: Double`
- `lng: Double`
- `isDefault: Boolean`
- `createdAt: Instant`
- `updatedAt: Instant`
- `deletedAt: Instant`

**Trường liên kết / quan hệ**
- `id`: được `Order.addressId` tham chiếu.
- Được lưu dưới profile khách hàng theo `userId`.
- `lat`, `lng`: dùng để kiểm tra khoảng cách giao hàng.

---

## 3. Quan hệ UML gợi ý từ dữ liệu

- `User` 1 --- n `Order` qua `Order.userId`
- `Store` 1 --- n `Product` qua `Product.storeId`
- `Store` 1 --- n `Order` qua `Order.storeId`
- `Store` 1 --- n `Review` qua `Review.storeId`
- `User` 1 --- n `Review` qua `Review.userId`
- `Order` 1 --- n `OrderItem`
- `Product` 1 --- n `Review` qua `Review.foodId`
- `Product` 1 --- n `OrderItem` qua `OrderItem.foodId`
- `User` 1 --- n `PaymentMethod`
- `User` 1 --- n `Address`
- `User` 1 --- n `Wallet`
- `Wallet` 1 --- n `Transaction`
- `Order` 1 --- n `Transaction` qua `Transaction.orderId`
- `Store` 1 --- n `Voucher`
- `User` 1 --- n `MyVoucher`

---

## 4. Luồng xử lý nghiệp vụ 1: Thanh toán và tạo đơn hàng (`CheckoutService.thucHienDatHang`)

### Mô tả step-by-step bằng tiếng Việt

1. **Nhận request checkout**
   - Đầu vào gồm:
     - `userId`
     - `addressId`
     - `storeId`
     - danh sách `items`
     - `paymentMethod`
     - các voucher tùy chọn
     - `idempotencyKey`

2. **Kiểm tra user đăng nhập có khớp với user của request hay không**
   - Nếu `authenticatedUserId == null` hoặc khác `request.userId`
     - **báo lỗi** `userId không khớp`
   - Ngược lại
     - tiếp tục xử lý

3. **Kiểm tra chống submit đơn trùng bằng `idempotencyKey`**
   - Nếu `idempotencyKey` khác rỗng:
     - **Đọc Firestore**
       - query collection `orders`
       - filter `userId == authenticatedUserId`
       - filter `idempotencyKey == request.idempotencyKey`
       - `limit(1)`
     - Nếu tìm thấy đơn cũ:
       - **báo lỗi** đơn hàng đã tồn tại
     - Nếu không thấy:
       - tiếp tục

4. **Kiểm tra giỏ hàng có món hay không**
   - Nếu `items == null` hoặc rỗng
     - **báo lỗi** giỏ hàng rỗng
   - Ngược lại
     - tiếp tục

5. **Lấy địa chỉ giao hàng**
   - **Đọc Firestore** qua `addressRepository.layMotDiaChi(userId, addressId)`
   - Nếu lỗi truy vấn
     - **báo lỗi hệ thống**
   - Nếu không tìm thấy địa chỉ
     - **báo lỗi** không tìm thấy địa chỉ
   - Nếu có địa chỉ
     - tiếp tục

6. **Lấy thông tin cửa hàng**
   - **Đọc Firestore** qua `storeRepository.getStoreById(storeId)`
   - Nếu lỗi truy vấn hoặc không tìm thấy store
     - **báo lỗi hệ thống**
   - Nếu có store
     - tiếp tục

7. **Kiểm tra khoảng cách giao hàng**
   - Lấy:
     - `store.lat`, `store.lng`
     - `address.lat`, `address.lng`
   - Nếu thiếu bất kỳ tọa độ nào
     - **bỏ qua bước kiểm tra khoảng cách**
   - Nếu đủ tọa độ:
     - dùng **thuật toán Haversine** để tính khoảng cách giữa cửa hàng và địa chỉ nhận hàng
     - nếu khoảng cách `> 10 km`
       - **báo lỗi** vượt giới hạn giao hàng
     - ngược lại
       - tiếp tục

8. **Kiểm tra tồn kho / trạng thái hết hàng của từng món**
   - Tạo danh sách `foodIds`
   - Với mỗi `foodId`:
     - **Đọc Firestore** qua `productRepository.findById(foodId)`
     - Nếu không tìm thấy product
       - xem như **không hợp lệ / hết hàng**
     - Nếu `product.isOutOfStock == true`
       - **báo lỗi** món ăn hết hàng trong giỏ
     - Nếu còn hàng
       - tiếp tục

9. **Tính tổng tiền hàng**
   - Với từng item trong request:
     - **Đọc Firestore** lại product qua `productRepository.findById(foodId)` để lấy giá gốc và option
     - xác định `selectedSize` nếu có nhóm option là size
     - tính đơn giá:
       - giá gốc `basePrice`
       - cộng thêm giá `size` nếu có
       - cộng thêm giá tất cả topping/options còn lại
     - nhân với `quantity`
   - Cộng tất cả thành `tongTienHang`

10. **Gán phí ship cơ bản**
    - `phiShip = 15000`

11. **Xử lý voucher giảm giá tổng**
    - Nếu `discountVoucherId` có giá trị:
      - gọi `kiemTraVaXuLyVoucher(userId, voucherId, tongTienHang)`
      - bên trong:
        - thử **đọc Firestore** voucher hệ thống từ collection `vouchers`
        - nếu không có thì thử **đọc Firestore** voucher cá nhân từ `customer_profiles/{userId}/my_vouchers`
      - các điều kiện kiểm tra:
        - nếu không tìm thấy ở cả 2 nơi → **báo lỗi voucher không tồn tại**
        - nếu voucher hệ thống có `remaining <= 0` → **báo lỗi hết số lượng**
        - nếu voucher cá nhân `isActive == false` → **báo lỗi chưa kích hoạt**
        - nếu voucher cá nhân hết hạn → **báo lỗi hết hạn**
        - nếu `tongTienHang < minOrderValue` → **báo lỗi không đạt đơn tối thiểu**
      - nếu hợp lệ:
        - tính tiền giảm:
          - nếu là freeship → giảm tối đa bằng phí ship
          - nếu type = phần trăm → `tongTienHang * percent`
          - nếu type = tiền mặt → giảm số tiền cố định nhưng không vượt `tongTienHang`

12. **Xử lý shop voucher**
    - Nếu `shopVoucherId` có giá trị:
      - luồng kiểm tra và tính giảm **tương tự bước 11**

13. **Xử lý voucher freeship**
    - Nếu `freeshpVoucherId` có giá trị:
      - luồng kiểm tra và tính giảm **tương tự bước 11**
      - nếu voucher là freeship thì số tiền giảm thường bằng phần phí ship còn lại

14. **Tính tổng giảm giá và tổng thanh toán**
    - `tongSoTienGiam = discountAmount + shopDiscountAmount + freeshipDiscountAmount`
    - `tongThanhToan = tongTienHang + phiShip - tongSoTienGiam`
    - Nếu `tongThanhToan < 0`
      - ép về `0`

15. **Chuẩn hóa danh sách item để ghi vào đơn**
    - Với mỗi item:
      - **Đọc Firestore** product để lấy giá option nếu cần
      - dựng object item lưu vào order:
        - `foodId`
        - `name`
        - `price`
        - `quantity`
        - `imageUrl`
        - `size`
        - `options`

16. **Tạo mã đơn tạm thời**
    - Sinh prefix dạng `FG-yyyymmdd-...`
    - ID thật sẽ có sau khi tạo document order

17. **Tạo đơn hàng atomically bằng Firestore WriteBatch**
    - Hàm `taoDonHangAtomic(...)`

### Pseudocode chi tiết cho `taoDonHangAtomic(...)`

```text
Bắt đầu WriteBatch

Sinh document mới trong collection "orders"
Lấy orderId mới

Chuẩn bị itemsData từ danh sách món

Tạo map orderData gồm:
- code
- userId
- storeId
- storeName
- items
- totalAmount
- deliveryFee
- discountAmount
- shopDiscountAmount
- freeshipDiscountAmount
- finalAmount
- paymentMethod
- deliveryAddress
- addressId
- deliveryLat
- deliveryLng
- receiverName
- receiverPhone
- status = 0
- paymentStatus = 1
- note
- createdAt = serverTimestamp
- updatedAt = serverTimestamp
- deletedAt = null
- idempotencyKey (nếu có)

Ghi batch.set(orderDocRef, orderData)

Nếu có voucher:
  Với từng voucher:
    Nếu là voucher hệ thống:
      batch.update("vouchers/{voucherId}", tăng remaining -1)
    Nếu là voucher cá nhân:
      batch.delete("customer_profiles/{userId}/my_vouchers/{voucherId}")

Đọc toàn bộ cart của user từ:
- customer_profiles/{userId}/cart

Với từng cart item:
  Nếu foodId giống item đã đặt
  và selectedOptions giống item đã đặt:
    batch.delete(cartItem đó)

Commit batch

Nếu commit lỗi:
  báo lỗi hệ thống "Không thể tạo đơn hàng"

Trả về orderId
```

18. **Các bước đọc/ghi Firestore trong `taoDonHangAtomic`**
   - **Ghi Firestore**
     - `batch.set` vào collection `orders`
   - **Ghi Firestore**
     - `batch.update` giảm `remaining` của voucher hệ thống
   - **Ghi Firestore**
     - `batch.delete` voucher cá nhân đã dùng
   - **Đọc Firestore**
     - query collection `customer_profiles/{userId}/cart`
   - **Ghi Firestore**
     - `batch.delete` các cart item đã khớp món vừa đặt
   - **Ghi Firestore**
     - `batch.commit()`

19. **Sau khi tạo đơn thành công**
   - cập nhật lại `orderCode` từ `orderId`
   - **Đọc Firestore** payment method qua `paymentRepository.layMotPhuongThuc(userId, paymentMethodId)` để lấy tên hiển thị
   - gửi notification cho merchant theo `storeId`

20. **Trả response checkout**
   - trả về:
     - `orderId`
     - `orderCode`
     - `storeId`
     - `storeName`
     - `userId`
     - `items`
     - `totalAmount`
     - `deliveryFee`
     - `discountAmount`
     - `shopDiscountAmount`
     - `freeshipDiscountAmount`
     - `finalAmount`
     - `paymentMethod`
     - `deliveryAddress`
     - `status = 0`
     - `paymentStatus = 1`
     - `createdAt`
     - `note`

---

## 5. Luồng xử lý nghiệp vụ 2: Phân công tài xế (`OrderAssignmentService`)

### Mục tiêu nghiệp vụ
- Khi đơn hàng đến trạng thái sẵn sàng giao, hệ thống tự động tìm tài xế gần cửa hàng.
- Ưu tiên tài xế:
  - đang active
  - nằm trong bán kính cho phép
  - nếu có thể thì cùng hướng giao hàng
- Nếu tài xế không phản hồi trong thời gian timeout, hệ thống tự quét đợt khác.

---

## 5.1. Luồng khởi động gán đơn

### Step-by-step

1. **Nhận yêu cầu trigger gán đơn**
   - Hàm `triggerAssignmentForOrder(orderId)`

2. **Kiểm tra `orderId`**
   - Nếu `orderId == null` hoặc rỗng
     - bỏ qua
   - Nếu đơn này đang nằm trong `processingOrders`
     - bỏ qua để tránh chạy trùng
   - Ngược lại
     - thêm `orderId` vào danh sách đang xử lý
     - gọi `xuLyGanDonTuFirestore(orderId)`

3. **Tạo context để gán đơn**
   - Trong `taoAssignmentContext(orderId)`:
     - **Đọc Firestore** order raw qua `statsRepository.findOrderRawById(orderId)`
   - Nếu không tìm thấy order:
     - dừng
   - Lấy `status` của order
   - Nếu `status != 1`
     - **không gán tài xế**
     - dừng
   - Nếu `status == 1`
     - tiếp tục

4. **Lấy vị trí cửa hàng**
   - Từ `orderData` lấy `storeId`
   - **Đọc Firestore** store qua `storeRepository.getStoreById(storeId)`
   - Lấy `storeLat`, `storeLng`

5. **Lấy vị trí giao hàng**
   - Đọc từ `orderData`:
     - `deliveryLat`
     - `deliveryLng`
     - `deliveryHeading`

6. **Nếu chưa có `deliveryHeading`**
   - Nếu đủ:
     - `storeLat`, `storeLng`, `deliveryLat`, `deliveryLng`
   - thì tính `deliveryHeading` bằng hàm `tinhHeading(...)`
   - Đây là góc hướng từ cửa hàng đến điểm giao

7. **Nếu thiếu dữ liệu tọa độ**
   - Nếu thiếu `storeLat` hoặc `storeLng` hoặc `deliveryLat` hoặc `deliveryLng`
     - hệ thống vẫn ghi log cảnh báo
     - context có thể vẫn được trả ra nhưng việc gán có thể thất bại ở bước sau

8. **Kiểm tra đã có `order_request` pending hay chưa**
   - **Đọc Firestore** qua `orderRequestRepository.findByOrderId(orderId)`
   - Nếu có document và `status == "pending"`
     - bỏ qua trigger mới
   - Nếu chưa có pending
     - tiếp tục

9. **Bắt đầu gán đơn**
   - gọi `batDauGanDon(orderId, storeLat, storeLng, deliveryLat, deliveryLng, deliveryHeading)`

---

## 5.2. Luồng tìm tài xế gần nhất và lọc theo hướng

### Step-by-step

1. **Kiểm tra vị trí cửa hàng**
   - Nếu `storeLat == null` hoặc `storeLng == null`
     - không thể gán đơn
     - gọi `thongBaoCuaHangKhongCoTaiXe(orderId)`
     - dừng

2. **Lấy danh sách tài xế active**
   - Gọi `timTaiXeGanNhat(storeLat, storeLng, null)`
   - Bên trong:
     - **Đọc Firestore** danh sách tài xế active qua `walletRepository.findActiveDriverProfiles()`
     - đây là query trên collection `driver_profiles` với điều kiện `isActive == true`

3. **Tính khoảng cách từ cửa hàng đến từng tài xế**
   - Với mỗi tài xế:
     - lấy `driver.lat`, `driver.lng`
     - nếu thiếu tọa độ → bỏ qua
     - tính khoảng cách bằng **thuật toán Haversine**
   - Điều kiện lọc:
     - nếu `khoangCach <= 5 km` thì giữ lại
     - nếu `> 5 km` thì loại

4. **Kiểm tra có tài xế gần không**
   - Nếu danh sách sau lọc rỗng
     - gọi `thongBaoCuaHangKhongCoTaiXe(orderId)`
     - dừng
   - Nếu có
     - tiếp tục

5. **Lọc tài xế cùng hướng giao hàng**
   - Gọi `locTaiXeCungHuong(nearbyDriverIds, storeLat, storeLng, deliveryHeading)`
   - Nếu `driverIds` rỗng hoặc `deliveryHeading == null`
     - trả nguyên danh sách ban đầu
   - Ngược lại:
     - với từng `driverId`
       - **Đọc Firestore** hồ sơ tài xế qua `walletRepository.findDriverProfileById(driverId)`
       - lấy `driverLat`, `driverLng`
       - nếu thiếu tọa độ → bỏ qua tài xế này
       - tính `headingToStore`: góc từ vị trí tài xế tới cửa hàng
       - so sánh với `deliveryHeading`
       - nếu độ lệch góc `<= 45 độ` thì xem là cùng hướng
       - nếu không thì loại

6. **Nếu lọc cùng hướng không còn ai**
   - fallback về danh sách tài xế gần ban đầu

7. **Gửi yêu cầu nhận đơn cho nhóm tài xế ứng viên**
   - gọi `guiYeuCauTaiXe(orderId, candidateDriverIds, ...)`

---

## 5.3. Luồng tạo `order_request` và gửi yêu cầu tới tài xế

### Step-by-step

1. **Kiểm tra danh sách tài xế mục tiêu**
   - Nếu `targetDriverIds` rỗng
     - gọi `thongBaoCuaHangKhongCoTaiXe(orderId)`
     - dừng

2. **Tạo thời gian hết hạn**
   - `expiresAt = now + 10 giây`

3. **Tạo object `orderRequest`**
   - Gồm:
     - `orderId`
     - `targetDriverIds`
     - `attemptedDriverIds = []`
     - `acceptedDriverId = null`
     - `storeLat`
     - `storeLng`
     - `deliveryLat`
     - `deliveryLng`
     - `deliveryHeading`
     - `expiresAt`
     - `status = "pending"`
     - `createdAt = now`

4. **Ghi order_request tổng**
   - **Ghi Firestore** qua `orderRequestRepository.save(orderRequest)`
   - Nếu lưu thất bại
     - log lỗi
     - các bước gửi cho tài xế có thể bị bỏ qua

5. **Tạo request riêng cho từng tài xế**
   - Với mỗi `driverId`:
     - **Ghi Firestore** qua `orderRequestRepository.savePerDriver(driverId, orderRequest)`
   - Nếu lưu thành công:
     - gửi realtime và push
   - Nếu lưu thất bại:
     - bỏ qua tài xế đó

6. **Gửi realtime qua WebSocket**
   - Trong `guiRealtimeOrderRequestDenTaiXe(...)`
   - Trước hết:
     - tính `estimatedEarning` bằng `tinhThuNhapUocTinh(orderId)`
       - **Đọc Firestore** order qua `statsRepository.findOrderRawById(orderId)`
       - lấy `deliveryFee` hoặc `shippingFee`
   - Tạo DTO đơn giao hàng realtime
   - Gửi qua:
     - `messagingTemplate.convertAndSendToUser(driverId, "/queue/order-request", event)`

7. **Gửi push notification**
   - Trong `guiPushDenTaiXe(...)`
   - **Đọc Firestore** profile tài xế qua `walletRepository.findDriverProfileById(driverId)`
   - Lấy `fcmToken`
   - Nếu `fcmToken` tồn tại và không rỗng:
     - gửi push FCM
   - Đồng thời:
     - **Ghi Firestore** thêm notification vào:
       - `driver_profiles/{driverId}/notifications`

---

## 5.4. Luồng timeout và quét đợt tài xế tiếp theo

### Step-by-step

1. **Job định kỳ kiểm tra timeout**
   - `kiemTraTimeout()` chạy theo lịch
   - **Đọc Firestore** qua `orderRequestRepository.findExpiredPendingRequests(Instant.now())`
   - lấy toàn bộ `order_request` đang pending nhưng đã hết hạn

2. **Xử lý từng request hết hạn**
   - với mỗi document expired:
     - lấy `docId`, `orderId`
     - gọi `xuLyTimeout(docId, orderId)`

3. **Chống chạy trùng timeout**
   - Nếu `orderId` đã có trong `timeoutOrdersInFlight`
     - bỏ qua
   - Ngược lại
     - thêm vào tập đang xử lý

4. **Đọc lại order_request hiện tại**
   - **Đọc Firestore** qua `orderRequestRepository.findByOrderId(orderId)`
   - Nếu không còn tồn tại
     - dừng

5. **Kiểm tra doc hiện tại có còn đúng không**
   - Nếu `docId` truyền vào khác `orderRequest.id` hiện tại
     - đây là timeout cũ
     - bỏ qua

6. **Kiểm tra trạng thái request**
   - Nếu `status != "pending"`
     - dừng
   - Nếu `expiresAt` vẫn chưa qua thời điểm hiện tại
     - dừng

7. **Lấy danh sách tài xế đã thử**
   - Đọc:
     - `currentTargets`
     - `attemptedDriverIds`
   - Tạo tập `loaiTru = attemptedDriverIds + currentTargets`

8. **Đánh dấu đợt tài xế hiện tại đã hết hạn**
   - **Ghi Firestore**
     - `attemptedDriverIds = loaiTru`
     - `targetDriverIds = []`
   - cập nhật qua `orderRequestRepository.updateFieldsByDocId(docId, ...)`

9. **Tìm đợt tài xế kế tiếp**
   - Dùng lại:
     - `storeLat`, `storeLng`, `deliveryHeading`, `deliveryLat`, `deliveryLng`
   - Gọi `timTaiXeGanNhat(storeLat, storeLng, loaiTru)`
     - **Đọc Firestore** active driver profiles
     - loại các tài xế đã thử
     - tính khoảng cách Haversine
     - giữ tài xế trong bán kính 5 km
   - Gọi `locTaiXeCungHuong(...)`
     - **Đọc Firestore** từng driver profile để lấy lat/lng
     - so heading lệch không quá 45 độ
   - Nếu danh sách lọc cùng hướng rỗng
     - fallback về `nextDrivers`

10. **Nếu không còn tài xế nào để thử**
   - gọi `thongBaoCuaHangKhongCoTaiXe(orderId)`
   - **Ghi Firestore**
     - cập nhật `status = "failed"`
     - `targetDriverIds = []`
   - kết thúc

11. **Nếu còn tài xế mới**
   - tạo `nextExpiresAt = now + 10 giây`
   - **Ghi Firestore**
     - cập nhật:
       - `attemptedDriverIds`
       - `targetDriverIds = candidates`
       - `expiresAt = nextExpiresAt`

12. **Đọc lại order_request vừa cập nhật**
   - **Đọc Firestore** qua `orderRequestRepository.findByOrderId(orderId)`
   - Nếu không đọc được
     - dừng

13. **Tạo request riêng cho từng tài xế mới**
   - Với mỗi `driverId` mới:
     - **Ghi Firestore** qua `orderRequestRepository.savePerDriver(driverId, refreshedOrderRequest)`
     - nếu thành công:
       - gửi WebSocket realtime
       - gửi FCM push
       - **Ghi Firestore** notification vào `driver_profiles/{driverId}/notifications`
     - nếu thất bại:
       - bỏ qua tài xế đó

14. **Giải phóng cờ timeout đang xử lý**
   - xóa `orderId` khỏi `timeoutOrdersInFlight`

---

## 6. Tóm tắt ngắn gọn 2 thuật toán quan trọng để vẽ activity diagram

### A. Thuật toán checkout / tạo đơn
```text
Nhận request checkout
-> kiểm tra user hợp lệ
-> kiểm tra idempotencyKey
-> đọc address từ Firestore
-> đọc store từ Firestore
-> kiểm tra khoảng cách Haversine
-> đọc từng product để kiểm tra hết hàng
-> tính tổng tiền
-> đọc voucher / my_voucher để validate
-> tính giảm giá + phí ship + tổng thanh toán
-> chuẩn hóa items
-> tạo WriteBatch:
   - ghi order
   - giảm remaining voucher hệ thống hoặc xóa my_voucher
   - đọc cart và xóa các item đã mua
-> commit batch
-> đọc payment method để lấy tên hiển thị
-> gửi notification cho merchant
-> trả kết quả checkout
```

### B. Thuật toán gán tài xế
```text
Nhận orderId cần gán
-> đọc order từ Firestore
-> nếu status != 1 thì dừng
-> đọc store để lấy tọa độ
-> lấy deliveryLat, deliveryLng, deliveryHeading
-> nếu chưa có heading thì tự tính
-> kiểm tra đã có order_request pending chưa
-> đọc danh sách driver_profiles active
-> tính khoảng cách Haversine, lọc tài xế <= 5km
-> lọc tiếp theo hướng giao hàng (lệch <= 45 độ)
-> ghi order_request pending vào Firestore
-> ghi request riêng cho từng tài xế
-> gửi WebSocket + push notification

Nếu hết 10 giây không ai nhận:
-> đọc order_request pending hết hạn
-> gom danh sách tài xế đã thử
-> tiếp tục quét đợt tài xế mới chưa thử
-> nếu không còn ai: cập nhật failed
-> nếu còn: cập nhật targetDriverIds mới, expiresAt mới
-> gửi lại request cho nhóm tài xế mới
```

---

## 7. Gợi ý UML cho báo cáo

### Class Diagram nên có các nhóm sau
- `User`
- `Store`
- `Product`
- `Order`
- `OrderItem`
- `Review`
- `Address`
- `PaymentMethod`
- `Voucher`
- `MyVoucher`
- `Wallet` *(thực thể logic từ Firestore)*
- `Transaction` *(thực thể logic từ Firestore)*

### Activity Diagram nên có ít nhất 2 sơ đồ
1. **Checkout / Tạo đơn hàng**
   - validate user
   - validate địa chỉ / cửa hàng
   - check khoảng cách
   - check tồn kho
   - áp voucher
   - ghi order + cập nhật voucher + xóa cart
2. **Gán tài xế tự động**
   - đọc order
   - đọc store
   - quét driver active
   - tính khoảng cách Haversine
   - lọc cùng hướng
   - gửi request
   - timeout thì quét tiếp
   - fail nếu hết tài xế
