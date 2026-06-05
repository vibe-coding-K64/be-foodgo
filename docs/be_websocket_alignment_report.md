# Báo cáo gửi Backend về luồng WebSocket và phản hồi đơn từ FE

> Mục tiêu của tài liệu này là tổng hợp những gì FE đang triển khai thực tế, các điểm đang lệch giữa docs và runtime, cùng các vấn đề cần BE xác minh để FE và BE thống nhất luồng xử lý.

---

## 1. Mục đích gửi BE

FE gửi báo cáo này để backend cùng xác nhận 3 nhóm vấn đề:

1. endpoint WebSocket/SockJS production hiện tại có đúng với cách FE đang kết nối hay không,
2. luồng phản hồi đơn nào mới là nguồn sự thật cuối cùng giữa STOMP và REST,
3. lỗi runtime `HTTP 500` khi upgrade WebSocket có nằm ở cấu hình backend/deployment hay không.

---

## 2. Tóm tắt ngắn gọn

### FE đang triển khai thực tế như sau

- FE dùng `stomp_dart_client`.
- FE kết nối tới endpoint: `https://be-foodgo.canluaz.io.vn/ws`
- FE subscribe 2 kênh:
  - `/user/queue/order-request`
  - `/user/queue/order-status`
- Với **order request realtime**, FE đang làm theo thứ tự:
  1. gửi STOMP `/app/driver/accept` hoặc `/app/driver/decline`
  2. sau đó gọi REST backup `POST /api/drivers/orders/{orderId}/respond`
- Với **available orders list**, FE gọi trực tiếp REST:
  - `POST /api/drivers/orders/{orderId}/accept`
  - `POST /api/drivers/orders/{orderId}/decline`

### Vấn đề hiện tại

- Docs trong repo từng mô tả không thống nhất giữa STOMP và REST.
- Runtime production hiện đang báo lỗi upgrade WebSocket với `HTTP 500`.
- FE cần BE xác nhận rõ endpoint đúng và source of truth cuối cùng cho luồng accept/decline.

---

## 3. Những gì FE đang dùng ở runtime

### 3.1. Client WebSocket hiện tại

FE hiện dùng `stomp_dart_client` với cấu hình SockJS, không dùng WebSocket thuần.

FE đang kết nối bằng:

- `https://be-foodgo.canluaz.io.vn/ws`

Lưu ý:

- Đây là endpoint dạng **SockJS** theo cách FE đang cấu hình.
- FE không mở socket trực tiếp bằng `wss://...` theo kiểu raw websocket client.

### 3.2. Header xác thực

FE đang gửi JWT qua header:

- `Authorization: Bearer <token>`

### 3.3. Kênh subscribe FE đang nghe

- `/user/queue/order-request`
- `/user/queue/order-status`

---

## 4. Luồng xử lý đơn phía FE đang triển khai

## 4.1. Luồng order request realtime

Đây là luồng tài xế nhận popup đơn mới do backend push realtime.

FE đang làm theo thứ tự sau:

1. Nhận `ORDER_REQUEST` từ WebSocket.
2. Hiển thị popup/bottom sheet cho tài xế.
3. Nếu tài xế accept/decline:
   - FE gửi STOMP trước:
     - `/app/driver/accept`
     - `/app/driver/decline`
4. Sau đó FE gọi REST backup:
   - `POST /api/drivers/orders/{orderId}/respond`
   - body: `{ "action": "accept" }` hoặc `{ "action": "decline" }`

### 4.2. Luồng available orders list

Đây là luồng tài xế vào màn hình danh sách đơn khả dụng và tự chọn đơn.

FE hiện không dùng WebSocket cho thao tác này.

FE gọi trực tiếp:

- `POST /api/drivers/orders/{orderId}/accept`
- `POST /api/drivers/orders/{orderId}/decline`

---

## 5. Những điểm FE cần BE xác nhận

### 5.1. Xác nhận endpoint WebSocket/SockJS chính thức

FE cần BE xác nhận:

- endpoint chính thức FE phải dùng là `https://be-foodgo.canluaz.io.vn/ws` hay `wss://be-foodgo.canluaz.io.vn/ws`
- backend hiện đang expose endpoint theo kiểu SockJS hay WebSocket thuần
- backend có hỗ trợ đầy đủ transport upgrade cho đường dẫn kiểu:
  - `/ws/{server-id}/{session-id}/websocket`

### 5.2. Xác nhận source of truth cho accept/decline

Hiện trong docs có 3 cách khác nhau:

- STOMP `/app/driver/accept|decline`
- REST `POST /api/drivers/orders/{orderId}/respond`
- REST `POST /api/drivers/orders/{orderId}/accept|decline`

FE cần BE chốt rõ:

1. Với **order request realtime**, backend kỳ vọng FE:
   - chỉ gửi STOMP,
   - chỉ gọi REST `/respond`,
   - hay gửi cả 2 như FE đang làm hiện tại.

