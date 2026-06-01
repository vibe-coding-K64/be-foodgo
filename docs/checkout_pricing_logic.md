# Logic Tính Tiền Trang Thanh Toán (Checkout)

> Xuất từ code thực tế của `checkout_view.dart` và các file liên quan.

---

## 1. Công Thức Cơ Bản

```
Tổng thanh toán = Tạm tính + Phí giao - Giảm giá - Giảm giá shop - Giảm freeship
```

**Code thực tế:**

```dart
double get _totalPayment {
  return _subtotal + _deliveryFee - _discount - _shopDiscount - _freeshipDiscount;
}
```

---

## 2. Chi Tiết Từng Bước

### Bước 1: Tạm Tính (Subtotal)

= Tổng giá tất cả các món đã chọn (đã bao gồm topping + size)

```dart
double get _subtotal {
  return _cartItems.fold(0, (sum, item) => sum + item.totalPrice);
}
```

Trong đó `item.totalPrice` = `unitPrice * quantity`

- `unitPrice` = giá base + giá size + giá toppings đã chọn

---

### Bước 2: Phí Giao Hàng (Delivery Fee)

= **Cố định 15.000 VND** (hardcoded, không tính theo khoảng cách)

```dart
double get _deliveryFee {
  return 15000;
}
```

---

### Bước 3: Giảm Giá Từ Voucher (Discount / Shop Discount)

```dart
double _getVoucherDiscount(VoucherModel? voucher) {
  if (voucher == null) return 0;

  if (voucher.isFreeship) {
    // Voucher freeship: giảm tối đa bằng phí giao
    return _deliveryFee.clamp(0, voucher.value);
  }

  if (voucher.type == 1) {
    // Type 1 = percent (phần trăm)
    return (_subtotal * voucher.value / 100).clamp(0, double.infinity);
  }

  // Type 2 = fixed amount (số tiền cố định VND)
  return voucher.value.clamp(0, double.infinity);
}
```

| Loại voucher | Type | Cách tính | Ví dụ |
|---|---|---|---|
| Giảm % | 1 | `subtotal * value / 100` | subtotal=198k, value=15 → 29.700 |
| Giảm tiền | 2 | Giá trị `value` VND | value=10.000 → 10.000 |
| Freeship | isFreeship=true | `min(value, deliveryFee)` | value=20k → 15.000 |

---

### Bước 4: Giảm Freeship

```dart
double get _freeshipDiscount {
  final voucher = _findSelectedFreeshipVoucher();
  if (voucher == null) return 0;
  return _deliveryFee.clamp(0, voucher.value);
}
```

Lấy giá trị `value` của voucher, giới hạn bằng phí giao (15k).

---

### Bước 5: Tổng Thanh Toán

```
Tổng = Subtotal + DeliveryFee - Discount - ShopDiscount - FreeshipDiscount
```

```dart
Tổng = _subtotal + _deliveryFee - _discount - _shopDiscount - _freeshipDiscount
```

---

## 3. Ví Dụ Thực Tế (Mock Data Mặc Định)

| Món | Giá base | SL | Topping | Thành tiền |
|---|---|---|---|---|
| Trà Sữa Trân Châu Đường | 35.000 | 2 | +13.000 | **109.000** |
| Cà Phê Sữa Đá | 29.000 | 1 | +0 | **29.000** |
| Trà Vải Thạch Quả Vời | 24.000 | 1 | +18.000 | **60.000** |

| Khoản mục | Giá trị |
|---|---|
| **Subtotal** | **198.000** |
| Phí giao | +15.000 |
| Voucher giảm 15% (type=1) | -29.700 |
| Voucher giảm shop 5.000 (type=2) | -5.000 |
| Voucher freeship 20.000 | -15.000 |
| **Tổng thanh toán** | **163.300** |

---

### Backend trả về 3 trường discount riêng biệt

```json
{
  "totalAmount": 198000.0,
  "deliveryFee": 15000.0,
  "discountAmount": 29700.0,
  "shopDiscountAmount": 5000.0,
  "freeshipDiscountAmount": 15000.0,
  "finalAmount": 163300.0
}
```

Công thức backend:
```
finalAmount = totalAmount + deliveryFee - discountAmount - shopDiscountAmount - freeshipDiscountAmount
```

---

## 4. Cấu Trúc Voucher

### Các trường trong `VoucherModel`

```dart
class VoucherModel {
  final String id;
  final String name;
  final String code;
  final String? description;
  final DateTime expiryDate;
  final int type;          // 1 = percent, 2 = fixed amount
  final double value;       // Giá trị giảm (VD: 15 = 15% hoặc 15.000 VND)
  final double minOrderValue; // Giá trị đơn hàng tối thiểu để áp dụng
  final String? storeId;    // null = voucher hệ thống, != null = voucher shop
  final bool isFreeship;   // true = voucher freeship
  final String? imageUrl;
  final int? remaining;     // Số lượng còn lại
  final String? terms;      // Điều kiện sử dụng
  final int pointsRequired; // Số điểm cần để đổi (0 = miễn phí)
}
```

