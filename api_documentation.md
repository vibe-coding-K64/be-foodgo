# Tài liệu API - API Thêm món vào giỏ hàng

## Mục lục

1. [Thông tin chung](#1-thông-tin-chung)
2. [Chi tiết API](#2-chi-tiết-api)
3. [Cấu trúc dữ liệu](#3-cấu-trúc-dữ-liệu)
4. [Mã lỗi trả về](#4-mã-lỗi-trả-về)
5. [Ví dụ](#5-ví-dụ)

---

## 1. Thông tin chung

| Thuộc tính          | Giá trị                                    |
| ------------------- | ------------------------------------------ |
| **Endpoint**        | `/api/cart/add`                            |
| **Method**          | `POST`                                     |
| **Mô tả**         | Thêm một món ăn vào giỏ hàng của khách hàng |
| **Phân hệ**         | Khách hàng                                 |
| **Mức độ truy cập**  | Công khai (chưa có xác thực JWT trong phiên bản này) |

### Các quy tắc nghiệp vụ (Business Rules)

1. **Kiểm tra tồn kho**: Hệ thống kiểm tra trường `isOutOfStock` của sản phẩm trong collection `products`. Nếu `true`, trả về lỗi 400.
2. **Quy tắc một cửa hàng**: Nếu giỏ hàng đã có món từ cửa hàng khác (khác `storeId`), trả về lỗi 400 yêu cầu xác nhận xóa giỏ hàng cũ.
3. **Tính toán giá phía Server**: Giá tổng = (basePrice + giáSize + tổngGiáTopping) * sốLượng. Giá từ phía client KHÔNG được sử dụng.

---

## 2. Chi tiết API

### POST /api/cart/add

**Mô tả**: Thêm một món ăn vào giỏ hàng của khách hàng.

**Request Headers**:

| Header            | Kiểu   | Bắt buộc | Mô tả                     |
| ----------------- | ------ | -------- | ------------------------- |
| `Content-Type`    | String | Có       | `application/json`        |

**Request Body** (JSON):

```json
{
  "userId": "user_001",
  "storeId": "store_001",
  "foodId": "prod_001",
  "size": "M",
  "toppings": [
    { "name": "Trân châu", "price": 5000.0 },
    { "name": "Thạch", "price": 3000.0 }
  ],
  "note": "Không thêm hành",
  "quantity": 2
}
```

**Các trường bắt buộc**: `userId`, `storeId`, `foodId`, `quantity`
**Các trường tùy chọn**: `size`, `toppings`, `note`

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Đã thêm món vào giỏ hàng thành công.",
  "data": {
    "id": "cart_item_auto_generated_id",
    "storeId": "store_001",
    "foodId": "prod_001",
    "name": "Cơm tấm sườn bì chả",
    "price": 113000.0,
    "quantity": 2,
    "size": "M",
    "sizePrice": 0.0,
    "toppings": [
      { "name": "Trân châu", "price": 5000.0 },
      { "name": "Thạch", "price": 3000.0 }
    ],
    "note": "Không thêm hành",
    "imageUrl": "https://images.unsplash.com/photo-xxx",
    "createdAt": "2026-05-22T15:30:00Z",
    "updatedAt": "2026-05-22T15:30:00Z"
  },
  "timestamp": "2026-05-22T15:30:00Z"
}
```

**Giá trị `price` trong response** được tính như sau:

```
đơnGiá = basePrice (từ products) + giáSize + tổngGiáTopping
tổngGiá = đơnGiá * sốLượng
```

Ví dụ: basePrice = 45000, size M = 0, toppings = 5000 + 3000 = 8000, sốLượng = 2
=> đơnGiá = 45000 + 0 + 8000 = 53000
=> price = 53000 * 2 = 106000

---

## 3. Cấu trúc dữ liệu

### 3.1. CartRequest (Request Body)

| Thuộc tính  | Kiểu                      | Bắt buộc | Mô tả                                         |
| ----------- | ------------------------- | -------- | --------------------------------------------- |
| `userId`    | String                    | Có       | ID người dùng khách hàng                       |
| `storeId`   | String                    | Có       | ID cửa hàng chứa món ăn                        |
| `foodId`    | String                    | Có       | ID sản phẩm (món ăn)                           |
| `size`      | String (nullable)         | Không    | Kích thước đã chọn (VD: "M", "L"). Có thể null |
| `toppings`  | List<ToppingOption>       | Không    | Danh sách topping đã chọn. Có thể null hoặc rỗng |
| `note`      | String (nullable)         | Không    | Ghi chú cho cửa hàng. Có thể null             |
| `quantity`  | Integer                   | Có       | Số lượng, phải lớn hơn 0                       |

**ToppingOption**:

| Thuộc tính | Kiểu   | Mô tả                  |
| ---------- | ------ | ---------------------- |
| `name`     | String | Tên topping            |
| `price`    | Double | Giá topping (VND)     |

### 3.2. CartItem (Response Data)

| Thuộc tính  | Kiểu                      | Mô tả                                         |
| ----------- | ------------------------- | -------------------------------------------- |
| `id`        | String                    | ID document trong Firestore (auto generated) |
| `storeId`   | String                    | ID cửa hàng chứa món ăn                       |
| `foodId`    | String                    | ID sản phẩm                                   |
| `name`      | String                    | Tên món ăn (lấy từ collection `products`)    |
| `price`     | Double                    | Tổng giá (đã nhân số lượng)                  |
| `quantity`  | Integer                   | Số lượng                                      |
| `size`      | String (nullable)         | Kích thước đã chọn                            |
| `sizePrice` | Double                    | Giá thêm của size                            |
| `toppings`  | List<ToppingItem>         | Danh sách topping đã chọn                     |
| `note`      | String (nullable)         | Ghi chú                                       |
| `imageUrl`  | String (nullable)         | URL ảnh món ăn                                |
| `createdAt` | ISO 8601 Timestamp        | Thời điểm tạo                                 |
| `updatedAt` | ISO 8601 Timestamp        | Thời điểm cập nhật gần nhất                   |

### 3.3. ApiResponse (Wrapper)

| Thuộc tính   | Kiểu    | Mô tả                                      |
| ------------ | ------- | ------------------------------------------ |
| `success`    | Boolean | true = thành công, false = thất bại        |
| `statusCode` | Integer | Mã HTTP status code                        |
| `message`    | String  | Thông báo kết quả                          |
| `data`       | Object  | Dữ liệu trả về (null khi thất bại)        |
| `timestamp`  | String  | Thời điểm phản hồi (ISO 8601)             |

---

## 4. Mã lỗi trả về

### 4.1. Lỗi nghiệp vụ (Business Error)

| HTTP Status | errorCode       | Trường hợp                                           | Lỗi trả về (message)                                           |
| ----------- | --------------- | ---------------------------------------------------- | -------------------------------------------------------------- |
| 400         | OUT_OF_STOCK    | Món ăn đang hết hàng                                 | "Món ăn với ID [xxx] đã hết hàng, vui lòng chọn món khác."    |
| 400         | STORE_MISMATCH  | Giỏ hàng đã có món từ cửa hàng khác                  | "Giỏ hàng hiện tại thuộc cửa hàng [xxx]. Món mới thuộc cửa hàng [xxx]. Vui lòng xác nhận xóa giỏ hàng cũ để tiếp tục." |
| 404         | PRODUCT_NOT_FOUND | Sản phẩm không tồn tại trong hệ thống              | "Không tìm thấy sản phẩm với ID [xxx]."                        |
| 500         | SYSTEM_ERROR    | Lỗi hệ thống khi truy vấn Firestore                 | "Lỗi hệ thống: Không thể truy vấn thông tin sản phẩm."         |

### 4.2. Lỗi xác thực đầu vào (Validation Error)

| HTTP Status | Trường hợp                             | Mô tả                                  |
| ----------- | -------------------------------------- | -------------------------------------- |
| 400         | Dữ liệu không hợp lệ                   | Các trường bắt buộc bị trống hoặc sai định dạng |

**Ví dụ lỗi validation**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Dữ liệu không hợp lệ: userId: userId không được để trống, quantity: Số lượng phải lớn hơn 0",
  "data": null,
  "timestamp": "2026-05-22T15:30:00Z"
}
```

### 4.3. Ví dụ các response lỗi nghiệp vụ

**HTTP 400 - Món ăn hết hàng (OUT_OF_STOCK)**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Món ăn với ID [prod_999] đã hết hàng, vui lòng chọn món khác.",
  "data": null,
  "timestamp": "2026-05-22T15:30:00Z"
}
```

**HTTP 400 - Cửa hàng không khớp (STORE_MISMATCH)**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Giỏ hàng hiện tại thuộc cửa hàng [store_001]. Món mới thuộc cửa hàng [store_002]. Vui lòng xác nhận xóa giỏ hàng cũ để tiếp tục.",
  "data": null,
  "timestamp": "2026-05-22T15:30:00Z"
}
```

**HTTP 404 - Sản phẩm không tồn tại (PRODUCT_NOT_FOUND)**:

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Không tìm thấy sản phẩm với ID [prod_xyz].",
  "data": null,
  "timestamp": "2026-05-22T15:30:00Z"
}
```

---

## 5. Ví dụ

### 5.1. Thêm món thành công

**Request**:

```http
POST http://localhost:8080/api/cart/add
Content-Type: application/json

{
  "userId": "user_001",
  "storeId": "store_001",
  "foodId": "prod_001",
  "size": "M",
  "toppings": [
    { "name": "Trân châu", "price": 5000.0 },
    { "name": "Thạch", "price": 3000.0 }
  ],
  "note": "Không thêm hành",
  "quantity": 2
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Đã thêm món vào giỏ hàng thành công.",
  "data": {
    "id": "AbCdEfGhIjKlMnOpQrStUvWxYz123456",
    "storeId": "store_001",
    "foodId": "prod_001",
    "name": "Cơm tấm sườn bì chả",
    "price": 106000.0,
    "quantity": 2,
    "size": "M",
    "sizePrice": 0.0,
    "toppings": [
      { "name": "Trân châu", "price": 5000.0 },
      { "name": "Thạch", "price": 3000.0 }
    ],
    "note": "Không thêm hành",
    "imageUrl": "https://images.unsplash.com/photo-xxx",
    "createdAt": "2026-05-22T15:30:00Z",
    "updatedAt": "2026-05-22T15:30:00Z"
  },
  "timestamp": "2026-05-22T15:30:00Z"
}
```

### 5.2. Thêm món không có size và topping

**Request**:

```http
POST http://localhost:8080/api/cart/add
Content-Type: application/json

{
  "userId": "user_001",
  "storeId": "store_001",
  "foodId": "prod_001",
  "quantity": 1
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Đã thêm món vào giỏ hàng thành công.",
  "data": {
    "id": "AbCdEfGhIjKlMnOpQrStUvWxYz789",
    "storeId": "store_001",
    "foodId": "prod_001",
    "name": "Cơm tấm sườn bì chả",
    "price": 45000.0,
    "quantity": 1,
    "size": null,
    "sizePrice": 0.0,
    "toppings": null,
    "note": null,
    "imageUrl": "https://images.unsplash.com/photo-xxx",
    "createdAt": "2026-05-22T15:35:00Z",
    "updatedAt": "2026-05-22T15:35:00Z"
  },
  "timestamp": "2026-05-22T15:35:00Z"
}
```

### 5.3. Thứ tự gọi API (Flow)

```
1. Flutter gọi POST /api/cart/add
   |
2. Server kiểm tra sản phẩm có tồn tại trong products
   |
3+-> Sản phẩm không tồn tại -> Trả về 404 PRODUCT_NOT_FOUND
|
4+-> Sản phẩm đang hết hàng -> Trả về 400 OUT_OF_STOCK
|
5. Server lấy tất cả món trong giỏ hàng (customer_profiles/{userId}/cart)
   |
6+-> Giỏ hàng rỗng -> Thêm món mới bình thường
|
7+-> Giỏ hàng có món của cửa hàng khác -> Trả về 400 STORE_MISMATCH
|
8. Server tính giá: (basePrice + sizePrice + toppingsPrice) * quantity
   |
9. Server lưu vào Firestore sub-collection cart
   |
10. Trả về 200 với CartItem đã được tạo
```

---

## 3. Chi tiết API - Các API khác

### Mục lục

- [3.1. PUT /api/cart/{itemId}/quantity - Cập nhật số lượng món](#31-put-apicartitemIdquantity---cập-nhật-số-lượng-món)
- [3.2. DELETE /api/cart/{itemId} - Xóa một món khỏi giỏ hàng](#32-delete-apicartitemId---xóa-một-món-khỏi-giỏ-hàng)
- [3.3. DELETE /api/cart - Xóa toàn bộ giỏ hàng](#33-delete-apicart---xóa-toàn-bộ-giỏ-hàng)

---

### 3.1. PUT /api/cart/{itemId}/quantity - Cập nhật số lượng món

**Mô tả**: Cập nhật số lượng của một món trong giỏ hàng của khách hàng.

**Request Headers**:

| Header | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `Content-Type` | String | Có | `application/json` |

**Path Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `itemId` | String | Có | ID của món trong giỏ hàng (cartItemId) |

**Request Body** (JSON):

```json
{
  "userId": "user_001",
  "quantity": 3
}
```

**Các trường bắt buộc**: `userId`, `quantity`
**Validation**: `quantity` phải lớn hơn 0 (>= 1)

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Cập nhật số lượng món thành công.",
  "data": null,
  "timestamp": "2026-05-25T10:00:00Z"
}
```

**Response lỗi - Số lượng không hợp lệ** (HTTP 400):

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Số lượng không hợp lệ.",
  "data": null,
  "timestamp": "2026-05-25T10:00:00Z"
}
```

**Response lỗi - Món không tồn tại** (HTTP 404):

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Không tìm thấy món với ID [cart_item_xyz] trong giỏ hàng.",
  "data": null,
  "timestamp": "2026-05-25T10:00:00Z"
}
```

**Luồng xử lý**:

```
1. Flutter gọi PUT /api/cart/{itemId}/quantity
   |
2. Server kiểm tra quantity > 0
   |
3+-> quantity <= 0 -> Trả về 400 BAD_REQUEST (IllegalArgumentException)
   |
4. Server truy vấn document tại customer_profiles/{userId}/cart/{itemId}
   |
5+-> Document không tồn tại -> Trả về 404 CART_ITEM_NOT_FOUND
   |
6. Server cập nhật trường quantity và updatedAt trong Firestore
   |
7. Trả về 200 thành công
```

**Ví dụ Request**:

```http
PUT http://localhost:8080/api/cart/AbCdEfGhIjKlMnOpQrStUvWxYz123456/quantity
Content-Type: application/json

{
  "userId": "user_001",
  "quantity": 3
}
```

---

### 3.2. DELETE /api/cart/{itemId} - Xóa một món khỏi giỏ hàng

**Mô tả**: Xóa một món ăn khỏi giỏ hàng của khách hàng. Phương thức này là **idempotent** - trả về thành công kể cả khi món không tồn tại trong giỏ hàng.

**Path Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `itemId` | String | Có | ID của món trong giỏ hàng (cartItemId) |

**Request Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `userId` | String | Có | ID người dùng khách hàng |

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Đã xóa món khỏi giỏ hàng thành công.",
  "data": null,
  "timestamp": "2026-05-25T10:05:00Z"
}
```

**Luồng xử lý**:

```
1. Flutter gọi DELETE /api/cart/{itemId}?userId={userId}
   |
