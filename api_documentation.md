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

