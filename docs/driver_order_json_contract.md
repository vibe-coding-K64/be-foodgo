# Driver Order JSON Contract For Frontend

> Tài liệu contract JSON chính thức để team frontend app tài xế dùng ngay cho REST API và WebSocket realtime.

---

## 1. Phạm vi áp dụng

Tài liệu này áp dụng cho các luồng của tài xế liên quan đến đơn hàng:

- lấy danh sách đơn có thể nhận
- lấy chi tiết đơn hàng
- lấy đơn hiện tại / active / history
- nhận đơn / từ chối đơn
- nhận popup realtime order request
- nhận event realtime sau khi accept / decline
- cập nhật trạng thái giao hàng nhiều bước

---

## 2. Các endpoint FE đang dùng

### REST

Base path:

```text
/api/drivers/orders
```

Các endpoint hiện có:

- `GET /api/drivers/orders/{id}`
- `GET /api/drivers/orders/available`
- `GET /api/drivers/orders/current`
- `GET /api/drivers/orders/active`
- `GET /api/drivers/orders/history`
- `POST /api/drivers/orders/{id}/accept`
- `POST /api/drivers/orders/{id}/decline`
- `POST /api/drivers/orders/{id}/respond`
- `PUT /api/drivers/orders/{id}/status`

### WebSocket / STOMP

- connect: `wss://be-foodgo.canluaz.io.vn/ws`
- subscribe:
  - `/user/queue/order-request`
  - `/user/queue/order-status`
- send:
  - `/app/driver/accept`
  - `/app/driver/decline`

---

## 3. Response wrapper chuẩn cho REST