2. Server xóa document tại customer_profiles/{userId}/cart/{itemId}
   |
3. Trả về 200 thành công (idempotent - không kiểm tra tồn tại)
```

**Ví dụ Request**:

```http
DELETE http://localhost:8080/api/cart/AbCdEfGhIjKlMnOpQrStUvWxYz123456?userId=user_001
```

---

### 3.3. DELETE /api/cart - Xóa toàn bộ giỏ hàng

**Mô tả**: Xóa tất cả các món trong giỏ hàng của khách hàng. Sử dụng WriteBatch để tối ưu số lần gọi API lên Firebase.

**Request Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `userId` | String | Có | ID người dùng khách hàng |

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Đã xóa toàn bộ giỏ hàng thành công.",
  "data": null,
  "timestamp": "2026-05-25T10:10:00Z"
}
```

**Luồng xử lý**:

```
1. Flutter gọi DELETE /api/cart?userId={userId}
   |
2. Server quét tất cả documents trong customer_profiles/{userId}/cart
   |
3. Server dùng WriteBatch để xóa tất cả documents trong một lần gọi
   |
4. Trả về 200 thành công
```

**Ví dụ Request**:

```http
DELETE http://localhost:8080/api/cart?userId=user_001
```

---

## 4. Bảng mã lỗi mở rộng

### 4.1. Lỗi nghiệp vụ (Business Error) - Các API Cart khác

| HTTP Status | errorCode | Trường hợp | Lỗi trả về (message) |
| --- | --- | --- | --- |
| 400 | INVALID_ARGUMENT | Số lượng <= 0 | "Số lượng không hợp lệ." |
| 404 | CART_ITEM_NOT_FOUND | Món không tồn tại trong giỏ hàng | "Không tìm thấy món với ID [xxx] trong giỏ hàng." |

---

## 5. Cấu trúc dữ liệu mở rộng

### 5.1. CartUpdateQuantityRequest (Request Body)

| Thuộc tính | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `userId` | String | Có | ID người dùng khách hàng |
| `quantity` | Integer | Có | Số lượng mới, phải lớn hơn 0 |

---

## 6. Nội dung Swagger UI

Sau khi chạy ứng dụng, truy cập Swagger UI tại:

```
http://localhost:8080/swagger-ui.html
```

Hoặc tại nội dung OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

---

## 7. API Quan Ly Dia Chi Giao Hang

### Mục lục

- [7.1. POST /api/addresses - Them dia chi moi](#71-post-apiaddresses---them-dia-chi-moi)
- [7.2. GET /api/addresses - Lay danh sach dia chi](#72-get-apiaddresses---lay-danh-sach-dia-chi)
- [7.3. GET /api/addresses/{id} - Lay thong tin mot dia chi](#73-get-apiaddressesid---lay-thong-tin-mot-dia-chi)
- [7.4. PUT /api/addresses/{id} - Cap nhat dia chi](#74-put-apiaddressesid---cap-nhat-dia-chi)
- [7.5. PUT /api/addresses/{id}/default - Dat dia chi lam mac dinh](#75-put-apiaddressesiddefault---dat-dia-chi-lam-mac-dinh)
- [7.6. DELETE /api/addresses/{id} - Xoa dia chi](#76-delete-apiaddressesid---xoa-dia-chi)

---

### 7.1. POST /api/addresses - Them dia chi moi

**Mô tả**: Them mot dia chi giao hang moi cho khach hang. Dia chi duoc luu vao sub-collection `customer_profiles/{userId}/addresses`.

**Request Headers**:

| Header | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `Content-Type` | String | Có | `application/json` |

**Request Body** (JSON):

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

**Các trường bắt buộc**: `userId`, `name`, `address`, `receiverName`, `receiverPhone`, `lat`, `lng`
**Các trường tùy chọn**: `isDefault` (mặc định: `false`)

**Các quy tắc nghiệp vụ**:

1. Neu `isDefault = true`, he thong se tu dong goi `xoaTatCaDiaChiMacDinh` de bo flag mac dinh cua cac dia chi cu.
2. Neu `isDefault` khong duoc truyen hoac la `false`, dia chi moi se khong phai la mac dinh.

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Da them dia chi thanh cong.",
  "data": {
    "id": "AbCdEfGhIjKlMnOpQrStUvWxYz123456",
    "name": "Nha rieng",
    "address": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "receiverName": "Khoi",
    "receiverPhone": "0123456789",
    "lat": 10.8455,
    "lng": 106.7939,
    "isDefault": true,
    "createdAt": "2026-05-25T10:00:00Z",
    "updatedAt": "2026-05-25T10:00:00Z",
    "deletedAt": null
  },
  "timestamp": "2026-05-25T10:00:00Z"
}
```

**Luồng xử lý**:

```
1. Flutter goi POST /api/addresses voi AddressRequest
   |
2. Server kiem tra du lieu dau vao (validation)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST
   |
4. Neu isDefault = true, goi addressRepository.xoaTatCaDiaChiMacDinh(userId, "")
   |
5. Server tao Address object va luu vao Firestore
   |
6. Tra ve 200 voi Address da duoc tao
```

**Ví dụ Request**:

```http
POST http://localhost:8080/api/addresses
Content-Type: application/json

{
  "userId": "user_001",
  "name": "Truong hoc",
  "address": "Truong Dai hoc, Quan 7, TP.HCM",
  "receiverName": "Khoi",
  "receiverPhone": "0123456789",
  "lat": 10.7291,
  "lng": 106.6989,
  "isDefault": false
}
```

---

### 7.2. GET /api/addresses - Lay danh sach dia chi

**Mô tả**: Lay tat ca dia chi giao hang cua khach hang tu sub-collection `customer_profiles/{userId}/addresses`.

**Request Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `userId` | String | Có | ID nguoi dung khach hang |

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Da lay danh sach dia chi thanh cong.",
  "data": [
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
      "updatedAt": "2026-05-25T10:00:00Z",
      "deletedAt": null
    },
    {
      "id": "addr_002",
      "name": "Truong hoc",
      "address": "Truong Dai hoc, Quan 7, TP.HCM",
      "receiverName": "Khoi",
      "receiverPhone": "0123456789",
      "lat": 10.7291,
      "lng": 106.6989,
      "isDefault": false,
      "createdAt": "2026-04-07T00:00:00Z",
      "updatedAt": "2026-04-07T00:00:00Z",
      "deletedAt": null
    }
  ],
  "timestamp": "2026-05-25T10:00:00Z"
}
```

**Luồng xử lý**:

```
1. Flutter goi GET /api/addresses?userId={userId}
   |
2. Server truy van tat ca documents trong customer_profiles/{userId}/addresses
   |
3. Tra ve 200 voi danh sach Address
```

**Ví dụ Request**:

```http
GET http://localhost:8080/api/addresses?userId=user_001
```

---

### 7.3. GET /api/addresses/{id} - Lay thong tin mot dia chi

**Mô tả**: Lay thong tin chi tiet cua mot dia chi giao hang cu the.

**Path Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `id` | String | Có | ID dia chi can lay |

**Request Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `userId` | String | Có | ID nguoi dung khach hang |

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Da lay thong tin dia chi thanh cong.",
  "data": {
    "id": "addr_001",
    "name": "Nha rieng",
    "address": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "receiverName": "Khoi",
    "receiverPhone": "0123456789",
    "lat": 10.8455,
    "lng": 106.7939,
    "isDefault": true,
    "createdAt": "2026-04-07T00:00:00Z",
    "updatedAt": "2026-05-25T10:00:00Z",
    "deletedAt": null
  },
  "timestamp": "2026-05-25T10:00:00Z"
}
```

**Response lỗi - Dia chi khong ton tai** (HTTP 404):

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Khong tim thay dia chi voi ID [addr_xyz].",
  "data": null,
  "timestamp": "2026-05-25T10:00:00Z"
}
```

**Luồng xử lý**:

```
1. Flutter goi GET /api/addresses/{id}?userId={userId}
   |
2. Server truy van document tai customer_profiles/{userId}/addresses/{id}
   |
3+-> Document khong ton tai -> Tra ve 404 ADDRESS_NOT_FOUND
   |
4. Tra ve 200 voi Address
```

**Ví dụ Request**:

```http
GET http://localhost:8080/api/addresses/addr_001?userId=user_001
```

---

### 7.4. PUT /api/addresses/{id} - Cap nhat dia chi

**Mô tả**: Cap nhat thong tin dia chi giao hang cua khach hang.

**Path Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `id` | String | Có | ID dia chi can cap nhat |

**Request Body** (JSON):

```json
{
  "userId": "user_001",
  "name": "Nha me",
  "address": "Dia chi moi, Quan 9, TP.HCM",
  "receiverName": "Khoi",
  "receiverPhone": "0987654321",
  "lat": 10.8500,
  "lng": 106.8000,
  "isDefault": true
}
```

**Các trường bắt buộc**: `userId`, `name`, `address`, `receiverName`, `receiverPhone`, `lat`, `lng`
**Các trường tùy chọn**: `isDefault`

**Các quy tắc nghiệp vụ**:

1. Neu dia chi hien tai chua phai mac dinh va `isDefault = true`, he thong se goi `xoaTatCaDiaChiMacDinh` truoc khi cap nhat.
2. Neu dia chi hien tai da la mac dinh va `isDefault = false`, chi cap nhat thong tin, giu nguyen mac dinh.

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Da cap nhat dia chi thanh cong.",
  "data": {
    "id": "addr_001",
    "name": "Nha me",
    "address": "Dia chi moi, Quan 9, TP.HCM",
    "receiverName": "Khoi",
    "receiverPhone": "0987654321",
    "lat": 10.8500,
    "lng": 106.8000,
    "isDefault": true,
    "createdAt": "2026-04-07T00:00:00Z",
    "updatedAt": "2026-05-25T10:30:00Z",
    "deletedAt": null
  },
  "timestamp": "2026-05-25T10:30:00Z"
}
```

**Response lỗi - Dia chi khong ton tai** (HTTP 404):

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Khong tim thay dia chi voi ID [addr_xyz].",
  "data": null,
  "timestamp": "2026-05-25T10:30:00Z"
}
```

**Luồng xử lý**:

```
1. Flutter goi PUT /api/addresses/{id} voi AddressRequest
   |
2. Server kiem tra du lieu dau vao (validation)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST
   |
4. Server truy van dia chi hien tai tai customer_profiles/{userId}/addresses/{id}
   |
5+-> Dia chi khong ton tai -> Tra ve 404 ADDRESS_NOT_FOUND
   |
6. Neu chuyen tu khong mac dinh sang mac dinh (isDefault: false -> true),
   goi addressRepository.xoaTatCaDiaChiMacDinh(userId, addressId)
   |
7. Server cap nhat document trong Firestore
   |
8. Tra ve 200 voi Address da duoc cap nhat
```

**Ví dụ Request**:

```http
PUT http://localhost:8080/api/addresses/addr_001
Content-Type: application/json

{
  "userId": "user_001",
  "name": "Nha me",
  "address": "Dia chi moi, Quan 9, TP.HCM",
  "receiverName": "Khoi",
  "receiverPhone": "0987654321",
  "lat": 10.8500,
  "lng": 106.8000,
  "isDefault": true
}
```

---

### 7.5. PUT /api/addresses/{id}/default - Dat dia chi lam mac dinh

**Mô tả**: Dat mot dia chi giao hang lam dia chi mac dinh cho khach hang. He thong se quet tat ca dia chi cua nguoi dung, bo flag `isDefault` cua cac dia chi cu, sau do dat `isDefault = true` cho dia chi duoc yeu cau.

**Path Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `id` | String | Có | ID dia chi can dat lam mac dinh |

**Request Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `userId` | String | Có | ID nguoi dung khach hang |

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Da dat dia chi lam mac dinh thanh cong.",
  "data": null,
  "timestamp": "2026-05-25T10:35:00Z"
}
```

**Response lỗi - Dia chi khong ton tai** (HTTP 404):

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Khong tim thay dia chi voi ID [addr_xyz].",
  "data": null,
  "timestamp": "2026-05-25T10:35:00Z"
}
```

**Luồng xử lý**:

```
1. Flutter goi PUT /api/addresses/{id}/default?userId={userId}
   |
2. Server truy van dia chi tai customer_profiles/{userId}/addresses/{id}
   |
3+-> Dia chi khong ton tai -> Tra ve 404 ADDRESS_NOT_FOUND
   |
4. Neu dia chi da la mac dinh -> Tra ve 200 ngay (khong can thay doi)
   |
5. Server quet tat ca dia chi cua nguoi dung, goi xoaTatCaDiaChiMacDinh(userId, addressId)
   de bo flag isDefault cua cac dia chi cu
   |
6. Server dat isDefault = true cho dia chi duoc yeu cau
   |
7. Tra ve 200 thanh cong
```

**Ví dụ Request**:

```http
PUT http://localhost:8080/api/addresses/addr_002/default?userId=user_001
```

---

### 7.6. DELETE /api/addresses/{id} - Xoa dia chi

**Mô tả**: Xoa mot dia chi giao hang cua khach hang. Phuong thuc nay la **idempotent** - tra ve thanh cong ke ca khi dia chi khong ton tai.

**Path Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `id` | String | Có | ID dia chi can xoa |

**Request Parameters**:

| Parameter | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `userId` | String | Có | ID nguoi dung khach hang |

**Response thành công** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Da xoa dia chi thanh cong.",
  "data": null,
  "timestamp": "2026-05-25T10:40:00Z"
}
```

**Luồng xử lý**:

```
1. Flutter goi DELETE /api/addresses/{id}?userId={userId}
   |
2. Server kiem tra dia chi co ton tai khong
   |
3+-> Dia chi khong ton tai -> Tra ve 200 (idempotent)
   |
4. Server xoa document tai customer_profiles/{userId}/addresses/{id}
   |
5. Tra ve 200 thanh cong
```

**Ví dụ Request**:

```http
DELETE http://localhost:8080/api/addresses/addr_001?userId=user_001
```

---

## 8. Cau Truc Du Lieu - Dia Chi

### 8.1. AddressRequest (Request Body)

| Thuộc tính | Kiểu | Bắt buộc | Mô tả |
| --- | --- | --- | --- |
| `userId` | String | Có | ID nguoi dung khach hang |
| `name` | String | Có | Nhan dia chi (VD: "Nha rieng", "Cong ty") |
| `address` | String | Có | Dia chi chi tiet day du |
| `receiverName` | String | Có | Ho ten nguoi nhan hang |
| `receiverPhone` | String | Có | So dien thoai nguoi nhan (bat dau bang 0, 10-11 chu so) |
| `lat` | Double | Có | Toa do vi do (latitude) |
| `lng` | Double | Có | Toa do kin do (longitude) |
| `isDefault` | Boolean | Không | Co phai dia chi mac dinh khong (mặc định: false) |

### 8.2. Address (Response Data)

| Thuộc tính | Kiểu | Mô tả |
| --- | --- | --- |
| `id` | String | ID document trong Firestore (auto generated) |
| `name` | String | Nhan dia chi |
| `address` | String | Dia chi chi tiet day du |
| `receiverName` | String | Ho ten nguoi nhan hang |
| `receiverPhone` | String | So dien thoai nguoi nhan |
| `lat` | Double | Toa do vi do |
| `lng` | Double | Toa do kin do |
| `isDefault` | Boolean | Co phai dia chi mac dinh khong |
| `createdAt` | ISO 8601 Timestamp | Thoi diem tao |
| `updatedAt` | ISO 8601 Timestamp | Thoi diem cap nhat gan nhat |
| `deletedAt` | ISO 8601 Timestamp (nullable) | Thoi diem xoa (neu co) |

---

## 9. Bang Ma Loi - Dia Chi

### 9.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
| --- | --- | --- | --- |
| 404 | ADDRESS_NOT_FOUND | Dia chi khong ton tai | "Khong tim thay dia chi voi ID [xxx]." |
| 500 | SYSTEM_ERROR | Loi he thong khi truy van Firestore | "Loi he thong: Khong the ..." |

### 9.2. Loi xac thuc dau vao (Validation Error)

| HTTP Status | Truong hop | Mô tả |
| --- | --- | --- |
| 400 | Du lieu khong hop le | Cac truong bat buoc bi trong hoac sai dinh dang |

**Ví dụ loi validation - So dien thoai sai dinh dang**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Du lieu khong hop le: receiverPhone: So dien thoai khong dung dinh dang (bat dau bang 0, 10-11 chu so)",
  "data": null,
  "timestamp": "2026-05-25T10:00:00Z"
}
```

**Ví dụ loi validation - Thieu truong bat buoc**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Du lieu khong hop le: name: Ten dia chi (nhan) khong duoc de trong",
  "data": null,
  "timestamp": "2026-05-25T10:00:00Z"
}
```

---

## 10. Thu Vien Swagger UI

Sau khi chay ung dung, truy cap Swagger UI tai:

```
http://localhost:8080/swagger-ui.html
```

Hoac tai noi dung OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

---

## 11. API Dat Hang (Checkout)

### Muc luc

- [11.1. POST /api/orders/checkout - Dat hang (Checkout)](#111-post-apiorderscheckout---dat-hang-checkout)

---

### 11.1. POST /api/orders/checkout - Dat hang (Checkout)

**Mo ta**: Thuc hien dat hang cho khach hang. Tao don hang moi, xoa gio hang, va cap nhat voucher (neu co) trong mot giao dich atomically.

**Request Headers**:

| Header | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `Content-Type` | String | Co | `application/json` |

**Request Body** (JSON):

```json
{
  "userId": "user_001",
  "addressId": "addr_001",
  "paymentMethod": "momo",
  "voucherId": "sys_voucher_001",
  "note": "Giao gap"
}
```

**Cac truong bat buoc**: `userId`, `addressId`, `paymentMethod`
**Cac truong tuy chon**: `voucherId`, `note`

**Gia tri paymentMethod**:

| Gia tri | Mo ta |
| --- | --- |
| `cash` | Tien mat |
| `momo` | Vi MoMo |
| `zalo` | ZaloPay |
| `card` | The ngan hang |

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
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
        "imageUrl": "https://example.com/comtam.jpg",
        "options": [
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
    "createdAt": "2026-05-25T10:30:00Z",
    "note": "Giao gap"
  },
  "timestamp": "2026-05-25T10:30:00Z"
}
```

