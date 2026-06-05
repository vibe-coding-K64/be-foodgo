# Hướng dẫn tích hợp WebSocket Realtime cho Frontend tài xế

Tài liệu này dành cho frontend app tài xế. Mục tiêu là cấu hình nhận đơn realtime qua WebSocket/STOMP, hiển thị popup nhận đơn, gửi phản hồi accept/decline về backend, và parse đúng payload realtime mới nhất từ backend.

---

## 1. Tổng quan kiến trúc realtime

- Frontend nhận đơn mới trực tiếp qua **WebSocket/STOMP**.
- Backend gửi sự kiện cá nhân tới đúng tài xế qua cơ chế `/user/queue/...`.
- Frontend phải giữ `requestId` để phản hồi đúng request realtime.
- Frontend nên subscribe cả kênh nhận đơn mới và kênh kết quả hành động accept/decline.
- Frontend nên ưu tiên WebSocket cho realtime, còn push notification có thể dùng như lớp hỗ trợ đánh thức app hoặc điều hướng vào màn hình tài xế.

---

## 2. Endpoint và cấu hình kết nối

### WebSocket endpoint

Frontend kết nối tới endpoint:

```text
/ws
```

Ví dụ nếu backend chạy ở:

```text
http://localhost:8086
```

thì URL kết nối là:

```text
http://localhost:8086/ws
```

Backend hiện dùng **SockJS**, nên frontend nên khởi tạo SockJS client rồi bọc bằng STOMP client.

---

## 3. Message broker và destination prefix

Backend đang cấu hình:

- **Application destination prefix:** `/app`
- **User destination prefix:** `/user`
- **Simple broker prefix:** `/topic`, `/queue`, `/user`

### Các kênh frontend phải subscribe

- `/user/queue/order-request`
- `/user/queue/order-status`

### Các kênh frontend phải send

- `/app/driver/accept`
- `/app/driver/decline`

---

## 4. Xác thực khi connect WebSocket

Backend hiện tại xác thực kết nối STOMP bằng header `Authorization` ngay ở frame `CONNECT`.

Frontend phải gửi header:

```text
Authorization: Bearer <access_token>
```

### Lưu ý rất quan trọng

- Không nên chỉ gắn token ở query string.
- Không nên giả định backend đọc token từ cookie.
- Header `Authorization` phải có ngay khi STOMP `CONNECT`.
- Nếu thiếu hoặc token không hợp lệ, backend sẽ từ chối kết nối WebSocket.

---

## 5. Luồng hoạt động frontend

### Khi app mở / tài xế online

1. Login thành công và lấy access token backend.
2. Kết nối WebSocket tới `/ws`.
3. Gửi `Authorization: Bearer <token>` trong `connectHeaders`.
4. Sau khi connect thành công, subscribe:
   - `/user/queue/order-request`
   - `/user/queue/order-status`
5. Khi nhận `ORDER_REQUEST`, hiển thị popup hoặc bottom sheet nhận đơn.
6. Khi tài xế bấm accept/decline, gửi message STOMP về backend.
7. Chờ ack từ `/user/queue/order-status` để cập nhật UI.

### Khi app mất kết nối

- FE nên auto reconnect.
- Sau reconnect phải subscribe lại toàn bộ queue.
- Nếu user đang mở popup nhận đơn, nên kiểm tra `expiresAt` hoặc `expiresInSeconds` trước khi cho thao tác.

---

## 6. Kênh nhận đơn mới từ backend

Frontend sẽ nhận message tại:

```text
/user/queue/order-request
```

Backend gửi event có `event = ORDER_REQUEST`.

### Shape khuyến nghị FE nên support

```json
{
  "event": "ORDER_REQUEST",
  "message": "Co don hang moi",
  "orderId": "abc123",
  "requestId": "req123",
  "estimatedEarning": 15000,
  "expiresAt": "2026-06-05T16:40:00Z",
  "expiresInSeconds": 10,
  "deliveryHeading": 120.0,
  "order": {
    "id": "abc123",
    "requestId": "req123",
    "estimatedEarning": 15000,
    "expiresAt": "2026-06-05T16:40:00Z",
    "expiresInSeconds": 10,
    "storeLat": 10.8,
    "storeLng": 106.7,
    "deliveryLat": 10.81,
    "deliveryLng": 106.71,
    "deliveryHeading": 120.0
  }
}
```

