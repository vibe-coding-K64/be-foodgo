# API - Don Hang (Order)

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/orders` |
| **Authentication** | Khong can Bearer Token |
| **Content-Type** | `application/json` |

---

## 1. Lay Danh Sach Don Hang Theo Cua Hang

**Endpoint:** `GET /api/orders`

**Mo ta:** Lay tat ca don hang cua mot cua hang theo storeId.

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `storeId` | String | **Co** | ID cua hang | `store_001` |

### Response (Thanh cong - 200)

```json
[
  {
    "id": "order_001",
    "userId": "user_001",
    "storeId": "store_001",
    "code": "ORDER01",
    "customerName": "Khach hang",
    "customerPhone": "0123456789",
    "deliveryAddress": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "driverName": "Le Van B",
    "driverPhone": "0912345678",
    "items": [
      {
        "name": "Com tam suon bi cha",
        "options": "M, + Tran chau",
        "quantity": 2,
        "price": 45000.0
      }
    ],
    "totalAmount": 140000.0,
    "shippingFee": 15000.0,
    "discountAmount": 0.0,
    "finalAmount": 155000.0,
    "paymentMethod": "momo",
    "status": "Dang chuan bi",
    "createdAt": "2026-05-25T10:00:00Z",
    "updatedAt": "2026-05-25T10:00:00Z"
  }
]
```

---

## 2. Lay Don Hang Theo ID

**Endpoint:** `GET /api/orders/{id}`

**Mo ta:** Lay thong tin chi tiet cua mot don hang theo orderId.

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID don hang | `order_001` |

### Response (Thanh cong - 200)

```json
{
  "id": "order_001",
  "userId": "user_001",
  "storeId": "store_001",
  "code": "ORDER01",
  "customerName": "Khach hang",
  "customerPhone": "0123456789",
  "deliveryAddress": "Ky tuc xa UTC2, Quan 9, TP.HCM",
  "driverName": "Le Van B",
  "driverPhone": "0912345678",
  "items": [
    {
      "name": "Com tam suon bi cha",
      "options": "M, + Tran chau",
      "quantity": 2,
      "price": 45000.0
    }
  ],
  "totalAmount": 140000.0,
  "shippingFee": 15000.0,
  "discountAmount": 0.0,
  "finalAmount": 155000.0,
  "paymentMethod": "momo",
  "status": "Dang chuan bi",
  "createdAt": "2026-05-25T10:00:00Z",
  "updatedAt": "2026-05-25T10:00:00Z"
}
```

### Response (That bai - 404)

```json
404 Not Found
```

---

## 3. Tao Don Hang Moi

**Endpoint:** `POST /api/orders`

**Mo ta:** Tao mot don hang moi (thu cong, khong dung qua Checkout API).

### Request Body

```json
{
  "userId": "user_001",
  "storeId": "store_001",
  "code": "ORD001",
  "customerName": "Nguyen Van A",
  "customerPhone": "0123456789",
  "deliveryAddress": "123 Nguyen Huu, Quan 9, TP.HCM",
  "items": [
    {
      "name": "Com tam suon bi cha",
      "options": "M",
      "quantity": 2,
      "price": 45000.0
    }
  ],
  "totalAmount": 90000.0,
  "shippingFee": 15000.0,
  "discountAmount": 0.0,
  "finalAmount": 105000.0,
  "paymentMethod": "cash",
  "status": "Cho xac nhan"
}
```

### Response (Thanh cong - 200)

```json
"AbCdEfGhIjKlMnOpQrStUvWxYz123456"
```

Tra ve string: ID cua document don hang moi duoc tao.

---

## 4. Cap Nhat Trang Thai Don Hang

**Endpoint:** `PATCH /api/orders/{id}/status`

**Mo ta:** Cap nhat trang thai cua mot don hang.

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID don hang | `order_001` |

### Request Body

```json
{
  "status": "Dang giao"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `status` | String | **Co** | Trang thai moi cua don hang | `Dang giao` |

### Response (Thanh cong - 200)

```json
"Dang giao"
```

### Response (That bai - 404)

```json
404 Not Found
```

---

## 5. Huy Don Hang

**Endpoint:** `POST /api/orders/{id}/cancel`

**Mo ta:** Cho phep khach hang huy don hang cua minh. Chi co the huy khi don hang o trang thai [Cho xac nhan] (status = 0). Don hang o trang thai [Dang chuan bi], [Dang giao], [Hoan thanh], hoac [Da huy] khong the huy.

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID don hang can huy | `order_001` |

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang (de xac thuc quyen so huu don hang) | `user_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Huy don hang thanh cong.",
  "data": {
    "id": "order_001",
    "userId": "user_001",
    "storeId": "store_001",
    "code": "ORDER01",
    "customerName": "Khach hang",
    "customerPhone": "0123456789",
    "deliveryAddress": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "driverName": null,
    "driverPhone": null,
    "items": [...],
    "totalAmount": 140000.0,
    "shippingFee": 15000.0,
    "discountAmount": 0.0,
    "finalAmount": 155000.0,
    "paymentMethod": "momo",
    "status": "Da huy",
    "createdAt": "2026-05-25T10:00:00Z",
    "updatedAt": "2026-05-26T11:00:00Z"
  }
}
```

### Response (That bai - 400 - Trang thai khong cho phep huy)

```json
{
  "success": false,
  "code": 400,
  "message": "Don hang dang o trang thai 'Dang giao', khong the huy."
}
```

### Response (That bai - 403 - Khong phai chu so huu)

```json
{
  "success": false,
  "code": 403,
  "message": "Ban khong co quyen huy don hang nay."
}
```

### Response (That bai - 404)

```json
{
  "success": false,
  "code": 404,
  "message": "Don hang khong ton tai."
}
```

---

## Bang Trang Thai Don Hang

| Gia tri (Integer) | Gia tri (String) | Mo ta | Hanh dong cho phep |
|---|---|---|---|
| `0` | Cho xac nhan | Don hang da dat, cho cua hang xac nhan | **Co the huy** |
| `1` | Dang chuan bi | Cua hang dang prepare don hang | Khong the huy |
| `2` | Dang giao | Tai xe dang giao hang | Khong the huy |
| `3` | Hoan thanh | Don hang da giao thanh cong | Khong the huy |
| `4` | Da huy | Don hang da bi huy | Khong the huy |

---

## Bieu Do Quy Trinh Huy Don

```
Nguoi dung xem chi tiet don hang (OrderDetailView)
       |
       v
  Kiem tra trang thai don hang:
       |
       +-- status == 0 (Cho xac nhan)
       |    |
       |    v
       |    Hien thi nut "Huy don hang" (mau do)
       |
       +-- status != 0
            |
            v
            Khong hien thi nut huy

========================================

Nguoi dung bam "Huy don hang"
       |
       v
  Hien thi Dialog xac nhan:
  "Ban co chac chan muon huy don hang [id]?"
       |
       v
  Nguoi dung bam "Dong y"
       |
       v
  POST /api/orders/{id}/cancel?userId=user_001
       |
       v
  +----+----+
  |         |
  OK       400 (Trang thai khong cho phep huy)
  |         |
  |         +-- 403 (Khong phai chu so huu)
  |         |
  |         +-- 404 (Don hang khong ton tai)
  |
  v
  Cap nhat giao dien:
  - status = "Da huy"
  - An nut huy don
  - An thong tin tai xe
```

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