---

**Cac quy tac nghiep vu (Business Rules)**:

1. **Buoc 1 - Kiem tra gio hang**: Truy van sub-collection `cart` cua `customer_profiles/{userId}`. Neu gio hang rong, tra ve loi 400 `CART_EMPTY`.
2. **Buoc 2 - Kiem tra khoang cach**: Lay document dia chi tu `addressId`, lay document cua hang tu `storeId` trong gio hang. Su dung cong thuc Haversine de tinh khoang cach giua toa do cua hang va khach hang. Neu khoang cach > 10km, tra ve loi 400 `DISTANCE_EXCEEDED`.
3. **Buoc 3 - Tinh tien server-side**: Truy van collection `products` de kiem tra `isOutOfStock`. Neu bat ky mon nao bi het hang, tra ve loi 400 `ITEM_OUT_OF_STOCK`. Tinh tong tien don hang dua tren `price` trong gio hang + phi ship co ban (15000 VND).
4. **Buoc 4 - Xu ly Voucher**: Neu co `voucherId`, kiem tra:
   - `isActive = true` (con hoat dong)
   - `remaining = limitCount - usedCount > 0` (con so luong)
   - `expiryDate` chua het han
   - `minOrder <= tongTienHang` (dat don toi thieu)
   - Tinh so tien giam: type=1 (phan tram), type=2 (tien mat)
5. **Buoc 5 - Transaction bang WriteBatch**:
   - Tao document moi trong collection `orders` voi `status = 0`
   - Xoa toan bo documents trong sub-collection `cart` cua user
   - Giam `usedCount` cua voucher di 1 (neu co voucher)
   - Neu loi xay ra, khong co thay doi nao duoc luu (atomic)

**Luu y quan trong**:
- Tong tien duoc tinh toan **hoan toan tu phia server**. Client khong gui danh sach mon an hay gia tien.
- Gio hang se bi xoa sau khi dat hang thanh cong.
- Don hang moi tao co `status = 0` (Cho xac nhan).

---

### Cac quy tac tinh tien

```
tongTienHang = SUM(item.price trong gio hang)
phiShip = 15000 VND (co dinh)
soTienGiam = 0 (neu khong co voucher)
  hoac = tongTienHang * (voucher.value / 100) (neu voucher.type == 1)
  hoac = voucher.value (neu voucher.type == 2, khong vuot qua tongTienHang)
tongThanhToan = tongTienHang + phiShip - soTienGiam
```

**Vi du**: 2 mon (45000 + 25000) + phi ship 15000 - giam 20000 = **65000 VND**

---

### Bang ma loi tra ve

#### 11.1.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
| --- | --- | --- | --- |
| 400 | `CART_EMPTY` | Gio hang rong | "Gio hang hien tai dang rong, vui long them mon truoc khi dat hang." |
| 400 | `DISTANCE_EXCEEDED` | Khoang cach vuot 10km | "Khoang cach tu cua hang den dia chi giao hang la X.X km, vuot qua gioi han 10.0 km. Vui long chon dia chi gan hon." |
| 400 | `ITEM_OUT_OF_STOCK` | Mon an trong gio hang het hang | "Mon an voi ID [xxx] trong gio hang da het hang, vui long xoa khoi gio hang hoac chon mon khac." |
| 400 | `VOUCHER_EXPIRED` | Voucher da het han | "Voucher voi ID [xxx] da het han." |
| 400 | `VOUCHER_EXHAUSTED` | Voucher da het so luong | "Voucher voi ID [xxx] da het so luong su dung." |
| 400 | `VOUCHER_MIN_ORDER_NOT_MET` | Khong dat don toi thieu | "Don hang phai co gia tri toi thieu X VND de su dung voucher [xxx]." |
| 404 | `VOUCHER_NOT_FOUND` | Voucher khong ton tai | "Khong tim thay voucher voi ID [xxx]." |
| 404 | `ADDRESS_NOT_FOUND` | Dia chi khong ton tai | "Khong tim thay dia chi voi ID [xxx]." |
| 500 | `SYSTEM_ERROR` | Loi he thong | "Loi he thong: Khong the tao don hang. Vui long thu lai sau." |

#### 11.1.2. Loi xac thuc dau vao (Validation Error)

| HTTP Status | Truong hop | Mo ta |
| --- | --- | --- |
| 400 | Du lieu khong hop le | Cac truong bat buoc bi trong hoac sai dinh dang |

**Vi du loi validation**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Du lieu khong hop le: userId: userId khong duoc de trong, paymentMethod: paymentMethod phai la mot trong cac gia tri: cash, momo, zalo, card",
  "data": null,
  "timestamp": "2026-05-25T10:30:00Z"
}
```

#### 11.1.3. Vi du cac response loi nghiep vu

**HTTP 400 - Gio hang rong (CART_EMPTY)**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Gio hang hien tai dang rong, vui long them mon truoc khi dat hang.",
  "data": null,
  "timestamp": "2026-05-25T10:30:00Z"
}
```

**HTTP 400 - Khoang cach vuot gioi han (DISTANCE_EXCEEDED)**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Khoang cach tu cua hang den dia chi giao hang la 12.5 km, vuot qua gioi han 10.0 km. Vui long chon dia chi gan hon.",
  "data": null,
  "timestamp": "2026-05-25T10:30:00Z"
}
```

**HTTP 400 - Mon an het hang (ITEM_OUT_OF_STOCK)**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Mon an voi ID [prod_999] trong gio hang da het hang, vui long xoa khoi gio hang hoac chon mon khac.",
  "data": null,
  "timestamp": "2026-05-25T10:30:00Z"
}
```

**HTTP 400 - Voucher khong dat don toi thieu (VOUCHER_MIN_ORDER_NOT_MET)**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Don hang phai co gia tri toi thieu 100000 VND de su dung voucher [sys_voucher_001].",
  "data": null,
  "timestamp": "2026-05-25T10:30:00Z"
}
```

---

### Vi du

#### 11.1.4. Dat hang thanh cong voi voucher

**Request**:

```http
POST http://localhost:8080/api/orders/checkout
Content-Type: application/json

{
  "userId": "user_001",
  "addressId": "addr_001",
  "paymentMethod": "momo",
  "voucherId": "sys_voucher_001",
  "note": "Giao gap"
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
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
        "imageUrl": "https://example.com/comtam.jpg",
        "options": [
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
    "createdAt": "2026-05-25T10:30:00Z",
    "note": "Giao gap"
  },
  "timestamp": "2026-05-25T10:30:00Z"
}
```

#### 11.1.5. Dat hang khong co voucher

**Request**:

```http
POST http://localhost:8080/api/orders/checkout
Content-Type: application/json

{
  "userId": "user_001",
  "addressId": "addr_001",
  "paymentMethod": "cash"
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Dat hang thanh cong, vui long cho cua hang xac nhan.",
  "data": {
    "orderId": "WxYzAbCdEfGhIjKlMnOpQrStUv123",
    "orderCode": "RSTUVX",
    "storeId": "store_001",
    "storeName": "Com tam Phuc Loc Tho",
    "userId": "user_001",
    "items": [
      {
        "foodId": "prod_001",
        "name": "Com tam suon bi cha",
        "price": 45000.0,
        "quantity": 2,
        "imageUrl": "https://example.com/comtam.jpg",
        "options": null
      }
    ],
    "totalAmount": 90000.0,
    "deliveryFee": 15000.0,
    "discountAmount": 0.0,
    "finalAmount": 105000.0,
    "paymentMethod": "cash",
    "deliveryAddress": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "status": 0,
    "createdAt": "2026-05-25T10:35:00Z",
    "note": null
  },
  "timestamp": "2026-05-25T10:35:00Z"
}
```

---

### 11.1.6. Thu tu goi API (Flow)

```
1. Flutter goi POST /api/orders/checkout
   |
2. Server kiem tra du lieu dau vao (validation)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST (Validation)
   |
4. Server truy van gio hang (customer_profiles/{userId}/cart)
   |
5+-> Gio hang rong -> Tra ve 400 CART_EMPTY
   |
6. Server truy van dia chi (customer_profiles/{userId}/addresses/{addressId})
   |
7+-> Dia chi khong ton tai -> Tra ve 404 ADDRESS_NOT_FOUND
   |
8. Server truy van cua hang (stores/{storeId})
   |
9. Server tinh khoang cach Haversine giua cua hang va dia chi giao
   |
10+-> Khoang cach > 10km -> Tra ve 400 DISTANCE_EXCEEDED
   |
11. Server kiem tra ton kho moi san pham trong gio hang (products/{foodId})
   |
12+-> Bat ky san pham nao bi het hang -> Tra ve 400 ITEM_OUT_OF_STOCK
   |
13. Server tinh tong tien hang + phi ship (15000 VND)
   |
14+-> Co voucherId -> Kiem tra voucher hop le (han, so luong, minOrder)
   |   |
   |   +-> Voucher khong hop le -> Tra ve loi 400/404 tuong ung
   |
15. Server tao don hang atomically bang WriteBatch:
   |   + Tao document orders
   |   + Xoa gio hang
   |   + Giam usedCount voucher (neu co)
   |
16+-> Loi WriteBatch -> Rollback toan bo, tra ve 500 SYSTEM_ERROR
   |
