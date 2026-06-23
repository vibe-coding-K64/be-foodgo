# API - Thanh Toan (Payment)

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/payments` |
| **Authentication** | **Bearer Token (JWT) bat buoc** |
| **Content-Type** | `application/json` |

> Tat ca cac endpoint trong phan nay deu yeu cau header `Authorization: Bearer <token>`. Server se trich xuat userId tu token.

---

## 1. Lay Danh Sach Phuong Thuc Thanh Toan

**Endpoint:** `GET /api/payments`

**Mo ta:** Tra ve danh sach tat ca phuong thuc thanh toan da dang ky cua nguoi dung hien tai.

### Headers

| Header | Gia tri |
|---|---|
| `Authorization` | `Bearer <JWT_TOKEN>` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Lay danh sach phuong thuc thanh toan thanh cong.",
  "data": [
    {
      "id": "pm_001",
      "name": "Vi MoMo cua toi",
      "type": "momo",
      "details": "",
      "isDefault": true,
      "cardBrand": null,
      "last4Digits": null,
      "walletBrand": "momo",
      "isLinked": true,
      "createdAt": "2026-05-26T10:00:00Z",
      "updatedAt": "2026-05-26T10:00:00Z"
    },
    {
      "id": "pm_002",
      "name": "The Visa cua toi",
      "type": "card",
      "details": "**** **** **** 1234",
      "isDefault": false,
      "cardBrand": "Visa",
      "last4Digits": "1234",
      "walletBrand": null,
      "isLinked": true,
      "createdAt": "2026-05-26T10:00:00Z",
      "updatedAt": "2026-05-26T10:00:00Z"
    }
  ]
}
```

### Response (That bai - 401)

```json
{
  "success": false,
  "code": 401,
  "message": "Chua xac thuc. Vui long dang nhap de tiep tuc."
}
```

---

## 2. Lay Mot Phuong Thuc Thanh Toan

**Endpoint:** `GET /api/payments/{id}`

**Mo ta:** Tra ve thong tin chi tiet cua mot phuong thuc thanh toan theo ID.

### Headers

| Header | Gia tri |
|---|---|
| `Authorization` | `Bearer <JWT_TOKEN>` |

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID phuong thuc thanh toan | `pm_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Lay phuong thuc thanh toan thanh cong.",
  "data": {
    "id": "pm_001",
    "name": "Vi MoMo cua toi",
    "type": "momo",
    "details": "",
    "isDefault": true,
    "cardBrand": null,
    "last4Digits": null,
    "walletBrand": "momo",
    "isLinked": true,
    "createdAt": "2026-05-26T10:00:00Z",
    "updatedAt": "2026-05-26T10:00:00Z"
  }
}
```

### Response (That bai - 404)

```json
{
  "success": false,
  "code": 404,
  "message": "Phuong thuc thanh toan khong ton tai."
}
```

---

## 3. Them Phuong Thuc Thanh Toan Moi

**Endpoint:** `POST /api/payments`

**Mo ta:** Them mot phuong thuc thanh toan moi cho nguoi dung. Neu isDefault=true, cac phuong thuc mac dinh cu se bi bo danh dau bang WriteBatch (atomic).

### Headers

| Header | Gia tri |
|---|---|
| `Authorization` | `Bearer <JWT_TOKEN>` |

### Request Body

```json
{
  "type": "momo",
  "name": "Vi MoMo cua toi",
  "details": "",
  "isDefault": true
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `type` | String | **Co** | Loai phuong thuc thanh toan: `momo`, `zalo`, `card`, `cash` | `momo` |
| `name` | String | **Co** | Ten hien thi cua phuong thuc thanh toan | `Vi MoMo cua toi` |
| `details` | String | Khong | Chi tiet bo sung (VD: so the, ten ngan hang) | `**** **** **** 1234` |
| `isDefault` | Boolean | Khong | Dat lam phuong thuc mac dinh (mac dinh: false) | `true` |

### Chi Tiet Xu Ly Theo Loai

| Loai (`type`) | `walletBrand` | `cardBrand` | `last4Digits` | `isLinked` |
|---|---|---|---|---|
| `momo` | `momo` | `null` | `null` | `true` |
| `zalo` | `zalopay` | `null` | `null` | `true` |
| `card` | `null` | `Visa` (mac dinh) | 4 chu so cuoi cua `details` | `true` |
| `cash` | `null` | `null` | `null` | `false` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Them phuong thuc thanh toan thanh cong.",
  "data": {
    "id": "pm_003",
    "name": "Vi MoMo cua toi",
    "type": "momo",
    "details": "",
    "isDefault": true,
    "cardBrand": null,
    "last4Digits": null,
    "walletBrand": "momo",
    "isLinked": true,
    "createdAt": "2026-05-26T10:00:00Z",
    "updatedAt": "2026-05-26T10:00:00Z"
  }
}
```

### Response (That bai - 400)

```json
{
  "success": false,
  "code": 400,
  "message": "Loai phuong thuc thanh toan khong hop le: unknown. Chi chap nhan: momo, zalo, card, cash."
}
```

---

## 4. Dat Phuong Thuc Thanh Toan Mac Dinh

**Endpoint:** `PUT /api/payments/{id}/default`

**Mo ta:** Dat mot phuong thuc thanh toan lam phuong thuc mac dinh. Su dung WriteBatch (atomic) de dong thoi bo danh dau cu va dat danh dau moi.

### Headers

| Header | Gia tri |
|---|---|
| `Authorization` | `Bearer <JWT_TOKEN>` |

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID phuong thuc thanh toan can dat lam mac dinh | `pm_002` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Dat phuong thuc thanh toan mac dinh thanh cong.",
  "data": null
}
```

### Response (That bai - 404)

```json
{
  "success": false,
  "code": 404,
  "message": "Phuong thuc thanh toan khong ton tai."
}
```

---

## 5. Xoa Phuong Thuc Thanh Toan

**Endpoint:** `DELETE /api/payments/{id}`

**Mo ta:** Xoa mot phuong thuc thanh toan. Tra ve 200 OK ngay ca khi phuong thuc khong ton tai (idempotent).

### Headers

| Header | Gia tri |
|---|---|
| `Authorization` | `Bearer <JWT_TOKEN>` |

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID phuong thuc thanh toan can xoa | `pm_002` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Xoa phuong thuc thanh toan thanh cong.",
  "data": null
}
```

---

## Bang Loai Phuong Thuc Thanh Toan

| Loai | Hien thi | Mo ta |
|---|---|---|
| `cash` | Tien mat | Thanh toan khi nhan hang |
| `momo` | MoMo | Thu dien tu MoMo |
| `zalo` | ZaloPay | Thu dien tu ZaloPay |
| `card` | The ngan hang | The Visa/MasterCard |

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