2. Nếu FE gửi cả STOMP và REST `/respond`, backend có xử lý idempotent không?
   - Có gây double-processing hoặc race condition không?
   - Nếu cùng một order bị nhận 2 lần theo 2 kênh, backend đang ưu tiên nguồn nào?

3. Với **available orders list**, backend có xác nhận đúng là nên dùng:
   - `POST /accept`
   - `POST /decline`
   thay vì `/respond` hay không?

### 5.3. Xác nhận payload và event response

FE cần BE xác nhận thêm:

- payload STOMP gửi lên `/app/driver/accept` và `/app/driver/decline` hiện có đúng schema backend mong đợi không
  - hiện FE đang gửi: `{ "orderId": "..." }`
- các event backend trả về ở `/user/queue/order-status` gồm chính xác những loại nào
  - ví dụ: `ORDER_ACCEPTED`, `ORDER_CANCELLED`, `ORDER_TAKEN_BY_OTHER`, ...
- khi accept thành công thì backend sẽ phản hồi bằng event nào là chuẩn để FE cập nhật UI.

---

## 6. Lỗi runtime FE đang gặp trên production

FE đang thấy log dạng:

```text
[WS] Connecting to https://be-foodgo.canluaz.io.vn/ws
WebSocketException: Connection to 'https://be-foodgo.canluaz.io.vn:0/ws/.../websocket#' was not upgraded to websocket, HTTP status code: 500
```

### Nhận định từ phía FE

Điều này cho thấy:

- FE đã bắt đầu kết nối vào endpoint `/ws`
- client SockJS tiếp tục thử transport WebSocket
- nhưng phía server/proxy đang trả `500` khi upgrade transport

### FE cần BE kiểm tra

1. Backend có đang bật SockJS/WebSocket endpoint đúng ở production không?
2. Reverse proxy hoặc gateway có forward đúng các header upgrade không?
3. Đường dẫn `/ws/**` có bị rewrite sai hoặc chặn ở production không?
4. Backend có log lỗi server-side tương ứng với request upgrade này không?
5. Nếu backend chỉ hỗ trợ một số transport nhất định, FE có cần khóa transport nào không?

---

## 7. Tác động hiện tại đến FE

Nếu WebSocket realtime không hoạt động ổn định:

- FE vẫn có thể fallback sang REST cho nhiều thao tác,
- nhưng luồng nhận đơn realtime sẽ bị ảnh hưởng,
- UX của tài xế sẽ kém hơn vì không còn cập nhật tức thời,
- popup nhận đơn có thể không xuất hiện đúng lúc.

Do đó FE cần backend xác minh sớm vấn đề production upgrade để tránh ảnh hưởng luồng nhận đơn.

---

## 8. Đề xuất thống nhất giữa FE và BE

FE đề xuất BE chốt rõ bằng văn bản hoặc docs 3 nội dung sau:

### 8.1. Endpoint kết nối chuẩn

Ví dụ cần chốt rõ một trong hai kiểu:

- FE dùng SockJS endpoint: `https://be-foodgo.canluaz.io.vn/ws`
- hoặc FE dùng raw websocket endpoint: `wss://be-foodgo.canluaz.io.vn/ws`

### 8.2. Luồng phản hồi đơn chuẩn cho từng ngữ cảnh

#### Realtime order request

Chốt rõ một trong các phương án:

- chỉ STOMP,
- chỉ REST `/respond`,
- hoặc STOMP + REST backup.

#### Available orders list

Chốt rõ có tiếp tục dùng:

- `POST /accept`
- `POST /decline`

hay cần đổi sang cơ chế khác.

### 8.3. Danh sách event chuẩn trả về cho FE

BE nên cung cấp danh sách chuẩn:

- tên event,
- schema payload,
- điều kiện phát event,
- event nào dùng để cập nhật UI popup,
- event nào dùng để đóng popup hoặc báo tài xế đơn đã bị người khác nhận.

---

## 9. Các câu hỏi FE cần BE trả lời

1. Endpoint production chuẩn cho FE là `https://.../ws` hay `wss://.../ws`?
2. Backend hiện đang dùng SockJS hay WebSocket thuần?
3. FE có nên tiếp tục gửi cả STOMP và REST `/respond` cho order request realtime không?
4. Nếu gửi cả 2, backend có xử lý chống double-processing không?
5. Với available orders list, BE xác nhận đúng là dùng `/accept` và `/decline` chứ?
6. Lỗi `HTTP 500` khi upgrade WebSocket trên production đến từ backend app hay reverse proxy/gateway?
7. Backend có thể cung cấp log hoặc hướng dẫn test chính xác endpoint realtime production không?

---

## 10. Kết luận

Từ phía FE, hiện có thể xác nhận rằng:

- FE đã triển khai WebSocket/STOMP thực tế,
- FE đang dùng mô hình WS trước + REST backup cho realtime order request,
- FE vẫn cần backend xác nhận source of truth chính thức,
- production hiện đang có lỗi upgrade WebSocket cần BE kiểm tra.

FE mong BE phản hồi các điểm trên để hai bên thống nhất docs, flow và cách triển khai production.