17. Tra ve 200 voi CheckoutResponse
```

---

## 12. API Huy Don Hang (Cancel Order)

### Muc luc

- [12.1. POST /api/orders/{id}/cancel - Huy don hang](#121-post-apiordersidcancel---huy-don-hang)

---

### 12.1. POST /api/orders/{id}/cancel - Huy don hang

**Mo ta**: Cho phep khach hang huy don hang cua minh. Chi co the huy khi don hang o trang thai [Cho xac nhan] (0). Don hang o trang thai [Dang chuan bi], [Dang giao], [Hoan thanh], hoac [Da huy] khong the huy duoc.

**Phan he**: Khach hang

**Muc do truy cap**: Cong khai (chua co xac thuc JWT trong phien ban nay)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Tim don hang**: Truy van document don hang tu collection `orders` theo `id`. Neu khong tim thay, tra ve loi 404 `ORDER_NOT_FOUND`.
2. **Buoc 2 - Kiem tra quyen so huu**: Kiem tra `userId` cua don hang co khop voi `userId` truyen len khong. Neu khong, tra ve loi 403 `FORBIDDEN`.
3. **Buoc 3 - Kiem tra trang thai**: Chi cho phep huy khi don hang o trang thai 0 (Cho xac nhan). Neu `status != 0`, tra ve loi 400 `ORDER_STATUS_CANNOT_CANCEL`.
4. **Buoc 4 - Cap nhat trang thai**: Neu hop le, cap nhat `status = 4` (Da huy) va `updatedAt` thoi diem hien tai.

---

#### Cac gia tri trang thai don hang

| Gia tri | Ten           | Mo ta                         | Co the huy? |
| --- | ------------- | ------------------------------ | ----------- |
| 0   | Cho xac nhan  | Don hang cho quan xac nhan     | Co         |
| 1   | Dang chuan bi | Quan dang chuan bi mon         | Khong      |
| 2   | Dang giao     | Tai xe dang giao hang          | Khong      |
| 3   | Hoan thanh    | Da giao thanh cong             | Khong      |
| 4   | Da huy        | Don hang da bi huy             | Khong      |

---

#### Chi tiet API

**Request Headers**:

| Header         | Kieu   | Bat buoc | Mo ta              |
| --- | ------ | -------- | ------------------- |
| `Content-Type` | String | Co       | `application/json`  |

**Path Parameters**:

| Parameter | Kieu   | Bat buoc | Mo ta                        |
| --- | ------ | -------- | ----------------------------- |
| `id`      | String | Co       | ID don hang can huy           |

**Request Parameters**:

| Parameter | Kieu   | Bat buoc | Mo ta                                         |
| --- | ------ | -------- | ---------------------------------------------- |
| `userId`  | String | Co       | ID nguoi dung khach hang (xac thuc quyen so huu) |

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Huy don hang thanh cong.",
  "data": {
    "id": "AbCdEfGhIjKlMnOpQrStUvWxYz123456",
    "userId": "user_001",
    "storeId": "store_001",
    "storeName": "Com tam Phuc Loc Tho",
    "code": "QRSTUV",
    "items": [
      {
        "foodId": "prod_001",
        "name": "Com tam suon bi cha",
        "price": 45000.0,
        "quantity": 2,
        "options": [
          { "name": "Tran chau", "price": 5000.0 }
        ]
      }
    ],
    "totalAmount": 90000.0,
    "deliveryFee": 15000.0,
    "discountAmount": 0.0,
    "finalAmount": 105000.0,
    "paymentMethod": "cash",
    "deliveryAddress": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "status": "Da huy",
    "createdAt": "2026-05-25T10:30:00Z",
    "updatedAt": "2026-05-25T11:00:00Z"
  },
  "timestamp": "2026-05-25T11:00:00Z"
}
```

---

#### Bang ma loi tra ve

##### 12.1.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode                  | Truong hop                                    | Loi tra ve (message)                                                                                                                               |
| --- | --- | --- | --- |
| 400 | `ORDER_STATUS_CANNOT_CANCEL` | Don hang khong o trang thai cho phep huy       | "Khong the huy don hang [xxx] vi don dang o trang thai [Dang chuan bi]. Chi co the huy don hang dang o trang thai [Cho xac nhan]." |
| 403 | `FORBIDDEN`                  | Nguoi dung khong phai chu don hang             | "Ban khong co quyen huy don hang [xxx]. Chi chu nhan cua don hang moi duoc phep huy."                                                             |
| 404 | `ORDER_NOT_FOUND`            | Don hang khong ton tai trong he thong          | "Khong tim thay don hang voi ID [xxx]."                                                                                                            |
| 500 | `SYSTEM_ERROR`              | Loi he thong khi truy van Firestore            | "Da xay ra loi khong mong muon. Vui long thu lai sau."                                                                                            |

##### 12.1.2. Vi du cac response loi nghiep vu

**HTTP 400 - Don hang khong the huy (ORDER_STATUS_CANNOT_CANCEL)**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Khong the huy don hang [AbCdEfGhIjKlMnOpQrStUvWxYz123456] vi don dang o trang thai [Dang chuan bi]. Chi co the huy don hang dang o trang thai [Cho xac nhan].",
  "data": null,
  "timestamp": "2026-05-25T11:00:00Z"
}
```

**HTTP 403 - Khong co quyen huy don hang (FORBIDDEN)**:

```json
{
  "success": false,
  "statusCode": 403,
  "message": "Ban khong co quyen huy don hang [AbCdEfGhIjKlMnOpQrStUvWxYz123456]. Chi chu nhan cua don hang moi duoc phep huy.",
  "data": null,
  "timestamp": "2026-05-25T11:00:00Z"
}
```

**HTTP 404 - Don hang khong ton tai (ORDER_NOT_FOUND)**:

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Khong tim thay don hang voi ID [order_xyz].",
  "data": null,
  "timestamp": "2026-05-25T11:00:00Z"
}
```

---

#### Vi du

##### 12.1.3. Huy don hang thanh cong

**Request**:

```http
POST http://localhost:8080/api/orders/AbCdEfGhIjKlMnOpQrStUvWxYz123456/cancel?userId=user_001
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Huy don hang thanh cong.",
  "data": {
    "id": "AbCdEfGhIjKlMnOpQrStUvWxYz123456",
    "userId": "user_001",
    "storeId": "store_001",
    "storeName": "Com tam Phuc Loc Tho",
    "code": "QRSTUV",
    "items": [
      {
        "foodId": "prod_001",
        "name": "Com tam suon bi cha",
        "price": 45000.0,
        "quantity": 2,
        "options": [
          { "name": "Tran chau", "price": 5000.0 }
        ]
      }
    ],
    "totalAmount": 90000.0,
    "deliveryFee": 15000.0,
    "discountAmount": 0.0,
    "finalAmount": 105000.0,
    "paymentMethod": "cash",
    "deliveryAddress": "Ky tuc xa UTC2, Quan 9, TP.HCM",
    "status": "Da huy",
    "createdAt": "2026-05-25T10:30:00Z",
    "updatedAt": "2026-05-25T11:00:00Z"
  },
  "timestamp": "2026-05-25T11:00:00Z"
}
```

##### 12.1.4. Thu tu goi API (Flow)

```
1. Flutter goi POST /api/orders/{id}/cancel?userId={userId}
   |
2. Server truy van document don hang tu collection orders theo id
   |
3+-> Don hang khong ton tai -> Tra ve 404 ORDER_NOT_FOUND
   |
4. Server kiem tra userId cua don hang co khop voi userId truyen len
   |
5+-> Khong khop -> Tra ve 403 FORBIDDEN
   |
6. Server doc gia tri trang thai (status) cua don hang
   |
7+-> status != 0 (Dang chuan bi / Dang giao / Hoan thanh / Da huy)
   |   -> Tra ve 400 ORDER_STATUS_CANNOT_CANCEL
   |
8. Server cap nhat trang thai don hang:
   |   + status = 4 (Da huy)
   |   + updatedAt = thoi diem hien tai
   |
9. Tra ve 200 voi OrderDTO da duoc cap nhat
```

##### 12.1.5. Cau truc du lieu tra ve (OrderDTO)

| Thuoc tinh         | Kieu        | Mo ta                                      |
| --- | --- | --- |
| `id`                | String      | ID don hang                                |
| `userId`            | String      | ID nguoi dat hang                         |
| `storeId`           | String      | ID cua hang                               |
| `storeName`        | String      | Ten cua hang                              |
| `code`              | String      | Ma don hang (6 ky tu cuoi cua ID)         |
| `items`             | ArrayObject | Danh sach mon an trong don                 |
| `totalAmount`       | Double      | Tong tien hang (VND)                     |
| `deliveryFee`       | Double      | Phi giao hang (VND)                       |
| `discountAmount`    | Double      | So tien giam gia (VND)                    |
| `finalAmount`       | Double      | Tong thanh toan (VND)                     |
| `paymentMethod`     | String      | Phuong thuc thanh toan                   |
| `deliveryAddress`    | String      | Dia chi giao hang                         |
| `status`            | String      | Trang thai don hang (text)                |
| `createdAt`         | Timestamp   | Thoi diem tao don                         |
| `updatedAt`         | Timestamp   | Thoi diem cap nhat gan nhat               |

---

#### Noi dung Swagger UI

Sau khi chay ung dung, truy cap Swagger UI tai:

```
http://localhost:8080/swagger-ui.html
```

Hoac tai noi dung OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

---

## 13. API Tao Danh Gia (Create Review)

### Muc luc

- [13.1. POST /api/reviews - Tao danh gia](#131-post-apireviews---tao-danh-gia)
- [13.2. GET /api/reviews - Lay danh sach danh gia theo cua hang](#132-get-apireviews---lay-danh-sach-danh-gia-theo-cua-hang)

---

### 13.1. POST /api/reviews - Tao danh gia

**Mo ta**: Tao mot danh gia moi cho don hang da nhan. Chi cho phep danh gia khi don hang o trang thai [Hoan thanh] (status = 3). Mot don hang chi duoc phep danh gia mot lan.

**Phan he**: Khach hang

**Muc do truy cap**: Cong khai (chua co xac thuc JWT trong phien ban nay)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Kiem tra don hang**: Truy van document don hang tu collection `orders` theo `orderId`. Neu khong tim thay, tra ve loi 404 `ORDER_NOT_FOUND`.
2. **Buoc 2 - Kiem tra quyen so huu**: Kiem tra `userId` cua don hang co khop voi `userId` truyen len khong. Neu khong, tra ve loi 403 `FORBIDDEN`.
3. **Buoc 3 - Kiem tra trang thai**: Chi cho phep danh gia khi don hang o trang thai `status = 3` (Hoan thanh). Neu trang thai khac 3, tra ve loi 400 `ORDER_STATUS_CANNOT_REVIEW`.
4. **Buoc 4 - Chong trung lap**: Kiem tra xem don hang da duoc danh gia chua (query collection `reviews` theo `orderId`). Neu da danh gia, tra ve loi 400 `ORDER_ALREADY_REVIEWED`.
5. **Buoc 5 - Luu danh gia**: Tao document moi trong collection `reviews` voi du lieu tu request kem theo `createdAt`.
6. **Buoc 6 - Cap nhat diem so cua hang**: Doc document cua hang tu collection `stores`, tinh toan lai `rating` trung binh va `reviewCount` (cong them 1), cap nhat vao document `stores`.

---

#### Chi tiet API

**Request Headers**:

| Header | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `Content-Type` | String | Co | `application/json` |

**Request Body** (JSON):

```json
{
  "orderId": "order_001",
  "storeId": "store_001",
  "userId": "user_001",
  "userName": "Khoi",
  "userAvatarUrl": "https://example.com/avatar/user001.jpg",
  "starRating": 5,
  "comment": "Do an rat ngon, giao hang nhanh, dong goi ky luong.",
  "imageUrls": [
    "https://example.com/review/rev001_1.jpg",
    "https://example.com/review/rev001_2.jpg"
  ]
}
```

**Cac truong bat buoc**: `orderId`, `storeId`, `userId`, `userName`, `starRating`, `comment`
**Cac truong tuy chon**: `userAvatarUrl`, `imageUrls`

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Tao danh gia thanh cong.",
  "data": {
    "id": "AbCdEfGhIjKlMnOpQrStUvWxYz123456",
    "orderId": "order_001",
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
    "createdAt": "2026-05-25T11:00:00Z",
    "updatedAt": "2026-05-25T11:00:00Z"
  },
  "timestamp": "2026-05-25T11:00:00Z"
}
```

---

#### Bang ma loi tra ve

##### 13.1.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
| --- | --- | --- | --- |
| 400 | `ORDER_STATUS_CANNOT_REVIEW` | Don hang khong o trang thai cho phep danh gia | "Khong the danh gia don hang [xxx] vi don dang o trang thai [Y]. Chi co the danh gia don hang dang o trang thai [Hoan thanh] (status = 3)." |
| 400 | `ORDER_ALREADY_REVIEWED` | Don hang da duoc danh gia roi | "Don hang [xxx] da duoc danh gia truoc do. Moi don hang chi duoc phep danh gia mot lan." |
| 403 | `FORBIDDEN` | Nguoi dung khong phai chu don hang | "Ban khong co quyen danh gia don hang [xxx]. Chi chu nhan cua don hang moi duoc phep danh gia." |
| 404 | `ORDER_NOT_FOUND` | Don hang khong ton tai trong he thong | "Khong tim thay don hang voi ID [xxx]." |
| 500 | `SYSTEM_ERROR` | Loi he thong khi truy van Firestore | "Da xay ra loi khong mong muon. Vui long thu lai sau." |

##### 13.1.2. Loi xac thuc dau vao (Validation Error)

| HTTP Status | Truong hop | Mo ta |
| --- | --- | --- |
| 400 | Du lieu khong hop le | Cac truong bat buoc bi trong hoac sai dinh dang |

**Vi du loi validation**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Du lieu khong hop le: starRating: So sao danh gia phai tu 1 den 5, comment: Noi dung binh luan khong duoc de trong",
  "data": null,
  "timestamp": "2026-05-25T11:00:00Z"
}
```

##### 13.1.3. Vi du cac response loi nghiep vu

**HTTP 400 - Don hang khong o trang thai cho phep danh gia (ORDER_STATUS_CANNOT_REVIEW)**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Khong the danh gia don hang [AbCdEfGhIjKlMnOpQrStUvWxYz123456] vi don dang o trang thai [Dang giao]. Chi co the danh gia don hang dang o trang thai [Hoan thanh] (status = 3).",
  "data": null,
  "timestamp": "2026-05-25T11:00:00Z"
}
```

**HTTP 400 - Don hang da duoc danh gia roi (ORDER_ALREADY_REVIEWED)**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Don hang [AbCdEfGhIjKlMnOpQrStUvWxYz123456] da duoc danh gia truoc do. Moi don hang chi duoc phep danh gia mot lan.",
  "data": null,
  "timestamp": "2026-05-25T11:00:00Z"
}
```

**HTTP 403 - Khong co quyen danh gia don hang (FORBIDDEN)**:

```json
{
  "success": false,
  "statusCode": 403,
  "message": "Ban khong co quyen danh gia don hang [AbCdEfGhIjKlMnOpQrStUvWxYz123456]. Chi chu nhan cua don hang moi duoc phep danh gia.",
  "data": null,
  "timestamp": "2026-05-25T11:00:00Z"
}
```

**HTTP 404 - Don hang khong ton tai (ORDER_NOT_FOUND)**:

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Khong tim thay don hang voi ID [order_xyz].",
  "data": null,
  "timestamp": "2026-05-25T11:00:00Z"
}
```

---

#### Vi du

##### 13.1.4. Tao danh gia thanh cong

**Request**:

```http
POST http://localhost:8080/api/reviews
Content-Type: application/json

