# Báo cáo rà soát cấu hình WebSocket FE

## 1. Mục tiêu rà soát

Tài liệu này tổng hợp kết quả đối chiếu giữa:

- `docs/driver_realtime_websocket_fe_guide.md`
- `docs/driver_api_for_fe.md`
- `docs/driver_order_flows.md`
- implementation FE thực tế trong codebase
- log runtime của app đang chạy

Mục tiêu là xác định:

- cấu hình WebSocket FE hiện tại có đúng hay không,
- tài liệu nào đang lệch với implementation,
- luồng phản hồi đơn nào đang là source of truth trong FE.

---

## 2. Kết luận nhanh

Sau khi rà soát, có thể kết luận:

1. FE hiện tại **đã có triển khai WebSocket/STOMP thực tế**.
2. FE **subscribe đúng** hai kênh realtime:
   - `/user/queue/order-request`
   - `/user/queue/order-status`
3. FE đang dùng mô hình **WS trước, REST sau** cho luồng phản hồi order request realtime:
   - gửi STOMP tới `/app/driver/accept` hoặc `/app/driver/decline`
   - sau đó gọi REST backup `POST /api/drivers/orders/{orderId}/respond`
4. Tài liệu trước đó **chưa khớp hoàn toàn** với implementation FE thực tế.
5. Runtime hiện tại cho thấy WebSocket production **đang lỗi upgrade** với HTTP 500, nên tài liệu có thể đúng theo FE code nhưng kết nối thực tế vẫn chưa ổn định.

---

## 3. Kết quả đối chiếu cấu hình

### 3.1. WebSocket endpoint

Implementation FE hiện tại đang dùng:

- `https://be-foodgo.canluaz.io.vn/ws`

Đây là **SockJS endpoint** trong code FE. Client `stomp_dart_client` sẽ xử lý handshake và nếu backend hỗ trợ đầy đủ thì có thể upgrade sang WebSocket transport.

Tài liệu cũ từng ghi:

- `wss://be-foodgo.canluaz.io.vn/ws`

Đánh giá:

- Nếu nói theo **địa chỉ logic WebSocket** thì `/ws` là đúng.
- Nếu nói theo **cách FE hiện đang cấu hình client** thì cần ghi rõ đây là **SockJS endpoint dạng `https://.../ws`** chứ không chỉ ghi `wss://...`.

### 3.2. Kênh subscribe realtime

Phần này khớp giữa docs và code:

- `/user/queue/order-request`
- `/user/queue/order-status`

Ý nghĩa:

- `/user/queue/order-request`: nhận thông báo có order request mới
- `/user/queue/order-status`: nhận cập nhật trạng thái order như bị hủy hoặc đã bị tài xế khác nhận

### 3.3. Header xác thực

FE truyền JWT qua header:

- `Authorization: Bearer <jwtToken>`

Điểm này phù hợp với docs và implementation.

### 3.4. Dependency thực tế

Code FE hiện tại dùng:

- `stomp_dart_client: ^3.0.1`

Tài liệu cũ ghi:

- `flutter_stomp_dart: ^2.1.0`

Đánh giá:

- Docs cũ không còn khớp với package thực tế trong `pubspec.yaml`.
- Cần cập nhật tài liệu theo dependency FE đang dùng.

---

## 4. Xung đột giữa các tài liệu

Hiện có 3 cách phản hồi đơn được mô tả trong repo:

### 4.1. Tài liệu realtime

`docs/driver_realtime_websocket_fe_guide.md` mô tả FE gửi phản hồi bằng STOMP:

- `/app/driver/accept`
- `/app/driver/decline`

### 4.2. Tài liệu API

`docs/driver_api_for_fe.md` mô tả REST endpoint:

- `POST /api/drivers/orders/{orderId}/respond`
- body:

```json
{
  "action": "accept"
}
```

Ngoài ra còn có:

- `POST /api/drivers/orders/{orderId}/accept`
- `POST /api/drivers/orders/{orderId}/decline`

### 4.3. Tài liệu flow

`docs/driver_order_flows.md` mô tả luồng phản hồi là:

- `POST /respond action=accept`
- `POST /respond action=decline`

### 4.4. Kết luận về xung đột docs

Nếu chỉ đọc tài liệu, sẽ thấy đồng thời tồn tại 3 cách:

1. STOMP `/app/driver/accept|decline`
2. REST `/respond`
3. REST `/accept|decline`

Điều này là nguyên nhân chính khiến tài liệu trước đó gây hiểu nhầm.

---

## 5. Source of truth theo implementation FE thực tế

Sau khi đối chiếu code FE, source of truth hiện tại là:

### 5.1. Với order request realtime

FE xử lý theo mô hình **kết hợp WS + REST**:

1. Nhận order request qua WebSocket
2. Tài xế nhấn accept hoặc decline
3. FE gửi STOMP trước:
   - `/app/driver/accept`
   - `/app/driver/decline`
4. Sau đó FE gọi REST backup:
   - `POST /api/drivers/orders/{orderId}/respond`

Mục đích:

