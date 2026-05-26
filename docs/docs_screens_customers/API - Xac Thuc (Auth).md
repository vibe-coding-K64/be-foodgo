# API - Xác Thực (Auth)

## Thông Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/auth` |
| **Authentication** | Khong can Bearer Token |
| **Content-Type** | `application/json` |

---

## 1. Dang Ky Tai Khoan

**Endpoint:** `POST /api/auth/register`

**Mo ta:** Tao tai khoan khach hang moi voi email, mat khau (ma hoa BCrypt), ho ten va so dien thoai. Mac dinh roles = [1] (Khach hang).

### Request Body

```json
{
  "email": "nguoidung@gmail.com",
  "password": "password123",
  "fullName": "Nguyen Van A",
  "phoneNumber": "0123456789"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `email` | String | **Co** | Dia chi email (dinh dang email hop le) | `nguoidung@gmail.com` |
| `password` | String | **Co** | Mat khau (it nhat 6 ky tu) | `password123` |
| `fullName` | String | **Co** | Ho va ten day du | `Nguyen Van A` |
| `phoneNumber` | String | **Co** | So dien thoai (bat dau bang 0, 10-11 chu so) | `0123456789` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "message": "Dang ky thanh cong",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInJvbGVzIjpbMV0sImV4cCI6NzIwMDAwMDAwMH0...",
    "tokenType": "Bearer",
    "expiresIn": 720000000,
    "user": {
      "id": "user_001",
      "email": "nguoidung@gmail.com",
      "fullName": "Nguyen Van A",
      "phoneNumber": "0123456789",
      "photoUrl": null,
      "roles": [1]
    }
  }
}
```

### Response (That bai - 400)

```json
{
  "success": false,
  "code": 400,
  "message": "Email da ton tai trong he thong. Vui long su dung email khac."
}
```

### Error Codes

| HTTP Status | Message | Ly do |
|---|---|---|
| 400 | `Email da ton tai trong he thong...` | Email da duoc dang ky |
| 400 | `So dien thoai da duoc su dung...` | So dien thoai da ton tai |

---

## 2. Dang Nhap

**Endpoint:** `POST /api/auth/login`

**Mo ta:** Dang nhap bang email va mat khau. Neu thanh cong, tra ve JWT token chua userId.

### Request Body

```json
{
  "email": "nguoidung@gmail.com",
  "password": "password123"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `email` | String | **Co** | Dia chi email | `nguoidung@gmail.com` |
| `password` | String | **Co** | Mat khau dang nhap | `password123` |

### Response (Thanh cong - 200)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInJvbGVzIjpbMV0sImV4cCI6NzIwMDAwMDAwMH0...",
  "tokenType": "Bearer",
  "expiresIn": 720000000,
  "user": {
    "id": "user_001",
    "email": "nguoidung@gmail.com",
    "fullName": "Nguyen Van A",
    "phoneNumber": "0123456789",
    "photoUrl": null,
    "roles": [1]
  }
}
```

### Response (That bai - 400)

```json
{
  "success": false,
  "code": 400,
  "message": "Email hoac mat khau khong dung."
}
```

---

## 3. Gui Ma OTP

**Endpoint:** `POST /api/auth/send-otp`

**Mo ta:** Gui ma OTP 6 chu so den email hoac so dien thoai de khoi phuc mat khau. Ma OTP co hieu luc 5 phut. Trong moi truong dev/demo, ma OTP se in ra console.

### Request Body

```json
{
  "emailOrPhone": "nguoidung@gmail.com"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `emailOrPhone` | String | **Co** | Email hoac so dien thoai can khoi phuc | `nguoidung@gmail.com` hoac `0123456789` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "message": "Ma OTP da duoc gui. Vui long kiem tra email/so dien thoai.",
  "data": {
    "emailOrPhone": "nguoidung@gmail.com",
    "message": "Ma OTP da duoc gui. Vui long kiem tra email/so dien thoai.",
    "otpCode": "123456",
    "expiresInSeconds": 300
  }
}
```

### Response (That bai - 400)

```json
{
  "success": false,
  "code": 400,
  "message": "Khong tim thay tai khoan voi email hoac so dien thoai nay."
}
```

> **Luu y:** `otpCode` chi hien thi trong moi truong dev/demo. Trong production, ma OTP se gui truc tiep den email/so dien thoai.

---

## 4. Xac Thuc Ma OTP

**Endpoint:** `POST /api/auth/verify-otp`

