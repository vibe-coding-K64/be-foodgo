# Tai lieu Firebase Firestore - FoodGo Customers

Tai lieu nay ghi lai tat ca cac Firebase Firestore Collections duoc su dung trong du an `fe_foodgo_customers`, bao gom cau truc truong du lieu (fields), kieu du lieu, du lieu mau (mock data), va muc dich su dung trong code.

---

## Muc luc

1. [Cau truc tong quan](#1-cau-truc-tong-quan)
2. [Bang goc (Root Collections)](#2-bang-goc-root-collections)
   - [2.1. users](#21-users)
   - [2.2. system_configs](#22-system_configs)
   - [2.3. wallets](#23-wallets)
   - [2.4. transactions](#24-transactions)
3. [Bang nhanh hoac Sub-collections](#3-bang-nhanh-hoac-sub-collections)
4. [Danh sach cac truong co ban](#4-danh-sach-cac-truong-co-ban)

---

## 1. Cau truc tong quan

Firestore su dung cau truc phan cap nhu sau:

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

## 2. Bang goc (Root Collections)

### 2.1. `users`

**Muc dich su dung:** Luu tru thong tin tai khoan nguoi dung co ban, dung de xac thuc dang nhap.

**Duong dan:** `/users/{userId}`

**Cac truong (Fields):**


| STT | Ten truong    | Kieu du lieu      | Bat buoc | Mo ta                                                                       |
| --- | ------------- | ----------------- | -------- | --------------------------------------------------------------------------- |
| 1   | `id`          | String            | Co       | ID document tu Firestore (tu dong tao)                                      |
| 2   | `email`       | String            | Co       | Dia chi email nguoi dung                                                    |
| 3   | `password`    | String            | Co       | Mat khau (can ma hoa)                                                       |
| 4   | `fullName`    | String            | Co       | Ho va ten day du                                                            |
| 5   | `phoneNumber` | String            | Co       | So dien thoai di dong                                                       |
| 6   | `photoUrl`    | String (nullable) | Khong    | Duong dan anh dai dien                                                      |
| 7   | `roles`       | ArrayNumber       | Khong    | Danh sach quyen. 1=Khach hang, 2=Tai xe, 3=Quan ban, 4=Admin. Mac dinh: [1] |
| 8   | `createdAt`   | Timestamp         | Co       | Thoi diem tao tai khoan                                                     |
| 9   | `updatedAt`   | Timestamp         | Khong    | Thoi diem cap nhat gan nhat                                                 |


**Du lieu mau (Mock Data):**

```json
{
  "id": "user_001",
  "email": "khachhang@gmail.com",
  "password": "password123",
  "fullName": "Khoi",
  "phoneNumber": "0123456789",
  "photoUrl": "https://example.com/avatar/user001.jpg",
  "roles": [1, 2, 3],
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Nguoi dung test:** user_001 (roles: [1,2,3] - Khach hang + Tai xe + Quan ban), user_002 (roles: [4] - Admin)

---

### 2.2. `system_configs`

**Muc dich su dung:** Luu tru cac thong so cau hinh he thong nhu phi platform, phi giao hang, ti le hoa hong, che do bao tri, va cac gioi han ve vi.

**Duong dan:** `/system_configs/{configId}`

**Cac truong (Fields):**


| STT | Ten truong                      | Kieu du lieu  | Bat buoc | Mo ta                                      |
| --- | ------------------------------- | ------------- | -------- | ------------------------------------------ |
| 1   | `id`                            | String        | Co       | ID document                                |
| 2   | `platformFeePercentage`         | Number        | Co       | Phan tram phi nền tảng (VD: 15 = 15%)     |
| 3   | `baseDeliveryFee`               | Number        | Co       | Phi giao hang co ban (VND)                 |
| 4   | `minDeliveryFee`                | Number        | Co       | Phi giao hang toi thieu (VND)              |
| 5   | `maxDeliveryFee`                | Number        | Co       | Phi giao hang toi da (VND)                 |
| 6   | `driverCommissionPercentage`     | Number        | Co       | % hoa hong cho tai xe (VD: 80 = 80%)      |
| 7   | `merchantCommissionPercentage`   | Number        | Co       | % hoa hong cho cua hang (VD: 85 = 85%)    |
| 8   | `minWithdrawalAmount`           | Number        | Co       | So du rut toi thieu (VND)                  |
| 9   | `maxWithdrawalAmount`           | Number        | Co       | So du rut toi da (VND)                     |
| 10  | `appVersion`                    | String        | Co       | Phien ban ung dung hien tai                |
| 11  | `maintenanceMode`               | Boolean       | Co       | Che do bao tri (true = dang bao tri)       |
| 12  | `createdAt`                     | Timestamp     | Co       | Thoi diem tao                              |
| 13  | `updatedAt`                     | Timestamp     | Co       | Thoi diem cap nhat gan nhat                |


**Du lieu mau (Mock Data):**

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

**Muc dich su dung:** Luu tru vi tien cho merchant va driver, gom so du hien tai, tong thu nhap, tong da rut, va so du cho.

**Duong dan:** `/wallets/{walletId}`

**Cac truong (Fields):**


| STT | Ten truong          | Kieu du lieu  | Bat buoc | Mo ta                               |
| --- | ------------------- | ------------- | -------- | ----------------------------------- |
| 1   | `id`                | String        | Co       | ID document                         |
| 2   | `userId`            | String        | Co       | ID nguoi dung so huu vi             |
| 3   | `role`              | String        | Co       | Vai tro: "merchant" hoac "driver"    |
| 4   | `balance`           | Number        | Co       | So du hien tai (VND)                |
| 5   | `totalEarned`       | Number        | Co       | Tong thu nhap tu truoc toi nay (VND)|
| 6   | `totalWithdrawn`    | Number        | Co       | Tong so da rut (VND)                |
| 7   | `pendingBalance`    | Number        | Co       | So du cho (chua giai ngan, VND)     |
| 8   | `createdAt`         | Timestamp     | Co       | Thoi diem tao vi                    |
| 9   | `updatedAt`         | Timestamp     | Co       | Thoi diem cap nhat gan nhat         |


**Du lieu mau (Mock Data):**

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

**Muc dich su dung:** Luu tru lich su giao dich cua vi, bao gom cac loai nhu thanh toan don hang, thu nhap giao hang, va rut tien.

**Duong dan:** `/transactions/{transactionId}`

**Cac truong (Fields):**


| STT | Ten truong      | Kieu du lieu  | Bat buoc | Mo ta                                     |
| --- | --------------- | ------------- | -------- | ----------------------------------------- |
| 1   | `id`            | String        | Co       | ID document                               |
| 2   | `walletId`      | String        | Co       | ID vi lien quan                           |
| 3   | `userId`        | String        | Co       | ID nguoi thuc hien giao dich              |
| 4   | `type`          | String        | Co       | Loai giao dich: order_payment, delivery_income, withdrawal, refund |
| 5   | `amount`        | Number        | Co       | Tong so tien giao dich (VND)              |
| 6   | `fee`           | Number        | Co       | Phi giao dich (VND)                       |
| 7   | `netAmount`     | Number        | Co       | So tien thuc nhan = amount - fee (VND)    |
| 8   | `description`   | String        | Khong    | Mo ta giao dich                           |
| 9   | `orderId`       | String (null) | Khong    | ID don hang lien quan (neu co)             |
| 10  | `status`        | String        | Co       | Trang thai: pending, completed, failed    |
| 11  | `createdAt`     | Timestamp     | Co       | Thoi diem tao                             |


**Du lieu mau (Mock Data):**

```json
{
  "id": "trans_001",
  "walletId": "wallet_001",
  "userId": "user_001",
  "type": "order_payment",
  "amount": 76500.0,
  "fee": 11475.0,
  "netAmount": 65025.0,
  "description": "Don hang order_001 - Phien ban tru phi hoa hong",
  "orderId": "order_001",
  "status": "completed",
  "createdAt": "2026-04-07T00:00:00Z"
}
```

**Cac loai giao dich (type):**

| type              | Mo ta                          |
| ----------------- | ------------------------------ |
| `order_payment`   | Thanh toan don hang (merchant) |
| `delivery_income` | Thu nhap giao hang (driver)     |
| `withdrawal`      | Rut tien                       |
| `refund`          | Hoan tien                      |

---

### 2.5. `system_categories`

**Muc dich su dung:** Luu tru danh sach danh muc mon an/loai cua hang hien thi tren trang chu (Com, Pho/Bun, Tra sua, An vat, ...).

**Duong dan:** `/system_categories/{categoryId}`

**Cac truong (Fields):**


| STT | Ten truong  | Kieu du lieu         | Bat buoc | Mo ta                                     |
| --- | ----------- | -------------------- | -------- | ----------------------------------------- |
| 1   | `id`        | String               | Co       | ID document tu Firestore                  |
| 2   | `name`      | String               | Co       | Ten danh muc (VD: "Com", "Tra sua")       |
| 3   | `icon`      | String               | Co       | Ten icon (VD: "restaurant", "local_cafe") |
| 4   | `order`     | Number               | Co       | Thu tu sap xep hien thi                   |
| 5   | `imageUrl`  | String               | Co       | Duong dan anh danh muc                    |
| 6   | `createdAt` | Timestamp            | Co       | Thoi diem tao                             |
| 7   | `updatedAt` | Timestamp            | Co       | Thoi diem cap nhat                        |
| 8   | `deletedAt` | Timestamp (nullable) | Khong    | Thoi diem xoa (neu co soft delete)        |


**Du lieu mau (Mock Data):**

```json
{
  "id": "cate_001",
  "name": "Com",
  "icon": "restaurant",
  "order": 1,
  "imageUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80",
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-01-01T00:00:00Z",
  "deletedAt": null
}
```

**Cac muc hien co:** Com (cate_001), Pho/Bun (cate_002), Tra sua (cate_003), An vat (cate_004), Ga ran (cate_005), Mon Han (cate_006), Mon Nhat (cate_007), Banh mi (cate_008), Lau/Buffet (cate_009), Tra cay (cate_010).

---

### 2.6. `stores`

**Muc dich su dung:** Luu tru thong tin chi tiet cua cac quan an/cua hang, bao gom ten, dia chi, danh gia, phi giao hang, thoi gian giao, va danh sach danh muc noi bo cua quan.

**Duong dan:** `/stores/{storeId}`

**Cac truong (Fields):**


| STT | Ten truong              | Kieu du lieu      | Bat buoc | Mo ta                                                        |
| --- | ----------------------- | ----------------- | -------- | ------------------------------------------------------------ |
| 1   | `id`                    | String            | Co       | ID document tu Firestore                                     |
| 2   | `name`                  | String            | Co       | Ten quan an                                                  |
| 3   | `address`               | String            | Co       | Dia chi cu the (VD: "123 Le Van Viet, TP. Thu Duc")          |
| 4   | `rating`                | Number            | Co       | Diem danh gia trung binh (0.0 - 5.0)                         |
| 5   | `reviewCount`           | Number            | Co       | Tong so danh gia                                             |
| 6   | `avtUrl`                | String            | Co       | Duong dan anh dai dien (avatar)                              |
| 7   | `backUrl`               | String            | Co       | Duong dan anh bia (backdrop)                                 |
| 8   | `isOpen`                | Boolean           | Co       | Quan co dang mo khong                                        |
| 9   | `deliveryTime`          | String            | Co       | Thoi gian giao uoc tinh (VD: "20-30 phut")                   |
| 10  | `deliveryFee`           | Number            | Co       | Phi giao hang (VND)                                          |
| 11  | `categoryIds`           | ArrayString       | Khong    | Danh sach ID danh muc he thong ma quan nay thuoc             |
| 12  | `restaurant_categories` | MapString, Object | Khong    | Danh muc noi bo cua quan (VD: mon chinh, mon phu, nuoc uong) |
| 13  | `createdAt`             | Timestamp         | Co       | Thoi diem tao                                                |
| 14  | `updatedAt`             | Timestamp         | Co       | Thoi diem cap nhat                                           |


**Du lieu mau (Mock Data):**

```json
{
  "id": "store_001",
  "name": "Com tam Phuc Loc Tho",
  "address": "123 Le Van Viet, TP. Thu Duc",
  "rating": 4.8,
  "reviewCount": 500,
  "avtUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80",
  "backUrl": "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&q=80",
  "isOpen": true,
  "deliveryTime": "20-30 phut",
  "deliveryFee": 15000.0,
  "categoryIds": ["cate_001", "cate_004"],
  "restaurant_categories": {
    "rest_cate_001": {
      "name": "Mon chinh",
      "order": 1,
      "createdAt": "2026-04-07T00:00:00Z",
      "updatedAt": "2026-04-07T00:00:00Z"
    },
    "rest_cate_002": {
      "name": "Mon phu",
      "order": 2,
      "createdAt": "2026-04-07T00:00:00Z",
      "updatedAt": "2026-04-07T00:00:00Z"
    }
  },
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Cac quan hien co:** store_001 (Com tam Phuc Loc Tho), store_002 (Tra sua Tocotoco), store_003 (Ga ran KFC Nguyen Cuu), store_004 (Bun bo Hue Ba Le).

---

### 2.7. `products`

**Muc dich su dung:** Luu tru thong tin san pham/mon an cua tung quan, bao gom gia, mo ta, tuy chon (size, topping), trang thai ton kho, va thong tin quang cao.

**Duong dan:** `/products/{productId}`

**Cac truong (Fields):**


| STT | Ten truong     | Kieu du lieu | Bat buoc | Mo ta                                        |
| --- | -------------- | ------------ | -------- | -------------------------------------------- |
| 1   | `id`           | String       | Co       | ID document tu Firestore                     |
| 2   | `storeId`      | String       | Co       | ID quan chua san pham nay                    |
| 3   | `categoryId`   | String       | Co       | ID danh muc he thong                         |
| 4   | `categoryName` | String       | Co       | Ten danh muc he thong                        |
| 5   | `name`         | String       | Co       | Ten mon an                                   |
| 6   | `description`  | String       | Co       | Mo ta chi tiet mon an                        |
| 7   | `basePrice`    | Number       | Co       | Gia co so (chua tinh size/topping)           |
| 8   | `imageUrl`     | String       | Co       | Duong dan anh mon an                         |
| 9   | `isOutOfStock` | Boolean      | Co       | Co dang het hang khong                       |
| 10  | `isFeatured`   | Boolean      | Co       | Co phai mon noi bat khong                    |
| 11  | `optionGroups` | ArrayObject  | Khong    | Danh sach nhom tuy chon (size, topping, ...) |
| 12  | `createdAt`    | Timestamp    | Co       | Thoi diem tao                                |
| 13  | `updatedAt`    | Timestamp    | Co       | Thoi diem cap nhat                           |


**Cau truc optionGroups (truong phuc tap):**

```json
"optionGroups": [
  {
    "name": "Kich thuoc",
    "options": [
      {"name": "M", "price": 0.0},
      {"name": "L", "price": 5000.0}
    ]
  },
  {
    "name": "Topping",
    "options": [
      {"name": "Tran chau", "price": 5000.0},
      {"name": "Thach", "price": 3000.0}
    ]
  }
]
```

**Du lieu mau (Mock Data):**

```json
{
  "id": "prod_001",
  "storeId": "store_001",
  "categoryId": "cate_001",
  "categoryName": "Com",
  "name": "Com tam suon bi cha",
  "description": "Com tam ngon chuan vi Sai Gon voi suon nuong thom phuc",
  "basePrice": 45000.0,
  "imageUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80",
  "isOutOfStock": false,
  "isFeatured": true,
  "optionGroups": [
    {
      "name": "Kich thuoc",
      "options": [
        {"name": "Vua", "price": 0.0},
        {"name": "Lon", "price": 10000.0}
      ]
    }
  ],
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Tong so san pham mau:** 15 san pham, phan bo cho 4 quan (store_001 den store_004).

---

### 2.8. `banners`

**Muc dich su dung:** Luu tru thong tin banner quang cao hien thi tren trang chu (carousel).

**Duong dan:** `/banners/{bannerId}`

**Cac truong (Fields):**


| STT | Ten truong  | Kieu du lieu      | Bat buoc | Mo ta                             |
| --- | ----------- | ----------------- | -------- | --------------------------------- |
| 1   | `id`        | String            | Co       | ID document tu Firestore          |
| 2   | `title`     | String            | Co       | Tieu de banner                    |
| 3   | `imageUrl`  | String            | Co       | Duong dan anh banner              |
| 4   | `storeId`   | String (nullable) | Khong    | Neu banner danh cho 1 quan cu the |
| 5   | `storeName` | String (nullable) | Khong    | Ten quan (neu co)                 |
| 6   | `isActive`  | Boolean           | Co       | Banner co dang hoat dong khong    |
| 7   | `order`     | Number            | Co       | Thu tu hien thi                   |
| 8   | `createdAt` | Timestamp         | Co       | Thoi diem tao                     |
| 9   | `updatedAt` | Timestamp         | Co       | Thoi diem cap nhat                |


**Du lieu mau (Mock Data):**

```json
{
  "id": "banner_001",
  "title": "Sieu sale giua thang",
  "imageUrl": "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&q=80",
  "storeId": null,
  "storeName": null,
  "isActive": true,
  "order": 1,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Cac banner hien co:** banner_001 (Sieu sale giua thang), banner_002 (Freeship 0 dong), banner_003 (Le hoi am thuc), banner_004 (Uong tra van chiu).

---

### 2.9. `vouchers` (Voucher he thong / Public)

**Muc dich su dung:** Luu tru thong tin voucher co san trong he thong, hien thi tai trangUu dai de khach hang xem. (Luu y: day la collection `vouchers`, phan biet voi `system_vouchers` ben duoi.)

**Duong dan:** `/vouchers/{voucherId}`

**Cac truong (Fields):**


| STT | Ten truong       | Kieu du lieu | Bat buoc | Mo ta                               |
| --- | ---------------- | ------------ | -------- | ----------------------------------- |
| 1   | `id`             | String       | Co       | ID document tu Firestore            |
| 2   | `title`          | String       | Co       | Tieu de voucher                     |
| 3   | `subtitle`       | String       | Co       | Mo ta ngan gon                      |
| 4   | `pointsRequired` | Number       | Co       | So diem can de doi voucher nay      |
| 5   | `imageUrl`       | String       | Co       | Duong dan anh voucher               |
| 6   | `remaining`      | Number       | Co       | So luong voucher con lai            |
| 7   | `terms`          | String       | Co       | Dieu khoan su dung                  |
| 8   | `minOrderValue`  | Number       | Co       | Don hang toi thieu de su dung (VND) |
| 9   | `createdAt`      | Timestamp    | Co       | Thoi diem tao                       |
| 10  | `updatedAt`      | Timestamp    | Co       | Thoi diem cap nhat                  |


**Du lieu mau (Mock Data):**

```json
{
  "id": "sys_voucher_001",
  "title": "Giam 20K cho don tu 100K",
  "subtitle": "Danh cho khach hang moi",
  "pointsRequired": 200,
  "imageUrl": "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400&q=80",
  "remaining": 100,
  "terms": "Ap dung cho tat ca quan an.",
  "minOrderValue": 100000.0,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.10. `reviews`

**Muc dich su dung:** Luu tru danh gia cua khach hang ve cac quan an, bao gom sao, binh luan, va hinh anh kem theo.

**Duong dan:** `/reviews/{reviewId}`

**Cac truong (Fields):**


| STT | Ten truong      | Kieu du lieu         | Bat buoc | Mo ta                           |
| --- | --------------- | -------------------- | -------- | ------------------------------- |
| 1   | `id`            | String               | Co       | ID document tu Firestore        |
| 2   | `storeId`       | String               | Co       | ID quan duoc danh gia           |
| 3   | `userId`        | String               | Co       | ID nguoi danh gia               |
| 4   | `userName`      | String               | Co       | Ten nguoi danh gia              |
| 5   | `userAvatarUrl` | String               | Co       | URL avatar nguoi danh gia       |
| 6   | `starRating`    | Number               | Co       | Diem sao (1-5)                  |
| 7   | `comment`       | String               | Co       | Noi dung binh luan              |
| 8   | `imageUrls`     | ArrayString          | Khong    | Danh sach URL hinh anh kem theo |
| 9   | `createdAt`     | Timestamp            | Co       | Thoi diem tao danh gia          |
| 10  | `updatedAt`     | Timestamp            | Co       | Thoi diem cap nhat              |
| 11  | `deletedAt`     | Timestamp (nullable) | Khong    | Thoi diem xoa (neu co)          |


**Du lieu mau (Mock Data):**

```json
{
  "id": "rev_001",
  "storeId": "store_001",
  "userId": "user_001",
  "userName": "Khoi",
  "userAvatarUrl": "https://example.com/avatar/user001.jpg",
  "starRating": 5,
  "comment": "Do an rat ngon, giao hang nhanh, dong goi ky luong.",
  "imageUrls": [
    "https://example.com/review/rev001_1.jpg",
    "https://example.com/review/rev001_2.jpg"
  ],
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z",
  "deletedAt": null
}
```

**Tong so danh gia mau:** 8 danh gia, phan bo cho 4 quan.

---

### 2.11. `orders`

**Muc dich su dung:** Luu tru thong tin don hang cua khach hang, bao gom danh sach mon, tong tien, trang thai, thong tin giao hang, va thong tin tai xe (neu co).

**Duong dan:** `/orders/{orderId}`

**Cac truong (Fields):**


| STT | Ten truong        | Kieu du lieu         | Bat buoc | Mo ta                                           |
| --- | ----------------- | -------------------- | -------- | ----------------------------------------------- |
| 1   | `id`              | String               | Co       | ID document tu Firestore                        |
| 2   | `userId`          | String               | Co       | ID nguoi dat hang                               |
| 3   | `storeId`         | String               | Co       | ID quan chuan bi don                            |
| 4   | `storeName`       | String               | Co       | Ten quan                                        |
| 5   | `items`           | ArrayObject          | Co       | Danh sach mon an trong don                      |
| 6   | `totalAmount`     | Number               | Co       | Tong tien don hang (VND)                        |
| 7   | `deliveryFee`     | Number               | Co       | Phi giao hang (VND)                             |
| 8   | `status`          | Number               | Co       | Trang thai don hang (0-4)                       |
| 9   | `deliveryAddress` | String               | Co       | Dia chi giao hang                               |
| 10  | `paymentMethod`   | String               | Co       | Phuong thuc thanh toan (cash, momo, zalo, card) |
| 11  | `driverId`        | String (nullable)    | Khong    | ID tai xe nhan don                              |
| 12  | `driverName`      | String (nullable)    | Khong    | Ten tai xe                                      |
| 13  | `driverPhone`     | String (nullable)    | Khong    | SDT tai xe                                      |
| 14  | `vehiclePlate`    | String (nullable)    | Khong    | Bien so xe                                      |
| 15  | `createdAt`       | Timestamp            | Co       | Thoi diem tao don                               |
| 16  | `updatedAt`       | Timestamp            | Co       | Thoi diem cap nhat gan nhat                     |
| 17  | `deletedAt`       | Timestamp (nullable) | Khong    | Thoi diem xoa                                   |


**Cac gia tri status:**


| Gia tri | Ten           | Mo ta                      |
| ------- | ------------- | -------------------------- |
| 0       | Cho xac nhan  | Don hang cho quan xac nhan |
| 1       | Dang chuan bi | Quan dang chuan bi mon     |
| 2       | Dang giao     | Tai xe dang giao hang      |
| 3       | Hoan thanh    | Da giao thanh cong         |
| 4       | Da huy        | Don hang da bi huy         |


**Cau truc items:**

```json
"items": [
  {
    "foodId": "prod_001",
    "name": "Com tam suon bi cha",
    "price": 45000.0,
    "quantity": 2,
    "imageUrl": "https://example.com/comtam.jpg",
    "options": [
      {"name": "Tran chau", "price": 5000.0},
      {"name": "Thach ca phe", "price": 8000.0}
    ]
  }
]
```

**Du lieu mau (Mock Data):**

```json
{
  "id": "order_001",
  "userId": "user_001",
  "storeId": "store_001",
  "storeName": "Com tam Phuc Loc Tho",
  "items": [
    {
      "foodId": "prod_001",
      "name": "Com tam suon bi cha",
      "price": 45000.0,
      "quantity": 2,
      "imageUrl": "https://example.com/comtam.jpg"
    }
  ],
  "totalAmount": 140000.0,
  "deliveryFee": 15000.0,
  "status": 2,
  "deliveryAddress": "Ky tuc xa UTC2, Quan 9, TP.HCM",
  "paymentMethod": "momo",
  "driverId": "user_001",
  "driverName": "Le Van B",
  "driverPhone": "0912345678",
  "vehiclePlate": "59A-123.45",
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z",
  "deletedAt": null
}
```

**Tong so don hang mau:** 7 don hang, cac trang thai khac nhau.

---

### 2.12. `customer_profiles`

**Muc dich su dung:** Bang nhanh luu tru profile mo rong cua khach hang, chua diem thanh vien, hang thanh vien, va cac sub-collections (dia chi, thanh toan, thong bao, gio hang, voucher).

**Duong dan:** `/customer_profiles/{userId}`

**Cac truong (Fields):**


| STT | Ten truong       | Kieu du lieu | Bat buoc | Mo ta                                               |
| --- | ---------------- | ------------ | -------- | --------------------------------------------------- |
| 1   | `id`             | String       | Co       | ID document (trung voi userId)                      |
| 2   | `loyaltyPoints`  | Number       | Co       | Diem tich luy hien tai                              |
| 3   | `membershipTier` | Number       | Co       | Hang thanh vien: 0=Dong, 1=Bac, 2=Vang, 3=Kim Cuong |
| 4   | `createdAt`      | Timestamp    | Co       | Thoi diem tao                                       |
| 5   | `updatedAt`      | Timestamp    | Co       | Thoi diem cap nhat                                  |


**Du lieu mau (Mock Data):**

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

**Muc dich su dung:** Bang nhanh luu tru profile tai xe giao hang, chua thong tin phuong tien, trang thai hoat dong, va sub-collection notifications.

**Duong dan:** `/driver_profiles/{userId}`

**Cac truong (Fields):**


| STT | Ten truong      | Kieu du lieu | Bat buoc | Mo ta                          |
| --- | --------------- | ------------ | -------- | ------------------------------ |
| 1   | `id`            | String       | Co       | ID document (trung voi userId) |
| 2   | `vehiclePlate`  | String       | Co       | Bien so xe                     |
| 3   | `vehicleType`   | String       | Co       | Loai phuong tien               |
| 4   | `driverLicense` | String       | Co       | Bang lai xe                    |
| 5   | `isActive`      | Boolean      | Co       | Trang thai hoat dong           |
| 6   | `rating`        | Number       | Co       | Diem danh gia trung binh       |
| 7   | `totalTrips`    | Number       | Co       | Tong so chuyen giao thanh cong |
| 8   | `createdAt`     | Timestamp    | Co       | Thoi diem tao                  |
| 9   | `updatedAt`     | Timestamp    | Co       | Thoi diem cap nhat             |


**Du lieu mau (Mock Data):**

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

**Muc dich su dung:** Bang nhanh luu tru profile quan ban/nguoi ban, chua thong tin kinh doanh va sub-collection notifications.

**Duong dan:** `/merchant_profiles/{userId}`

**Cac truong (Fields):**


| STT | Ten truong        | Kieu du lieu | Bat buoc | Mo ta                                        |
| --- | ----------------- | ------------ | -------- | -------------------------------------------- |
| 1   | `id`              | String       | Co       | ID document (trung voi userId)               |
| 2   | `businessName`    | String       | Co       | Ten doanh nghiep/quan                        |
| 3   | `businessLicense` | String       | Co       | Giay phep kinh doanh                         |
| 4   | `taxCode`         | String       | Co       | Ma so thue                                   |
| 5   | `storeIds`        | ArrayString  | Co       | Danh sach ID cac cua hang thuoc merchant nay |
| 6   | `createdAt`       | Timestamp    | Co       | Thoi diem tao                                |
| 7   | `updatedAt`       | Timestamp    | Co       | Thoi diem cap nhat                           |


**Du lieu mau (Mock Data):**

```json
{
  "id": "user_001",
  "businessName": "Com tam Phuc Loc Tho",
  "businessLicense": "BL123456789",
  "taxCode": "TAX123456789",
  "storeIds": ["store_001"],
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.15. `admin_profiles`

**Muc dich su dung:** Bang nhanh luu tru profile quan tri vien, chua thong tin cap bac, phong ban, va quyen han.

**Duong dan:** `/admin_profiles/{userId}`

**Cac truong (Fields):**


| STT | Ten truong    | Kieu du lieu | Bat buoc | Mo ta                                        |
| --- | ------------- | ------------ | -------- | -------------------------------------------- |
| 1   | `id`          | String       | Co       | ID document (trung voi userId)               |
| 2   | `adminLevel`  | Number       | Co       | Cap bac admin: 1=Admin thuong, 2=Super admin |
| 3   | `department`  | String       | Co       | Bo phan lam viec (VD: "Bo phan van hanh")              |
| 4   | `permissions` | ArrayString  | Co       | Danh sach quyen han                          |
| 5   | `createdAt`   | Timestamp    | Co       | Thoi diem tao                                |
| 6   | `updatedAt`   | Timestamp    | Co       | Thoi diem cap nhat                           |


**Du lieu mau (Mock Data):**

```json
{
  "id": "user_002",
  "adminLevel": 1,
  "department": "Bo phan van hanh",
  "permissions": ["manage_users", "manage_orders", "manage_stores", "view_reports"],
  "createdAt": "2026-03-01T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

---

### 2.16. `system_vouchers`

**Muc dich su dung:** Luu tru voucher he thong ma khach hang co the doi diem (phan biet voi `vouchers`). Chua thong tin diem can thiet, so luong con lai, va dieu khoan.

**Duong dan:** `/system_vouchers/{systemVoucherId}`

**Cac truong (Fields):**


| STT | Ten truong       | Kieu du lieu | Bat buoc | Mo ta                    |
| --- | ---------------- | ------------ | -------- | ------------------------ |
| 1   | `id`             | String       | Co       | ID document tu Firestore |
| 2   | `title`          | String       | Co       | Tieu de voucher          |
| 3   | `subtitle`       | String       | Co       | Mo ta ngan gon           |
| 4   | `pointsRequired` | Number       | Co       | So diem can de doi       |
| 5   | `imageUrl`       | String       | Co       | Duong dan anh voucher    |
| 6   | `remaining`      | Number       | Co       | So luong voucher con lai |
| 7   | `terms`          | String       | Co       | Dieu khoan su dung       |
| 8   | `minOrderValue`  | Number       | Co       | Don hang toi thieu (VND) |
| 9   | `createdAt`      | Timestamp    | Co       | Thoi diem tao            |
| 10  | `updatedAt`      | Timestamp    | Co       | Thoi diem cap nhat       |


**Ghi chu:** Hien tai trong code, collection nay chua duoc seed. Chi `vouchers` (root) duoc seed.neu nguoi dung muon su dung `system_vouchers`, can bo sung seed trong `DataSeeder`.

---

## 3. Bang nhanh hoac Sub-collections

### 3.1. `customer_profiles/{userId}/addresses`

**Muc dich su dung:** Luu tru danh sach dia chi giao hang cua khach hang.

**Duong dan:** `/customer_profiles/{userId}/addresses/{addressId}`

**Cac truong (Fields):**


| STT | Ten truong      | Kieu du lieu         | Bat buoc | Mo ta                                     |
| --- | --------------- | -------------------- | -------- | ----------------------------------------- |
| 1   | `id`            | String               | Co       | ID document tu Firestore                  |
| 2   | `name`          | String               | Co       | Nhan dia chi (VD: "Nha rieng", "Cong ty") |
| 3   | `address`       | String               | Co       | Dia chi chi tiet day du                   |
| 4   | `receiverName`  | String               | Co       | Ho ten nguoi nhan                         |
| 5   | `receiverPhone` | String               | Co       | SDT nguoi nhan                            |
| 6   | `lat`           | Number               | Co       | Vi do (latitude)                          |
| 7   | `lng`           | Number               | Co       | Kinh do (longitude)                       |
| 8   | `isDefault`     | Boolean              | Co       | Co phai dia chi mac dinh khong            |
| 9   | `createdAt`     | Timestamp            | Co       | Thoi diem tao                             |
| 10  | `updatedAt`     | Timestamp            | Co       | Thoi diem cap nhat                        |
| 11  | `deletedAt`     | Timestamp (nullable) | Khong    | Thoi diem xoa (neu co)                    |


**Du lieu mau (Mock Data):**

```json
{
  "id": "addr_001",
  "name": "Nha rieng",
  "address": "Ky tuc xa UTC2, Quan 9, TP.HCM",
  "receiverName": "Khoi",
  "receiverPhone": "0123456789",
  "lat": 10.8455,
  "lng": 106.7939,
  "isDefault": true,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z",
  "deletedAt": null
}
```

**Ghi chu:** Co 2 dia chi mau cho user_001: addr_001 (mac dinh) va addr_002 (Truong hoc).

---

### 3.2. `customer_profiles/{userId}/payment_methods`

**Muc dich su dung:** Luu tru cac phuong thuc thanh toan da lien ket cua khach hang.

**Duong dan:** `/customer_profiles/{userId}/payment_methods/{paymentMethodId}`

**Cac truong (Fields):**


| STT | Ten truong    | Kieu du lieu      | Bat buoc | Mo ta                                                   |
| --- | ------------- | ----------------- | -------- | ------------------------------------------------------- |
| 1   | `id`          | String            | Co       | ID document tu Firestore                                |
| 2   | `type`        | Number            | Co       | Loai: 1=Tien mat, 2=Vi dien tu, 3=The ngan hang         |
| 3   | `isDefault`   | Boolean           | Co       | Co phai phuong thuc mac dinh khong                      |
| 4   | `cardBrand`   | String (nullable) | Khong    | Thuong hieu the (neu type=3): "visa", "mastercard"      |
| 5   | `last4Digits` | String (nullable) | Khong    | 4 chu so cuoi the (neu type=3)                          |
| 6   | `walletBrand` | String (nullable) | Khong    | Thuong hieu vi (neu type=2): "momo", "zalopay", "vnpay" |
| 7   | `isLinked`    | Boolean           | Co       | Da lien ket chua (neu type=2)                           |
| 8   | `createdAt`   | Timestamp         | Co       | Thoi diem tao                                           |
| 9   | `updatedAt`   | Timestamp         | Co       | Thoi diem cap nhat                                      |


**Du lieu mau (Mock Data):**

```json
// Vi dien tu
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

// The ngan hang
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

**Ghi chu:** Co 2 phuong thuc mau: pm_001 (MoMo vi dien tu, mac dinh) va pm_002 (The Visa ****1234).

---

### 3.3. `customer_profiles/{userId}/notifications`

**Muc dich su dung:** Luu tru thong bao cua khach hang, bao gom thong bao he thong, khuyen mai, va cap nhat don hang.

**Duong dan:** `/customer_profiles/{userId}/notifications/{notificationId}`

**Cac truong (Fields):**


| STT | Ten truong    | Kieu du lieu | Bat buoc | Mo ta                                                |
| --- | ------------- | ------------ | -------- | ---------------------------------------------------- |
| 1   | `id`          | String       | Co       | ID document tu Firestore                             |
| 2   | `type`        | Number       | Co       | Loai thong bao: 0=He thong, 1=Khuyen mai, 2=Don hang |
| 3   | `title`       | String       | Co       | Tieu de thong bao                                    |
| 4   | `body`        | String       | Co       | Noi dung thong bao                                   |
| 5   | `referenceId` | String       | Co       | ID tham chieu (VD: orderId, voucherId)               |
| 6   | `isRead`      | Boolean      | Co       | Da doc chua                                          |
| 7   | `createdAt`   | Timestamp    | Co       | Thoi diem tao                                        |


**Du lieu mau (Mock Data):**

```json
// Thong bao don hang
{
  "id": "notif_001",
  "type": 2,
  "title": "Don hang da duoc giao thanh cong",
  "body": "Don hang order_001 da duoc giao",
  "referenceId": "order_001",
  "isRead": false,
  "createdAt": "2026-04-07T00:00:00Z"
}

// Thong bao khuyen mai
{
  "id": "notif_002",
  "type": 1,
  "title": "Khuyen mai dac biet",
  "body": "Giam 20% cho don hang dau tien",
  "referenceId": "voucher_001",
  "isRead": true,
  "createdAt": "2026-04-06T00:00:00Z"
}
```

---

### 3.4. `customer_profiles/{userId}/cart`

**Muc dich su dung:** Luu tru gio hang tam thoi cua khach hang, dong bo real-time voi UI qua Firestore stream.

**Duong dan:** `/customer_profiles/{userId}/cart/{cartItemId}`

**Cac truong (Fields):**


| STT | Ten truong  | Kieu du lieu      | Bat buoc | Mo ta                             |
| --- | ----------- | ----------------- | -------- | --------------------------------- |
| 1   | `id`        | String            | Co       | ID document tu Firestore          |
| 2   | `storeId`   | String            | Co       | ID quan chua san pham             |
| 3   | `foodId`    | String            | Co       | ID san pham (mon an)              |
| 4   | `name`      | String            | Co       | Ten mon an                        |
| 5   | `price`     | Number            | Co       | Don gia (da bao gom size/topping) |
| 6   | `quantity`  | Number            | Co       | So luong                          |
| 7   | `size`      | String (nullable) | Khong    | Kich thuoc da chon (VD: "M", "L") |
| 8   | `sizePrice` | Number (nullable) | Khong    | Gia them cua size                 |
| 9   | `toppings`  | ArrayObject       | Khong    | Danh sach topping da chon         |
| 10  | `note`      | String (nullable) | Khong    | Ghi chu cho quan                  |
| 11  | `imageUrl`  | String (nullable) | Khong    | URL anh mon an                    |
| 12  | `createdAt` | Timestamp         | Co       | Thoi diem tao                     |
| 13  | `updatedAt` | Timestamp         | Co       | Thoi diem cap nhat                |


**Cau truc toppings:**

```json
"toppings": [
  {"name": "Tran chau trang", "price": 10000.0},
  {"name": "Thach trai cay", "price": 8000.0}
]
```

**Du lieu mau (Mock Data):**

```json
{
  "id": "cart_item_001",
  "storeId": "store_001",
  "foodId": "prod_001",
  "name": "Com tam suon bi cha",
  "price": 45000.0,
  "quantity": 2,
  "imageUrl": "https://example.com/comtam.jpg",
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Ghi chu:** Co 2 item mau: cart_item_001 (Com tam) va cart_item_002 (Tra sua trach tang). Gia tri `price` trong cart chua bao gom toppings - toppings duoc luu rieng trong mang `toppings`.

---

### 3.5. `customer_profiles/{userId}/my_vouchers`

**Muc dich su dung:** Luu tru voucher ma khach hang da doi hoac da nhan.

**Duong dan:** `/customer_profiles/{userId}/my_vouchers/{myVoucherId}`

**Cac truong (Fields):**


| STT | Ten truong      | Kieu du lieu | Bat buoc | Mo ta                              |
| --- | --------------- | ------------ | -------- | ---------------------------------- |
| 1   | `id`            | String       | Co       | ID document tu Firestore           |
| 2   | `name`          | String       | Co       | Ten voucher                        |
| 3   | `code`          | String       | Co       | Ma voucher                         |
| 4   | `description`   | String       | Co       | Mo ta chi tiet                     |
| 5   | `expiryDate`    | Timestamp    | Co       | Ngay het han                       |
| 6   | `discountValue` | Number       | Co       | Gia tri giam (neu khong phan tram) |
| 7   | `isPercentage`  | Boolean      | Co       | La phan tram giam khong            |
| 8   | `minOrderValue` | Number       | Co       | Don hang toi thieu (VND)           |
| 9   | `createdAt`     | Timestamp    | Co       | Thoi diem tao                      |
| 10  | `updatedAt`     | Timestamp    | Co       | Thoi diem cap nhat                 |


**Du lieu mau (Mock Data):**

```json
{
  "id": "mv_001",
  "name": "Giam 20K phi giao hang",
  "code": "FREESHIP20",
  "description": "Ap dung cho don tu 100K",
  "expiryDate": "2026-04-30T23:59:59Z",
  "discountValue": 20000.0,
  "isPercentage": false,
  "minOrderValue": 100000.0,
  "createdAt": "2026-04-07T00:00:00Z",
  "updatedAt": "2026-04-07T00:00:00Z"
}
```

**Ghi chu:** Co 2 voucher mau: mv_001 (FREESHIP20 - giam 20K phi giao hang) va mv_002 (SAVE10 - giam 10%).

---

### 3.6. `driver_profiles/{userId}/notifications`

**Muc dich su dung:** Luu tru thong bao dành riêng cho tai xe giao hang.

**Duong dan:** `/driver_profiles/{userId}/notifications/{notificationId}`

**Cac truong (Fields):** Tuong tu nhu `customer_profiles/{userId}/notifications`, nhung `type` có them gia tri 11 (Yeu cau nhan don) và 12 (Thong bao giao hang).


| Gia tri type | Mo ta                |
| ------------ | -------------------- |
| 11           | Yeu cau nhan don moi |
| 12           | Thong bao giao hang  |


**Du lieu mau (Mock Data):**

```json
{
  "id": "dnotif_001",
  "type": 11,
  "title": "Yeu cau nhan don moi",
  "body": "Ban co don hang moi cho nhan: order_002",
  "referenceId": "order_002",
  "isRead": false,
  "createdAt": "2026-04-07T00:00:00Z"
}
```

---

### 3.7. `merchant_profiles/{userId}/notifications`

**Muc dich su dung:** Luu tru thong bao dành riêng cho quan ban/nguoi kinh doanh.

**Duong dan:** `/merchant_profiles/{userId}/notifications/{notificationId}`

**Cac truong (Fields):** Tuong tu nhu notifications khach hang, nhung `type` có them gia tri 21 (Don hang moi).


| Gia tri type | Mo ta                      |
| ------------ | -------------------------- |
| 21           | Don hang moi tu khach hang |


**Du lieu mau (Mock Data):**

```json
{
  "id": "mnotif_001",
  "type": 21,
  "title": "Don hang moi tu khach hang",
  "body": "Ban co don hang moi: order_003",
  "referenceId": "order_003",
  "isRead": false,
  "createdAt": "2026-04-07T00:00:00Z"
}
```

---

### 3.8. `users/{userId}/search_history`

**Muc dich su dung:** Luu tru lich su tim kiem cua khach hang, giup goi y tu khoa da tim.

**Duong dan:** `/users/{userId}/search_history/{historyId}`

**Cac truong (Fields):**


| STT | Ten truong  | Kieu du lieu | Bat buoc | Mo ta                                  |
| --- | ----------- | ------------ | -------- | -------------------------------------- |
| 1   | `id`        | String       | Co       | ID document tu Firestore               |
| 2   | `keyword`   | String       | Co       | Tu khoa tim kiem                       |
| 3   | `createdAt` | Timestamp    | Co       | Thoi diem tim kiem (hoac cap nhat lai) |


**Ghi chu:** Trong code, duong dan su dung la `users/{userId}/search_history`, nhung trong `clearAllSeededData` cua `DataSeeder`, no nam trong danh sach `userSubCollections` cua bang nhanh `users` (khong phai root collection riêng). Day la mot diem can luu y - `search_history` nam trong `users` chu khong phai trong `customer_profiles`.

---

## 4. Danh sach cac truong co ban

Dưới đây là bảng tổng hợp các kiểu dữ liệu được sử dụng xuyên suốt các collections:


| Kieu Firestore | Tuong ung Dart                  | Mo ta                  |
| -------------- | ------------------------------- | ---------------------- |
| `String`       | `String`                        | Chuoi van ban          |
| `Number`       | `int` hoac `double`             | So nguyen hoac so thuc |
| `Boolean`      | `bool`                          | Dung/Sai               |
| `Timestamp`    | `DateTime`                      | Thoi diem (ngay gio)   |
| `Array<T>`     | `List<T>`                       | Mang                   |
| `Map`          | `Map<String, dynamic>`          | Doi tuong / Dictionary |
| `null`         | `nullable` (String?, int?, ...) | Gia tri co the rong    |


### Quy uoc dat ten truong

- Ten truong Firestore su dung `camelCase` (VD: `createdAt`, `isDefault`, `loyaltyPoints`)
- Ten icon su dung `snake_case` (VD: `restaurant`, `local_cafe`)
- Ma voucher su dung `UPPERCASE` (VD: `FREESHIP20`, `SAVE10`)

---

## Lich su cap nhat


| Ngay       | Mo ta                                                |
| ---------- | ---------------------------------------------------- |
| 2026-05-22 | Phien ban dau tien - tai lieu day du cac collections |


