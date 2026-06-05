# Những Thứ FE Cần Sửa

> Tổng hợp từ báo cáo `docs/websocket_fe_review_report.md`. Ghi lại những chỗ tài liệu cũ chưa đúng so với FE thực tế và những gì FE cần điều chỉnh.

---

## 1. Package trong `pubspec.yaml`

### Hiện tại (tài liệu cũ)

```yaml
flutter_stomp_dart: ^2.1.0
```

### Cần sửa thành

```yaml
stomp_dart_client: ^3.0.1
```

---

## 2. Endpoint WebSocket

### Hiện tại (tài liệu cũ)

```dart
wss://be-foodgo.canluaz.io.vn/ws
```

### Cần sửa thành

```dart
https://be-foodgo.canluaz.io.vn/ws
```

### Lý do

- Đây là **SockJS endpoint**, không phải WebSocket thuần.
- FE hiện dùng `stomp_dart_client` với SockJS transport.
- Cấu hình kiểu `wss://` (WebSocket thuần) sẽ không hoạt động đúng với cách FE hiện tại đang cài.

---

## 3. Code WebSocket Service

### Hiện tại (tài liệu cũ — dùng `flutter_stomp_dart`)

```dart
import 'package:flutter_stomp_dart/flutter_stomp_dart.dart';

_client = SockJsClient(
  [_wsUrl],
  onConnect: _onConnect,
  ...
);
```

### Cần sửa thành (dùng `stomp_dart_client`)

```dart
import 'package:stomp_dart_client/stomp_dart_client.dart';

_client = StompClient(
  StompConfig.sockJS(
    url: 'https://be-foodgo.canluaz.io.vn/ws',
    stompConnectHeaders: {
      'Authorization': 'Bearer $jwtToken',
    },
    webSocketConnectHeaders: {
      'Authorization': 'Bearer $jwtToken',
    },
    onConnect: _onConnect,
    onDisconnect: _onDisconnect,
    onWebSocketError: (error) => debugPrint('[WS] Error: $error'),
    onStompError: (frame) => debugPrint('[WS] STOMP Error: ${frame.body}'),
  ),
);

_client!.activate();
```

### Điểm khác biệt chính

| | Tài liệu cũ | Cần sửa |
|---|---|---|
| package | `flutter_stomp_dart` | `stomp_dart_client` |
| client class | `SockJsClient` | `StompClient` |
| config | tạo `SockJsClient` trực tiếp | dùng `StompConfig.sockJS()` |
| activate | gọi `.connect()` | gọi `.activate()` |
| disconnect | `.disconnect()` | `.deactivate()` |

---

## 4. Mô Hình Phản Hồi Đơn

### Hiện tại (tài liệu cũ)

Tài liệu cũ mô tả có 3 cách phản hồi đơn khác nhau, gây nhầm lẫn.

### Cần phân biệt rõ 2 ngữ cảnh

#### Ngữ cảnh 1: Luồng order request realtime (popup nhận đơn)

Đây là luồng khi tài xế nhận thông báo có đơn mới qua WebSocket.

**Thứ tự thực hiện:**

1. Tài xế nhận được `ORDER_REQUEST` qua WebSocket
2. FE hiện popup nhận đơn
3. Tài xế bấm **Nhận** hoặc **Từ chối**
4. FE gửi **STOMP trước**:
   - Nhận đơn → `/app/driver/accept`
   - Từ chối đơn → `/app/driver/decline`
5. Sau đó FE gọi **REST backup**:
   - `POST /api/drivers/orders/{orderId}/respond`
   - body: `{"action": "accept"}` hoặc `{"action": "decline"}`

#### Ngữ cảnh 2: Luồng danh sách đơn khả dụng

Đây là luồng khi tài xế vào màn hình danh sách đơn và chọn đơn để nhận.

**Thứ tự thực hiện:**

FE không dùng WebSocket cho thao tác này.

