# Backend Cần Phản Hồi Gì Cho FE

> Tài liệu này tổng hợp ngắn gọn các điểm backend cần xác nhận cho frontend sau báo cáo `docs/be_websocket_alignment_report.md`.

---

## 1. Mục tiêu

Sau khi FE đã chuẩn hóa lại tài liệu và mô tả rõ implementation hiện tại, BE cần phản hồi để hai bên thống nhất:

1. endpoint realtime chính thức
2. source of truth cho luồng accept/decline
3. schema payload và event trả về
4. cơ chế chống xử lý trùng
5. nguyên nhân lỗi WebSocket production

---

## 2. Những gì FE đang làm hiện tại

Để BE phản hồi đúng ngữ cảnh, đây là implementation FE hiện tại:

### Realtime order request

- FE kết nối tới: `https://be-foodgo.canluaz.io.vn/ws`
- FE subscribe:
  - `/user/queue/order-request`
  - `/user/queue/order-status`
- Khi tài xế bấm accept/decline trên popup realtime:
  1. FE gửi STOMP trước
     - `/app/driver/accept`
     - `/app/driver/decline`
  2. Sau đó FE gọi REST backup
     - `POST /api/drivers/orders/{orderId}/respond`

### Available orders list

- FE không dùng WebSocket
- FE gọi trực tiếp REST:
  - `POST /api/drivers/orders/{orderId}/accept`
  - `POST /api/drivers/orders/{orderId}/decline`

---

## 3. Backend Cần Chốt Với FE

## 3.1. Endpoint realtime chính thức

BE cần xác nhận chính xác FE phải dùng kiểu nào:

### Phương án A: SockJS endpoint

```text
https://be-foodgo.canluaz.io.vn/ws
```

### Phương án B: Raw WebSocket endpoint

```text
wss://be-foodgo.canluaz.io.vn/ws
```

### BE cần trả lời rõ

- Backend hiện expose endpoint theo kiểu **SockJS** hay **raw WebSocket**?
- FE có được kỳ vọng dùng `stomp_dart_client` với `StompConfig.sockJS(...)` không?
- Đường dẫn transport kiểu `/ws/{server-id}/{session-id}/websocket` có phải là đường dẫn hợp lệ ở production không?

---

## 3.2. Source of truth cho accept/decline

Hiện FE đang dùng đồng thời nhiều cách tùy ngữ cảnh. Backend cần chốt rõ contract chính thức.

### Với luồng realtime order request

BE cần chọn và xác nhận **một** trong các mô hình sau:

#### Phương án 1: Chỉ STOMP

- FE chỉ gửi:
  - `/app/driver/accept`
  - `/app/driver/decline`
- Không cần gọi REST `/respond`

#### Phương án 2: Chỉ REST `/respond`

- FE không gửi STOMP accept/decline
- FE chỉ gọi:
  - `POST /api/drivers/orders/{orderId}/respond`

#### Phương án 3: STOMP + REST backup

- FE gửi STOMP trước
- Sau đó gọi REST `/respond`
- Backend phải hỗ trợ idempotent và tránh xử lý trùng

### Với luồng available orders list

BE cần xác nhận có tiếp tục dùng:

- `POST /api/drivers/orders/{orderId}/accept`
- `POST /api/drivers/orders/{orderId}/decline`

hay muốn gom về `/respond` để đồng nhất API.

---

## 3.3. Cơ chế chống double-processing

Nếu BE chấp nhận mô hình **STOMP + REST backup**, cần phản hồi rõ:

- Backend có idempotent không?
- Nếu cùng một order được gửi accept qua cả STOMP và REST thì backend xử lý thế nào?
- Backend có chống race condition khi 2 request gần như cùng lúc không?
- Backend ưu tiên nguồn nào nếu dữ liệu tới qua 2 kênh khác nhau?

### BE nên mô tả rõ các trường hợp

1. Cùng một tài xế bấm accept một lần nhưng FE gửi qua 2 kênh
2. Hai tài xế khác nhau cùng accept gần như đồng thời
3. FE gửi STOMP thành công nhưng REST timeout
4. FE gửi STOMP fail nhưng REST success
5. FE reconnect và gửi lại thao tác cũ

---

## 3.4. Payload STOMP chính thức

BE cần xác nhận schema payload chuẩn cho:

- `/app/driver/accept`
- `/app/driver/decline`

### FE hiện đang gửi

```json
{
  "orderId": "..."
}
```

### Backend cần trả lời

- Payload trên đã đúng chưa?
- Có cần thêm `driverId`, `requestId`, `timestamp`, `sessionId` hoặc trường nào khác không?
- Có cần một `correlationId` hoặc `clientActionId` để chống duplicate không?

---

## 3.5. Danh sách event backend trả về

