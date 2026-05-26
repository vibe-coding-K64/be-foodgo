# API - Dia Chi (Address)

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/addresses` |
| **Authentication** | Khong can Bearer Token (userId truyen qua body/query) |
| **Content-Type** | `application/json` |

---

## 1. Them Dia Chi Moi

**Endpoint:** `POST /api/addresses`

**Mo ta:** Them mot dia chi giao hang moi cho khach hang. Neu request gui isDefault = true, he thong se tu dong bo mac dinh cac dia chi cu. Dia chi duoc luu vao sub-collection `customer_profiles/{userId}/addresses`.

### Request Body

```json
{
  "userId": "user_001",
  "name": "Nha rieng",
  "address": "Ky tuc xa UTC2, Quan 9, TP.HCM",
  "receiverName": "Khoi",
  "receiverPhone": "0123456789",
  "lat": 10.8455,
  "lng": 106.7939,
  "isDefault": true
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |
| `name` | String | **Co** | Nhan dia chi (VD: 'Nha rieng', 'Cong ty') | `Nha rieng` |
| `address` | String | **Co** | Dia chi chi tiet day du | `123 Nguyen Huu, Quan 9, TP.HCM` |
| `receiverName` | String | **Co** | Ho ten nguoi nhan hang | `Khoi` |
| `receiverPhone` | String | **Co** | So dien thoai nguoi nhan (bat dau bang 0, 10-11 chu so) | `0123456789` |
| `lat` | Double | **Co** | Toa do vi do (latitude) | `10.8455` |
| `lng` | Double | **Co** | Toa do kin do (longitude) | `106.7939` |
| `isDefault` | Boolean | Khong | Co phai dia chi mac dinh khong. Neu true, cac dia chi cu se bi bo mac dinh. | `true` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Da them dia chi thanh cong.",
  "data": {
    "id": "addr_003",
    "userId": "user_001",
    "name": "Nha rieng",
    "address": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "receiverName": "Khoi",
    "receiverPhone": "0123456789",
    "lat": 10.8455,
    "lng": 106.7939,
    "isDefault": true,
    "createdAt": "2026-05-26T10:00:00Z",
    "updatedAt": "2026-05-26T10:00:00Z",
    "deletedAt": null
  }
}
```

### Response (That bai - 400)

```json
{
  "success": false,
  "code": 400,
  "message": "Du lieu dau vao khong hop le"
}
```

---

## 2. Lay Danh Sach Dia Chi

**Endpoint:** `GET /api/addresses`

**Mo ta:** Lay tat ca dia chi giao hang cua khach hang tu sub-collection `customer_profiles/{userId}/addresses`.

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Da lay danh sach dia chi thanh cong.",
  "data": [
    {
      "id": "addr_001",
      "userId": "user_001",
      "name": "Nha rieng",
      "address": "Ky tuc xa UTC2, Quan 9, TP.HCM",
      "receiverName": "Khoi",
      "receiverPhone": "0123456789",
      "lat": 10.8455,
      "lng": 106.7939,
      "isDefault": true,
      "createdAt": "2026-05-26T10:00:00Z",
      "updatedAt": "2026-05-26T10:00:00Z",
      "deletedAt": null
    },
    {
      "id": "addr_002",
      "userId": "user_001",
      "name": "Truong hoc",
      "address": "Truong Dai hoc Giao thong Van tai, TP. Thu Duc",
      "receiverName": "Khoi",
      "receiverPhone": "0123456789",
      "lat": 10.8490,
      "lng": 106.7890,
      "isDefault": false,
      "createdAt": "2026-05-26T10:00:00Z",
      "updatedAt": "2026-05-26T10:00:00Z",
      "deletedAt": null
    }
  ]
}
```

---

## 3. Lay Thong Tin Mot Dia Chi

**Endpoint:** `GET /api/addresses/{id}`

