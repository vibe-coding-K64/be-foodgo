# API Documentation - Customer App

## Muc Luc

1. [Xac Thuc (Auth)](#1-xac-thuc-auth)
2. [Ho So (Profile)](#2-ho-so-profile)
3. [Dia Chi (Address)](#3-dia-chi-address)
4. [Gio Hang (Cart)](#4-gio-hang-cart)
5. [Dat Hang (Checkout)](#5-dat-hang-checkout)
6. [Thanh Toan (Payment)](#6-thanh-toan-payment)
7. [Tim Kiem (Search)](#7-tim-kiem-search)
8. [Don Hang (Order)](#8-don-hang-order)
9. [Danh Gia (Review)](#9-danh-gia-review)

---

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api` |
| **Content-Type** | `application/json` |

### Authentication

| API | Yeu cau Bearer Token |
|---|---|
| Auth | Khong |
| Profile | **Co** (JWT) |
| Address | Khong |
| Cart | Khong |
| Checkout | Khong |
| Payment | **Co** (JWT) |
| Search | Khong |
| Order | Khong |
| Review | Khong |

### Common Response Structure

Tat ca cac response deu tra ve JSON voi cau truc chung:

```json
{
  "success": true,
  "code": 200,
  "message": "Mo ta ket qua",
  "data": { ... }
}
```

| Thuoc tinh | Kieu | Mo ta |
|---|---|---|
| `success` | Boolean | Trang thai thanh cong hay that bai |
| `code` | Integer | Ma HTTP status |
| `message` | String | Thong bao ket qua |
| `data` | Object/Array/null | Du lieu tra ve (null neu khong co data) |

---

## 1. Xac Thuc (Auth)

**Base URL:** `/api/auth`

| Method | Endpoint | Mo ta | Auth |
|---|---|---|---|
| `POST` | `/register` | Dang ky tai khoan moi | Khong |
| `POST` | `/login` | Dang nhap bang email/password | Khong |
| `POST` | `/send-otp` | Gui ma OTP | Khong |
| `POST` | `/verify-otp` | Xac thuc ma OTP | Khong |
| `POST` | `/reset-password` | Dat lai mat khau | Khong |
| `POST` | `/register-merchant` | Dang ky tai khoan nguoi ban | Khong |
| `GET` | `/check-merchant` | Kiem tra quyen nguoi ban | Khong |

**Chi tiet:** [API - Xac Thuc (Auth)](API - Xac Thuc (Auth).md)

---

## 2. Ho So (Profile)

**Base URL:** `/api/customers`

| Method | Endpoint | Mo ta | Auth |
|---|---|---|---|
| `PUT` | `/profile` | Cap nhat thong tin ho so | **JWT** |
| `PUT` | `/password` | Doi mat khau chu dong | **JWT** |

**Chi tiet:** [API - Ho So (Profile)](API - Ho So (Profile).md)

---

## 3. Dia Chi (Address)

**Base URL:** `/api/addresses`

| Method | Endpoint | Mo ta | Auth |
|---|---|---|---|
| `POST` | `/` | Them dia chi moi | Khong |
| `GET` | `/` | Lay danh sach dia chi | Khong |
| `GET` | `/{id}` | Lay thong tin mot dia chi | Khong |
| `PUT` | `/{id}` | Cap nhat dia chi | Khong |
| `PUT` | `/{id}/default` | Dat dia chi lam mac dinh | Khong |
| `DELETE` | `/{id}` | Xoa dia chi | Khong |

**Chi tiet:** [API - Dia Chi (Address)](API - Dia Chi (Address).md)

---

## 4. Gio Hang (Cart)

**Base URL:** `/api/cart`

| Method | Endpoint | Mo ta | Auth |
|---|---|---|---|
| `POST` | `/add` | Them mon vao gio hang | Khong |
| `PUT` | `/{itemId}/quantity` | Cap nhat so luong mon | Khong |
| `DELETE` | `/{itemId}` | Xoa mot mon khoi gio hang | Khong |
| `DELETE` | `/` | Xoa toan bo gio hang | Khong |

**Chi tiet:** [API - Gio Hang (Cart)](API - Gio Hang (Cart).md)

---

## 5. Dat Hang (Checkout)

**Base URL:** `/api/orders`

| Method | Endpoint | Mo ta | Auth |
|---|---|---|---|
| `POST` | `/checkout` | Dat hang (Checkout) | Khong |

**Chi tiet:** [API - Dat Hang (Checkout)](API - Dat Hang (Checkout).md)

---

## 6. Thanh Toan (Payment)

**Base URL:** `/api/payments`

| Method | Endpoint | Mo ta | Auth |
|---|---|---|---|
| `GET` | `/` | Lay danh sach phuong thuc thanh toan | **JWT** |
| `GET` | `/{id}` | Lay mot phuong thuc thanh toan | **JWT** |
| `POST` | `/` | Them phuong thuc thanh toan moi | **JWT** |
| `PUT` | `/{id}/default` | Dat phuong thuc thanh toan mac dinh | **JWT** |
| `DELETE` | `/{id}` | Xoa phuong thuc thanh toan | **JWT** |

**Chi tiet:** [API - Thanh Toan (Payment)](API - Thanh Toan (Payment).md)

---

## 7. Tim Kiem (Search)

**Base URL:** `/api/search`

| Method | Endpoint | Mo ta | Auth |
|---|---|---|---|
| `GET` | `/` | Tim kiem mon an va quan an | Khong |

**Chi tiet:** [API - Tim Kiem (Search)](API - Tim Kiem (Search).md)

---

## 8. Don Hang (Order)

**Base URL:** `/api/orders`

| Method | Endpoint | Mo ta | Auth |
|---|---|---|---|
| `GET` | `/` | Lay danh sach don hang | Khong |
| `GET` | `/{id}` | Lay don hang theo ID | Khong |
| `POST` | `/` | Tao don hang moi | Khong |
| `PATCH` | `/{id}/status` | Cap nhat trang thai don hang | Khong |
| `POST` | `/{id}/cancel` | Huy don hang | Khong |

**Chi tiet:** [API - Don Hang (Order)](API - Don Hang (Order).md)

---

## 9. Danh Gia (Review)

**Base URL:** `/api/reviews`

| Method | Endpoint | Mo ta | Auth |
|---|---|---|---|
| `POST` | `/` | Tao danh gia | Khong |
| `GET` | `/` | Lay danh sach danh gia theo cua hang | Khong |

**Chi tiet:** [API - Danh Gia (Review)](API - Danh Gia (Review).md)

---

## Bang Tom Tat Tat Ca Endpoint

| STT | Module | Method | Endpoint | Auth | Mo ta |
|---|---|---|---|---|---|
| 1 | Auth | POST | `/api/auth/register` | Khong | Dang ky tai khoan |
| 2 | Auth | POST | `/api/auth/login` | Khong | Dang nhap |
| 3 | Auth | POST | `/api/auth/send-otp` | Khong | Gui OTP |
| 4 | Auth | POST | `/api/auth/verify-otp` | Khong | Xac thuc OTP |
| 5 | Auth | POST | `/api/auth/reset-password` | Khong | Dat lai mat khau |
| 6 | Auth | POST | `/api/auth/register-merchant` | Khong | Dang ky nguoi ban |
| 7 | Auth | GET | `/api/auth/check-merchant` | Khong | Kiem tra quyen nguoi ban |
| 8 | Profile | PUT | `/api/customers/profile` | **JWT** | Cap nhat ho so |
| 9 | Profile | PUT | `/api/customers/password` | **JWT** | Doi mat khau |
| 10 | Address | POST | `/api/addresses` | Khong | Them dia chi |
| 11 | Address | GET | `/api/addresses` | Khong | Danh sach dia chi |
| 12 | Address | GET | `/api/addresses/{id}` | Khong | Mot dia chi |
| 13 | Address | PUT | `/api/addresses/{id}` | Khong | Cap nhat dia chi |
| 14 | Address | PUT | `/api/addresses/{id}/default` | Khong | Dat mac dinh |
| 15 | Address | DELETE | `/api/addresses/{id}` | Khong | Xoa dia chi |
| 16 | Cart | POST | `/api/cart/add` | Khong | Them mon |
| 17 | Cart | PUT | `/api/cart/{itemId}/quantity` | Khong | Cap nhat so luong |
| 18 | Cart | DELETE | `/api/cart/{itemId}` | Khong | Xoa mot mon |
| 19 | Cart | DELETE | `/api/cart` | Khong | Xoa toan bo |
| 20 | Checkout | POST | `/api/orders/checkout` | Khong | Dat hang |
| 21 | Payment | GET | `/api/payments` | **JWT** | Danh sach thanh toan |
| 22 | Payment | GET | `/api/payments/{id}` | **JWT** | Mot thanh toan |
| 23 | Payment | POST | `/api/payments` | **JWT** | Them thanh toan |
| 24 | Payment | PUT | `/api/payments/{id}/default` | **JWT** | Dat mac dinh |
| 25 | Payment | DELETE | `/api/payments/{id}` | **JWT** | Xoa thanh toan |
| 26 | Search | GET | `/api/search` | Khong | Tim kiem |
| 27 | Order | GET | `/api/orders` | Khong | Danh sach don |
| 28 | Order | GET | `/api/orders/{id}` | Khong | Mot don |
| 29 | Order | POST | `/api/orders` | Khong | Tao don |
| 30 | Order | PATCH | `/api/orders/{id}/status` | Khong | Cap nhat trang thai |
| 31 | Order | POST | `/api/orders/{id}/cancel` | Khong | Huy don |
| 32 | Review | POST | `/api/reviews` | Khong | Tao danh gia |
| 33 | Review | GET | `/api/reviews` | Khong | Danh sach danh gia |

**Tong cong: 33 endpoints**

---

## Collections Firestore Lien Quan

| Collection | Mo ta |
|---|---|
| `users` | Tai khoan nguoi dung |
| `customer_profiles/{userId}/addresses` | Sub-collection dia chi |
| `customer_profiles/{userId}/cart` | Sub-collection gio hang |
| `customer_profiles/{userId}/payment_methods` | Sub-collection phuong thuc thanh toan |
| `customer_profiles/{userId}/my_vouchers` | Sub-collection voucher ca nhan |
| `customer_profiles/{userId}/notifications` | Sub-collection thong bao |
| `customer_profiles/{userId}/search_history` | Sub-collection lich su tim kiem |
| `orders` | Don hang |
| `products` | San pham |
| `stores` | Cua hang |
| `vouchers` | Voucher he thong |
| `reviews` | Danh gia |

---

## Cac Loai Roles

| Gia tri | Mo ta |
|---|---|
| `1` | Khach hang |
| `2` | Tai xe |
| `3` | Nguoi ban |
| `4` | Admin |

---

## Cac Trang Thai Don Hang

| Gia tri | Mo ta |
|---|---|
| `0` | Cho xac nhan |
| `1` | Dang chuan bi |
| `2` | Dang giao |
| `3` | Hoan thanh |
| `4` | Da huy |

---

## Cac Loai Phuong Thuc Thanh Toan

| Loai | Hien thi |
|---|---|
| `cash` | Tien mat |
| `momo` | MoMo |
| `zalo` | ZaloPay |
| `card` | The ngan hang |

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
