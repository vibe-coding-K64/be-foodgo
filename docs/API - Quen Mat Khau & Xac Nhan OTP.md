# API - Quen Mat Khau va Xac Nhan OTP

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/auth` |
| **Authentication** | Khong can Bearer Token |
| **Content-Type** | `application/json` |

---

## Muc Luc

1. [Gui Ma OTP](#1-gui-ma-otp) - `POST /api/auth/send-otp`
2. [Gui Lai Ma OTP](#2-gui-lai-ma-otp) - `POST /api/auth/resend-otp`
3. [Xac Thuc Ma OTP](#3-xac-thuc-ma-otp) - `POST /api/auth/verify-otp`
4. [Dat Lai Mat Khau](#4-dat-lai-mat-khau) - `POST /api/auth/reset-password`
5. [Quy Trinh Hoan Chinh](#5-quy-trinh-hoan-chinh)
6. [Cau Hinh & Tham So](#6-cau-hinh--tham-so)
7. [Lich Su Thay Doi](#7-lich-su-thay-doi)

---

## 1. Gui Ma OTP

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
| `emailOrPhone` | String | **Co** | Email hoac so dien thoai can khoi phuc mat khau | `nguoidung@gmail.com` hoac `0123456789` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "message": "Ma OTP da duoc gui. Vui long kiem tra email.",
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

### Response (That bai - 429)

```json
{
  "success": false,
  "code": 429,
  "message": "Vui long cho 45 giay truoc khi gui lai OTP."
}
```

> **Luu y:** `otpCode` chi hien thi trong moi truong dev/demo. Trong production, ma OTP se gui truc tiep den email.

---

## 2. Gui Lai Ma OTP

**Endpoint:** `POST /api/auth/resend-otp`

**Mo ta:** Gui lai ma OTP 6 chu so. Chi cho phep gui lai sau 60 giay tu lan gui truoc. Neu chua het cooldown, tra ve loi 429.

### Request Body

```json
{
  "emailOrPhone": "nguoidung@gmail.com"
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `emailOrPhone` | String | **Co** | Email hoac so dien thoai can gui lai OTP | `nguoidung@gmail.com` hoac `0123456789` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "message": "Ma OTP moi da duoc gui.",
  "data": {
    "emailOrPhone": "nguoidung@gmail.com",
    "message": "Ma OTP da duoc gui. Vui long kiem tra email/so dien thoai.",
    "otpCode": "654321",
    "expiresInSeconds": 300
  }
}
```

### Response (That bai - 429)

```json
{
  "success": false,
  "code": 429,
  "message": "Vui long cho 45 giay truoc khi gui lai OTP."
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

---

## 3. Xac Thuc Ma OTP

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
  "message": "Xac thuc OTP thanh cong. Ban co the dat lai mat khau.",
  "data": {
    "tempToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9SRU4ifQ...",
    "tokenType": "Bearer",
    "expiresIn": 300000,
    "expiresAt": "2026-05-26T13:05:00Z"
  }
}
```

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `tempToken` | String | JWT tam thoi dung de dat lai mat khau | `eyJhbGciOiJIUzI1NiJ9...` |
| `tokenType` | String | Loai token (luon la `Bearer`) | `Bearer` |
| `expiresIn` | Long | Thoi gian het han (miliseconds) | `300000` |
| `expiresAt` | String | Thoi gian het han (timestamp ISO 8601) | `2026-05-26T13:05:00Z` |

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

### Error Codes

| HTTP Status | Message | Ly do |
|---|---|---|
| 400 | `Ma OTP khong hop le hoac da het han...` | OTP khong ton tai hoac da bi xoa khoi bo nho |
| 400 | `Ma OTP da het han...` | OTP da qua thoi gian hieu luc (5 phut) |
| 400 | `Ma OTP khong dung...` | Ma OTP khong khop voi ma da gui |
| 400 | `Khong tim thay tai khoan...` | So dien thoai khong lien ket voi tai khoan nao |

---

## 4. Dat Lai Mat Khau

**Endpoint:** `POST /api/auth/reset-password`

**Mo ta:** Dat lai mat khau moi sau khi xac thuc OTP thanh cong. Token tam thoi co hieu luc 5 phut sau khi xac thuc OTP.

### Request Body

```json
{
  "tempToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9SRU4ifQ...",
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

```json
{
  "success": false,
  "code": 400,
  "message": "Tai khoan khong ton tai."
}
```

---

## 5. Quy Trinh Hoan Chinh

```
Nguoi dung yeu cau khoi phuc mat khau
                    |
                    v
           POST /api/auth/send-otp
                    | { emailOrPhone }
                    v
     +-------------+-------------+
     |                          |
  200 OK                   400 / 429
  OTP gui thanh cong       Tai khoan khong ton tai
     |                     hoac dang cho cooldown
     v
  [DEV] In ma OTP ra console
  [PROD] Gui email den nguoi dung
     |
     v
  Nguoi dung nhap ma OTP
     |
     v
           POST /api/auth/verify-otp
                    | { emailOrPhone, otpCode }
                    v
     +-------------+-------------+
     |                          |
  200 OK                   400
  Tra ve tempToken         OTP sai/het han
  (hieu luc 5 phut)
     |
     v
  Nguoi dung nhap mat khau moi
     |
     v
         POST /api/auth/reset-password
                    | { tempToken, newPassword }
                    v
     +-------------+-------------+
     |                          |
  200 OK                   400
  Mat khau duoc cap nhat  Token khong hop le
  Thanh cong!            hoac da het han
```

### Chi Tiet Cac Buoc

| Buoc | Endpoint | Moi Truong | Ghi Chu |
|---|---|---|---|
| 1 | `POST /api/auth/send-otp` | Dev + Prod | Co gioi han gui lai: 1 lan / 60 giay |
| 2 | `POST /api/auth/resend-otp` | Dev + Prod | Same logic voi send-otp, chi khac message |
| 3 | `POST /api/auth/verify-otp` | Dev + Prod | Tra ve tempToken JWT hieu luc 5 phut |
| 4 | `POST /api/auth/reset-password` | Dev + Prod | Mat khau moi duoc ma hoa BCrypt |

---

## 6. Cau Hinh & Tham So

### 6.1 Tham So OTP (Backend - AuthService.java)

| Tham so | Gia tri | Mo ta |
|---|---|---|
| `OTP_LENGTH` | `6` | Do dai ma OTP (6 chu so) |
| `OTP_TTL_SECONDS` | `300L` | Thoi gian hieu luc OTP: 5 phut |
| `TEMP_TOKEN_TTL_MS` | `300000L` | Thoi gian hieu luc token tam thoi: 5 phut |
| `RESEND_COOLDOWN_SECONDS` | `60L` | Thoi gian cho giua 2 lan gui OTP: 60 giay |

### 6.2 Cau Hinh Email (Backend - EmailService.java)

| Thuoc tinh | Gia tri mac dinh | Mo ta |
|---|---|---|
| `spring.mail.username` | `noreply@foodgo.com` | Dia chi nguoi gui email |
| `app.email.enabled` | `true` | Bat/tat gui email |

**Cau hinh trong `application.properties`:**

```properties
spring.mail.username=noreply@foodgo.com
app.email.enabled=true
```

### 6.3 Email Template

Khi gui OTP thanh cong, he thong gui email voi noi dung HTML:

**Tieu de:** `Ma xac thuc dat lai mat khau - FoodGo`

**Noi dung:** Giao dien HTML voi:
- Logo "FoodGo" noi bat
- Tieu de "Dat lai mat khau"
- Hoi chào nguoi dung
- Khung hien thi ma OTP 6 chu so voi style noi bat (mau cam #FF6B35)
- Thong bao het han sau 5 phut
- Footer voi thong tin ban quyen

### 6.4 Luu Tru OTP (In-Memory)

| Kho | Mo ta |
|---|---|
| `otpStore` | `ConcurrentHashMap<String, OtpEntry>` - Luu ma OTP theo email/phone |
| `lastOtpSentAt` | `ConcurrentHashMap<String, Long>` - Luu thoi diem gui OTP cuoi |

> **Luu y quan trong:** OTP duoc luu tru in-memory (RAM). Dieu nay co nghia la:
> - OTP se bi mat khi restart server
> - Phu hop cho dev/demo; can thay the bang Redis cho production

### 6.5 Du Lieu Nguoi Dung (Firestore)

| Collection | Document ID | Thuoc tinh lien quan |
|---|---|---|
| `users` | `user_xxx` | Thong tin tai khoan nguoi dung |

---

## 7. Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-06-23 | Phien ban dau tien, tai lieu chi tiet cho 2 API Quen Mat Khau va Xac Nhan OTP |

---

## File Nguon Tham Khao

| File | Duong dan | Mo ta |
|---|---|---|
| AuthController.java | `be-foodgo/src/main/java/com/example/be_foodgo/controller/AuthController.java` | Controller chua cac endpoint |
| AuthService.java | `be-foodgo/src/main/java/com/example/be_foodgo/service/AuthService.java` | Logic xu ly OTP va reset password |
| EmailService.java | `be-foodgo/src/main/java/com/example/be_foodgo/service/EmailService.java` | Gui email xac thuc |
| OtpSendRequest.java | `be-foodgo/src/main/java/com/example/be_foodgo/dto/OtpSendRequest.java` | DTO request gui OTP |
| OtpVerifyRequest.java | `be-foodgo/src/main/java/com/example/be_foodgo/dto/OtpVerifyRequest.java` | DTO request xac thuc OTP |
| ResetPasswordRequest.java | `be-foodgo/src/main/java/com/example/be_foodgo/dto/ResetPasswordRequest.java` | DTO request dat lai mat khau |
| OtpSendResponse.java | `be-foodgo/src/main/java/com/example/be_foodgo/dto/OtpSendResponse.java` | DTO response gui OTP |
| OtpVerifyResponse.java | `be-foodgo/src/main/java/com/example/be_foodgo/dto/OtpVerifyResponse.java` | DTO response xac thuc OTP |
