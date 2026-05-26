# API - Dat Hang (Checkout)

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/orders` |
| **Endpoint** | `POST /api/orders/checkout` |
| **Authentication** | Khong can Bearer Token (userId truyen qua body) |
| **Content-Type** | `application/json` |

---

## 1. Dat Hang (Checkout)

**Endpoint:** `POST /api/orders/checkout`

**Mo ta:** Thuc hien dat hang cho khach hang. Quy trinh gom: kiem tra gio hang, kiem tra khoang cach Haversine, tinh tong tien server-side, xu ly voucher, va tao don hang atomically. Sau khi dat hang thanh cong, gio hang se bi xoa.

### Request Body

```json
{
  "userId": "user_001",
  "addressId": "addr_001",
  "paymentMethod": "momo",
  "voucherId": "sys_voucher_001",
  "note": "Giao gap"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |
| `addressId` | String | **Co** | ID dia chi giao hang cua khach hang | `addr_001` |
| `paymentMethod` | String | **Co** | Phuong thuc thanh toan: `cash`, `momo`, `zalo`, `card` | `momo` |
| `voucherId` | String | Khong | ID voucher su dung (co the null) | `sys_voucher_001` |
| `note` | String | Khong | Ghi chu cho don hang | `Giao gap` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Dat hang thanh cong, vui long cho cua hang xac nhan.",
  "data": {
    "orderId": "AbCdEfGhIjKlMnOpQrStUvWxYz123456",
    "orderCode": "QRSTUV",
    "storeId": "store_001",
    "storeName": "Com tam Phuc Loc Tho",
    "userId": "user_001",
    "items": [
      {
        "foodId": "prod_001",
        "name": "Com tam suon bi cha",
        "price": 45000.0,
        "quantity": 2,
        "imageUrl": "https://images.unsplash.com/photo-xxx",
        "options": [
          { "name": "M", "price": 0.0 },
          { "name": "Tran chau", "price": 5000.0 }
        ]
      }
    ],
    "totalAmount": 90000.0,
    "deliveryFee": 15000.0,
    "discountAmount": 20000.0,
    "finalAmount": 85000.0,
    "paymentMethod": "momo",
    "deliveryAddress": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "status": 0,
    "createdAt": "2026-05-26T10:30:00Z",
    "note": "Giao gap"
  }
}
```

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `orderId` | String | ID don hang duoc tao (Firestore auto-generated ID) | `AbCdEfGhIjKlMnOpQrStUvWxYz123456` |
| `orderCode` | String | Ma don hang (6 ky tu cuoi cua orderId, viet hoa) | `QRSTUV` |
| `storeId` | String | ID cua hang | `store_001` |
| `storeName` | String | Ten cua hang | `Com tam Phuc Loc Tho` |
| `userId` | String | ID nguoi dung | `user_001` |
| `items` | List<OrderItemData> | Danh sach mon an trong don | Xem vi du |
| `items[].foodId` | String | ID san pham | `prod_001` |
| `items[].name` | String | Ten mon an | `Com tam suon bi cha` |
| `items[].price` | Double | Don gia | `45000.0` |
| `items[].quantity` | Integer | So luong | `2` |
| `items[].imageUrl` | String | URL anh mon an | `https://...` |
| `items[].options` | List<ToppingOption> | Cac tuy chon da chon (size, topping) | Xem vi du |
| `items[].options[].name` | String | Ten tuy chon | `M` |
| `items[].options[].price` | Double | Gia tuy chon | `0.0` |
| `totalAmount` | Double | Tong tien hang (chua tinh ph ship va giam gia) | `90000.0` |
| `deliveryFee` | Double | Phi giao hang (co dinh 15000 VND) | `15000.0` |
| `discountAmount` | Double | So tien duoc giam (neu co voucher) | `20000.0` |
| `finalAmount` | Double | Tong so tien phai thanh toan | `85000.0` |
| `paymentMethod` | String | Phuong thuc thanh toan | `momo` |
| `deliveryAddress` | String | Dia chi giao hang | `Ky tuc xa UTC2...` |
| `status` | Integer | Trang thai don hang (0 = Cho xac nhan) | `0` |
| `createdAt` | String (ISO 8601) | Thoi gian tao don | `2026-05-26T10:30:00Z` |
| `note` | String | Ghi chu | `Giao gap` |