FE gọi trực tiếp REST:

- Nhận đơn → `POST /api/drivers/orders/{orderId}/accept`
- Từ chối đơn → `POST /api/drivers/orders/{orderId}/decline`

### Tóm tắt

| Ngữ cảnh | Kênh dùng |
|---|---|
| Realtime popup (ORDER_REQUEST) | STOMP trước + REST `/respond` sau |
| Available orders list | REST trực tiếp `/accept` hoặc `/decline` |

---

## 5. Đồng Bộ UI Sau Khi Accept / Decline

### Hiện tại (tài liệu cũ)

Chỉ ghi chung chung "gọi REST để đồng bộ".

### Contract mới backend cho `ORDER_REQUEST` và `/respond`

BE đã sửa luồng gửi order request realtime để **trả thẳng `requestId` trong WebSocket event, FCM data và notification document**, thay vì bắt FE phải chờ Firestore request document mới biết `requestId`.

Điều này có nghĩa là FE cần đổi như sau:

#### 1. Đọc `requestId` trực tiếp từ payload realtime

Với sự kiện `ORDER_REQUEST`, FE cần lấy `requestId` trực tiếp từ payload backend.

Ví dụ payload mới:

```json
{
  "event": "ORDER_REQUEST",
  "message": "Co don hang moi",
  "orderId": "order_123",
  "requestId": "req_456",
  "driverId": "user_007",
  "status": "PENDING",
  "expiresAt": "..."
}
```

Nếu app nhận qua FCM data thì data payload giờ cũng có:

```json
{
  "type": "new_order_request",
  "orderId": "order_123",
  "requestId": "req_456",
  "driverId": "user_007"
}
```

#### 2. Không chờ Firestore request document mới hiện popup

FE cần bỏ logic kiểu:

- nhận FCM/WebSocket
- đợi query Firestore `order_requests/{driverId}/requests/*`
- khi nào thấy document mới hiện popup

Thay vào đó:

- nhận WebSocket hoặc FCM
- parse `orderId`, `requestId`, `expiresAt`
- hiện popup ngay
- chỉ dùng Firestore nếu thật sự còn mục đích phụ khác

#### 3. Bắt buộc gửi `requestId` khi gọi realtime accept/decline và REST `/respond`

Với luồng realtime popup (`ORDER_REQUEST`):

- STOMP `/app/driver/accept`
- STOMP `/app/driver/decline`
- REST `POST /api/drivers/orders/{orderId}/respond`

đều phải dùng `requestId` của chính request realtime vừa nhận.

Body STOMP / REST cần giữ dạng:

```json
{
  "orderId": "order_123",
  "requestId": "req_456"
}
```

hoặc với REST `/respond`:

```json
{
  "action": "accept",
  "requestId": "req_456"
}
```

#### 4. FE phải lưu `requestId` trong local state

Object popup / bottom sheet / pending request model phía FE nên giữ ít nhất:

```json
{
  "orderId": "order_123",
  "requestId": "req_456",
  "expiresAt": "...",
  "source": "websocket_or_fcm"
}
```

`requestId` phải đi cùng popup cho tới lúc tài xế bấm accept/decline.

#### 5. FE không tự delete Firestore

Backend vẫn tự cleanup document tạm tại:

```text
order_requests/{driverId}/requests/{requestId}
```

Vì vậy FE cần:

- không tự xóa Firestore document
- không phụ thuộc delete client-side để hoàn tất flow
- chỉ cập nhật local UI theo kết quả backend

### Cần bổ sung cụ thể

Sau khi tài xế bấm accept hoặc decline, FE nên gọi lại:

#### Với accept

```dart
// sau khi gửi STOMP accept
// gọi REST backup
await api.post('/api/drivers/orders/$orderId/respond', body: {
  'action': 'accept',
  'requestId': requestId,
});

// sau đó refresh danh sách đơn hiện tại
await api.get('/api/drivers/orders/current');
// hoặc
await api.get('/api/drivers/orders/active');
```