{
  "orderId": "order_001",
  "storeId": "store_001",
  "userId": "user_001",
  "userName": "Khoi",
  "userAvatarUrl": "https://example.com/avatar/user001.jpg",
  "starRating": 5,
  "comment": "Do an rat ngon, giao hang nhanh, dong goi ky luong.",
  "imageUrls": [
    "https://example.com/review/rev001_1.jpg"
  ]
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Tao danh gia thanh cong.",
  "data": {
    "id": "AbCdEfGhIjKlMnOpQrStUvWxYz123456",
    "orderId": "order_001",
    "storeId": "store_001",
    "userId": "user_001",
    "userName": "Khoi",
    "userAvatarUrl": "https://example.com/avatar/user001.jpg",
    "starRating": 5,
    "comment": "Do an rat ngon, giao hang nhanh, dong goi ky luong.",
    "imageUrls": [
      "https://example.com/review/rev001_1.jpg"
    ],
    "createdAt": "2026-05-25T11:00:00Z",
    "updatedAt": "2026-05-25T11:00:00Z"
  },
  "timestamp": "2026-05-25T11:00:00Z"
}
```

##### 13.1.5. Thu tu goi API (Flow)

```
1. Flutter goi POST /api/reviews voi ReviewRequest
   |
2. Server kiem tra du lieu dau vao (validation)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST (Validation)
   |
4. Server truy van document don hang tu collection orders theo orderId
   |
5+-> Don hang khong ton tai -> Tra ve 404 ORDER_NOT_FOUND
   |
6. Server kiem tra userId cua don hang co khop voi userId truyen len
   |
7+-> Khong khop -> Tra ve 403 FORBIDDEN
   |
8. Server doc gia tri trang thai (status) cua don hang
   |
9+-> status != 3 (Hoan thanh) -> Tra ve 400 ORDER_STATUS_CANNOT_REVIEW
   |
10. Server kiem tra don hang da duoc danh gia chua
    (query collection reviews theo orderId)
    |
11+-> Da danh gia -> Tra ve 400 ORDER_ALREADY_REVIEWED
    |
12. Server tao document danh gia moi trong collection reviews
    |
13. Server doc document cua hang tu stores, tinh toan lai rating
    moi = (ratingCu * reviewCountCu + starRatingMoi) / (reviewCountCu + 1)
    reviewCountMoi = reviewCountCu + 1
    |
14. Server cap nhat rating va reviewCount cua cua hang
    |
15. Tra ve 200 voi ReviewDTO da duoc tao
```

---

### 13.2. GET /api/reviews - Lay danh sach danh gia theo cua hang

**Mo ta**: Lay tat ca danh gia cua mot cua hang theo storeId.

**Phan he**: Khach hang

**Muc do truy cap**: Cong khai (chua co xac thuc JWT trong phien ban nay)

---

#### Chi tiet API

**Request Parameters**:

| Parameter | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `storeId` | String | Co | ID cua hang can lay danh gia |

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Lay danh sach danh gia thanh cong.",
  "data": [
    {
      "id": "rev_001",
      "orderId": "order_001",
      "storeId": "store_001",
      "userId": "user_001",
      "userName": "Khoi",
      "userAvatarUrl": "https://example.com/avatar/user001.jpg",
      "starRating": 5,
      "comment": "Do an rat ngon, giao hang nhanh.",
      "imageUrls": [
        "https://example.com/review/rev001_1.jpg"
      ],
      "createdAt": "2026-04-07T00:00:00Z",
      "updatedAt": "2026-04-07T00:00:00Z"
    },
    {
      "id": "rev_002",
      "orderId": "order_002",
      "storeId": "store_001",
      "userId": "user_002",
      "userName": "Minh",
      "userAvatarUrl": "https://example.com/avatar/user002.jpg",
      "starRating": 4,
      "comment": "Do an ngon, nhung giao hang hoi tre.",
      "imageUrls": null,
      "createdAt": "2026-04-08T00:00:00Z",
      "updatedAt": "2026-04-08T00:00:00Z"
    }
  ],
  "timestamp": "2026-05-25T11:00:00Z"
}
```

**Luong xu ly**:

```
1. Flutter goi GET /api/reviews?storeId={storeId}
   |
2. Server truy van collection reviews voi dieu kien storeId
   |
3. Tra ve 200 voi danh sach ReviewDTO
```

---

#### Cau truc du lieu

##### 13.2.1. ReviewRequest (Request Body)

| Thuoc tinh | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `orderId` | String | Co | ID don hang can danh gia |
| `storeId` | String | Co | ID cua hang duoc danh gia |
| `userId` | String | Co | ID nguoi dung khach hang |
| `userName` | String | Co | Ten nguoi danh gia |
| `userAvatarUrl` | String | Khong | URL anh dai dien nguoi danh gia |
| `starRating` | Integer | Co | So sao danh gia (1-5) |
| `comment` | String | Co | Noi dung binh luan danh gia |
| `imageUrls` | List<String> | Khong | Danh sach URL hinh anh kem theo |

##### 13.2.2. ReviewDTO (Response Data)

| Thuoc tinh | Kieu | Mo ta |
| --- | --- | --- |
| `id` | String | ID document trong Firestore (auto generated) |
| `orderId` | String | ID don hang da danh gia |
| `storeId` | String | ID cua hang duoc danh gia |
| `userId` | String | ID nguoi danh gia |
| `userName` | String | Ten nguoi danh gia |
| `userAvatarUrl` | String | URL anh dai dien nguoi danh gia |
| `starRating` | Integer | So sao danh gia (1-5) |
| `comment` | String | Noi dung binh luan danh gia |
| `imageUrls` | List<String> | Danh sach URL hinh anh kem theo |
| `createdAt` | ISO 8601 Timestamp | Thoi diem tao danh gia |
| `updatedAt` | ISO 8601 Timestamp | Thoi diem cap nhat gan nhat |

---

#### Noi dung Swagger UI

Sau khi chay ung dung, truy cap Swagger UI tai:

```
http://localhost:8080/swagger-ui.html
```

Hoac tai noi dung OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```


## 14. API Tim Kiem Mon An Va Quan An (Search)

### Muc luc

- [14.1. GET /api/search - Tim kiem mon an va quan an](#141-get-apisearch---tim-kiem-mon-an-va-quan-an)

---

### 14.1. GET /api/search - Tim kiem mon an va quan an

**Mo ta**: Tim kiem mon an hoac quan an theo tu khoa, loc theo khoang cach toi da 10km tu vi tri nguoi dung, luu lich su tim kiem va sap xep ket qua.

**Phan he**: Khach hang

**Muc do truy cap**: Cong khai (chua co xac thuc JWT trong phien ban nay)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Luu lich su**: Neu `userId` khong null va `query` khong rong, tao document luu `keyword` va `createdAt` vao sub-collection `users/{userId}/search_history`.
2. **Buoc 2 - Tinh khoang cach va loc quan**: Lay toan bo danh sach `stores` tu Firestore. Su dung cong thuc Haversine de tinh khoang cach duong chim bay tu `(userLat, userLng)` den `(lat, lng)` cua tung quan. Chi giu lai cac quan an co khoang cach <= 10.0 km. Luu gia tri `distance` vao Map tam trong bo nho.
3. **Buoc 3 - Loc mon an**: Tu danh sach cac quan an thoa man khoang cach o Buoc 2, lay danh sach cac `products` thuoc ve cac quan nay. Chuyen ten mon an va `query` thanh chu thuong (lowercase) bo dau de so sanh tuong doi (contains). Loc ra nhung mon an co ten chua tu khoa `query` HOAC thuoc ve cua hang co ten chua `query`.
4. **Buoc 4 - Mapping va sap xep**:
   - Map danh sach san pham da loc sang `SearchResultResponse`, gan them thuoc tinh `distance` da tinh o Buoc 2 tuong ung voi `storeId`.
   - Ap dung logic sap xep theo tham so `sortBy` truyen vao.

---

#### Chi tiet API

**Request Parameters**:

| Parameter | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `query` | String | Co | Tu khoa tim kiem (ten mon an hoac ten quan an) |
| `userLat` | Double | Co | Vi do cua dia chi giao hang (VD: 10.8500) |
| `userLng` | Double | Co | Kinh do cua dia chi giao hang (VD: 106.7900) |
| `sortBy` | String | Khong | Chieu sap xep: `priceAsc` (gia tang dan), `priceDesc` (gia giam dan), `ratingDesc` (danh gia giam dan) |
| `userId` | String | Khong | ID nguoi dung de luu lich su tim kiem |

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Tim thay 5 ket qua phu hop.",
  "data": [
    {
      "productId": "prod_001",
      "productName": "Com tam suon bi cha",
      "storeId": "store_001",
      "storeName": "Com tam Phuc Loc Tho",
      "price": 45000.0,
      "rating": 4.8,
      "reviewCount": 500,
      "distance": 2.5,
      "imageUrl": "https://images.unsplash.com/photo-xxx"
    },
    {
      "productId": "prod_002",
      "productName": "Com tam ga xoi mo",
      "storeId": "store_001",
      "storeName": "Com tam Phuc Loc Tho",
      "price": 50000.0,
      "rating": 4.8,
      "reviewCount": 500,
      "distance": 2.5,
      "imageUrl": "https://images.unsplash.com/photo-yyy"
    }
  ],
  "timestamp": "2026-05-26T12:00:00Z"
}
```

---

#### Bang ma loi tra ve

##### 14.1.1. Loi xac thuc dau vao (Validation Error)

| HTTP Status | Truong hop | Mo ta |
| --- | --- | --- |
| 400 | Tu khoa rong hoac null | Tu khoa tim kiem khong duoc de trong |
| 400 | Toa do khong hop le | Toa do nguoi dung (userLat, userLng) khong hop le |

**Vi du loi validation - Tu khoa rong**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Tu khoa tim kiem khong duoc de trong.",
  "data": null,
  "timestamp": "2026-05-26T12:00:00Z"
}
```

**Vi du loi validation - Toa do khong hop le**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Toa do nguoi dung (userLat, userLng) khong hop le.",
  "data": null,
  "timestamp": "2026-05-26T12:00:00Z"
}
```

##### 14.1.2. Loi he thong

| HTTP Status | Truong hop | Mo ta |
| --- | --- | --- |
| 500 | Loi he thong | Loi he thong khi truy van Firestore |

---

#### Vi du

##### 14.1.3. Tim kiem thanh cong

**Request**:

```http
GET http://localhost:8080/api/search?query=com+tam&userLat=10.8500&userLng=106.7900&sortBy=ratingDesc&userId=user_001
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Tim thay 3 ket qua phu hop.",
  "data": [
    {
      "productId": "prod_001",
      "productName": "Com tam suon bi cha",
      "storeId": "store_001",
      "storeName": "Com tam Phuc Loc Tho",
      "price": 45000.0,
      "rating": 4.8,
      "reviewCount": 500,
      "distance": 2.5,
      "imageUrl": "https://images.unsplash.com/photo-xxx"
    },
    {
      "productId": "prod_005",
      "productName": "Com tam rang",
      "storeId": "store_001",
      "storeName": "Com tam Phuc Loc Tho",
      "price": 40000.0,
      "rating": 4.8,
      "reviewCount": 500,
      "distance": 2.5,
      "imageUrl": "https://images.unsplash.com/photo-yyy"
    },
    {
      "productId": "prod_010",
      "productName": "Com rang dui ga",
      "storeId": "store_003",
      "storeName": "Com tam My Ga",
      "price": 55000.0,
      "rating": 4.2,
      "reviewCount": 120,
      "distance": 5.8,
      "imageUrl": "https://images.unsplash.com/photo-zzz"
    }
  ],
  "timestamp": "2026-05-26T12:00:00Z"
}
```

##### 14.1.4. Tim kiem khong co ket qua

**Request**:

```http
GET http://localhost:8080/api/search?query=sushi&userLat=10.8500&userLng=106.7900
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Khong tim thay mon an hoac quan an nao phu hop.",
  "data": [],
  "timestamp": "2026-05-26T12:00:00Z"
}
```

##### 14.1.5. Tim kiem theo gia tang dan

**Request**:

```http
GET http://localhost:8080/api/search?query=tra+suong&userLat=10.8500&userLng=106.7900&sortBy=priceAsc
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Tim thay 4 ket qua phu hop.",
  "data": [
    {
      "productId": "prod_007",
      "productName": "Tra sua tran chau",
      "storeId": "store_002",
      "storeName": "Tra sua Tocotoco",
      "price": 20000.0,
      "rating": 4.5,
      "reviewCount": 300,
      "distance": 1.2,
      "imageUrl": "https://images.unsplash.com/photo-aaa"
    },
    {
      "productId": "prod_008",
      "productName": "Tra sua kem cheese",
      "storeId": "store_002",
      "storeName": "Tra sua Tocotoco",
      "price": 35000.0,
      "rating": 4.5,
      "reviewCount": 300,
      "distance": 1.2,
      "imageUrl": "https://images.unsplash.com/photo-bbb"
    }
  ],
  "timestamp": "2026-05-26T12:00:00Z"
}
```

##### 14.1.6. Thu tu goi API (Flow)

```
1. Flutter goi GET /api/search?query={query}&userLat={lat}&userLng={lng}&sortBy={sortBy}&userId={userId}
   |
2. Server kiem tra du lieu dau vao (query khong rong, toa do khong null)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST
   |
4. Neu userId khong null va query khong rong:
   |   Tao document trong users/{userId}/search_history voi keyword va createdAt
   |
5. Server lay toan bo stores tu Firestore
   |
6. Server tinh khoang cach Haversine cho tung quan
   |   Chi giu lai quan co khoang cach <= 10km
   |
7. Server lay toan bo products tu Firestore
   |
8. Server loc products:
   |   + Chi giu lai products thuoc quan trong pham vi 10km
   |   + Chuyen query va ten mon thanh chu thuong bo dau
   |   + Loc products co ten chua query HOAC cua hang co ten chua query
   |
9. Server map products sang SearchResultResponse
   |   + Gan distance tuong ung voi storeId
   |
10+-> Co sortBy -> Sap xep ket qua (priceAsc / priceDesc / ratingDesc)
   |
11. Tra ve 200 voi danh sach SearchResultResponse
```

##### 14.1.7. Cau truc du lieu tra ve (SearchResultResponse)

| Thuoc tinh | Kieu | Mo ta |
| --- | --- | --- |
| `productId` | String | ID cua san pham (mon an) |
| `productName` | String | Ten mon an |
| `storeId` | String | ID cua cua hang |
| `storeName` | String | Ten cua hang |
| `price` | Double | Gia co so cua mon an (VND) |
| `rating` | Double | Diem danh gia trung binh cua cua hang (0.0 - 5.0) |
| `reviewCount` | Integer | Tong so danh gia cua cua hang |
| `distance` | Double | Khoang cach tu vi tri nguoi dung den cua hang (km) |
| `imageUrl` | String | URL hinh anh mon an |

##### 14.1.8. Cong thuc Haversine

Cong thuc Haversine duoc su dung de tinh khoang cach duong chim bay giua hai diem tren mat dat:

```
a = sin^2(dLat/2) + cos(lat1) * cos(lat2) * sin^2(dLng/2)
c = 2 * atan2(sqrt(a), sqrt(1-a))
khoangCach = R * c

Trong do:
- R = 6371.0 km (ban kinh trai dat)
- lat1, lng1: Toa do nguoi dung
- lat2, lng2: Toa do cua hang
- dLat = lat2 - lat1 (radian)
- dLng = lng2 - lng1 (radian)
```

