# API - Ho So (Profile)

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/customers` |
| **Authentication** | **Bearer Token (JWT) bat buoc** |
| **Content-Type** | `application/json` |

> Tat ca cac endpoint trong phan nay deu yeu cau header `Authorization: Bearer <token>`.

---

## 1. Cap Nhat Thong Tin Ho So

**Endpoint:** `PUT /api/customers/profile`

**Mo ta:** Cap nhat ho va ten va anh dai dien cua tai khoan dang nhap. Khong cho phep thay doi email hoac so dien thoai tai day.

### Headers

| Header | Gia tri |
|---|---|
| `Authorization` | `Bearer <JWT_TOKEN>` |

### Request Body

```json
{
  "fullName": "Nguyen Van A Updated",
  "avatarUrl": "https://example.com/avatar/new_avatar.jpg"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `fullName` | String | **Co** | Ho va ten day du (toi da 100 ky tu) | `Nguyen Van A Updated` |
| `avatarUrl` | String | Khong | Duong dan URL anh dai dien | `https://example.com/avatar/new_avatar.jpg` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "message": "Cap nhat ho so thanh cong.",
  "data": {
    "id": "user_001",
    "email": "nguoidung@gmail.com",
    "fullName": "Nguyen Van A Updated",
    "phoneNumber": "0123456789",
    "photoUrl": "https://example.com/avatar/new_avatar.jpg",
    "roles": [1]
  }
}
```

### Response (That bai - 401 - Chua xac thuc)

```json
{
  "success": false,
  "code": 401,
  "message": "Chua xac thuc. Vui long dang nhap de tiep tuc."
}
```

### Response (That bai - 404 - Khong tim thay tai khoan)

```json
{
  "success": false,
  "code": 404,
  "message": "Khong tim thay tai khoan voi ID: user_001"
}
```

---

## 2. Doi Mat Khau Chu Dong

**Endpoint:** `PUT /api/customers/password`

**Mo ta:** Doi mat khau cu sang mat khau moi. Yeu cau nhap dung mat khau cu de xac nhan.

### Headers

| Header | Gia tri |
|---|---|
| `Authorization` | `Bearer <JWT_TOKEN>` |

### Request Body

```json
{
  "oldPassword": "matkhaucu123",
  "newPassword": "matkhaumoi123"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `oldPassword` | String | **Co** | Mat khau cu hien tai | `matkhaucu123` |
| `newPassword` | String | **Co** | Mat khau moi (it nhat 6 ky tu) | `matkhaumoi123` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Doi mat khau thanh cong.",
  "data": null
}
```

### Response (That bai - 400 - Mat khau cu khong dung)

```json
{
  "success": false,
  "code": 400,
  "message": "Mat khau cu khong dung."
}
```

### Response (That bai - 401 - Chua xac thuc)

```json
{
  "success": false,
  "code": 401,
  "message": "Chua xac thuc. Vui long dang nhap de tiep tuc."
}
```

---

## Bieu Do Quy Trinh Cap Nhat Ho So

```
Nguoi dung mo man hinh Chinh Sua Ho So (EditProfileView)
       |
       v
  Bam "Luu thay doi"
       |
       v
  +----+----+
  |         |
  |    Step 1: Hien thi Dialog nhap mat khau cu
  |         |
  |         v
  |    Nguoi dung nhap mat khau cu
  |         |
  |         v
  |    Bam "Tiep tuc"
  |         |
  +----+----+
       |
       v
  +----+----+
  |         |
  |    Step 2: Hien thi Dialog nhap OTP 6 so
  |         |
  |         v
  |    Ma OTP gui den so dien thoai cua nguoi dung
  |         |
  |         v
  |    Nguoi dung nhap OTP
  |         |
  |         v
  |    Bam "Xac nhan"
  |         |
  +----+----+
       |
       v
  PUT /api/customers/profile
  (Authorization: Bearer <token>)
       |
       v
  +----+----+
  |         |
  OK       400/401/404
  |
  v
  Hien thi SnackBar thanh cong
  Quay ve ProfileView
```

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
