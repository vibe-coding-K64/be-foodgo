# Tài liệu Firebase Firestore - FoodGo Backend

Tài liệu này ghi lại tất cả các Firebase Firestore Collections được sử dụng trong dự án `be_foodgo`, bao gồm cấu trúc trường dữ liệu (fields), kiểu dữ liệu, dữ liệu mẫu (mock data), và mục đích sử dụng trong code.

---

## Mục lục

1. [Cấu trúc tổng quan](#1-cấu-trúc-tổng-quan)
2. [Bảng gốc (Root Collections)](#2-bảng-gốc-root-collections)
   - [2.1. users](#21-users)
   - [2.2. system_configs](#22-system_configs)
   - [2.3. wallets](#23-wallets)
   - [2.4. transactions](#24-transactions)
   - [2.5. system_categories](#25-system_categories)
   - [2.6. stores](#26-stores)
   - [2.7. products](#27-products)
   - [2.8. banners](#28-banners)
   - [2.9. vouchers](#29-vouchers)
   - [2.10. reviews](#210-reviews)
   - [2.11. orders](#211-orders)
   - [2.12. customer_profiles](#212-customer_profiles)
   - [2.13. driver_profiles](#213-driver_profiles)
   - [2.14. merchant_profiles](#214-merchant_profiles)
   - [2.15. admin_profiles](#215-admin_profiles)
   - [2.16. system_vouchers](#216-system_vouchers)
3. [Bảng nhánh hoặc Sub-collections](#3-bảng-nhánh-hoặc-sub-collections)
   - [3.1. customer_profiles/{userId}/addresses](#31-customer_profilesuseridaddresses)
   - [3.2. customer_profiles/{userId}/payment_methods](#32-customer_profilesuseridpayment_methods)
   - [3.3. customer_profiles/{userId}/notifications](#33-customer_profilesuseridnotifications)
   - [3.4. customer_profiles/{userId}/cart](#34-customer_profilesuseridcart)
   - [3.5. customer_profiles/{userId}/my_vouchers](#35-customer_profilesuseridmy_vouchers)
   - [3.6. driver_profiles/{userId}/notifications](#36-driver_profilesuseridnotifications)
   - [3.7. merchant_profiles/{userId}/notifications](#37-merchant_profilesuseridnotifications)
   - [3.8. users/{userId}/search_history](#38-usersuseridsearch_history)
4. [Danh sách các trường cơ bản](#4-danh-sách-các-trường-cơ-bản)

---

## 1. Cấu trúc tổng quan

Firestore sử dụng cấu trúc phân cấp như sau:

```
Firestore Root
├── system_configs                   (Root Collection)
├── wallets                         (Root Collection)
├── transactions                     (Root Collection)
├── users                          (Root Collection)
│   └── {userId}
│       └── search_history        (Sub-collection)
├── customer_profiles              (Root Collection)
│   └── {userId}
│       ├── addresses             (Sub-collection)
│       ├── payment_methods       (Sub-collection)
│       ├── notifications        (Sub-collection)
│       ├── cart                 (Sub-collection)
│       └── my_vouchers          (Sub-collection)
├── driver_profiles                (Root Collection)
│   └── {userId}
│       └── notifications        (Sub-collection)
├── merchant_profiles              (Root Collection)
│   └── {userId}
│       └── notifications        (Sub-collection)
├── admin_profiles                 (Root Collection)
├── system_categories              (Root Collection)
├── stores                        (Root Collection)
├── products                      (Root Collection)
├── banners                       (Root Collection)
├── vouchers                      (Root Collection)
├── system_vouchers               (Root Collection)
├── reviews                       (Root Collection)
└── orders                        (Root Collection)
```

---

## 2. Bảng gốc (Root Collections)

### 2.1. `users`

**Mục đích sử dụng:** Lưu trữ thông tin tài khoản người dùng cơ bản, dùng để xác thực đăng nhập.

**Đường dẫn:** `/users/{userId}`

**Các trường (Fields):**

| STT | Tên trường    | Kiểu dữ liệu      | Bắt buộc | Mô tả                                                                       |
| --- | ------------- | ----------------- | -------- | --------------------------------------------------------------------------- |
| 1   | `id`          | String            | Có       | ID document từ Firestore (tự động tạo)                                      |
| 2   | `email`       | String            | Có       | Địa chỉ email người dùng                                                    |
| 3   | `password`    | String            | Có       | Mật khẩu (cần mã hóa)                                                       |
| 4   | `fullName`    | String            | Có       | Họ và tên đầy đủ                                                            |
| 5   | `phoneNumber` | String            | Có       | Số điện thoại di động                                                       |
| 6   | `photoUrl`    | String (nullable) | Không    | Đường dẫn ảnh đại diện                                                      |
| 7   | `roles`       | ArrayNumber       | Không    | Danh sách quyền. 1=Khách hàng, 2=Tài xế, 3=Quán bán, 4=Admin. Mặc định: [1] |
| 8   | `createdAt`   | Timestamp         | Có       | Thời điểm tạo tài khoản                                                     |
| 9   | `updatedAt`   | Timestamp         | Không    | Thời điểm cập nhật gần nhất                                                 |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "user_001",
  "email": "khachhang@gmail.com",
  "password": "password123",
  "fullName": "Khôi",
  "phoneNumber": "0123456789",
  "photoUrl": "https://example.com/avatar/user001.jpg",
  "roles": [1, 2, 3],
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Người dùng test:** user_001 (roles: [1,2,3] - Khách hàng + Tài xế + Quán bán), user_002 (roles: [4] - Admin)

---

### 2.2. `system_configs`

**Mục đích sử dụng:** Lưu trữ các thông số cấu hình hệ thống như phí platform, phí giao hàng, tỷ lệ hoa hồng, chế độ bảo trì, và các giới hạn về ví.

**Đường dẫn:** `/system_configs/{configId}`

**Các trường (Fields):**

| STT | Tên trường                      | Kiểu dữ liệu  | Bắt buộc | Mô tả                                      |
| --- | ------------------------------- | ------------- | -------- | ------------------------------------------ |
| 1   | `id`                            | String        | Có       | ID document                                |
| 2   | `platformFeePercentage`         | Number        | Có       | Phần trăm phí nền tảng (VD: 15 = 15%)     |
| 3   | `baseDeliveryFee`               | Number        | Có       | Phí giao hàng cơ bản (VND)                |
| 4   | `minDeliveryFee`                | Number        | Có       | Phí giao hàng tối thiểu (VND)              |
| 5   | `maxDeliveryFee`                | Number        | Có       | Phí giao hàng tối đa (VND)                |
| 6   | `driverCommissionPercentage`     | Number        | Có       | % hoa hồng cho tài xế (VD: 80 = 80%)      |
| 7   | `merchantCommissionPercentage`   | Number        | Có       | % hoa hồng cho cửa hàng (VD: 85 = 85%)    |
| 8   | `minWithdrawalAmount`           | Number        | Có       | Số dư rút tối thiểu (VND)                 |
| 9   | `maxWithdrawalAmount`           | Number        | Có       | Số dư rút tối đa (VND)                    |
| 10  | `appVersion`                    | String        | Có       | Phiên bản ứng dụng hiện tại               |
| 11  | `maintenanceMode`               | Boolean       | Có       | Chế độ bảo trì (true = đang bảo trì)      |
| 12  | `createdAt`                     | Timestamp     | Có       | Thời điểm tạo                             |
| 13  | `updatedAt`                     | Timestamp     | Có       | Thời điểm cập nhật gần nhất              |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "config_001",
  "platformFeePercentage": 15.0,
  "baseDeliveryFee": 15000.0,
  "minDeliveryFee": 5000.0,
  "maxDeliveryFee": 50000.0,
  "driverCommissionPercentage": 80.0,
  "merchantCommissionPercentage": 85.0,
  "minWithdrawalAmount": 50000.0,
  "maxWithdrawalAmount": 50000000.0,
  "appVersion": "1.0.0",
  "maintenanceMode": false,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.3. `wallets`

**Mục đích sử dụng:** Lưu trữ ví tiền cho merchant và driver, gồm số dư hiện tại, tổng thu nhập, tổng đã rút, và số dư chờ.

**Đường dẫn:** `/wallets/{walletId}`

**Các trường (Fields):**

| STT | Tên trường          | Kiểu dữ liệu  | Bắt buộc | Mô tả                               |
| --- | ------------------- | ------------- | -------- | ----------------------------------- |
| 1   | `id`                | String        | Có       | ID document                         |
| 2   | `userId`            | String        | Có       | ID người dùng sở hữu ví            |
| 3   | `role`              | String        | Có       | Vai trò: "merchant" hoặc "driver"    |
| 4   | `balance`           | Number        | Có       | Số dư hiện tại (VND)               |
| 5   | `totalEarned`       | Number        | Có       | Tổng thu nhập từ trước đến nay (VND)|
| 6   | `totalWithdrawn`    | Number        | Có       | Tổng số đã rút (VND)               |
| 7   | `pendingBalance`    | Number        | Có       | Số dư chờ (chưa giải ngân, VND)     |
| 8   | `createdAt`         | Timestamp     | Có       | Thời điểm tạo ví                   |
| 9   | `updatedAt`         | Timestamp     | Có       | Thời điểm cập nhật gần nhất        |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "wallet_001",
  "userId": "user_001",
  "role": "merchant",
  "balance": 2500000.0,
  "totalEarned": 5000000.0,
  "totalWithdrawn": 2500000.0,
  "pendingBalance": 0.0,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.4. `transactions`

**Mục đích sử dụng:** Lưu trữ lịch sử giao dịch của ví, bao gồm các loại như thanh toán đơn hàng, thu nhập giao hàng, và rút tiền.

**Đường dẫn:** `/transactions/{transactionId}`

**Các trường (Fields):**

| STT | Tên trường      | Kiểu dữ liệu  | Bắt buộc | Mô tả                                     |
| --- | --------------- | ------------- | -------- | ----------------------------------------- |
| 1   | `id`            | String        | Có       | ID document                               |
| 2   | `walletId`      | String        | Có       | ID ví liên quan                           |
| 3   | `userId`        | String        | Có       | ID người thực hiện giao dịch             |
| 4   | `type`          | String        | Có       | Loại giao dịch: order_payment, delivery_income, withdrawal, refund |
| 5   | `amount`        | Number        | Có       | Tổng số tiền giao dịch (VND)             |
| 6   | `fee`           | Number        | Có       | Phí giao dịch (VND)                       |
| 7   | `netAmount`     | Number        | Có       | Số tiền thực nhận = amount - fee (VND)   |
| 8   | `description`   | String        | Không    | Mô tả giao dịch                           |
| 9   | `orderId`       | String (null) | Không    | ID đơn hàng liên quan (nếu có)           |
| 10  | `status`        | String        | Có       | Trạng thái: pending, completed, failed    |
| 11  | `createdAt`     | Timestamp     | Có       | Thời điểm tạo                            |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "trans_001",
  "walletId": "wallet_001",
  "userId": "user_001",
  "type": "order_payment",
  "amount": 76500.0,
  "fee": 11475.0,
  "netAmount": 65025.0,
  "description": "Đơn hàng order_001 - Phiên bản trừ phí hoa hồng",
  "orderId": "order_001",
  "status": "completed",
  "createdAt": "2026-04-07T00:00:00Z"
}
```

**Các loại giao dịch (type):**

| type              | Mô tả                          |
| ----------------- | ------------------------------ |
| `order_payment`   | Thanh toán đơn hàng (merchant) |
| `delivery_income` | Thu nhập giao hàng (driver)     |
| `withdrawal`      | Rút tiền                       |
| `refund`          | Hoàn tiền                      |

---

### 2.5. `system_categories`

**Mục đích sử dụng:** Lưu trữ danh sách danh mục món ăn/loại cửa hàng hiển thị trên trang chủ (Cơm, Phở/Bún, Trà sữa, Ăn vặt...).

**Đường dẫn:** `/system_categories/{categoryId}`

**Các trường (Fields):**

| STT | Tên trường  | Kiểu dữ liệu         | Bắt buộc | Mô tả                                     |
| --- | ----------- | -------------------- | -------- | ----------------------------------------- |
| 1   | `id`        | String               | Có       | ID document từ Firestore                  |
| 2   | `name`      | String               | Có       | Tên danh mục (VD: "Cơm", "Trà sữa")      |
| 3   | `icon`      | String               | Có       | Tên icon (VD: "restaurant", "local_cafe") |
| 4   | `order`     | Number               | Có       | Thứ tự sắp xếp hiển thị                  |
| 5   | `imageUrl`  | String               | Có       | Đường dẫn ảnh danh mục                   |
| 6   | `createdAt` | Timestamp            | Có       | Thời điểm tạo                            |
| 7   | `updatedAt` | Timestamp            | Có       | Thời điểm cập nhật                       |
| 8   | `deletedAt` | Timestamp (nullable)  | Không    | Thời điểm xóa (nếu có soft delete)       |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "cate_001",
  "name": "Cơm",
  "icon": "restaurant",
  "order": 1,
  "imageUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80",
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-01-01T00:00:00Z",
  "deletedAt": null
}
```

**Các mục hiện có:** Cơm (cate_001), Phở/Bún (cate_002), Trà sữa (cate_003), Ăn vặt (cate_004), Gà rán (cate_005), Món Hàn (cate_006), Món Nhật (cate_007), Bánh mì (cate_008), Lẩu/Buffet (cate_009), Trà trái cây (cate_010).

---

### 2.6. `stores`

**Mục đích sử dụng:** Lưu trữ thông tin chi tiết của các quán ăn/cửa hàng, bao gồm tên, địa chỉ, đánh giá, phí giao hàng, thời gian giao, và danh sách danh mục nội bộ của quán.

**Đường dẫn:** `/stores/{storeId}`

**Các trường (Fields):**

| STT | Tên trường              | Kiểu dữ liệu      | Bắt buộc | Mô tả                                                        |
| --- | ----------------------- | ----------------- | -------- | ------------------------------------------------------------ |
| 1   | `id`                    | String            | Có       | ID document từ Firestore                                     |
| 2   | `name`                  | String            | Có       | Tên quán ăn                                                 |
| 3   | `address`               | String            | Có       | Địa chỉ cụ thể (VD: "123 Lê Văn Việt, TP. Thủ Đức")        |
| 4   | `rating`                | Number            | Có       | Điểm đánh giá trung bình (0.0 - 5.0)                        |
| 5   | `reviewCount`           | Number            | Có       | Tổng số đánh giá                                            |
| 6   | `avtUrl`                | String            | Có       | Đường dẫn ảnh đại diện (avatar)                            |
| 7   | `backUrl`               | String            | Có       | Đường dẫn ảnh bìa (backdrop)                                |
| 8   | `isOpen`                | Boolean           | Có       | Quán có đang mở không                                        |
| 9   | `deliveryTime`          | String            | Có       | Thời gian giao ước tính (VD: "20-30 phút")                   |
| 10  | `deliveryFee`           | Number            | Có       | Phí giao hàng (VND)                                          |
| 11  | `categoryIds`           | ArrayString       | Không    | Danh sách ID danh mục hệ thống mà quán này thuộc            |
| 12  | `lat`                  | Number            | Không    | Vĩ độ (latitude) của tọa độ quán (VD: 10.8500)              |
| 13  | `lng`                  | Number            | Không    | Kinh độ (longitude) của tọa độ quán (VD: 106.7900)          |
| 14  | `restaurant_categories` | MapString, Object | Không    | Danh mục nội bộ của quán (VD: món chính, món phụ, nước uống) |
| 15  | `createdAt`             | Timestamp         | Có       | Thời điểm tạo                                               |
| 16  | `updatedAt`             | Timestamp         | Có       | Thời điểm cập nhật                                          |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "store_001",
  "name": "Cơm tám Phúc Lộc Thọ",
  "address": "123 Lê Văn Việt, TP. Thủ Đức",
  "rating": 4.8,
  "reviewCount": 500,
  "avtUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80",
  "backUrl": "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&q=80",
  "isOpen": true,
  "deliveryTime": "20-30 phút",
  "deliveryFee": 15000.0,
  "lat": 10.8500,
  "lng": 106.7900,
  "categoryIds": ["cate_001", "cate_004"],
  "restaurant_categories": {
    "rest_cate_001": {
      "name": "Món chính",
      "order": 1,
      "createdAt": "2026-04-07T00:00:00Z",
      "updatedAt": "2026-04-07T00:00:00Z"
    },
    "rest_cate_002": {
      "name": "Món phụ",
      "order": 2,
      "createdAt": "2026-04-07T00:00:00Z",
      "updatedAt": "2026-04-07T00:00:00Z"
    }
  },
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Các quán hiện có:** store_001 (Cơm tám Phúc Lộc Thọ), store_002 (Trà sữa Tocotoco), store_003 (Gà rán KFC Nguyễn Cửu), store_004 (Bún bò Huế Ba Lê).

---

### 2.7. `products`

**Mục đích sử dụng:** Lưu trữ thông tin sản phẩm/món ăn của từng quán, bao gồm giá, mô tả, tùy chọn (size, topping), trạng thái tồn kho, và thông tin quảng cáo.

**Đường dẫn:** `/products/{productId}`

**Các trường (Fields):**

| STT | Tên trường     | Kiểu dữ liệu | Bắt buộc | Mô tả                                        |
| --- | -------------- | ------------ | -------- | -------------------------------------------- |
| 1   | `id`           | String       | Có       | ID document từ Firestore                     |
| 2   | `storeId`      | String       | Có       | ID quán chứa sản phẩm này                   |
| 3   | `categoryId`   | String       | Có       | ID danh mục hệ thống                        |
| 4   | `categoryName` | String       | Có       | Tên danh mục hệ thống                       |
| 5   | `name`         | String       | Có       | Tên món ăn                                   |
| 6   | `description`  | String       | Có       | Mô tả chi tiết món ăn                        |
| 7   | `basePrice`    | Number       | Có       | Giá cơ sở (chưa tính size/topping)          |
| 8   | `imageUrl`     | String       | Có       | Đường dẫn ảnh món ăn                        |
| 9   | `isOutOfStock` | Boolean      | Có       | Có đang hết hàng không                        |
| 10  | `isFeatured`   | Boolean      | Có       | Có phải món nổi bật không                    |
| 11  | `optionGroups` | ArrayObject  | Không    | Danh sách nhóm tùy chọn (size, topping...)   |
| 12  | `createdAt`    | Timestamp    | Có       | Thời điểm tạo                               |
| 13  | `updatedAt`    | Timestamp    | Có       | Thời điểm cập nhật                          |

**Cấu trúc optionGroups (trường phức tạp):**

```json
"optionGroups": [
  {
    "name": "Kích thước",
    "options": [
      {"name": "M", "price": 0.0},
      {"name": "L", "price": 5000.0}
    ]
  },
  {
    "name": "Topping",
    "options": [
      {"name": "Trân châu", "price": 5000.0},
      {"name": "Thạch", "price": 3000.0}
    ]
  }
]
```

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "prod_001",
  "storeId": "store_001",
  "categoryId": "cate_001",
  "categoryName": "Cơm",
  "name": "Cơm tám sườn bì chả",
  "description": "Cơm tám ngon chuẩn vị Sài Gòn với sườn nướng thơm phức",
  "basePrice": 45000.0,
  "imageUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80",
  "isOutOfStock": false,
  "isFeatured": true,
  "optionGroups": [
    {
      "name": "Kích thước",
      "options": [
        {"name": "Vừa", "price": 0.0},
        {"name": "Lớn", "price": 10000.0}
      ]
    }
  ],
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Tổng số sản phẩm mẫu:** 15 sản phẩm, phân bổ cho 4 quán (store_001 đến store_004).

---

### 2.8. `banners`

**Mục đích sử dụng:** Lưu trữ thông tin banner quảng cáo hiển thị trên trang chủ (carousel).

**Đường dẫn:** `/banners/{bannerId}`

**Các trường (Fields):**

| STT | Tên trường  | Kiểu dữ liệu      | Bắt buộc | Mô tả                             |
| --- | ----------- | ----------------- | -------- | --------------------------------- |
| 1   | `id`        | String            | Có       | ID document từ Firestore          |
| 2   | `title`     | String            | Có       | Tiêu đề banner                   |
| 3   | `imageUrl`  | String            | Có       | Đường dẫn ảnh banner              |
| 4   | `storeId`   | String (nullable) | Không    | Nếu banner dành cho 1 quán cụ thể |
| 5   | `storeName` | String (nullable) | Không    | Tên quán (nếu có)                  |
| 6   | `isActive`  | Boolean           | Có       | Banner có đang hoạt động không     |
| 7   | `order`     | Number            | Có       | Thứ tự hiển thị                   |
| 8   | `createdAt` | Timestamp         | Có       | Thời điểm tạo                     |
| 9   | `updatedAt` | Timestamp         | Có       | Thời điểm cập nhật                |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "banner_001",
  "title": "Siêu sale giữa tháng",
  "imageUrl": "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&q=80",
  "storeId": null,
  "storeName": null,
  "isActive": true,
  "order": 1,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Các banner hiện có:** banner_001 (Siêu sale giữa tháng), banner_002 (Freeship 0 đồng), banner_003 (Lễ hội ẩm thực), banner_004 (Uống trà vẫn chiều).

---

### 2.9. `vouchers` (Voucher hệ thống / Public)

**Mục đích sử dụng:** Lưu trữ thông tin voucher có sẵn trong hệ thống, hiển thị tại trang Ưu đãi để khách hàng xem. (Lưu ý: đây là collection `vouchers`, phân biệt với `system_vouchers` bên dưới.)

**Đường dẫn:** `/vouchers/{voucherId}`

**Các trường (Fields):**

| STT | Tên trường       | Kiểu dữ liệu | Bắt buộc | Mô tả                               |
| --- | ---------------- | ------------ | -------- | ----------------------------------- |
| 1   | `id`             | String       | Có       | ID document từ Firestore            |
| 2   | `title`          | String       | Có       | Tiêu đề voucher                     |
| 3   | `subtitle`       | String       | Có       | Mô tả ngắn gọn                      |
| 4   | `pointsRequired` | Number       | Có       | Số điểm cần để đổi voucher này     |
| 5   | `imageUrl`       | String       | Có       | Đường dẫn ảnh voucher               |
| 6   | `remaining`      | Number       | Có       | Số lượng voucher còn lại            |
| 7   | `terms`          | String       | Có       | Điều khoản sử dụng                  |
| 8   | `minOrderValue`  | Number       | Có       | Đơn hàng tối thiểu để sử dụng (VND) |
| 9   | `createdAt`      | Timestamp    | Có       | Thời điểm tạo                       |
| 10  | `updatedAt`      | Timestamp    | Có       | Thời điểm cập nhật                  |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "sys_voucher_001",
  "title": "Giảm 20K cho đơn từ 100K",
  "subtitle": "Dành cho khách hàng mới",
  "pointsRequired": 200,
  "imageUrl": "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400&q=80",
  "remaining": 100,
  "terms": "Áp dụng cho tất cả quán ăn.",
  "minOrderValue": 100000.0,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.10. `reviews`

**Mục đích sử dụng:** Lưu trữ đánh giá của khách hàng về các quán ăn, bao gồm sao, bình luận, và hình ảnh kèm theo.

**Đường dẫn:** `/reviews/{reviewId}`

**Các trường (Fields):**

| STT | Tên trường      | Kiểu dữ liệu         | Bắt buộc | Mô tả                           |
| --- | --------------- | -------------------- | -------- | ------------------------------- |
| 1   | `id`            | String               | Có       | ID document từ Firestore        |
| 2   | `storeId`       | String               | Có       | ID quán được đánh giá           |
| 3   | `userId`        | String               | Có       | ID người đánh giá               |
| 4   | `userName`      | String               | Có       | Tên người đánh giá              |
| 5   | `userAvatarUrl` | String               | Có       | URL avatar người đánh giá        |
| 6   | `starRating`    | Number               | Có       | Điểm sao (1-5)                  |
| 7   | `comment`       | String               | Có       | Nội dung bình luận              |
| 8   | `imageUrls`     | ArrayString          | Không    | Danh sách URL hình ảnh kèm theo |
| 9   | `createdAt`     | Timestamp            | Có       | Thời điểm tạo đánh giá          |
| 10  | `updatedAt`     | Timestamp            | Có       | Thời điểm cập nhật              |
| 11  | `deletedAt`     | Timestamp (nullable) | Không    | Thời điểm xóa (nếu có)          |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "rev_001",
  "storeId": "store_001",
  "userId": "user_001",
  "userName": "Khôi",
  "userAvatarUrl": "https://example.com/avatar/user001.jpg",
  "starRating": 5,
  "comment": "Đồ ăn rất ngon, giao hàng nhanh, đóng gói kỹ lưỡng.",
  "imageUrls": [
    "https://example.com/review/rev001_1.jpg",
    "https://example.com/review/rev001_2.jpg"
  ],
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z",
  "deletedAt": null
}
```

**Tổng số đánh giá mẫu:** 8 đánh giá, phân bổ cho 4 quán.

---

### 2.11. `orders`

**Mục đích sử dụng:** Lưu trữ thông tin đơn hàng của khách hàng, bao gồm danh sách món, tổng tiền, trạng thái, thông tin giao hàng, và thông tin tài xế (nếu có).

**Đường dẫn:** `/orders/{orderId}`

**Các trường (Fields):**

| STT | Tên trường        | Kiểu dữ liệu         | Bắt buộc | Mô tả                                           |
| --- | ----------------- | -------------------- | -------- | ----------------------------------------------- |
| 1   | `id`              | String               | Có       | ID document từ Firestore                        |
| 2   | `userId`          | String               | Có       | ID người đặt hàng                               |
| 3   | `storeId`         | String               | Có       | ID quán chuẩn bị đơn                            |
| 4   | `storeName`       | String               | Có       | Tên quán                                        |
| 5   | `items`           | ArrayObject          | Có       | Danh sách món ăn trong đơn                      |
| 6   | `totalAmount`     | Number               | Có       | Tổng tiền đơn hàng (VND)                        |
| 7   | `deliveryFee`     | Number               | Có       | Phí giao hàng (VND)                             |
| 8   | `status`          | Number               | Có       | Trạng thái đơn hàng (0-4)                       |
| 9   | `deliveryAddress` | String               | Có       | Địa chỉ giao hàng                               |
| 10  | `paymentMethod`   | String               | Có       | Phương thức thanh toán (cash, momo, zalo, card) |
| 11  | `driverId`        | String (nullable)    | Không    | ID tài xế nhận đơn                              |
| 12  | `driverName`      | String (nullable)    | Không    | Tên tài xế                                      |
| 13  | `driverPhone`     | String (nullable)    | Không    | SĐT tài xế                                      |
| 14  | `vehiclePlate`    | String (nullable)    | Không    | Biển số xe                                      |
| 15  | `createdAt`       | Timestamp            | Có       | Thời điểm tạo đơn                               |
| 16  | `updatedAt`       | Timestamp            | Có       | Thời điểm cập nhật gần nhất                    |
| 17  | `deletedAt`       | Timestamp (nullable) | Không    | Thời điểm xóa                                   |

**Các giá trị status:**

| Giá trị | Tên           | Mô tả                      |
| ------- | ------------- | -------------------------- |
| 0       | Chờ xác nhận  | Đơn hàng chờ quán xác nhận |
| 1       | Đang chuẩn bị | Quán đang chuẩn bị món     |
| 2       | Đang giao     | Tài xế đang giao hàng       |
| 3       | Hoàn thành    | Đã giao thành công          |
| 4       | Đã hủy        | Đơn hàng đã bị hủy          |

**Cấu trúc items:**

```json
"items": [
  {
    "foodId": "prod_001",
    "name": "Cơm tám sườn bì chả",
    "price": 45000.0,
    "quantity": 2,
    "imageUrl": "https://example.com/comtam.jpg",
    "options": [
      {"name": "Trân châu", "price": 5000.0},
      {"name": "Thạch cà phê", "price": 8000.0}
    ]
  }
]
```

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "order_001",
  "userId": "user_001",
  "storeId": "store_001",
  "storeName": "Cơm tám Phúc Lộc Thọ",
  "items": [
    {
      "foodId": "prod_001",
      "name": "Cơm tám sườn bì chả",
      "price": 45000.0,
      "quantity": 2,
      "imageUrl": "https://example.com/comtam.jpg"
    }
  ],
  "totalAmount": 140000.0,
  "deliveryFee": 15000.0,
  "status": 2,
  "deliveryAddress": "Ký túc xá UTC2, Quận 9, TP.HCM",
  "paymentMethod": "momo",
  "driverId": "user_001",
  "driverName": "Lê Văn B",
  "driverPhone": "0912345678",
  "vehiclePlate": "59A-123.45",
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z",
  "deletedAt": null
}
```

**Tổng số đơn hàng mẫu:** 7 đơn hàng, các trạng thái khác nhau.

---

### 2.12. `customer_profiles`

**Mục đích sử dụng:** Bảng nhánh lưu trữ profile mở rộng của khách hàng, chứa điểm thành viên, hạng thành viên, và các sub-collections (địa chỉ, thanh toán, thông báo, giỏ hàng, voucher).

**Đường dẫn:** `/customer_profiles/{userId}`

**Các trường (Fields):**

| STT | Tên trường       | Kiểu dữ liệu | Bắt buộc | Mô tả                                               |
| --- | ---------------- | ------------ | -------- | --------------------------------------------------- |
| 1   | `id`             | String       | Có       | ID document (trùng với userId)                      |
| 2   | `loyaltyPoints`  | Number       | Có       | Điểm tích lũy hiện tại                              |
| 3   | `membershipTier` | Number       | Có       | Hạng thành viên: 0=Đồng, 1=Bạc, 2=Vàng, 3=Kim Cương |
| 4   | `createdAt`      | Timestamp    | Có       | Thời điểm tạo                                       |
| 5   | `updatedAt`      | Timestamp    | Có       | Thời điểm cập nhật                                 |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "user_001",
  "loyaltyPoints": 1500,
  "membershipTier": 1,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.13. `driver_profiles`

**Mục đích sử dụng:** Bảng nhánh lưu trữ profile tài xế giao hàng, chứa thông tin phương tiện, trạng thái hoạt động, và sub-collection notifications.

**Đường dẫn:** `/driver_profiles/{userId}`

**Các trường (Fields):**

| STT | Tên trường      | Kiểu dữ liệu | Bắt buộc | Mô tả                          |
| --- | --------------- | ------------ | -------- | ------------------------------ |
| 1   | `id`            | String       | Có       | ID document (trùng với userId) |
| 2   | `vehiclePlate`  | String       | Có       | Biển số xe                     |
| 3   | `vehicleType`   | String       | Có       | Loại phương tiện               |
| 4   | `driverLicense` | String       | Có       | Bằng lái xe                    |
| 5   | `isActive`      | Boolean      | Có       | Trạng thái hoạt động           |
| 6   | `rating`        | Number       | Có       | Điểm đánh giá trung bình       |
| 7   | `totalTrips`    | Number       | Có       | Tổng số chuyến giao thành công |
| 8   | `createdAt`     | Timestamp    | Có       | Thời điểm tạo                  |
| 9   | `updatedAt`     | Timestamp    | Có       | Thời điểm cập nhật             |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "user_001",
  "vehiclePlate": "59A-123.45",
  "vehicleType": "Honda Wave Alpha",
  "driverLicense": "DL123456789",
  "isActive": true,
  "rating": 4.9,
  "totalTrips": 150,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.14. `merchant_profiles`

**Mục đích sử dụng:** Bảng nhánh lưu trữ profile quán bán/người bán, chứa thông tin kinh doanh và sub-collection notifications.

**Đường dẫn:** `/merchant_profiles/{userId}`

**Các trường (Fields):**

| STT | Tên trường        | Kiểu dữ liệu | Bắt buộc | Mô tả                                        |
| --- | ----------------- | ------------ | -------- | -------------------------------------------- |
| 1   | `id`              | String       | Có       | ID document (trùng với userId)               |
| 2   | `businessName`    | String       | Có       | Tên doanh nghiệp/quán                        |
| 3   | `businessLicense` | String       | Có       | Giấy phép kinh doanh                        |
| 4   | `taxCode`         | String       | Có       | Mã số thuế                                   |
| 5   | `storeIds`        | ArrayString  | Có       | Danh sách ID các cửa hàng thuộc merchant này |
| 6   | `createdAt`       | Timestamp    | Có       | Thời điểm tạo                                |
| 7   | `updatedAt`       | Timestamp    | Có       | Thời điểm cập nhật                           |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "user_001",
  "businessName": "Cơm tám Phúc Lộc Thọ",
  "businessLicense": "BL123456789",
  "taxCode": "TAX123456789",
  "storeIds": ["store_001"],
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.15. `admin_profiles`

**Mục đích sử dụng:** Bảng nhánh lưu trữ profile quản trị viên, chứa thông tin cấp bậc, phòng ban, và quyền hạn.

**Đường dẫn:** `/admin_profiles/{userId}`

**Các trường (Fields):**

| STT | Tên trường    | Kiểu dữ liệu | Bắt buộc | Mô tả                                        |
| --- | ------------- | ------------ | -------- | -------------------------------------------- |
| 1   | `id`          | String       | Có       | ID document (trùng với userId)               |
| 2   | `adminLevel`  | Number       | Có       | Cấp bậc admin: 1=Admin thường, 2=Super admin |
| 3   | `department`  | String       | Có       | Bộ phận làm việc (VD: "Bộ phận vận hành")    |
| 4   | `permissions` | ArrayString  | Có       | Danh sách quyền hạn                           |
| 5   | `createdAt`   | Timestamp    | Có       | Thời điểm tạo                                |
| 6   | `updatedAt`   | Timestamp    | Có       | Thời điểm cập nhật                           |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "user_002",
  "adminLevel": 1,
  "department": "Bộ phận vận hành",
  "permissions": ["manage_users", "manage_orders", "manage_stores", "view_reports"],
  "createdAt": "2026-03-01T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.16. `system_vouchers`

**Mục đích sử dụng:** Lưu trữ voucher hệ thống mà khách hàng có thể đổi điểm (phân biệt với `vouchers`). Chứa thông tin điểm cần thiết, số lượng còn lại, và điều khoản.

**Đường dẫn:** `/system_vouchers/{systemVoucherId}`

**Các trường (Fields):**

| STT | Tên trường       | Kiểu dữ liệu | Bắt buộc | Mô tả                    |
| --- | ---------------- | ------------ | -------- | ------------------------ |
| 1   | `id`             | String       | Có       | ID document từ Firestore |
| 2   | `title`          | String       | Có       | Tiêu đề voucher          |
| 3   | `subtitle`       | String       | Có       | Mô tả ngắn gọn           |
| 4   | `pointsRequired` | Number       | Có       | Số điểm cần để đổi       |
| 5   | `imageUrl`       | String       | Có       | Đường dẫn ảnh voucher    |
| 6   | `remaining`      | Number       | Có       | Số lượng voucher còn lại |
| 7   | `terms`          | String       | Có       | Điều khoản sử dụng       |
| 8   | `minOrderValue`  | Number       | Có       | Đơn hàng tối thiểu (VND) |
| 9   | `createdAt`      | Timestamp    | Có       | Thời điểm tạo            |
| 10  | `updatedAt`      | Timestamp    | Có       | Thời điểm cập nhật       |

**Ghi chú:** Hiện tại trong code, collection này chưa được seed. Chỉ `vouchers` (root) được seed. Nếu người dùng muốn sử dụng `system_vouchers`, cần bổ sung seed trong `DataSeeder`.

---

## 3. Bảng nhánh hoặc Sub-collections

### 3.1. `customer_profiles/{userId}/addresses`

**Mục đích sử dụng:** Lưu trữ danh sách địa chỉ giao hàng của khách hàng.

**Đường dẫn:** `/customer_profiles/{userId}/addresses/{addressId}`

**Các trường (Fields):**

| STT | Tên trường      | Kiểu dữ liệu         | Bắt buộc | Mô tả                                     |
| --- | --------------- | -------------------- | -------- | ----------------------------------------- |
| 1   | `id`            | String               | Có       | ID document từ Firestore                  |
| 2   | `name`          | String               | Có       | Nhãn địa chỉ (VD: "Nhà riêng", "Công ty") |
| 3   | `address`       | String               | Có       | Địa chỉ chi tiết đầy đủ                   |
| 4   | `receiverName`  | String               | Có       | Họ tên người nhận                         |
| 5   | `receiverPhone` | String               | Có       | SĐT người nhận                            |
| 6   | `lat`           | Number               | Có       | Vĩ độ (latitude)                          |
| 7   | `lng`           | Number               | Có       | Kinh độ (longitude)                       |
| 8   | `isDefault`     | Boolean              | Có       | Có phải địa chỉ mặc định không            |
| 9   | `createdAt`     | Timestamp            | Có       | Thời điểm tạo                             |
| 10  | `updatedAt`     | Timestamp            | Có       | Thời điểm cập nhật                        |
| 11  | `deletedAt`     | Timestamp (nullable) | Không    | Thời điểm xóa (nếu có)                   |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "addr_001",
  "name": "Nhà riêng",
  "address": "Ký túc xá UTC2, Quận 9, TP.HCM",
  "receiverName": "Khôi",
  "receiverPhone": "0123456789",
  "lat": 10.8455,
  "lng": 106.7939,
  "isDefault": true,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z",
  "deletedAt": null
}
```

**Ghi chú:** Có 2 địa chỉ mẫu cho user_001: addr_001 (mặc định) và addr_002 (Trường học).

---

### 3.2. `customer_profiles/{userId}/payment_methods`

**Mục đích sử dụng:** Lưu trữ các phương thức thanh toán đã liên kết của khách hàng.

**Đường dẫn:** `/customer_profiles/{userId}/payment_methods/{paymentMethodId}`

**Các trường (Fields):**

| STT | Tên trường    | Kiểu dữ liệu      | Bắt buộc | Mô tả                                                   |
| --- | ------------- | ----------------- | -------- | ------------------------------------------------------- |
| 1   | `id`          | String            | Có       | ID document từ Firestore                                |
| 2   | `type`        | Number            | Có       | Loại: 1=Tiền mặt, 2=Ví điện tử, 3=Thẻ ngân hàng       |
| 3   | `isDefault`   | Boolean           | Có       | Có phải phương thức mặc định không                      |
| 4   | `cardBrand`   | String (nullable) | Không    | Thương hiệu thẻ (nếu type=3): "visa", "mastercard"     |
| 5   | `last4Digits` | String (nullable) | Không    | 4 chữ số cuối thẻ (nếu type=3)                          |
| 6   | `walletBrand` | String (nullable) | Không    | Thương hiệu ví (nếu type=2): "momo", "zalopay", "vnpay" |
| 7   | `isLinked`    | Boolean           | Có       | Đã liên kết chưa (nếu type=2)                           |
| 8   | `createdAt`   | Timestamp         | Có       | Thời điểm tạo                                           |
| 9   | `updatedAt`   | Timestamp         | Có       | Thời điểm cập nhật                                      |

**Dữ liệu mẫu (Mock Data):**

```json
// Ví điện tử
{
  "id": "pm_001",
  "type": 2,
  "isDefault": true,
  "cardBrand": null,
  "last4Digits": null,
  "walletBrand": "momo",
  "isLinked": true,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}

// Thẻ ngân hàng
{
  "id": "pm_002",
  "type": 3,
  "isDefault": false,
  "cardBrand": "Visa",
  "last4Digits": "1234",
  "walletBrand": null,
  "isLinked": true,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Ghi chú:** Có 2 phương thức mẫu: pm_001 (MoMo ví điện tử, mặc định) và pm_002 (Thẻ Visa ****1234).

---

### 3.3. `customer_profiles/{userId}/notifications`

**Mục đích sử dụng:** Lưu trữ thông báo của khách hàng, bao gồm thông báo hệ thống, khuyến mãi, và cập nhật đơn hàng.

**Đường dẫn:** `/customer_profiles/{userId}/notifications/{notificationId}`

**Các trường (Fields):**

| STT | Tên trường    | Kiểu dữ liệu | Bắt buộc | Mô tả                                                |
| --- | ------------- | ------------ | -------- | ---------------------------------------------------- |
| 1   | `id`          | String       | Có       | ID document từ Firestore                             |
| 2   | `type`        | Number       | Có       | Loại thông báo: 0=Hệ thống, 1=Khuyến mãi, 2=Đơn hàng |
| 3   | `title`       | String       | Có       | Tiêu đề thông báo                                    |
| 4   | `body`        | String       | Có       | Nội dung thông báo                                   |
| 5   | `referenceId` | String       | Có       | ID tham chiếu (VD: orderId, voucherId)                |
| 6   | `isRead`      | Boolean      | Có       | Đã đọc chưa                                          |
| 7   | `createdAt`   | Timestamp    | Có       | Thời điểm tạo                                        |

**Dữ liệu mẫu (Mock Data):**

```json
// Thông báo đơn hàng
{
  "id": "notif_001",
  "type": 2,
  "title": "Đơn hàng đã được giao thành công",
  "body": "Đơn hàng order_001 đã được giao",
  "referenceId": "order_001",
  "isRead": false,
  "createdAt": "2026-04-07T00:00:00Z"
}

// Thông báo khuyến mãi
{
  "id": "notif_002",
  "type": 1,
  "title": "Khuyến mãi đặc biệt",
  "body": "Giảm 20% cho đơn hàng đầu tiên",
  "referenceId": "voucher_001",
  "isRead": true,
  "createdAt": "2026-04-06T00:00:00Z"
}
```

---

### 3.4. `customer_profiles/{userId}/cart`

**Mục đích sử dụng:** Lưu trữ giỏ hàng tạm thời của khách hàng, đồng bộ real-time với UI qua Firestore stream.

**Đường dẫn:** `/customer_profiles/{userId}/cart/{cartItemId}`

**Các trường (Fields):**

| STT | Tên trường  | Kiểu dữ liệu      | Bắt buộc | Mô tả                             |
| --- | ----------- | ----------------- | -------- | --------------------------------- |
| 1   | `id`        | String            | Có       | ID document từ Firestore          |
| 2   | `storeId`   | String            | Có       | ID quán chứa sản phẩm             |
| 3   | `foodId`    | String            | Có       | ID sản phẩm (món ăn)              |
| 4   | `name`      | String            | Có       | Tên món ăn                        |
| 5   | `price`     | Number            | Có       | Đơn giá (đã bao gồm size/topping) |
| 6   | `quantity`  | Number            | Có       | Số lượng                          |
| 7   | `size`      | String (nullable) | Không    | Kích thước đã chọn (VD: "M", "L") |
| 8   | `sizePrice` | Number (nullable) | Không    | Giá thêm của size                 |
| 9   | `toppings`  | ArrayObject       | Không    | Danh sách topping đã chọn          |
| 10  | `note`      | String (nullable) | Không    | Ghi chú cho quán                  |
| 11  | `imageUrl`  | String (nullable) | Không    | URL ảnh món ăn                    |
| 12  | `createdAt` | Timestamp         | Có       | Thời điểm tạo                     |
| 13  | `updatedAt` | Timestamp         | Có       | Thời điểm cập nhật                |

**Cấu trúc toppings:**

```json
"toppings": [
  {"name": "Trân châu trắng", "price": 10000.0},
  {"name": "Thạch trái cây", "price": 8000.0}
]
```

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "cart_item_001",
  "storeId": "store_001",
  "foodId": "prod_001",
  "name": "Cơm tám sườn bì chả",
  "price": 45000.0,
  "quantity": 2,
  "imageUrl": "https://example.com/comtam.jpg",
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Ghi chú:** Có 2 item mẫu: cart_item_001 (Cơm tám) và cart_item_002 (Trà sữa trà chanh táo). Giá trị `price` trong cart đã bao gồm tổng giá = (basePrice + sizePrice + toppingPrice) * quantity. Toppings được lưu riêng trong mảng `toppings` để hiển thị chi tiết.

---

### 3.5. `customer_profiles/{userId}/my_vouchers`

**Mục đích sử dụng:** Lưu trữ voucher mà khách hàng đã đổi hoặc đã nhận.

**Đường dẫn:** `/customer_profiles/{userId}/my_vouchers/{myVoucherId}`

**Các trường (Fields):**

| STT | Tên trường      | Kiểu dữ liệu | Bắt buộc | Mô tả                              |
| --- | --------------- | ------------ | -------- | ---------------------------------- |
| 1   | `id`            | String       | Có       | ID document từ Firestore           |
| 2   | `name`          | String       | Có       | Tên voucher                        |
| 3   | `code`          | String       | Có       | Mã voucher                         |
| 4   | `description`   | String       | Có       | Mô tả chi tiết                     |
| 5   | `expiryDate`    | Timestamp    | Có       | Ngày hết hạn                       |
| 6   | `discountValue` | Number       | Có       | Giá trị giảm (nếu không phần trăm) |
| 7   | `isPercentage`  | Boolean      | Có       | Là phần trăm giảm không            |
| 8   | `minOrderValue` | Number       | Có       | Đơn hàng tối thiểu (VND)           |
| 9   | `createdAt`     | Timestamp    | Có       | Thời điểm tạo                      |
| 10  | `updatedAt`     | Timestamp    | Có       | Thời điểm cập nhật                 |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "mv_001",
  "name": "Giảm 20K phí giao hàng",
  "code": "FREESHIP20",
  "description": "Áp dụng cho đơn từ 100K",
  "expiryDate": "2026-04-30T23:59:59Z",
  "discountValue": 20000.0,
  "isPercentage": false,
  "minOrderValue": 100000.0,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Ghi chú:** Có 2 voucher mẫu: mv_001 (FREESHIP20 - giảm 20K phí giao hàng) và mv_002 (SAVE10 - giảm 10%).

---

### 3.6. `driver_profiles/{userId}/notifications`

**Mục đích sử dụng:** Lưu trữ thông báo dành riêng cho tài xế giao hàng.

**Đường dẫn:** `/driver_profiles/{userId}/notifications/{notificationId}`

**Các trường (Fields):** Tương tự như `customer_profiles/{userId}/notifications`, nhưng `type` có thêm giá trị 11 (Yêu cầu nhận đơn) và 12 (Thông báo giao hàng).

| Giá trị type | Mô tả                |
| ------------ | -------------------- |
| 11           | Yêu cầu nhận đơn mới |
| 12           | Thông báo giao hàng  |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "dnotif_001",
  "type": 11,
  "title": "Yêu cầu nhận đơn mới",
  "body": "Bạn có đơn hàng mới cần nhận: order_002",
  "referenceId": "order_002",
  "isRead": false,
  "createdAt": "2026-04-07T00:00:00Z"
}
```

---

### 3.7. `merchant_profiles/{userId}/notifications`

**Mục đích sử dụng:** Lưu trữ thông báo dành riêng cho quán bán/người kinh doanh.

**Đường dẫn:** `/merchant_profiles/{userId}/notifications/{notificationId}`

**Các trường (Fields):** Tương tự như notifications khách hàng, nhưng `type` có thêm giá trị 21 (Đơn hàng mới).

| Giá trị type | Mô tả                      |
| ------------ | -------------------------- |
| 21           | Đơn hàng mới từ khách hàng |

**Dữ liệu mẫu (Mock Data):**

```json
{
  "id": "mnotif_001",
  "type": 21,
  "title": "Đơn hàng mới từ khách hàng",
  "body": "Bạn có đơn hàng mới: order_003",
  "referenceId": "order_003",
  "isRead": false,
  "createdAt": "2026-04-07T00:00:00Z"
}
```

---

### 3.8. `users/{userId}/search_history`

**Mục đích sử dụng:** Lưu trữ lịch sử tìm kiếm của khách hàng, giúp gợi ý từ khóa đã tìm.

**Đường dẫn:** `/users/{userId}/search_history/{historyId}`

**Các trường (Fields):**

| STT | Tên trường  | Kiểu dữ liệu | Bắt buộc | Mô tả                                  |
| --- | ----------- | ------------ | -------- | -------------------------------------- |
| 1   | `id`        | String       | Có       | ID document từ Firestore               |
| 2   | `keyword`   | String       | Có       | Từ khóa tìm kiếm                      |
| 3   | `createdAt` | Timestamp    | Có       | Thời điểm tìm kiếm (hoặc cập nhật lại) |

**Ghi chú:** Trong code, đường dẫn sử dụng là `users/{userId}/search_history`, nhưng trong `clearAllSeededData` của `DataSeeder`, nó nằm trong danh sách `userSubCollections` của bảng nhánh `users` (không phải root collection riêng). Đây là một điểm cần lưu ý - `search_history` nằm trong `users` chứ không phải trong `customer_profiles`.

---

## 4. Danh sách các trường cơ bản

Dưới đây là bảng tổng hợp các kiểu dữ liệu được sử dụng xuyên suốt các collections:

| Kiểu Firestore | Tương ứng Java/Dart              | Mô tả                  |
| -------------- | ------------------------------- | ---------------------- |
| `String`       | `String`                        | Chuỗi văn bản          |
| `Number`       | `int` hoặc `double`             | Số nguyên hoặc số thực |
| `Boolean`      | `boolean` / `bool`              | Đúng/Sai               |
| `Timestamp`    | `Date` / `DateTime`             | Thời điểm (ngày giờ)   |
| `Array<T>`     | `List<T>` / `ArrayList<T>`      | Mảng                   |
| `Map`          | `Map<String, dynamic>` / `HashMap` | Đối tượng / Dictionary |
| `null`         | `nullable` (String?, int?, ...) | Giá trị có thể rỗng    |

### Quy ước đặt tên trường

- Tên trường Firestore sử dụng `camelCase` (VD: `createdAt`, `isDefault`, `loyaltyPoints`)
- Tên icon sử dụng `snake_case` (VD: `restaurant`, `local_cafe`)
- Mã voucher sử dụng `UPPERCASE` (VD: `FREESHIP20`, `SAVE10`)

---

## Lịch sử cập nhật

| Ngày       | Mô tả                                                |
| ---------- | ---------------------------------------------------- |
| 2026-05-22 | Phiên bản đầu tiên - tài liệu đầy đủ các collections |