---

#### Noi dung Swagger UI

Sau khi chay ung dung, truy cap Swagger UI tai:

```
http://localhost:8080/swagger-ui.html
```

Hoac tai noi dung OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

|```
---
## 15. API Xac Thuc (Authentication & Security)

### Muc luc

- [15.1. POST /api/auth/register - Dang ky tai khoan khach hang](#151-post-apiauthregister---dang-ky-tai-khoan-khach-hang)
- [15.2. POST /api/auth/login - Dang nhap](#152-post-apiauthlogin---dang-nhap)
- [15.3. POST /api/auth/send-otp - Gui ma OTP](#153-post-apiauthsend-otp---gui-ma-otp)
- [15.4. POST /api/auth/verify-otp - Xac thuc ma OTP](#154-post-apiauthverify-otp---xac-thuc-ma-otp)
- [15.5. POST /api/auth/reset-password - Dat lai mat khau](#155-post-apiauthreset-password---dat-lai-mat-khau)
- [15.6. POST /api/auth/register-merchant - Dang ky tai khoan nguoi ban](#156-post-apiauthregister-merchant---dang-ky-tai-khoan-nguoi-ban)
- [15.7. GET /api/auth/check-merchant - Kiem tra quyen nguoi ban](#157-get-apiauthcheck-merchant---kiem-tra-quyen-nguoi-ban)

---

### 15.1. POST /api/auth/register - Dang ky tai khoan khach hang

**Mo ta**: Tao tai khoan khach hang moi trong he thong. Tai khoan se co roles mac dinh la [1] (Khach hang). Mat khau duoc ma hoa bang BCrypt truoc khi luu vao Firestore.

**Phan he**: Khach hang

**Muc do truy cap**: Cong khai (khong can xac thuc)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Kiem tra trung lap email**: Truy van collection `users` de kiem tra xem email da ton tai chua. Neu da ton tai, tra ve loi 400.
2. **Buoc 2 - Kiem tra trung lap so dien thoai**: Truy van collection `users` de kiem tra xem so dien thoai da duoc su dung chua. Neu da ton tai, tra ve loi 400.
3. **Buoc 3 - Sinh ID**: Sinh userId moi theo dinh dang `user_XXX` (VD: user_001, user_002...).
4. **Buoc 4 - Ma hoa mat khau**: Su dung BCrypt (PasswordEncoder) de ma hoa mat khau nguoi dung.
5. **Buoc 5 - Tao document**: Tao document moi trong collection `users` voi cac truong: id, email, password (da ma hoa), fullName, phoneNumber, photoUrl (null), roles ([1]), createdAt.
6. **Buoc 6 - Tao JWT**: Su dung JwtTokenProvider de tao JWT token chua userId. Tra ve token cung thong tin nguoi dung.

---

#### Chi tiet API

**Request Headers**:

| Header | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `Content-Type` | String | Co | `application/json` |

**Request Body** (JSON):

```json
{
  "email": "nguoidung@gmail.com",
  "password": "password123",
  "fullName": "Nguyen Van A",
  "phoneNumber": "0123456789"
}
```

**Cac truong bat buoc**: `email`, `password`, `fullName`, `phoneNumber`

**Validation**:

| Truong | Quy tac |
| --- | --- |
| `email` | Dinh dang email hop le, khong trung voi email da dang ky |
| `password` | It nhat 6 ky tu |
| `fullName` | Khong duoc de trong |
| `phoneNumber` | Bat dau bang 0, 10-11 chu so |

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Dang ky tai khoan thanh cong.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMyIsImlhdCI6MTc1MDAwMDAwMH0...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "user_003",
      "email": "nguoidung@gmail.com",
      "fullName": "Nguyen Van A",
      "phoneNumber": "0123456789",
      "photoUrl": null,
      "roles": [1]
    }
  },
  "timestamp": "2026-05-26T14:00:00Z"
}
```

---

#### Bang ma loi tra ve

##### 15.1.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
| --- | --- | --- | --- |
| 400 | EMAIL_EXISTS | Email da ton tai | "Email da ton tai trong he thong. Vui long su dung email khac." |
| 400 | PHONE_EXISTS | So dien thoai da duoc su dung | "So dien thoai da duoc su dung. Vui long su dung so dien thoai khac." |
| 500 | SYSTEM_ERROR | Loi he thong khi truy van Firestore | "Da xay ra loi khong mong muon. Vui long thu lai sau." |

##### 15.1.2. Loi xac thuc dau vao (Validation Error)

| HTTP Status | Truong hop | Mo ta |
| --- | --- | --- |
| 400 | Du lieu khong hop le | Cac truong bat buoc bi trong hoac sai dinh dang |

**Vi du loi validation**:

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Du lieu khong hop le: email: Email khong dung dinh dang, password: Mat khau phai co it nhat 6 ky tu, phoneNumber: So dien thoai khong dung dinh dang (bat dau bang 0, 10-11 chu so)",
  "data": null,
  "timestamp": "2026-05-26T14:00:00Z"
}
```

---

#### Vi du

##### 15.1.3. Dang ky tai khoan thanh cong

**Request**:

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "nguoidung@gmail.com",
  "password": "password123",
  "fullName": "Nguyen Van A",
  "phoneNumber": "0123456789"
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Dang ky tai khoan thanh cong.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMyIsImlhdCI6MTc1MDAwMDAwMH0...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "user_003",
      "email": "nguoidung@gmail.com",
      "fullName": "Nguyen Van A",
      "phoneNumber": "0123456789",
      "photoUrl": null,
      "roles": [1]
    }
  },
  "timestamp": "2026-05-26T14:00:00Z"
}
```

##### 15.1.4. Thu tu goi API (Flow)

```
1. Flutter goi POST /api/auth/register voi RegisterRequest
   |
2. Server kiem tra du lieu dau vao (validation)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST (Validation)
   |
4. Server truy van collection users theo email
   |
5+-> Email da ton tai -> Tra ve 400 "Email da ton tai trong he thong..."
   |
6. Server truy van collection users theo phoneNumber
   |
7+-> So dien thoai da ton tai -> Tra ve 400 "So dien thoai da duoc su dung..."
   |
8. Server sinh userId moi (user_XXX)
   |
9. Server ma hoa mat khau bang BCrypt
   |
10. Server tao document moi trong collection users
   |
11. Server tao JWT token chua userId (hieu luc 24 gio)
   |
12. Tra ve 200 voi AuthResponse (token + user info)
```

---

### 15.2. POST /api/auth/login - Dang nhap

**mo ta**: Dang nhap bang email va mat khau. Neu thanh cong, tra ve JWT token chua userId de su dung cho cac API can xac thuc.

**Phan he**: Khach hang

**Muc do truy cap**: Cong khai (khong can xac thuc)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Tim tai khoan**: Truy van collection `users` theo email. Neu khong tim thay, tra ve loi 400.
2. **Buoc 2 - Kiem tra mat khau**: Su dung BCrypt PasswordEncoder de so sanh mat khau nguoi dung cung cap voi password da luu trong Firestore. Neu khong khop, tra ve loi 400.
3. **Buoc 3 - Tao JWT**: Neu mat khau dung, tao JWT token chua userId. Tra ve token cung thong tin nguoi dung.

---

#### Chi tiet API

**Request Headers**:

| Header | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `Content-Type` | String | Co | `application/json` |

**Request Body** (JSON):

```json
{
  "email": "nguoidung@gmail.com",
  "password": "password123"
}
```

**Cac truong bat buoc**: `email`, `password`

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Dang nhap thanh cong.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsImlhdCI6MTc1MDAwMDAwMH0...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "user_001",
      "email": "khachhang@gmail.com",
      "fullName": "Khoi",
      "phoneNumber": "0123456789",
      "photoUrl": "https://example.com/avatar/user001.jpg",
      "roles": [1, 2, 3]
    }
  },
  "timestamp": "2026-05-26T14:00:00Z"
}
```

---

#### Bang ma loi tra ve

##### 15.2.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
| --- | --- | --- | --- |
| 400 | INVALID_CREDENTIALS | Email khong ton tai | "Email hoac mat khau khong dung." |
| 400 | INVALID_CREDENTIALS | Mat khau khong dung | "Email hoac mat khau khong dung." |
| 500 | SYSTEM_ERROR | Loi he thong khi truy van Firestore | "Da xay ra loi khong mong muon. Vui long thu lai sau." |

##### 15.2.2. Loi xac thuc dau vao (Validation Error)

| HTTP Status | Truong hop | Mo ta |
| --- | --- | --- |
| 400 | Du lieu khong hop le | Cac truong bat buoc bi trong hoac sai dinh dang |

---

#### Vi du

##### 15.2.3. Dang nhap thanh cong

**Request**:

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "khachhang@gmail.com",
  "password": "password123"
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Dang nhap thanh cong.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsImlhdCI6MTc1MDAwMDAwMH0...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "user_001",
      "email": "khachhang@gmail.com",
      "fullName": "Khoi",
      "phoneNumber": "0123456789",
      "photoUrl": "https://example.com/avatar/user001.jpg",
      "roles": [1, 2, 3]
    }
  },
  "timestamp": "2026-05-26T14:00:00Z"
}
```

##### 15.2.4. Dang nhap that bai - mat khau sai

**Request**:

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "khachhang@gmail.com",
  "password": "saimatkhau"
}
```

**Response** (HTTP 400):

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Email hoac mat khau khong dung.",
  "data": null,
  "timestamp": "2026-05-26T14:00:00Z"
}
```

##### 15.2.5. Thu tu goi API (Flow)

```
1. Flutter goi POST /api/auth/login voi LoginRequest
   |
2. Server kiem tra du lieu dau vao (validation)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST (Validation)
   |
4. Server truy van collection users theo email
   |
5+-> Khong tim thay email -> Tra ve 400 "Email hoac mat khau khong dung."
   |
6. Server so sanh mat khau bang BCrypt
   |
7+-> Mat khau khong dung -> Tra ve 400 "Email hoac mat khau khong dung."
   |
8. Server tao JWT token chua userId (hieu luc 24 gio)
   |
9. Tra ve 200 voi AuthResponse (token + user info)
```

---

### 15.3. POST /api/auth/send-otp - Gui ma OTP

**mo ta**: Gui ma OTP 6 chu so den email hoac so dien thoai de khoi phuc mat khau. Ma OTP co hieu luc 5 phut (300 giay). Trong moi truong dev/demo, ma OTP se in ra console.

**Phan he**: Khach hang

**Muc do truy cap**: Cong khai (khong can xac thuc)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Kiem tra tai khoan ton tai**: Kiem tra xem email hoac so dien thoai co ton tai trong collection `users` hay khong. Neu khong ton tai, tra ve loi 400.
2. **Buoc 2 - Sinh ma OTP**: Tao ma OTP 6 chu so ngau nhien (000000 - 999999).
3. **Buoc 3 - Luu OTP**: Luu ma OTP vao bo nho tam (ConcurrentHashMap) voi key la email/so dien thoai, value la OtpEntry(otp, userId, expiresAtMs). TTL = 300 giay.
4. **Buoc 4 - In ra console**: Trong moi truong dev, in ma OTP ra console de thuan tien test.

---

#### Chi tiet API

**Request Headers**:

| Header | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `Content-Type` | String | Co | `application/json` |

**Request Body** (JSON):

```json
{
  "emailOrPhone": "nguoidung@gmail.com"
}
```

**Cac truong bat buoc**: `emailOrPhone`

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Ma OTP da duoc gui. Vui long kiem tra email/so dien thoai.",
  "data": {
    "emailOrPhone": "nguoidung@gmail.com",
    "message": "Ma OTP da duoc gui. Vui long kiem tra email/so dien thoai.",
    "otpCode": "847291",
    "expiresInSeconds": 300
  },
  "timestamp": "2026-05-26T14:00:00Z"
}
```

---

#### Bang ma loi tra ve

##### 15.3.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
| --- | --- | --- | --- |
| 400 | USER_NOT_FOUND | Email/so dien thoai khong ton tai | "Khong tim thay tai khoan voi email hoac so dien thoai nay." |

##### 15.3.2. Loi xac thuc dau vao (Validation Error)

| HTTP Status | Truong hop | Mo ta |
| --- | --- | --- |
| 400 | Du lieu khong hop le | emailOrPhone bi trong |

---

#### Vi du

##### 15.3.3. Gui OTP thanh cong

**Request**:

```http
POST http://localhost:8080/api/auth/send-otp
Content-Type: application/json

{
  "emailOrPhone": "nguoidung@gmail.com"
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Ma OTP da duoc gui. Vui long kiem tra email/so dien thoai.",
  "data": {
    "emailOrPhone": "nguoidung@gmail.com",
    "message": "Ma OTP da duoc gui. Vui long kiem tra email/so dien thoai.",
    "otpCode": "847291",
    "expiresInSeconds": 300
  },
  "timestamp": "2026-05-26T14:00:00Z"
}
```

##### 15.3.4. Gui OTP - Tai khoan khong ton tai

**Request**:

```http
POST http://localhost:8080/api/auth/send-otp
Content-Type: application/json

{
  "emailOrPhone": "khongtontai@gmail.com"
}
```

**Response** (HTTP 400):

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Khong tim thay tai khoan voi email hoac so dien thoai nay.",
  "data": null,
  "timestamp": "2026-05-26T14:00:00Z"
}
```

##### 15.3.5. Thu tu goi API (Flow)

```
1. Flutter goi POST /api/auth/send-otp voi OtpSendRequest
   |
2. Server kiem tra du lieu dau vao (emailOrPhone khong trong)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST
   |
4. Server kiem tra email hoac so dien thoai co ton tai trong users khong
   |
5+-> Khong ton tai -> Tra ve 400 "Khong tim thay tai khoan..."
   |
6. Server sinh ma OTP 6 chu so ngau nhien
   |
7. Server luu OTP vao bo nho tam voi TTL 300 giay
   |
8. Server in ma OTP ra console (dev mode)
   |
9. Tra ve 200 voi OtpSendResponse
```

##### 15.3.6. Luu y ve OTP trong moi truong Production

Trong moi truong production, can tich hop voi cac dich vu OTP thuc te nhu:
- SMS Gateway: Twilio, VNPT, Viettel...
- Email Service: SendGrid, AWS SES, Firebase Cloud Messaging...

Ma OTP trong `otpCode` chi duoc tra ve trong moi truong dev/demo.

---

### 15.4. POST /api/auth/verify-otp - Xac thuc ma OTP

**mo ta**: Xac thuc ma OTP nhan duoc. Neu dung, tra ve token tam thoi (hieu luc 5 phut) de su dung cho reset-password.

**Phan he**: Khach hang