**Mo ta:** Lay thong tin chi tiet cua mot dia chi giao hang cu the theo addressId.

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID dia chi can lay | `addr_001` |

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Da lay thong tin dia chi thanh cong.",
  "data": {
    "id": "addr_001",
    "userId": "user_001",
    "name": "Nha rieng",
    "address": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "receiverName": "Khoi",
    "receiverPhone": "0123456789",
    "lat": 10.8455,
    "lng": 106.7939,
    "isDefault": true,
    "createdAt": "2026-05-26T10:00:00Z",
    "updatedAt": "2026-05-26T10:00:00Z",
    "deletedAt": null
  }
}
```

### Response (That bai - 404)

```json
{
  "success": false,
  "code": 404,
  "message": "Dia chi khong ton tai"
}
```

---

## 4. Cap Nhat Dia Chi

**Endpoint:** `PUT /api/addresses/{id}`

**Mo ta:** Cap nhat thong tin dia chi giao hang cua khach hang. Neu isDefault = true, he thong se tu dong bo mac dinh cac dia chi cu.

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID dia chi can cap nhat | `addr_001` |

### Request Body

```json
{
  "userId": "user_001",
  "name": "Nha rieng - Cap nhat",
  "address": "Ky tuc xa UTC2, Quan 9, TP.HCM - Cap nhat",
  "receiverName": "Khoi Updated",
  "receiverPhone": "0123456789",
  "lat": 10.8455,
  "lng": 106.7939,
  "isDefault": false
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |
| `name` | String | **Co** | Nhan dia chi moi | `Nha rieng - Cap nhat` |
| `address` | String | **Co** | Dia chi chi tiet moi | `123 Nguyen Huu, Quan 9, TP.HCM` |
| `receiverName` | String | **Co** | Ho ten nguoi nhan moi | `Khoi Updated` |
| `receiverPhone` | String | **Co** | So dien thoai nguoi nhan | `0123456789` |
| `lat` | Double | **Co** | Toa do vi do | `10.8455` |
| `lng` | Double | **Co** | Toa do kin do | `106.7939` |
| `isDefault` | Boolean | Khong | Dat lam dia chi mac dinh | `false` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Da cap nhat dia chi thanh cong.",
  "data": {
    "id": "addr_001",
    "userId": "user_001",
    "name": "Nha rieng - Cap nhat",
    "address": "Ky tuc xa UTC2, Quan 9, TP.HCM - Cap nhat",
    "receiverName": "Khoi Updated",
    "receiverPhone": "0123456789",
    "lat": 10.8455,
    "lng": 106.7939,
    "isDefault": false,
    "createdAt": "2026-05-26T10:00:00Z",
    "updatedAt": "2026-05-26T11:00:00Z",
    "deletedAt": null
  }
}
```

---

## 5. Dat Dia Chi Lam Mac Dinh

**Endpoint:** `PUT /api/addresses/{id}/default`

**Mo ta:** Dat mot dia chi giao hang lam dia chi mac dinh cho khach hang. He thong se quet tat ca dia chi cua nguoi dung, bo flag isDefault cua cac dia chi cu, sau do dat flag isDefault = true cho dia chi duoc yeu cau.

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID dia chi can dat lam mac dinh | `addr_002` |

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Da dat dia chi lam mac dinh thanh cong.",
  "data": null
}
```

### Response (That bai - 404)

```json
{
  "success": false,
  "code": 404,
  "message": "Dia chi khong ton tai"
}
```

---

## 6. Xoa Dia Chi

**Endpoint:** `DELETE /api/addresses/{id}`

**Mo ta:** Xoa mot dia chi giao hang cua khach hang. Phuong thuc nay la idempotent - tra ve thanh cong ke ca khi dia chi khong ton tai.

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `id` | String | ID dia chi can xoa | `addr_002` |

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Da xoa dia chi thanh cong.",
  "data": null
}
```

---

## Bieu Do Quy Trinh Them Dia Chi

```
Nguoi dung mo man hinh AddressFormView (che do Them moi)
       |
       v
  Nguoi dung nhap thong tin dia chi
  - Ten nguoi nhan
  - So dien thoai
  - So nha / Ten duong
  - Quan / Huyện
  - Tinh / Thanh pho
       |
       v
  (Tuy chon) Bam "Chon vi tri tren ban do"
  -> Mo MapPickerView de chon toa do
       |
       v
  Chon loai dia chi (Nha / Van phong / Khac)
  Toggle "Dat lam dia chi mac dinh"
       |
       v
  Bam "Luu dia chi"
       |
       v
  POST /api/addresses
       |
       v
  +----+----+
  |         |
  OK       400 (Validation error)
  |
  v
  Quay ve AddressManagementView
  (Danh sach tu dong cap nhat qua StreamBuilder)
```

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