Tất cả REST API của nhóm tài xế - đơn hàng trả theo wrapper sau:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Lay chi tiet don hang thanh cong.",
  "data": {},
  "timestamp": "2026-06-05T14:05:10Z",
  "errors": null
}
```

### Ý nghĩa các field wrapper

- `success`: `true` nếu request thành công, `false` nếu lỗi
- `statusCode`: mã trạng thái nghiệp vụ backend trả về
- `message`: thông điệp hiển thị hoặc log cho FE
- `data`: dữ liệu chính của API
- `timestamp`: thời điểm backend trả response
- `errors`: danh sách lỗi field-level nếu có validate error

---

## 4. Bảng enum status chính thức

Mỗi giá trị status chỉ có **một meaning duy nhất** trong contract driver order.

### 4.1. Numeric enum

- `0 = PENDING_STORE_CONFIRMATION`
- `1 = WAITING_DRIVER`
- `2 = DELIVERING`
- `3 = COMPLETED`
- `4 = CANCELLED`

### 4.2. Ý nghĩa nghiệp vụ

- `PENDING_STORE_CONFIRMATION`: đơn mới tạo, đang chờ cửa hàng xác nhận
- `WAITING_DRIVER`: đơn đã sẵn sàng hoặc đang chờ tài xế nhận
- `DELIVERING`: tài xế đã nhận đơn và đang trong quá trình giao
- `COMPLETED`: đơn đã giao thành công
- `CANCELLED`: đơn đã bị hủy

### 4.3. Các field status trả về trong DTO

Ngoài `status` dạng số, backend còn trả thêm:

- `statusCode`
- `statusDescription`

Ví dụ:

```json
{
  "status": 2,
  "statusCode": "DELIVERING",
  "statusDescription": "Tài xế đã nhận đơn và đang trong quá trình giao."
}
```

---

## 5. DeliveryOrderDTO chuẩn FE nên dùng

Đây là model JSON chính FE nên dùng cho mọi màn hình đơn hàng của tài xế.

```json
{
  "id": "order_123",
  "orderCode": "FG12345",
  "userId": "user_001",
  "customerName": "Tran Thi B",
  "customerPhone": "0901234567",
  "customerAvatarUrl": "https://example.com/avatar.jpg",
  "recipientName": "Tran Thi B",
  "recipientPhone": "0901234567",
  "storeId": "store_001",
  "storeName": "Com Tam Phuc Loc Tho",
  "storeAddress": "123 Le Van Viet, TP. Thu Duc",
  "storeLat": 10.85,
  "storeLng": 106.79,
  "items": [
    {
      "foodId": "food_001",
      "name": "Com tam suon bi cha",
      "price": 45000,
      "quantity": 2,
      "imageUrl": "https://...",
      "options": [
        {
          "name": "Them trung",
          "price": 5000
        }
      ]
    }
  ],
  "totalAmount": 100000,
  "itemsSubtotal": 90000,
  "optionsSubtotal": 10000,
  "discountAmount": 5000,
  "deliveryFee": 15000,
  "finalAmount": 110000,
  "driverCollectAmount": 110000,
  "status": 2,
  "statusCode": "DELIVERING",
  "statusDescription": "Tài xế đã nhận đơn và đang trong quá trình giao.",
  "paymentStatus": 1,
  "deliveryAddress": "Ky tuc xa UTC2, Thu Duc",
  "deliveryLat": 10.8455,
  "deliveryLng": 106.7939,
  "distance": 3.5,
  "pickupDistanceKm": null,
  "deliveryDistanceKm": 3.5,
  "estimatedDurationMinutes": 14,
  "paymentMethod": 1,
  "driverId": "driver_001",
  "driverName": "Nguyen Van A",
  "driverPhone": "0912345678",
  "vehiclePlate": "59A1-12345",
  "arrivedAtStoreAt": null,
  "pickedUpAt": "2026-06-05T14:02:00Z",
  "deliveredAt": null,
  "deliveryStep": "ON_THE_WAY",
  "createdAt": "2026-06-05T14:00:00Z",
  "updatedAt": "2026-06-05T14:02:00Z",
  "note": "Giao gap",
  "requestId": "req_456",
  "estimatedEarning": 15000,
  "expiresAt": "2026-06-05T14:05:10Z",
  "expiresInSeconds": 10
}
```

### 5.1. Nguyên tắc nhất quán

Các field sau xuất hiện nhất quán trong:

- `GET /{id}`
- `GET /available`
- `GET /current`
- `GET /active`
- `GET /history`
- response `accept`
- response `status update`
- `order` trong realtime event `ORDER_REQUEST`
- `order` trong realtime event `ORDER_ACCEPTED`

Các field có thể `null` nếu backend không có dữ liệu nguồn tại thời điểm trả response, ví dụ:

- `pickupDistanceKm`
- `customerAvatarUrl`
- `arrivedAtStoreAt`
- `pickedUpAt`
- `deliveredAt`

### 5.2. Ý nghĩa tiền tệ

Backend chốt rõ semantics như sau:

- `itemsSubtotal`: tổng tiền phần món chính, chưa tính option
- `optionsSubtotal`: tổng tiền option/topping
- `totalAmount`: tổng tiền món + option, **chưa gồm** `deliveryFee`
- `discountAmount`: tổng tiền giảm giá áp vào đơn
- `deliveryFee`: phí giao hàng
- `finalAmount`: tổng tiền khách phải trả sau giảm giá và **đã gồm** `deliveryFee`
- `driverCollectAmount`:
  - với COD: số tiền tài xế cần thu/giữ hộ
  - với thanh toán online: `0`

### 5.3. Ý nghĩa route info

- `distance`: khoảng cách từ quán tới điểm giao
- `deliveryDistanceKm`: cùng semantics với `distance`, field rõ nghĩa hơn cho FE
- `pickupDistanceKm`: khoảng cách từ vị trí tài xế tới quán nếu backend tính được, có thể `null`
- `estimatedDurationMinutes`: thời gian ước tính giao hàng nếu backend tính được

### 5.4. UI nhiều bước giao hàng

Backend chốt rằng FE sẽ dùng trực tiếp field:

- `deliveryStep`

Các giá trị hiện tại:

- `PENDING_STORE_CONFIRMATION`
- `WAITING_DRIVER`
- `WAITING_PICKUP`
- `ARRIVED_STORE`
- `ON_THE_WAY`
- `DELIVERED`
- `CANCELLED`
- `UNKNOWN`

Timestamps vẫn được trả thêm để FE hiển thị chi tiết nếu cần:

- `arrivedAtStoreAt`
- `pickedUpAt`
- `deliveredAt`

---

## 6. Contract cho các API GET

## 6.1. `GET /api/drivers/orders/{id}`

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Lay chi tiet don hang thanh cong.",
  "data": {
    "id": "order_123",
    "orderCode": "FG12345",
    "userId": "user_001",
    "customerName": "Tran Thi B",
    "customerPhone": "0901234567",
    "customerAvatarUrl": null,
    "recipientName": "Tran Thi B",
    "recipientPhone": "0901234567",
    "storeId": "store_001",
    "storeName": "Com Tam Phuc Loc Tho",
    "storeAddress": "123 Le Van Viet, TP. Thu Duc",
    "storeLat": 10.85,
    "storeLng": 106.79,
    "items": [],
    "totalAmount": 100000,
    "itemsSubtotal": 90000,
    "optionsSubtotal": 10000,
    "discountAmount": 5000,
    "deliveryFee": 15000,
    "finalAmount": 110000,
    "driverCollectAmount": 110000,
    "status": 2,
    "statusCode": "DELIVERING",
    "statusDescription": "Tài xế đã nhận đơn và đang trong quá trình giao.",
    "paymentStatus": 1,
    "deliveryAddress": "Ky tuc xa UTC2, Thu Duc",
    "deliveryLat": 10.8455,
    "deliveryLng": 106.7939,
    "distance": 3.5,
    "pickupDistanceKm": null,
    "deliveryDistanceKm": 3.5,
    "estimatedDurationMinutes": 14,
    "paymentMethod": 1,
    "driverId": "driver_001",
    "driverName": "Nguyen Van A",
    "driverPhone": "0912345678",
    "vehiclePlate": "59A1-12345",
    "arrivedAtStoreAt": null,
    "pickedUpAt": "2026-06-05T14:02:00Z",
    "deliveredAt": null,
    "deliveryStep": "ON_THE_WAY",
    "createdAt": "2026-06-05T14:00:00Z",
    "updatedAt": "2026-06-05T14:02:00Z",
    "note": "Giao gap",
    "requestId": null,
    "estimatedEarning": null,
    "expiresAt": null,
    "expiresInSeconds": null
  },
  "timestamp": "2026-06-05T14:05:10Z",
  "errors": null
}
```

