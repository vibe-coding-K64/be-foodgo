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

## 6. Nội dung Swagger UI

Sau khi chạy ứng dụng, truy cập Swagger UI tại:

```
http://localhost:8080/swagger-ui.html
```

Hoặc tại nội dung OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```