#### Với decline

```dart
// sau khi gửi STOMP decline
// gọi REST backup
await api.post('/api/drivers/orders/$orderId/respond', body: {
  'action': 'decline',
  'requestId': requestId,
});

// refresh danh sách đơn khả dụng
await api.get('/api/drivers/orders/available');
```

#### Hành vi UI khuyến nghị

- Accept success:
  - đóng popup
  - remove item local
  - refresh `current` hoặc `active`
- Decline success:
  - đóng popup
  - remove item local
  - refresh `available` nếu cần
- API fail:
  - giữ item/popup hoặc hiển thị lỗi
  - không cleanup local như thể backend đã thành công

---

## 6. Mẫu Payload Và Dart Model Để FE Áp Dụng Nhanh

### 6.1. Payload WebSocket `ORDER_REQUEST`

Khi backend đẩy đơn mới qua `/user/queue/order-request`, FE nên parse payload theo schema sau:

```json
{
  "event": "ORDER_REQUEST",
  "message": "Co don hang moi",
  "orderId": "order_123",
  "requestId": "req_456",
  "driverId": "user_007",
  "status": "PENDING",
  "expiresAt": "2026-06-05T13:35:20Z",
  "storeLat": 10.7765,
  "storeLng": 106.7009,
  "deliveryLat": 10.7842,
  "deliveryLng": 106.6951,
  "deliveryHeading": 42.5
}
```

### 6.2. Payload FCM data

Nếu app nhận được push FCM, FE nên parse `message.data` theo schema sau:

```json
{
  "type": "new_order_request",
  "orderId": "order_123",
  "requestId": "req_456",
  "driverId": "user_007"
}
```

### 6.3. Payload STOMP khi tài xế bấm accept

Gửi lên `/app/driver/accept`:

```json
{
  "orderId": "order_123",
  "requestId": "req_456"
}
```

### 6.4. Payload STOMP khi tài xế bấm decline

Gửi lên `/app/driver/decline`:

```json
{
  "orderId": "order_123",
  "requestId": "req_456"
}
```

### 6.5. Payload REST backup `/respond`

Accept:

```json
{
  "action": "accept",
  "requestId": "req_456"
}
```

Decline:

```json
{
  "action": "decline",
  "requestId": "req_456"
}
```

### 6.6. Dart model mẫu cho `ORDER_REQUEST`

```dart
class DriverOrderRequestEvent {
  final String event;
  final String message;
  final String orderId;
  final String requestId;
  final String driverId;
  final String status;
  final DateTime? expiresAt;
  final double? storeLat;
  final double? storeLng;
  final double? deliveryLat;
  final double? deliveryLng;
  final double? deliveryHeading;

  const DriverOrderRequestEvent({
    required this.event,
    required this.message,
    required this.orderId,
    required this.requestId,
    required this.driverId,
    required this.status,
    this.expiresAt,
    this.storeLat,
    this.storeLng,
    this.deliveryLat,
    this.deliveryLng,
    this.deliveryHeading,
  });

  factory DriverOrderRequestEvent.fromJson(Map<String, dynamic> json) {
    return DriverOrderRequestEvent(
      event: (json['event'] ?? '').toString(),
      message: (json['message'] ?? '').toString(),
      orderId: (json['orderId'] ?? '').toString(),
      requestId: (json['requestId'] ?? '').toString(),
      driverId: (json['driverId'] ?? '').toString(),
      status: (json['status'] ?? '').toString(),
      expiresAt: json['expiresAt'] != null
          ? DateTime.tryParse(json['expiresAt'].toString())
          : null,
      storeLat: _toDouble(json['storeLat']),
      storeLng: _toDouble(json['storeLng']),
      deliveryLat: _toDouble(json['deliveryLat']),
      deliveryLng: _toDouble(json['deliveryLng']),
      deliveryHeading: _toDouble(json['deliveryHeading']),
    );
  }

  Map<String, dynamic> toAcceptSocketBody() {
    return {
      'orderId': orderId,
      'requestId': requestId,
    };
  }

  Map<String, dynamic> toDeclineSocketBody() {
    return {
      'orderId': orderId,
      'requestId': requestId,
    };
  }

  Map<String, dynamic> toAcceptRespondBody() {
    return {
      'action': 'accept',
      'requestId': requestId,
    };
  }

  Map<String, dynamic> toDeclineRespondBody() {
    return {
      'action': 'decline',
      'requestId': requestId,
    };
  }

  bool get isValidForAction => orderId.isNotEmpty && requestId.isNotEmpty;

  static double? _toDouble(Object? value) {
    if (value == null) return null;
    if (value is num) return value.toDouble();
    return double.tryParse(value.toString());
  }
}
```

