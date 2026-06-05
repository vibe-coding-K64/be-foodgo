# Hướng dẫn API Đổi Mật Khẩu cho Frontend

## 1. Tổng quan

API cho phép người dùng đã đăng nhập chủ động thay đổi mật khẩu của tài khoản. Yêu cầu người dùng nhập mật khẩu cũ để xác thực trước khi đặt mật khẩu mới.

---

## 2. Thông tin Endpoint

| Thuộc tính | Giá trị |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/customers/password` |
| **Content-Type** | `application/json` |
| **Auth** | Bearer Token (JWT) |
| **Controller** | `ProfileController` |
| **Service** | `ProfileService.changePassword` |

---

## 3. Request

### Headers

| Header | Giá trị | Bắt buộc |
|---|---|---|
| `Authorization` | `Bearer <access_token>` | **Có** |
| `Content-Type` | `application/json` | **Có** |

### Body

```json
{
  "oldPassword": "matkhaucu123",
  "newPassword": "matkhaumoi456"
}
```

### Mô tả các trường

| Trường | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| `oldPassword` | String | **Có** | Mật khẩu hiện tại của tài khoản |
| `newPassword` | String | **Có** | Mật khẩu mới (tối thiểu **6 ký tự**) |

### Validation phía backend

- `oldPassword`: không được rỗng
- `newPassword`: không được rỗng, tối thiểu 6 ký tự
- Nếu validation thất bại → HTTP `400` với message lỗi cụ thể

---

## 4. Response

### Thành công

**HTTP Status:** `200 OK`

```json
{
  "success": true,
  "message": "Doi mat khau thanh cong.",
  "data": null
}
```

### Thất bại

**HTTP Status:** `400` — Mật khẩu cũ không đúng

```json
{
  "success": false,
  "code": 400,
  "message": "Mật khẩu cũ không đúng."
}
```

**HTTP Status:** `400` — Validation thất bại

```json
{
  "success": false,
  "code": 400,
  "message": "Mat khau moi phai co it nhat 6 ky tu"
}
```

**HTTP Status:** `401` — Chưa đăng nhập / Token hết hạn

```json
{
  "success": false,
  "code": 401,
  "message": "Chua xac thuc. Vui long dang nhap de tiep tuc."
}
```

**HTTP Status:** `404` — Không tìm thấy tài khoản

```json
{
  "success": false,
  "code": 404,
  "message": "Khong tim thay tai khoan voi ID: ..."
}
```

**HTTP Status:** `500` — Lỗi hệ thống

```json
{
  "success": false,
  "code": 500,
  "message": "Da xay ra loi khong mong muon. Vui long thu lai sau."
}
```

---

## 5. Luồng xử lý phía Backend

```
1. Trích xuất userId từ JWT token trong Authorization header
2. Tìm user trong Firestore theo userId
3. BCrypt verify: oldPassword == hashedPassword (trong Firestore)
   → Sai → throw IllegalArgumentException → HTTP 400
4. BCrypt encode newPassword → hashedPassword
5. Cập nhật trường password trong Firestore (users/{userId})
6. Trả về HTTP 200
```

---

## 6. Ví dụ mã nguồn

### JavaScript / TypeScript

```javascript
const changePassword = async (accessToken, oldPassword, newPassword) => {
  const response = await fetch('https://api.foodgo.com/api/customers/password', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      oldPassword,
      newPassword
    })
  });

  const data = await response.json();

  if (response.ok) {
    console.log('Đổi mật khẩu thành công');
    // Có thể tự động logout hoặc yêu cầu đăng nhập lại
    // Access token vẫn còn hiệu lực nên không bắt buộc phải logout
  } else {
    console.error('Lỗi:', data.message);
  }
};
```

### Flutter / Dart

```dart
Future<void> changePassword(String accessToken, String oldPassword, String newPassword) async {
  final response = await http.put(
    Uri.parse('https://api.foodgo.com/api/customers/password'),
    headers: {
      'Authorization': 'Bearer $accessToken',
      'Content-Type': 'application/json',
    },
    body: jsonEncode({
      'oldPassword': oldPassword,
      'newPassword': newPassword,
    }),
  );

  final data = jsonDecode(response.body);

  if (response.statusCode == 200) {
    // Thành công
  } else {
    // Xử lý lỗi: data['message']
  }
}
```

---

## 7. Lưu ý cho Frontend

### Mật khẩu mới phải thỏa mãn

- Tối thiểu **6 ký tự**
- Nên validate phía FE trước khi gọi API (hiển thị lỗi ngay thay vì đợi response)

### UX khuyến nghị

- Hiển thị mật khẩu cũ / mới dưới dạng `obscured` (••••) và thêm nút **"hiện/ẩn"**
- Sau khi đổi thành công, FE có thể:
  - Hiển thị toast/thông báo "Đổi mật khẩu thành công"
  - Tùy chọn: tự động đăng xuất và yêu cầu đăng nhập lại (recommend)
- Không nên cho dùng mật khẩu mới trùng với mật khẩu cũ (validate phía FE)

### Lấy Access Token

- Sau khi đăng nhập thành công, BE trả về `AuthResponse` chứa `accessToken` và `refreshToken`
- Lưu trữ token (ví dụ: SecureStorage, AsyncStorage) và attach vào header `Authorization: Bearer <token>`

---

## 8. Các API Auth liên quan

| API | Method | URL | Mô tả |
|---|---|---|---|
| Đổi mật khẩu (chủ động) | `PUT` | `/api/customers/password` | User đã login, biết mk cũ |
| Quên mật khẩu (bước 1) | `POST` | `/api/auth/send-otp` | Gửi OTP về email/phone |
| Quên mật khẩu (bước 2) | `POST` | `/api/auth/verify-otp` | Xác thực OTP → tempToken |
| Quên mật khẩu (bước 3) | `POST` | `/api/auth/reset-password` | Đặt mk mới bằng tempToken |
| Đăng nhập | `POST` | `/api/auth/login` | Nhận access token |
| Đăng ký | `POST` | `/api/auth/register/verify-email` | Gửi OTP đăng ký |
| Hoàn tất đăng ký | `POST` | `/api/auth/register/complete` | Tạo tài khoản |