**Muc do truy cap**: Cong khai (khong can xac thuc)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Tim OTP**: Doc OTP tu bo nho tam theo email/so dien thoai. Neu khong co, tra ve loi 400.
2. **Buoc 2 - Kiem tra han su dung**: So sanh thoi gian hien tai voi expiresAtMs. Neu da het han, xoa OTP khoi bo nho va tra ve loi 400.
3. **Buoc 3 - Kiem tra ma OTP**: So sanh ma OTP nguoi dung cung cap voi ma da luu. Neu khong khop, tra ve loi 400.
4. **Buoc 4 - Tao token tam thoi**: Tao JWT token tam thoi chua userId voi claim `type = "TEMP_TOKEN"` va hieu luc 5 phut. Xoa OTP khoi bo nho.
5. **Buoc 5 - Tra ve**: Tra ve token tam thoi de nguoi dung su dung cho reset-password.

---

#### Chi tiet API

**Request Headers**:

| Header | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `Content-Type` | String | Co | `application/json` |

**Request Body** (JSON):

```json
{
  "emailOrPhone": "nguoidung@gmail.com",
  "otpCode": "847291"
}
```

**Cac truong bat buoc**: `emailOrPhone`, `otpCode`

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Xac thuc OTP thanh cong.",
  "data": {
    "tempToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9LRU4ifQ...",
    "tokenType": "Bearer",
    "expiresIn": 300000,
    "expiresAt": "2026-05-26T14:05:00Z"
  },
  "timestamp": "2026-05-26T14:00:00Z"
}
```

---

#### Bang ma loi tra ve

##### 15.4.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
| --- | --- | --- | --- |
| 400 | OTP_INVALID | Khong co ma OTP nao duoc gui | "Ma OTP khong hop le hoac da het han. Vui long gui lai ma OTP." |
| 400 | OTP_EXPIRED | Ma OTP da het han | "Ma OTP da het han. Vui long gui lai ma OTP." |
| 400 | OTP_MISMATCH | Ma OTP khong dung | "Ma OTP khong dung. Vui long thu lai." |

##### 15.4.2. Loi xac thuc dau vao (Validation Error)

| HTTP Status | Truong hop | Mo ta |
| --- | --- | --- |
| 400 | Du lieu khong hop le | Cac truong bat buoc bi trong |

---

#### Vi du

##### 15.4.3. Xac thuc OTP thanh cong

**Request**:

```http
POST http://localhost:8080/api/auth/verify-otp
Content-Type: application/json

{
  "emailOrPhone": "nguoidung@gmail.com",
  "otpCode": "847291"
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Xac thuc OTP thanh cong.",
  "data": {
    "tempToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9LRU4ifQ...",
    "tokenType": "Bearer",
    "expiresIn": 300000,
    "expiresAt": "2026-05-26T14:05:00Z"
  },
  "timestamp": "2026-05-26T14:00:00Z"
}
```

##### 15.4.4. Xac thuc OTP - Ma khong dung

**Request**:

```http
POST http://localhost:8080/api/auth/verify-otp
Content-Type: application/json

{
  "emailOrPhone": "nguoidung@gmail.com",
  "otpCode": "000000"
}
```

**Response** (HTTP 400):

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Ma OTP khong dung. Vui long thu lai.",
  "data": null,
  "timestamp": "2026-05-26T14:00:00Z"
}
```

##### 15.4.5. Thu tu goi API (Flow)

```
1. Flutter goi POST /api/auth/verify-otp voi OtpVerifyRequest
   |
2. Server kiem tra du lieu dau vao (validation)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST
   |
4. Server doc OTP tu bo nho tam theo emailOrPhone
   |
5+-> Khong co OTP (da xoa hoac chua gui) -> Tra ve 400 "Ma OTP khong hop le..."
   |
6. Server kiem tra han su dung cua OTP
   |
7+-> OTP da het han -> Xoa OTP, tra ve 400 "Ma OTP da het han..."
   |
8. Server so sanh ma OTP
   |
9+-> Ma khong khop -> Tra ve 400 "Ma OTP khong dung..."
   |
10. Server tao JWT token tam thoi (hieu luc 5 phut)
   |
11. Server xoa OTP khoi bo nho tam
   |
12. Tra ve 200 voi OtpVerifyResponse (tempToken)
```

---

### 15.5. POST /api/auth/reset-password - Dat lai mat khau

**mo ta**: Dat lai mat khau moi sau khi xac thuc OTP thanh cong. Token tam thoi co hieu luc 5 phut sau khi xac thuc OTP.

**Phan he**: Khach hang

**Muc do truy cap**: Cong khai (khong can xac thuc)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Xac thuc token tam thoi**: Su dung JwtTokenProvider de xac thuc token tam thoi. Neu khong hop le hoac da het han, tra ve loi 400.
2. **Buoc 2 - Trich xuat userId**: Lay userId tu token tam thoi. Neu khong trich xuat duoc, tra ve loi 400.
3. **Buoc 3 - Kiem tra tai khoan ton tai**: Truy van collection `users` theo userId. Neu khong ton tai, tra ve loi 400.
4. **Buoc 4 - Cap nhat mat khau**: Ma hoa mat khau moi bang BCrypt va cap nhat vao document `users/{userId}`.

---

#### Chi tiet API

**Request Headers**:

| Header | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `Content-Type` | String | Co | `application/json` |

**Request Body** (JSON):

```json
{
  "tempToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9LRU4ifQ...",
  "newPassword": "newpassword123"
}
```

**Cac truong bat buoc**: `tempToken`, `newPassword`

**Validation**: `newPassword` phai it nhat 6 ky tu

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Dat lai mat khau thanh cong.",
  "data": null,
  "timestamp": "2026-05-26T14:05:00Z"
}
```

---

#### Bang ma loi tra ve

##### 15.5.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
| --- | --- | --- | --- |
| 400 | TOKEN_INVALID | Token tam thoi khong hop le | "Token khong hop le hoac da het han. Vui long gui lai ma OTP." |
| 400 | TOKEN_EXPIRED | Token tam thoi da het han | "Token khong hop le hoac da het han. Vui long gui lai ma OTP." |
| 400 | USER_NOT_FOUND | Tai khoan khong ton tai | "Tai khoan khong ton tai." |
| 500 | SYSTEM_ERROR | Loi he thong khi truy van Firestore | "Da xay ra loi khong mong muon. Vui long thu lai sau." |

##### 15.5.2. Loi xac thuc dau vao (Validation Error)

| HTTP Status | Truong hop | Mo ta |
| --- | --- | --- |
| 400 | Du lieu khong hop le | newPassword it hon 6 ky tu |

---

#### Vi du

##### 15.5.3. Dat lai mat khau thanh cong

**Request**:

```http
POST http://localhost:8080/api/auth/reset-password
Content-Type: application/json

{
  "tempToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9LRU4ifQ...",
  "newPassword": "newpassword123"
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Dat lai mat khau thanh cong.",
  "data": null,
  "timestamp": "2026-05-26T14:05:00Z"
}
```

##### 15.5.4. Dat lai mat khau - Token da het han

**Request**:

```http
POST http://localhost:8080/api/auth/reset-password
Content-Type: application/json

{
  "tempToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSIsInR5cGUiOiJUTVBfVE9LRU4ifQ...",
  "newPassword": "newpassword123"
}
```

**Response** (HTTP 400):

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Token khong hop le hoac da het han. Vui long gui lai ma OTP.",
  "data": null,
  "timestamp": "2026-05-26T14:05:00Z"
}
```

##### 15.5.5. Thu tu goi API (Flow)

```
1. Flutter goi POST /api/auth/reset-password voi ResetPasswordRequest
   |
2. Server kiem tra du lieu dau vao (validation)
   |
3+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST
   |
4. Server xac thuc token tam thoi bang JwtTokenProvider
   |
5+-> Token khong hop le -> Tra ve 400 "Token khong hop le..."
   |
6. Server trich xuat userId tu token
   |
7+-> Khong trich xuat duoc -> Tra ve 400 "Token khong hop le..."
   |
8. Server tim tai khoan trong collection users theo userId
   |
9+-> Tai khoan khong ton tai -> Tra ve 400 "Tai khoan khong ton tai."
   |
10. Server ma hoa mat khau moi bang BCrypt
   |
11. Server cap nhat password va updatedAt vao document users/{userId}
   |
12. Tra ve 200 thanh cong
```

---

### 15.6. POST /api/auth/register-merchant - Dang ky tai khoan nguoi ban

**mo ta**: Tao tai khoan nguoi ban moi trong he thong. Tai khoan se co roles mac dinh la [3] (Nguoi ban).

**Phan he**: Nguoi ban

**Muc do truy cap**: Cong khai (khong can xac thuc)

---

#### Chi tiet API

**Request Headers**:

| Header | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `Content-Type` | String | Co | `application/json` |

**Request Body** (JSON):

```json
{
  "email": "nguiban@gmail.com",
  "password": "password123",
  "fullName": "Cua hang An Giang",
  "phoneNumber": "0987654321"
}
```

**Cac truong bat buoc**: `email`, `password`, `fullName`, `phoneNumber`

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Dang ky tai khoan nguoi ban thanh cong.",
  "data": {
    "uid": "user_004",
    "message": "Dang ky tai khoan nguoi ban thanh cong"
  },
  "timestamp": "2026-05-26T14:00:00Z"
}
```

---

#### Bang ma loi tra ve

##### 15.6.1. Loi nghiep vu (Business Error)

| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
| --- | --- | --- | --- |
| 400 | EMAIL_EXISTS | Email da ton tai | "Email da ton tai trong he thong. Vui long su dung email khac." |
| 400 | PHONE_EXISTS | So dien thoai da duoc su dung | "So dien thoai da duoc su dung. Vui long su dung so dien thoai khac." |
| 500 | SYSTEM_ERROR | Loi he thong | "Da xay ra loi khong mong muon. Vui long thu lai sau." |

---

### 15.7. GET /api/auth/check-merchant - Kiem tra quyen nguoi ban

**mo ta**: Kiem tra xem tai khoan co quyen nguoi ban (role = 3) hay khong.

**Phan he**: Nguoi ban

**Muc do truy cap**: Cong khai (khong can xac thuc)

---

#### Chi tiet API

**Request Parameters**:

| Parameter | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `uid` | String | Co | ID tai khoan nguoi dung |

**Response thanh cong** (HTTP 200):

```json
{
  "isMerchant": true,
  "storeId": "store_001"
}
```

---

## 16. Cau Truc Du Lieu - Xac Thuc

### 16.1. RegisterRequest (Request Body)

| Thuoc tinh | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `email` | String | Co | Dia chi email (dinh dang hop le) |
| `password` | String | Co | Mat khau (it nhat 6 ky tu) |
| `fullName` | String | Co | Ho va ten day du |
| `phoneNumber` | String | Co | So dien thoai (bat dau bang 0, 10-11 chu so) |

### 16.2. LoginRequest (Request Body)

| Thuoc tinh | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `email` | String | Co | Dia chi email |
| `password` | String | Co | Mat khau dang nhap |

### 16.3. OtpSendRequest (Request Body)

| Thuoc tinh | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `emailOrPhone` | String | Co | Email hoac so dien thoai can khoi phuc mat khau |

### 16.4. OtpVerifyRequest (Request Body)

| Thuoc tinh | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `emailOrPhone` | String | Co | Email hoac so dien thoai da nhan ma OTP |
| `otpCode` | String | Co | Ma OTP 6 chu so |

### 16.5. ResetPasswordRequest (Request Body)

| Thuoc tinh | Kieu | Bat buoc | Mo ta |
| --- | --- | --- | --- |
| `tempToken` | String | Co | Token tam thoi nhan duoc sau khi xac thuc OTP |
| `newPassword` | String | Co | Mat khau moi (it nhat 6 ky tu) |

### 16.6. AuthResponse (Response Data)

| Thuoc tinh | Kieu | Mo ta |
| --- | --- | --- |
| `token` | String | Token JWT truy cap |
| `tokenType` | String | Loai token (luon la "Bearer") |
| `expiresIn` | Long | Thoi gian het han cua token (miliseconds) |
| `user` | UserResponse | Thong tin nguoi dung |

### 16.7. UserResponse (Response Data)

| Thuoc tinh | Kieu | Mo ta |
| --- | --- | --- |
| `id` | String | ID tai khoan nguoi dung |
| `email` | String | Dia chi email |
| `fullName` | String | Ho va ten day du |
| `phoneNumber` | String | So dien thoai di dong |
| `photoUrl` | String | URL anh dai dien (co the null) |
| `roles` | List<Integer> | Danh sach quyen: 1=Khach hang, 2=Tai xe, 3=Nguoi ban, 4=Admin |

### 16.8. OtpSendResponse (Response Data)

| Thuoc tinh | Kieu | Mo ta |
| --- | --- | --- |
| `emailOrPhone` | String | Email hoac so dien thoai nhan ma OTP |
| `message` | String | Thong bao ket qua |
| `otpCode` | String | Ma OTP (chi hien thi trong dev/demo) |
| `expiresInSeconds` | Integer | Thoi gian het han cua OTP (giay) |

### 16.9. OtpVerifyResponse (Response Data)

| Thuoc tinh | Kieu | Mo ta |
| --- | --- | --- |
| `tempToken` | String | Token tam thoi de dat lai mat khau |
| `tokenType` | String | Loai token (luon la "Bearer") |
| `expiresIn` | Long | Thoi gian het han (miliseconds) |
| `expiresAt` | String | Thoi gian het han (ISO 8601) |

---

## 17. Cau Hinh Spring Security & JWT

### 17.1. Cau hinh SecurityConfig

- Tat CSRF (AbstractHttpConfigurer::disable)
- SessionCreationPolicy = STATELESS (khong su dung session)
- Cho phep truy cap cong khai: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- Tat ca cac endpoint con lai yeu cau xac thuc (authenticated)
- JwtAuthenticationFilter chay truoc UsernamePasswordAuthenticationFilter

### 17.2. Cau hinh JWT (application.properties)

```properties
jwt.secret=FoodGoJwtSecretKey2026Nam3Ki2VeryLongAndSecure256BitSecretKeyForSigningTokens
jwt.expiration=86400000
jwt.bearer-prefix=Bearer
```

| Thuoc tinh | Mo ta |
| --- | --- |
| `jwt.secret` | Chuoi bi mat (secret) ky va xac thuc JWT (phai dai 256 bit) |
| `jwt.expiration` | Thoi gian hieu luc access token (miliseconds, mac dinh 24 gio) |
| `jwt.bearer-prefix` | Tien to Bearer token trong header Authorization |

### 17.3. Cac gia tri roles

| Gia tri | Ten | Mo ta |
| --- | --- | --- |
| 1 | Khach hang | Tai khoan khach hang thong thuong |
| 2 | Tai xe | Tai xe giao hang |
| 3 | Nguoi ban | Chu cua hang/quan an |
| 4 | Admin | Quan tri vien he thong |

### 17.4. Thu tu goi API (Auth Flow)

```
DANG KY:
POST /api/auth/register -> Tao user (BCrypt) + JWT -> Login ngay