### 6.7. Dart model mẫu cho FCM data

```dart
class DriverOrderRequestPushData {
  final String type;
  final String orderId;
  final String requestId;
  final String driverId;

  const DriverOrderRequestPushData({
    required this.type,
    required this.orderId,
    required this.requestId,
    required this.driverId,
  });

  factory DriverOrderRequestPushData.fromMap(Map<String, dynamic> data) {
    return DriverOrderRequestPushData(
      type: (data['type'] ?? '').toString(),
      orderId: (data['orderId'] ?? '').toString(),
      requestId: (data['requestId'] ?? '').toString(),
      driverId: (data['driverId'] ?? '').toString(),
    );
  }

  bool get isNewOrderRequest => type == 'new_order_request';
  bool get isValid => orderId.isNotEmpty && requestId.isNotEmpty;
}
```

### 6.8. Ví dụ parse WebSocket event trong Flutter

```dart
socketService.orderRequestStream.listen((raw) {
  final event = DriverOrderRequestEvent.fromJson(raw);

  if (event.event != 'ORDER_REQUEST') return;
  if (!event.isValidForAction) return;

  showIncomingOrderPopup(
    orderId: event.orderId,
    requestId: event.requestId,
    expiresAt: event.expiresAt,
    storeLat: event.storeLat,
    storeLng: event.storeLng,
    deliveryLat: event.deliveryLat,
    deliveryLng: event.deliveryLng,
  );
});
```

### 6.9. Ví dụ parse FCM data trong Flutter

```dart
Future<void> handleDriverForegroundMessage(RemoteMessage message) async {
  final pushData = DriverOrderRequestPushData.fromMap(message.data);

  if (!pushData.isNewOrderRequest || !pushData.isValid) return;

  showIncomingOrderPopup(
    orderId: pushData.orderId,
    requestId: pushData.requestId,
  );
}
```

### 6.10. Ví dụ gửi accept từ popup

```dart
Future<void> acceptIncomingOrder(
  DriverOrderRequestEvent event,
) async {
  if (!event.isValidForAction) return;

  stompClient.send(
    destination: '/app/driver/accept',
    body: jsonEncode(event.toAcceptSocketBody()),
  );

  await api.post(
    '/api/drivers/orders/${event.orderId}/respond',
    body: event.toAcceptRespondBody(),
  );

  await api.get('/api/drivers/orders/current');
}
```

### 6.11. Ví dụ gửi decline từ popup

```dart
Future<void> declineIncomingOrder(
  DriverOrderRequestEvent event,
) async {
  if (!event.isValidForAction) return;

  stompClient.send(
    destination: '/app/driver/decline',
    body: jsonEncode(event.toDeclineSocketBody()),
  );

  await api.post(
    '/api/drivers/orders/${event.orderId}/respond',
    body: event.toDeclineRespondBody(),
  );

  await api.get('/api/drivers/orders/available');
}
```

### 6.12. Checklist tích hợp nhanh cho FE