### Ý nghĩa các field top-level quan trọng

- `event`: loại event, hiện tại cho đơn mới là `ORDER_REQUEST`
- `message`: thông điệp hiển thị nhanh
- `orderId`: id đơn hàng
- `requestId`: id request realtime tạm thời, bắt buộc phải giữ để phản hồi accept/decline
- `estimatedEarning`: thu nhập ước tính cho tài xế
- `expiresAt`: thời điểm hết hạn nhận đơn
- `expiresInSeconds`: số giây còn lại khi backend phát event
- `deliveryHeading`: hướng từ cửa hàng tới điểm giao, phục vụ UI/map
- `order`: object chi tiết đơn hàng

---

## 7. Các field quan trọng trong `order`

Frontend nên tối thiểu đọc các field sau từ `order`:

- `id`
- `requestId`
- `estimatedEarning`
- `expiresAt`
- `expiresInSeconds`
- `storeLat`
- `storeLng`
- `deliveryLat`
- `deliveryLng`
- `deliveryHeading`
- `storeName`
- `storeAddress`
- `deliveryAddress`
- `distance`
- `estimatedDurationMinutes`
- `items`
- `finalAmount`
- `deliveryFee`
- `paymentMethod`
- `note`

### Khuyến nghị fallback khi parse

Để frontend ổn định hơn, nên parse theo thứ tự:

- `requestId = payload.requestId ?? payload.order?.requestId`
- `estimatedEarning = payload.estimatedEarning ?? payload.order?.estimatedEarning`
- `expiresInSeconds = payload.expiresInSeconds ?? payload.order?.expiresInSeconds`
- `deliveryHeading = payload.deliveryHeading ?? payload.order?.deliveryHeading`

Cách này giúp FE ít bị vỡ nếu payload backend thay đổi nhẹ nhưng vẫn tương thích.

---

## 8. Gửi accept/decline từ frontend

### Accept

Frontend publish tới:

```text
/app/driver/accept
```

Payload:

```json
{
  "orderId": "abc123",
  "requestId": "req123"
}
```

### Decline

Frontend publish tới:

```text
/app/driver/decline
```

Payload:

```json
{
  "orderId": "abc123",
  "requestId": "req123"
}
```

### Lưu ý

- `requestId` là bắt buộc.
- Không nên chỉ gửi `orderId`.
- Nếu popup hiển thị nhiều request theo thời gian, FE phải gắn đúng `requestId` của popup hiện tại.

---

## 9. Kênh nhận kết quả hành động từ backend

Frontend sẽ nhận message tại:

```text
/user/queue/order-status
```

Các event backend có thể gửi gồm:

- `ORDER_ACCEPTED`
- `ORDER_ACCEPT_FAILED`
- `ORDER_DECLINED`
- `ORDER_DECLINE_FAILED`

### 9.1. Accept thành công

Ví dụ shape:

```json
{
  "event": "ORDER_ACCEPTED",
  "message": "Nhan don hang thanh cong",
  "orderId": "abc123",
  "requestId": "req123",
  "status": "SUCCESS",
  "order": {
    "id": "abc123",
    "status": 3,
    "statusCode": "DELIVERING"
  }
}
```

### 9.2. Accept thất bại

Ví dụ shape:

```json
{
  "event": "ORDER_ACCEPT_FAILED",
  "message": "Don hang da co tai xe nhan hoac da het han",
  "orderId": "abc123",
  "requestId": "req123",
  "status": "ERROR",
  "actionResult": {
    "orderId": "abc123",
    "requestId": "req123",
    "status": "ACCEPT_FAILED"
  }
}
```

### 9.3. Decline thành công

Ví dụ shape:

```json
{
  "event": "ORDER_DECLINED",
  "message": "Tu choi don hang thanh cong",
  "orderId": "abc123",
  "requestId": "req123",
  "status": "SUCCESS",
  "actionResult": {
    "orderId": "abc123",
    "requestId": "req123",
    "status": "DECLINED"
  }
}
```

### 9.4. Decline thất bại

Ví dụ shape:

```json
{
  "event": "ORDER_DECLINE_FAILED",
  "message": "Da xay ra loi khong mong muon. Vui long thu lai sau.",
  "orderId": "abc123",
  "requestId": "req123",
  "status": "ERROR",
  "actionResult": {
    "orderId": "abc123",
    "requestId": "req123",
    "status": "DECLINE_FAILED"
  }
}
```