### Response (That bai - 400 - Gio hang rong)

```json
{
  "success": false,
  "code": 400,
  "message": "Gio hang cua ban dang rong. Vui long them mon an vao gio hang truoc khi dat hang."
}
```

### Response (That bai - 400 - Khoang cach vuot gioi han)

```json
{
  "success": false,
  "code": 400,
  "message": "Cua hang nay khong the giao hang den dia chi cua ban. Khoang cach hien tai la 12.5 km, vuot qua gioi han 10.0 km."
}
```

### Response (That bai - 400 - Voucher khong hop le)

```json
{
  "success": false,
  "code": 400,
  "message": "Voucher khong hop le hoac da het han."
}
```

```json
{
  "success": false,
  "code": 400,
  "message": "Don hang toi thieu 100.000 VND de su dung voucher nay."
}
```

### Response (That bai - 400 - Mon an het hang)

```json
{
  "success": false,
  "code": 400,
  "message": "Mot so mon an trong gio hang da het hang. Vui long xoa va dat lai."
}
```

### Response (That bai - 404)

```json
{
  "success": false,
  "code": 404,
  "message": "Dia chi khong ton tai."
}
```

---

## Quy Trinh Xu Ly Phia Server

```
1. Lay gio hang tu Firestore (customer_profiles/{userId}/cart)
       |
       v
2. Kiem tra gio hang rong?
       |
       +-- Co --> Tra ve loi 400 (Gio hang rong)
       |
       v (Khong)
3. Lay thong tin dia chi giao hang
       |
       v
4. Lay thong tin cua hang
       |
       v
5. Kiem tra khoang cach Haversine (gioi han 10km)
       |
       +-- Vuot gioi han --> Tra ve loi 400 (Khoang cach vuot gioi han)
       |
       v (Trong pham vi)
6. Kiem tra ton kho san pham (isOutOfStock)
       |
       +-- Co mon het hang --> Tra ve loi 400 (Mon an het hang)
       |
       v (Tat ca con hang)
7. Tinh tong tien hang (tong cua tat ca mon * so luong)
       |
       v
8. Neu co voucherId:
       |
       +-- Tim voucher trong collections (vouchers / my_vouchers)
       |    |
       |    +-- Khong tim thay --> Loi 400 (Voucher khong ton tai)
       |    |
       |    +-- Con han su dung / con so luong / don toi thieu?
       |         |
       |         +-- Khong --> Loi 400 (Voucher khong hop le)
       |         |
       |         v (Hop le)
       |    Tinh so tien giam:
       |    - Type 1 (phan tram): tongTien * (value / 100)
       |    - Type 2 (tien mat): value (nhung khong vuot tongTien)
       |
       v
9. Tinh tong thanh toan: tongTienHang + phiShip - soTienGiam
       |
       v
10. Tao don hang atomically (WriteBatch):
    - Tao document trong collection "orders"
    - Xoa tat ca document trong cart (customer_profiles/{userId}/cart)
    - Giam remaining cua voucher (neu la voucher he thong)
    - Xoa voucher ca nhan khoi my_vouchers (neu la voucher ca nhan)
       |
       v
11. Tra ve CheckoutResponse
```

---

## Cac Trang Thai Don Hang

| Gia tri | Mo ta |
|---|---|
| `0` | Cho xac nhan |
| `1` | Dang chuan bi |
| `2` | Dang giao |
| `3` | Hoan thanh |
| `4` | Da huy |

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