### 3 Nhóm Voucher Độc Lập

| Nhóm | Nguồn | Mô tả | Backend trường |
|---|---|---|---|
| `discountVouchers` | myVouchers + vouchers hệ thống | Giảm giá chung | `discountAmount` |
| `shopVouchers` | vouchers có storeId | Giảm giá riêng của shop | `shopDiscountAmount` |
| `freeshipVouchers` | vouchers isFreeship=true | Miễn phí giao hàng | `freeshipDiscountAmount` |

User có thể chọn **cả 3 voucher cùng lúc** (độc lập với nhau).

---

## 5. Điều Kiện Áp Dụng Voucher

```dart
void _validateSelectedVouchers() {
  final newSubtotal = _subtotal;

  final removedDiscount = _selectedDiscountVoucher.isNotEmpty &&
      (_findSelectedDiscountVoucher()?.minOrderValue ?? 0) > newSubtotal;
  final removedShop = _selectedShopVoucher.isNotEmpty &&
      (_findSelectedShopVoucher()?.minOrderValue ?? 0) > newSubtotal;
  final removedFreeship = _selectedFreeshipVoucher.isNotEmpty &&
      (_findSelectedFreeshipVoucher()?.minOrderValue ?? 0) > newSubtotal;

  if (removedDiscount || removedShop || removedFreeship) {
    // Xóa voucher và hiện cảnh báo
  }
}
```

- Mỗi voucher có `minOrderValue` - nếu subtotal nhỏ hơn → voucher bị tự động xóa
- Hiện SnackBar cảnh báo: *"Voucher không còn áp dụng do giá trị đơn hàng giảm"*

---

## 6. Mô Hình Tính Tiền (Sơ Đồ)

```
┌─────────────────────────────────────────────────────┐
│                    CART ITEMS                        │
│  (unitPrice × quantity, đã bao gồm toppings/size)   │
└─────────────────┬───────────────────────────────────┘
                  │ sum(item.totalPrice)
                  ▼
         ┌──────────────────┐
         │    SUBTOTAL       │
         │  (Tạm tính)      │
         └────────┬──────────┘
                  │
     ┌────────────┼────────────┐
     │            │            │
     ▼            │            ▼
┌─────────┐       │      ┌──────────────┐
│DISCOUNT │       │      │SHOP DISCOUNT │
│(type 1/2)       │      │ (type 1/2)   │
└────┬────┘       │      └──────┬───────┘
     │            │             │
     │            ▼             │
     │     ┌──────────────┐     │
     │     │DELIVERY FEE  │     │
     │     │  (15.000đ)   │     │
     │     └──────┬───────┘     │
     │            │             │
     │     ┌──────┴──────┐      │
     │     │FREESHIP     │      │
     │     │(min(val,15k))      │
     │     └─────────────┘      │
     │                         │
     └─────────┬───────────────┘
               │
               ▼
     ┌──────────────────┐
     │  TOTAL PAYMENT    │
     │  (Tổng thanh toán)│
     └──────────────────┘
```

---

## 7. Lưu Ý Quan Trọng

| # | Lưu ý | Chi tiết |
|---|---|---|
| 1 | Phí giao cố định | Hiện tại hardcoded 15k, chưa tính theo khoảng cách thực tế |
| 2 | 3 voucher độc lập | User có thể chọn cả 3 voucher cùng lúc |
| 3 | Tổng giảm không âm | Tất cả discount đều có `.clamp(0, ...)` |
| 4 | Backend tính lại | Khi gọi API checkout, server tính lại để đảm bảo chính xác |
| 5 | 3 discount riêng biệt | Backend trả về `discountAmount`, `shopDiscountAmount`, `freeshipDiscountAmount` riêng biệt, khớp với FE |

---

## 8. Files Liên Quan

| File | Mục đích |
|---|---|
| `lib/features/checkout/views/checkout_view.dart` | View chính, chứa logic tính tiền |
| `lib/features/checkout/views/widgets/checkout_summary.dart` | Widget hiển thị chi tiết hóa đơn |
| `lib/features/checkout/models/checkout_models.dart` | Models cho request/response checkout |
| `lib/features/checkout/models/voucher_model.dart` | Model voucher, phân loại type |
| `lib/features/checkout/services/checkout_service.dart` | Gọi API checkout |
| `lib/features/checkout/services/my_voucher_firestore_service.dart` | Load voucher từ Firestore |