## 6.2. `GET /api/drivers/orders/available`

- `data` là `DeliveryOrderDTO[]`
- shape của từng phần tử giống `DeliveryOrderDTO` ở trên
- `requestId`, `estimatedEarning`, `expiresAt`, `expiresInSeconds` thường là `null`

## 6.3. `GET /api/drivers/orders/current`

Backend đã chuẩn hóa endpoint này thành:

- `data: DeliveryOrderDTO | null`
- **không trả array**

### Khi có đơn hiện tại

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Lay don hien tai thanh cong.",
  "data": {
    "id": "order_123",
    "status": 2,
    "statusCode": "DELIVERING",
    "deliveryStep": "ON_THE_WAY"
  },
  "timestamp": "2026-06-05T14:05:10Z",
  "errors": null
}
```

### Khi không có đơn hiện tại

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Lay don hien tai thanh cong.",
  "data": null,
  "timestamp": "2026-06-05T14:05:10Z",
  "errors": null
}
```

## 6.4. `GET /api/drivers/orders/active`

- `data` là `DeliveryOrderDTO[]`
- mỗi phần tử dùng cùng shape `DeliveryOrderDTO`

## 6.5. `GET /api/drivers/orders/history`

- `data` là `DeliveryOrderDTO[]`
- mỗi phần tử dùng cùng shape `DeliveryOrderDTO`
- với đơn đã hoàn thành, `status = 3`, `statusCode = "COMPLETED"`, `deliveryStep = "DELIVERED"`