---

## 10. Ví dụ tích hợp bằng TypeScript

```ts
import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';

const token = '<access_token>';
const baseUrl = 'http://localhost:8086';

const client = new Client({
  webSocketFactory: () => new SockJS(`${baseUrl}/ws`),
  connectHeaders: {
    Authorization: `Bearer ${token}`,
  },
  reconnectDelay: 5000,
  debug: (msg) => console.log('[STOMP]', msg),
});

client.onConnect = () => {
  client.subscribe('/user/queue/order-request', (message: IMessage) => {
    const payload = JSON.parse(message.body);
    console.log('ORDER_REQUEST', payload);
  });

  client.subscribe('/user/queue/order-status', (message: IMessage) => {
    const payload = JSON.parse(message.body);
    console.log('ORDER_STATUS', payload);
  });
};

client.onStompError = (frame) => {
  console.error('Broker error:', frame.headers['message'], frame.body);
};

client.activate();
```

### Gửi accept

```ts
client.publish({
  destination: '/app/driver/accept',
  body: JSON.stringify({
    orderId: 'abc123',
    requestId: 'req123',
  }),
});
```

### Gửi decline

```ts
client.publish({
  destination: '/app/driver/decline',
  body: JSON.stringify({
    orderId: 'abc123',
    requestId: 'req123',
  }),
});
```

---

## 11. Checklist frontend để tránh miss realtime

Frontend nên đảm bảo đủ các điểm sau:

- connect tới đúng `BASE_URL + /ws`
- dùng `SockJS`
- gửi `Authorization: Bearer <token>` trong `connectHeaders`
- subscribe chỉ sau khi `onConnect` chạy thành công
- subscribe đúng:
  - `/user/queue/order-request`
  - `/user/queue/order-status`
- parse `message.body` bằng `JSON.parse`
- khi bấm accept/decline phải gửi cả `orderId` và `requestId`
- khi hiển thị countdown nên ưu tiên `expiresInSeconds`, fallback sang tự tính từ `expiresAt`
- khi reconnect phải subscribe lại
- khi popup đã hết hạn thì khóa nút accept/decline

---

## 12. Khuyến nghị xử lý UI

### Với popup đơn mới

- Hiển thị ngay khi nhận `ORDER_REQUEST`
- Lưu `requestId` vào state popup
- Hiển thị countdown từ `expiresInSeconds`
- Nếu `expiresInSeconds <= 0` thì tự đóng popup hoặc đánh dấu hết hạn

### Với màn hình bản đồ

- Dùng `storeLat/storeLng` để đặt marker cửa hàng
- Dùng `deliveryLat/deliveryLng` để đặt marker điểm giao
- Có thể dùng `deliveryHeading` nếu cần vẽ hướng, xoay icon, hoặc hỗ trợ logic cùng hướng

### Với action accept/decline

- Disable nút sau khi bấm để tránh gửi double
- Chỉ mở lại nếu nhận event failed
- Khi nhận `ORDER_ACCEPTED`, điều hướng sang màn hình giao hàng hiện tại
- Khi nhận `ORDER_DECLINED`, đóng popup và chờ đơn tiếp theo

---

## 13. Tóm tắt cấu hình FE ngắn gọn

- Endpoint: `/ws`
- Connect header: `Authorization: Bearer <token>`
- Subscribe:
  - `/user/queue/order-request`
  - `/user/queue/order-status`
- Publish:
  - `/app/driver/accept`
  - `/app/driver/decline`
- Payload đơn mới: `ORDER_REQUEST`
- Payload phản hồi hành động: `ORDER_ACCEPTED`, `ORDER_ACCEPT_FAILED`, `ORDER_DECLINED`, `ORDER_DECLINE_FAILED`
- Bắt buộc giữ và gửi lại `requestId`

---

## 14. Ghi chú tương thích

Backend hiện gửi payload theo hướng tương thích tốt cho FE:

- Có field quan trọng ở top-level
- Đồng thời giữ lại field tương ứng trong nested `order`

Vì vậy FE nên support cả hai lớp dữ liệu để tăng độ bền khi backend mở rộng payload trong tương lai.