- parse `requestId` trực tiếp từ WebSocket `ORDER_REQUEST`
- parse `requestId` trực tiếp từ FCM `message.data`
- popup state phải giữ `orderId` + `requestId`
- accept/decline luôn gửi đúng `requestId`
- không chờ Firestore request doc mới hiện popup
- không tự delete Firestore request doc phía client

---

## 7. Xử Lý Lỗi WebSocket Production

### Hiện tại

Tài liệu chưa đề cập đến việc xử lý khi WebSocket production lỗi upgrade.

### Cần bổ sung

Runtime app hiện tại đang gặp lỗi:

```
WebSocketException: Connection to 'https://be-foodgo.canluaz.io.vn:0/ws/.../websocket#'
was not upgraded to websocket, HTTP status code: 500
```

Điều này có nghĩa là khi WebSocket không kết nối được, app vẫn cần hoạt động được.

#### Những gì FE cần thêm vào code

##### Tự động reconnect

```dart
void _onDisconnect(StompFrame? frame) {
  _connectionController.add(false);
  _client = null;

  // Thử reconnect sau 3 giây
  Future.delayed(Duration(seconds: 3), () {
    if (_token != null) {
      connect(_token!);
    }
  });
}
```

##### Fallback sang polling nhẹ khi mất kết nối

```dart
Timer? _pollingTimer;

void startPolling() {
  _pollingTimer?.cancel();
  _pollingTimer = Timer.periodic(Duration(seconds: 10), (_) async {
    if (!_isConnected) {
      await _refreshAvailableOrders();
    }
  });
}

void stopPolling() {
  _pollingTimer?.cancel();
  _pollingTimer = null;
}

Future<void> _refreshAvailableOrders() async {
  // gọi GET /api/drivers/orders/available
}
```

##### Theo dõi trạng thái kết nối trên UI

```dart
socketService.connectionStream.listen((connected) {
  if (!connected) {
    // hiển thị banner hoặc icon mất realtime
  } else {
    // ẩn banner, bật lại realtime
  }
});
```

---

## 7. Kiểm Tra Lại Các File Tài Liệu Liên Quan

Báo cáo khuyến nghị rà soát lại các file sau để đảm bảo thống nhất:

- `docs/driver_api_for_fe.md`
- `docs/driver_order_flows.md`

Những file này có thể vẫn mô tả luồng phản hồi đơn không đồng bộ với những gì FE thực tế đang làm.

---

## 8. Checklist Sửa Tài Liệu

- [ ] Đổi package từ `flutter_stomp_dart` sang `stomp_dart_client`
- [ ] Đổi endpoint từ `wss://...` sang `https://...` (SockJS)
- [ ] Cập nhật code mẫu WebSocket service theo `StompClient` + `StompConfig.sockJS()`
- [ ] Tách rõ 2 ngữ cảnh phản hồi đơn:
  - realtime popup: STOMP + REST `/respond`
  - available orders: REST trực tiếp `/accept` hoặc `/decline`
- [ ] Bổ sung phần reconnect khi mất kết nối
- [ ] Bổ sung polling fallback khi WebSocket production lỗi
- [ ] Đồng bộ lại các file tài liệu còn lại

---

## 9. Ghi Chú Quan Trọng

### Về WebSocket production

Lỗi upgrade HTTP 500 hiện tại nhiều khả năng nằm ở:

- cấu hình SockJS trên backend
- reverse proxy (Nginx) chặn upgrade
- firewall hoặc load balancer không hỗ trợ WebSocket upgrade

Đây là vấn đề **phía backend/production deployment**, không phải do code FE.

### Về mô hình WS + REST

Mô hình FE đang dùng là hợp lý:

- STOMP để phản hồi nhanh, độ trễ thấp
- REST backup để đồng bộ và xác nhận trạng thái cuối cùng từ backend

Không nên bỏ REST backup vì WebSocket có thể không ổn định ở production.