- STOMP giúp phản hồi nhanh, độ trễ thấp
- REST backup giúp đồng bộ và xác nhận trạng thái từ backend

### 5.2. Với màn hình danh sách đơn khả dụng

FE không dùng WebSocket cho thao tác nhận/từ chối ở màn hình này.

Thay vào đó FE gọi trực tiếp REST:

- `POST /api/drivers/orders/{orderId}/accept`
- `POST /api/drivers/orders/{orderId}/decline`

### 5.3. Kết luận về source of truth

Không có một endpoint duy nhất áp dụng cho mọi ngữ cảnh. FE hiện dùng:

- **WS + REST `/respond`** cho luồng order request realtime
- **REST `/accept` và `/decline`** cho luồng available orders

Do đó tài liệu cần nói rõ **ngữ cảnh sử dụng từng endpoint**, thay vì mô tả như thể chỉ có một cách duy nhất.

---

## 6. Đối chiếu với runtime app hiện tại

Log runtime hiện tại cho thấy:

- app bắt đầu kết nối tới `https://be-foodgo.canluaz.io.vn/ws`
- sau đó lặp lại lỗi dạng:

```text
WebSocketException: Connection to 'https://be-foodgo.canluaz.io.vn:0/ws/.../websocket#' was not upgraded to websocket, HTTP status code: 500
```

### Nhận định

Điều này cho thấy:

- FE đã cố gắng kết nối đúng SockJS endpoint theo cấu hình hiện tại
- nhưng backend production chưa upgrade transport thành công
- lỗi này nhiều khả năng nằm ở backend deploy hoặc cấu hình reverse proxy / SockJS / WebSocket transport

### Ý nghĩa đối với tài liệu

- Tài liệu có thể đã được cập nhật đúng theo code FE
- nhưng chưa thể kết luận toàn bộ hệ thống realtime đang hoạt động ổn định ở production

---

## 7. Các điểm đã được chuẩn hóa trong tài liệu

Tài liệu `docs/driver_realtime_websocket_fe_guide.md` đã được cập nhật để:

1. dùng dependency đúng với FE hiện tại:
   - `stomp_dart_client: ^3.0.1`
2. mô tả đúng SockJS endpoint:
   - `https://be-foodgo.canluaz.io.vn/ws`
3. cập nhật mẫu service theo `StompClient` + `StompConfig.sockJS(...)`
4. bổ sung phần **source of truth trong FE hiện tại**
5. làm rõ rằng:
   - order request realtime dùng WS trước, REST sau
   - available orders dùng REST trực tiếp

---

## 8. Rủi ro và vấn đề còn tồn tại

### 8.1. Backend WS production đang lỗi upgrade

Đây là vấn đề vận hành thực tế cần phía backend kiểm tra.

### 8.2. Tài liệu API và tài liệu flow vẫn có thể gây hiểu nhầm

Dù tài liệu realtime đã được cập nhật, các file sau vẫn nên được rà soát đồng bộ tiếp nếu muốn toàn bộ docs thống nhất hoàn toàn:

- `docs/driver_api_for_fe.md`
- `docs/driver_order_flows.md`

### 8.3. Có nhiều luồng phản hồi đơn theo từng ngữ cảnh

Nếu không ghi rõ ngữ cảnh, người đọc dễ hiểu sai rằng FE chỉ dùng một endpoint duy nhất cho mọi thao tác.

---

## 9. Khuyến nghị ti��p theo

### Khuyến nghị cho FE

- Giữ mô hình hiện tại cho realtime:
  - nhận qua WebSocket
  - gửi STOMP trước
  - gọi REST backup sau
- tiếp tục dùng REST trực tiếp cho available orders nếu đó là chủ đích sản phẩm

### Khuyến nghị cho BE

- kiểm tra lại endpoint `/ws` trên production
- xác minh cấu hình SockJS/WebSocket upgrade
- kiểm tra reverse proxy nếu có Nginx / gateway ở trước backend
- xác minh transport `/ws/{server-id}/{session-id}/websocket` không bị lỗi 500

### Khuyến nghị cho tài liệu

- đồng bộ lại toàn bộ docs liên quan order response
- ghi rõ từng luồng theo ngữ cảnh:
  - realtime request flow
  - available orders flow
  - active/current order sync flow

---

## 10. Kết luận cuối cùng

Kết quả rà soát cho thấy cấu hình WebSocket FE **đã có triển khai thực tế và về mặt code là nhất quán nội bộ** sau khi cập nhật tài liệu. Tuy nhiên:

- docs trước đó chưa phản ánh đúng implementation hiện tại,
- luồng phản hồi đơn trong repo từng bị mô tả không thống nhất,
- backend production hiện vẫn có dấu hiệu lỗi WebSocket upgrade.

Vì vậy, có thể chốt như sau:

- **FE code hiện tại:** hợp lý theo mô hình WS + REST backup
- **tài liệu realtime:** đã được chuẩn hóa theo implementation FE
- **hệ thống production:** cần backend xác minh thêm để xử lý lỗi upgrade 500