**Mo ta:** Xac thuc ma OTP nhan duoc. Neu dung, tra ve token tam thoi (hieu luc 5 phut) de su dung cho reset-password.

### Request Body

```json
{
  "emailOrPhone": "nguoidung@gmail.com",
  "otpCode": "123456"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `emailOrPhone` | String | **Co** | Email hoac so dien thoai da nhan ma OTP | `nguoidung@gmail.com` |
| `otpCode` | String | **Co** | Ma OTP 6 chu so | `123456` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "message": "Xac thuc OTP thanh cong.",
  "data": {
    "tempToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9LRU4ifQ...",
    "tokenType": "Bearer",
    "expiresIn": 300000,
    "expiresAt": "2026-05-26T13:05:00Z"
  }
}
```

### Response (That bai - 400)

```json
{
  "success": false,
  "code": 400,
  "message": "Ma OTP khong hop le hoac da het han. Vui long gui lai ma OTP."
}
```

```json
{
  "success": false,
  "code": 400,
  "message": "Ma OTP khong dung. Vui long thu lai."
}
```

---

## 5. Dat Lai Mat Khau

**Endpoint:** `POST /api/auth/reset-password`

**Mo ta:** Dat lai mat khau moi sau khi xac thuc OTP thanh cong. Token tam thoi co hieu luc 5 phut sau khi xac thuc OTP.

### Request Body

```json
{
  "tempToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9LRU4ifQ...",
  "newPassword": "newpassword123"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `tempToken` | String | **Co** | Token tam thoi nhan duoc sau khi xac thuc OTP | `eyJhbGciOiJIUzI1NiJ9...` |
| `newPassword` | String | **Co** | Mat khau moi (it nhat 6 ky tu) | `newpassword123` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Dat lai mat khau thanh cong.",
  "data": null
}
```

### Response (That bai - 400)

```json
{
  "success": false,
  "code": 400,
  "message": "Token khong hop le hoac da het han. Vui long gui lai ma OTP."
}
```

---

## 6. Dang Ky Tai Khoan Nguoi Ban

**Endpoint:** `POST /api/auth/register-merchant`

**Mo ta:** Tao tai khoan nguoi ban moi voi roles = [3]. Su dung de tich hop voi quy trinh dang ky nguoi ban cua he thong.

### Request Body

```json
{
  "email": "merchant@gmail.com",
  "password": "password123",
  "fullName": "Tran Thi B",
  "phoneNumber": "0987654321"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `email` | String | **Co** | Dia chi email | `merchant@gmail.com` |
| `password` | String | **Co** | Mat khau | `password123` |
| `fullName` | String | **Co** | Ho va ten | `Tran Thi B` |
| `phoneNumber` | String | **Co** | So dien thoai | `0987654321` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "message": "Dang ky tai khoan nguoi ban thanh cong",
  "data": {
    "uid": "user_002",
    "roles": [3]
  }
}
```

---

## 7. Kiem Tra Quyen Nguoi Ban

**Endpoint:** `GET /api/auth/check-merchant`

**Mo ta:** Kiem tra xem tai khoan co quyen nguoi ban (role = 3) hay khong.

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `uid` | String | **Co** | ID tai khoan nguoi dung | `user_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "message": "Kiem tra quyen thanh cong.",
  "data": {
    "isMerchant": true,
    "storeId": null
  }
}
```

---

## Bieu Do Quy Trinh Xac Thuc

```
Dang nhap (Dang ky)
       |
       v
  POST /api/auth/register  (hoac /api/auth/login)
       |
       v
  +----+----+
  |         |
  OK       400 (Email/SDT da ton tai)
  |
  v
Tra ve AuthResponse:
  - token: JWT token (720000000 ms)
  - user: UserResponse (id, email, fullName, phoneNumber, roles)

========================================

Quen mat khau
       |
       v
  POST /api/auth/send-otp
       | (emailOrPhone)
       v
  +----+----+
  |         |
  OK       400 (Tai khoan khong ton tai)
  |
  v
  Ma OTP gui den email/SDT (in ra console dev)
       |
       v
  POST /api/auth/verify-otp
       | (emailOrPhone, otpCode)
       v
  +----+----+
  |         |
  OK       400 (OTP sai/het han)
  |
  v
  Tra ve OtpVerifyResponse:
  - tempToken: JWT tam thoi (300000 ms)
       |
       v
  POST /api/auth/reset-password
       | (tempToken, newPassword)
       v
  +----+----+
  |         |
  OK       400 (Token het han)
  |
  v
  Mat khau duoc cap nhat
```

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