BE cần cung cấp danh sách event chuẩn FE sẽ nhận tại:

- `/user/queue/order-request`
- `/user/queue/order-status`

### Cần chốt rõ cho từng event

- tên event
- cấu trúc payload
- khi nào event được phát
- FE nên cập nhật UI như thế nào

### Ví dụ những event FE đang giả định có thể tồn tại

- `ORDER_REQUEST`
- `ORDER_ACCEPTED`
- `ORDER_CANCELLED`
- `ORDER_TAKEN_BY_OTHER`

### BE cần xác nhận thêm

- accept thành công thì event chuẩn là gì?
- khi đơn bị tài xế khác nhận thì event nào được gửi?
- khi khách hủy đơn thì event nào được gửi?
- có event nào FE phải dùng để đóng popup ngay lập tức không?

---

## 3.6. Cách FE nên xác nhận thành công sau thao tác

Backend cần nói rõ cho FE:

Sau khi tài xế bấm accept/decline, FE nên coi thao tác thành công dựa trên cái nào?

### Các khả năng có thể có

- dựa vào STOMP ACK hoặc message response
- dựa vào event trên `/user/queue/order-status`
- dựa vào REST response `/respond`
- dựa vào việc reload `current/active/available` order qua REST

### Backend cần chốt

- tín hiệu thành công chính thức là gì
- tín hiệu thất bại chính thức là gì
- FE cần retry trong trường hợp nào

---

## 4. Backend Cần Kiểm Tra Ở Production

FE đang thấy lỗi:

```text
WebSocketException: Connection to 'https://be-foodgo.canluaz.io.vn:0/ws/.../websocket#' was not upgraded to websocket, HTTP status code: 500
```

Backend cần kiểm tra các điểm sau.

### 4.1. Ứng dụng backend

- endpoint `/ws` có thật sự được expose ở production không
- config WebSocket/SockJS có bật đúng không
- có interceptor/auth nào chặn handshake không
- server có log lỗi nội bộ nào lúc request upgrade tới không

### 4.2. Reverse proxy / gateway

- có forward header `Upgrade` và `Connection` đúng không
- có rewrite sai `/ws/**` không
- có chặn transport `/websocket` của SockJS không
- timeout/proxy buffer có làm hỏng websocket upgrade không

### 4.3. Hạ tầng

- load balancer có hỗ trợ websocket upgrade không
- firewall/WAF có chặn request websocket không
- SSL termination có làm sai scheme hoặc port không

---

## 5. Backend Nên Trả Lời FE Theo Mẫu Này

BE có thể phản hồi ngắn gọn theo checklist dưới đây.

### 5.1. Endpoint chuẩn

- [ ] FE phải dùng SockJS endpoint `https://be-foodgo.canluaz.io.vn/ws`
- [ ] FE phải dùng raw WebSocket endpoint `wss://be-foodgo.canluaz.io.vn/ws`
- [ ] Backend hỗ trợ transport SockJS `/ws/{server-id}/{session-id}/websocket`

### 5.2. Luồng realtime order request

- [ ] FE chỉ gửi STOMP
- [ ] FE chỉ gọi REST `/respond`
- [ ] FE gửi STOMP trước, REST backup sau

### 5.3. Luồng available orders

- [ ] FE tiếp tục dùng `/accept` và `/decline`
- [ ] FE đổi sang `/respond`
- [ ] Có luồng khác, BE sẽ cung cấp docs riêng

### 5.4. Idempotency

- [ ] Backend đã chống duplicate khi FE gửi qua nhiều kênh
- [ ] Backend chưa chống duplicate, FE không nên gửi cả STOMP và REST
- [ ] Backend cần FE gửi thêm `clientActionId`

### 5.5. Event contract

- [ ] BE sẽ cung cấp danh sách event chuẩn
- [ ] BE xác nhận FE đang dùng đúng các event hiện tại
- [ ] BE sẽ bổ sung schema payload chi tiết

### 5.6. Production issue

- [ ] Lỗi 500 nằm ở backend app
- [ ] Lỗi 500 nằm ở reverse proxy/gateway
- [ ] Lỗi 500 chưa xác định, cần thêm log/test

---

## 6. Kết luận

Sau báo cáo từ FE, phía backend cần phản hồi chính thức để chốt contract tích hợp. Nếu chưa chốt, hai rủi ro lớn nhất là:

1. FE và BE hiểu khác nhau về endpoint và flow chuẩn
2. production vẫn lỗi realtime dù code hai bên tưởng là đã đúng

Ưu tiên cao nhất hiện tại là:

- chốt endpoint chuẩn
- chốt source of truth cho accept/decline
- xác nhận idempotency
- xử lý lỗi WebSocket upgrade HTTP 500 trên production