---

## 7. Contract cho REST action responses

## 7.1. `POST /api/drivers/orders/{id}/accept`

Response thành công trả **full `DeliveryOrderDTO`**.

## 7.2. `POST /api/drivers/orders/{id}/respond`

### Accept

Request body:

```json
{
  "action": "accept",
  "requestId": "req_456"
}
```

Response thành công trả **full `DeliveryOrderDTO`**.

### Decline

Request body:

```json
{
  "action": "decline",
  "requestId": "req_456"
}
```

Response thành công trả object tối thiểu:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Tu choi don hang thanh cong.",
  "data": {
    "orderId": "order_123",
    "requestId": "req_456",
    "status": "DECLINED"
  },
  "timestamp": "2026-06-05T14:05:10Z",
  "errors": null
}
```

## 7.3. `POST /api/drivers/orders/{id}/decline`

Response thành công cũng trả object tối thiểu cùng shape:

```json
{
  "orderId": "order_123",
  "requestId": null,
  "status": "DECLINED"
}
```

## 7.4. `PUT /api/drivers/orders/{id}/status`

Response thành công trả **full `DeliveryOrderDTO`**.

---

## 8. Contract cho realtime order popup

## 8.1. Subscribe `/user/queue/order-request`

```json
{
  "event": "ORDER_REQUEST",
  "message": "Co don hang moi",
  "orderId": "order_123",
  "requestId": "req_456",
  "estimatedEarning": 15000,
  "expiresAt": "2026-06-05T14:05:10Z",
  "expiresInSeconds": 10,
  "order": {
    "id": "order_123",
    "orderCode": "FG12345",
    "userId": "user_001",
    "customerName": "Tran Thi B",
    "customerPhone": "0901234567",
    "customerAvatarUrl": null,
    "recipientName": "Tran Thi B",
    "recipientPhone": "0901234567",
    "storeId": "store_001",
    "storeName": "Com Tam Phuc Loc Tho",
    "storeAddress": "123 Le Van Viet, TP. Thu Duc",
    "storeLat": 10.85,
    "storeLng": 106.79,
    "items": [],
    "totalAmount": 100000,
    "itemsSubtotal": 90000,
    "optionsSubtotal": 10000,
    "discountAmount": 5000,
    "deliveryFee": 15000,
    "finalAmount": 110000,
    "driverCollectAmount": 110000,
    "status": 1,
    "statusCode": "WAITING_DRIVER",
    "statusDescription": "Đơn đã sẵn sàng hoặc đang chờ tài xế nhận.",
    "deliveryAddress": "Ky tuc xa UTC2, Thu Duc",
    "deliveryLat": 10.8455,
    "deliveryLng": 106.7939,
    "distance": 3.5,
    "pickupDistanceKm": null,
    "deliveryDistanceKm": 3.5,
    "estimatedDurationMinutes": 14,
    "paymentMethod": 1,
    "driverId": null,
    "driverName": null,
    "driverPhone": null,
    "vehiclePlate": null,
    "arrivedAtStoreAt": null,
    "pickedUpAt": null,
    "deliveredAt": null,
    "deliveryStep": "WAITING_DRIVER",
    "createdAt": "2026-06-05T14:00:00Z",
    "updatedAt": "2026-06-05T14:00:10Z",
    "note": "Giao gap",
    "requestId": "req_456",
    "estimatedEarning": 15000,
    "expiresAt": "2026-06-05T14:05:10Z",
    "expiresInSeconds": 10
  }
}
```

---

## 9. Contract cho realtime order status

## 9.1. `ORDER_ACCEPTED`

Backend trả:

- `event`
- `message`
- `orderId`
- `requestId`
- `status`
- `order` là full `DeliveryOrderDTO`

Ví dụ:

```json
{
  "event": "ORDER_ACCEPTED",
  "message": "Nhan don hang thanh cong",
  "orderId": "order_123",
  "requestId": "req_456",
  "status": "SUCCESS",
  "order": {
    "id": "order_123",
    "status": 2,
    "statusCode": "DELIVERING",
    "deliveryStep": "WAITING_PICKUP"
  },
  "actionResult": null
}
```

## 9.2. `ORDER_DECLINED`

Backend trả tối thiểu:

- `event`
- `message`
- `orderId`
- `requestId`
- `status`
- `actionResult`

Ví dụ:

```json
{
  "event": "ORDER_DECLINED",
  "message": "Tu choi don hang thanh cong",
  "orderId": "order_123",
  "requestId": "req_456",
  "status": "SUCCESS",
  "order": null,
  "actionResult": {
    "orderId": "order_123",
    "requestId": "req_456",
    "status": "DECLINED"
  }
}
```

## 9.3. `ORDER_ACCEPT_FAILED` / `ORDER_DECLINE_FAILED`

Các event fail cũng trả:

- `orderId`
- `requestId`
- `status = "ERROR"`
- `actionResult` với trạng thái chi tiết như `ACCEPT_FAILED` hoặc `DECLINE_FAILED`

---

## 10. Xác nhận các field mới xuất hiện nhất quán

Backend xác nhận các field sau đã được chuẩn hóa để xuất hiện nhất quán ở:

- tất cả endpoint REST trả `DeliveryOrderDTO`
- `order` trong realtime event `ORDER_REQUEST`
- `order` trong realtime event `ORDER_ACCEPTED`

Danh sách field:

- `orderCode`
- `customerName`
- `customerPhone`
- `customerAvatarUrl`
- `recipientName`
- `recipientPhone`
- `itemsSubtotal`
- `optionsSubtotal`
- `discountAmount`
- `deliveryFee`
- `finalAmount`
- `driverCollectAmount`
- `distance`
- `pickupDistanceKm`
- `deliveryDistanceKm`
- `estimatedDurationMinutes`
- `arrivedAtStoreAt`
- `pickedUpAt`
- `deliveredAt`
- `deliveryStep`
- `statusCode`
- `statusDescription`
- `expiresInSeconds` trong các realtime order payload có `expiresAt`

Lưu ý:

- một số field có thể `null` nếu không có dữ liệu nguồn thực tế
- backend không fabricate dữ liệu giả chỉ để làm đầy contract

---

## 11. Ghi chú tích hợp cho frontend

- FE nên luôn parse `data` trong REST theo wrapper `ApiResponse`.
- `GET /current` phải parse `DeliveryOrderDTO | null`, không parse array.
- FE nên ưu tiên `deliveryStep` để render UI giao hàng nhiều bước.
- FE có thể dùng thêm các timestamp để hiển thị timeline chi tiết.
- FE nên ưu tiên dữ liệu từ `order` trong event realtime thay vì tự map thủ công từ nhiều field rời.
- Khi bấm accept/decline từ popup realtime, FE phải giữ lại `requestId` để gửi lên backend.
- Nếu cần fallback bằng REST `/respond`, FE vẫn dùng đúng `requestId` đã nhận từ event realtime.

---

## 12. File backend liên quan

- `src/main/java/com/example/be_foodgo/constant/DeliveryOrderStatus.java`
- `src/main/java/com/example/be_foodgo/dto/DeliveryOrderDTO.java`
- `src/main/java/com/example/be_foodgo/dto/DriverOrderActionResultDTO.java`
- `src/main/java/com/example/be_foodgo/dto/DriverOrderRequestRealtimeEvent.java`
- `src/main/java/com/example/be_foodgo/dto/DriverRealtimeEvent.java`
- `src/main/java/com/example/be_foodgo/controller/DeliveryOrderController.java`
- `src/main/java/com/example/be_foodgo/controller/DriverRealtimeController.java`
- `src/main/java/com/example/be_foodgo/service/DeliveryOrderService.java`
- `src/main/java/com/example/be_foodgo/service/OrderAssignmentService.java`