DANG NHAP:
POST /api/auth/login -> BCrypt verify -> JWT -> Su dung JWT cho cac API tiep theo

QUEN MAT KHAU:
1. POST /api/auth/send-otp -> Gui OTP (in console trong dev)
2. POST /api/auth/verify-otp -> Xac thuc OTP -> Tra ve tempToken (5 phut)
3. POST /api/auth/reset-password -> Dat lai mat khau (BCrypt)

SU DUNG TOKEN:
- Header: Authorization: Bearer <jwt_token>
- JwtAuthenticationFilter xac thuc token -> Lay userId -> Dat vao SecurityContext
```

---

## 18. Noi dung Swagger UI

Sau khi chay ung dung, truy cap Swagger UI tai:

```
http://localhost:8080/swagger-ui.html
```

Hoac tai noi dung OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

**Huong dan su dung Bearer Token tren Swagger UI**:

1. Mo Swagger UI
2. Chon endpoint muon test (VD: POST /api/auth/register)
3. Nhap du lieu request
4. Click "Execute"
5. Lay token tu response
6. Click nut "Authorize" (o goc phai man hinh)
7. Nhap "Bearer <token>" vao o BearerAuth
8. Click "Authorize" de xac nhan
9. Cac API can xac thuc bay gio se su dung token nay

```
VD: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSJ9...
```

---

## 19. API Quan Ly Ho So (Profile)

### Muc luc

- [19.1. PUT /api/customers/profile - Cap nhat thong tin ho so](#191-put-apicustomersprofile---cap-nhat-thong-tin-ho-so)
- [19.2. PUT /api/customers/password - Doi mat khau chu dong](#192-put-apicustomerspassword---doi-mat-khau-chu-dong)

---

### 19.1. PUT /api/customers/profile - Cap nhat thong tin ho so

**Mo ta**: Cap nhat ho va ten va anh dai dien cua tai khoan dang nhap. Khong cho phep thay doi email hoac so dien thoai tai day.

**Phan he**: Khach hang

**Muc do truy cap**: Yeu cau xac thuc JWT (Bearer Token)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Xac thuc nguoi dung**: Trich xuat userId tu JWT token trong header Authorization. Neu token khong hop le hoac khong co, tra ve loi 401.
2. **Buoc 2 - Tim tai khoan**: Truy van document trong collection `users` theo userId. Neu khong ton tai, tra ve loi 404.
3. **Buoc 3 - Cap nhat thong tin**: Chi cap nhat cac truong `fullName` va `photoUrl` (neu co gia tri). Dong thoi cap nhat `updatedAt` voi thoi diem hien tai.
4. **Buoc 4 - Tra ve ket qua**: Tra ve thong tin tai khoan da duoc cap nhat.

---

#### Chi tiet API

**Request Headers**:

|| Header           | Kieu   | Bat buoc | Mo ta                    |
|| --- | ------ | -------- | ------------------------- |
|| `Content-Type`    | String | Co       | `application/json`         |
|| `Authorization`   | String | Co       | `Bearer <jwt_token>`       |

**Request Body** (JSON):

```json
{
  "fullName": "Nguyen Van A",
  "avatarUrl": "https://example.com/avatar/user001.jpg"
}
```

**Cac truong bat buoc**: Khong co (tat ca deu tuy chon)
**Cac truong tuy chon**: `fullName`, `avatarUrl`

**Validation**:

|| Truong | Quy tac |
|| --- | --- |
|| `fullName` | Khong duoc vuot qua 100 ky tu |
|| `avatarUrl` | URL hop le (neu duoc truyen) |

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Cap nhat ho so thanh cong.",
  "data": {
    "id": "user_001",
    "email": "khachhang@gmail.com",
    "fullName": "Nguyen Van A",
    "phoneNumber": "0123456789",
    "photoUrl": "https://example.com/avatar/user001.jpg",
    "roles": [1, 2, 3]
  },
  "timestamp": "2026-05-26T15:00:00Z"
}
```

---

#### Bang ma loi tra ve

##### 19.1.1. Loi nghiep vu (Business Error)

|| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
|| --- | --- | --- | --- |
|| 404 | USER_NOT_FOUND | Tai khoan khong ton tai | "Khong tim thay tai khoan voi ID: [xxx]." |
|| 500 | SYSTEM_ERROR | Loi he thong khi truy van Firestore | "Da xay ra loi khong mong muon. Vui long thu lai sau." |

##### 19.1.2. Loi xac thuc (Authentication Error)

|| HTTP Status | Truong hop | Mo ta |
|| --- | --- | --- |
|| 401 | Token khong hop le hoac khong co | "Chua xac thuc. Vui long dang nhap de tiep tuc." |

##### 19.1.3. Loi xac thuc dau vao (Validation Error)

|| HTTP Status | Truong hop | Mo ta |
|| --- | --- | --- |
|| 400 | Du lieu khong hop le | Cac truong vuot qua gioi han cho phep |

---

#### Vi du

##### 19.1.4. Cap nhat ho so thanh cong

**Request**:

```http
PUT http://localhost:8080/api/customers/profile
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSJ9...

{
  "fullName": "Nguyen Van A",
  "avatarUrl": "https://example.com/avatar/user001.jpg"
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Cap nhat ho so thanh cong.",
  "data": {
    "id": "user_001",
    "email": "khachhang@gmail.com",
    "fullName": "Nguyen Van A",
    "phoneNumber": "0123456789",
    "photoUrl": "https://example.com/avatar/user001.jpg",
    "roles": [1, 2, 3]
  },
  "timestamp": "2026-05-26T15:00:00Z"
}
```

##### 19.1.5. Cap nhat chi ho ten

**Request**:

```http
PUT http://localhost:8080/api/customers/profile
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSJ9...

{
  "fullName": "Tran Thi B"
}
```

##### 19.1.6. Cap nhat chi anh dai dien

**Request**:

```http
PUT http://localhost:8080/api/customers/profile
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSJ9...

{
  "avatarUrl": "https://example.com/avatar/new-avatar.jpg"
}
```

##### 19.1.7. Loi 401 - Chua xac thuc

**Response** (HTTP 401):

```json
{
  "success": false,
  "statusCode": 401,
  "message": "Chua xac thuc. Vui long dang nhap de tiep tuc.",
  "data": null,
  "timestamp": "2026-05-26T15:00:00Z"
}
```

##### 19.1.8. Loi 404 - Tai khoan khong ton tai

**Response** (HTTP 404):

```json
{
  "success": false,
  "statusCode": 404,
  "message": "Khong tim thay tai khoan voi ID: user_xyz.",
  "data": null,
  "timestamp": "2026-05-26T15:00:00Z"
}
```

##### 19.1.9. Thu tu goi API (Flow)

```
1. Flutter goi PUT /api/customers/profile voi Authorization header
   |
2. Server trich xuat JWT token tu header Authorization
   |
3+-> Token khong hop le hoac khong co -> Tra ve 401 "Chua xac thuc..."
   |
4. Server trich xuat userId tu JWT token
   |
5. Server truy van document trong collection users theo userId
   |
6+-> Tai khoan khong ton tai -> Tra ve 404 USER_NOT_FOUND
   |
7. Server kiem tra du lieu dau vao (validation)
   |
8+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST (Validation)
   |
9. Server cap nhat fullName va photoUrl (neu co) trong Firestore
   |
10. Server cap nhat updatedAt voi thoi diem hien tai
   |
11. Tra ve 200 voi UserResponse da duoc cap nhat
```

##### 19.1.10. Cau truc du lieu tra ve (UserResponse)

|| Thuoc tinh | Kieu | Mo ta |
|| --- | --- | --- |
|| `id` | String | ID tai khoan nguoi dung |
|| `email` | String | Dia chi email |
|| `fullName` | String | Ho va ten day du |
|| `phoneNumber` | String | So dien thoai di dong |
|| `photoUrl` | String | URL anh dai dien (co the null) |
|| `roles` | List<Integer> | Danh sach quyen: 1=Khach hang, 2=Tai xe, 3=Nguoi ban, 4=Admin |

---

### 19.2. PUT /api/customers/password - Doi mat khau chu dong

**Mo ta**: Doi mat khau cu sang mat khau moi. Yeu cau nhap dung mat khau cu de xac nhan truoc khi dat mat khau moi.

**Phan he**: Khach hang

**Muc do truy cap**: Yeu cau xac thuc JWT (Bearer Token)

---

#### Cac quy tac nghiep vu (Business Rules)

1. **Buoc 1 - Xac thuc nguoi dung**: Trich xuat userId tu JWT token trong header Authorization. Neu token khong hop le hoac khong co, tra ve loi 401.
2. **Buoc 2 - Tim tai khoan**: Truy van document trong collection `users` theo userId. Neu khong ton tai, tra ve loi 404.
3. **Buoc 3 - Xac thuc mat khau cu**: Su dung PasswordEncoder.matches() de so sanh mat khau cu nguoi dung cung cap voi password da luu trong Firestore. Neu khong khop, tra ve loi 400.
4. **Buoc 4 - Ma hoa va luu mat khau moi**: Su dung BCrypt (PasswordEncoder) de ma hoa mat khau moi, sau do luu de vao document `users/{userId}` cung voi updatedAt.

---

#### Chi tiet API

**Request Headers**:

|| Header           | Kieu   | Bat buoc | Mo ta                    |
|| --- | ------ | -------- | ------------------------- |
|| `Content-Type`    | String | Co       | `application/json`         |
|| `Authorization`   | String | Co       | `Bearer <jwt_token>`       |

**Request Body** (JSON):

```json
{
  "oldPassword": "matkhaucu123",
  "newPassword": "matkhaumoi123"
}
```

**Cac truong bat buoc**: `oldPassword`, `newPassword`

**Validation**:

|| Truong | Quy tac |
|| --- | --- |
|| `oldPassword` | Khong duoc de trong |
|| `newPassword` | Khong duoc de trong, it nhat 6 ky tu |

**Response thanh cong** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Doi mat khau thanh cong.",
  "data": null,
  "timestamp": "2026-05-26T15:05:00Z"
}
```

---

#### Bang ma loi tra ve

##### 19.2.1. Loi nghiep vu (Business Error)

|| HTTP Status | errorCode | Truong hop | Loi tra ve (message) |
|| --- | --- | --- | --- |
|| 400 | PASSWORD_MISMATCH | Mat khau cu khong dung | "Mat khau cu khong dung." |
|| 404 | USER_NOT_FOUND | Tai khoan khong ton tai | "Khong tim thay tai khoan voi ID: [xxx]." |
|| 500 | SYSTEM_ERROR | Loi he thong khi truy van Firestore | "Da xay ra loi khong mong muon. Vui long thu lai sau." |

##### 19.2.2. Loi xac thuc (Authentication Error)

|| HTTP Status | Truong hop | Mo ta |
|| --- | --- | --- |
|| 401 | Token khong hop le hoac khong co | "Chua xac thuc. Vui long dang nhap de tiep tuc." |

##### 19.2.3. Loi xac thuc dau vao (Validation Error)

|| HTTP Status | Truong hop | Mo ta |
|| --- | --- | --- |
|| 400 | Du lieu khong hop le | `newPassword` it hon 6 ky tu hoac `oldPassword` trong |

---

#### Vi du

##### 19.2.4. Doi mat khau thanh cong

**Request**:

```http
PUT http://localhost:8080/api/customers/password
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSJ9...

{
  "oldPassword": "matkhaucu123",
  "newPassword": "matkhaumoi123"
}
```

**Response** (HTTP 200):

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Doi mat khau thanh cong.",
  "data": null,
  "timestamp": "2026-05-26T15:05:00Z"
}
```

##### 19.2.5. Loi 400 - Mat khau cu khong dung

**Response** (HTTP 400):

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Mat khau cu khong dung.",
  "data": null,
  "timestamp": "2026-05-26T15:05:00Z"
}
```

##### 19.2.6. Loi 400 - Mat khau moi qua ngan

**Request**:

```http
PUT http://localhost:8080/api/customers/password
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSJ9...

{
  "oldPassword": "matkhaucu123",
  "newPassword": "abc"
}
```

**Response** (HTTP 400):

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Du lieu khong hop le: newPassword: Mat khau moi phai co it nhat 6 ky tu",
  "data": null,
  "timestamp": "2026-05-26T15:05:00Z"
}
```

##### 19.2.7. Loi 401 - Chua xac thuc

**Response** (HTTP 401):

```json
{
  "success": false,
  "statusCode": 401,
  "message": "Chua xac thuc. Vui long dang nhap de tiep tuc.",
  "data": null,
  "timestamp": "2026-05-26T15:05:00Z"
}
```

##### 19.2.8. Thu tu goi API (Flow)

```
1. Flutter goi PUT /api/customers/password voi Authorization header
   |
2. Server trich xuat JWT token tu header Authorization
   |
3+-> Token khong hop le hoac khong co -> Tra ve 401 "Chua xac thuc..."
   |
4. Server trich xuat userId tu JWT token
   |
5. Server truy van document trong collection users theo userId
   |
6+-> Tai khoan khong ton tai -> Tra ve 404 USER_NOT_FOUND
   |
7. Server kiem tra du lieu dau vao (validation)
   |
8+-> Du lieu khong hop le -> Tra ve 400 BAD_REQUEST (Validation)
   |
9. Server so sanh mat khau cu bang PasswordEncoder.matches()
   |
10+-> Mat khau cu khong dung -> Tra ve 400 PASSWORD_MISMATCH
   |
11. Server ma hoa mat khau moi bang BCrypt
   |
12. Server cap nhat password va updatedAt vao document users/{userId}
   |
13. Tra ve 200 thanh cong
```

##### 19.2.9. Cau truc du lieu (ChangePasswordRequest)

|| Thuoc tinh | Kieu | Bat buoc | Mo ta |
|| --- | --- | --- | --- |
|| `oldPassword` | String | Co | Mat khau cu hien tai |
|| `newPassword` | String | Co | Mat khau moi (it nhat 6 ky tu) |

---

#### Noi dung Swagger UI

Sau khi chay ung dung, truy cap Swagger UI tai:

```
http://localhost:8080/swagger-ui.html
```

Hoac tai noi dung OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

**Huong dan su dung Bearer Token tren Swagger UI cho API Ho so**:

1. Mo Swagger UI
2. Chon endpoint muon test (VD: PUT /api/customers/profile)
3. Nhap du lieu request
4. Click nut "Authorize" (o goc phai man hinh)
5. Nhap "Bearer <token>" vao o BearerAuth
6. Click "Authorize" de xac nhan
7. Cac API Ho so bay gio se su dung token nay
8. Click "Execute" de test endpoint

```
VD: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAwMSJ9...
```
