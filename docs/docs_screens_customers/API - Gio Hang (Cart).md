# API - Gio Hang (Cart)

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/cart` |
| **Authentication** | Khong can Bearer Token (userId truyen qua body) |
| **Content-Type** | `application/json` |

---

## 1. Them Mon Vao Gio Hang

**Endpoint:** `POST /api/cart/add`

**Mo ta:** Them mot mon an vao gio hang cua khach hang. Neu gio hang da co mon tu cua hang khac, he thong se tra ve loi yeu cau xac nhan xoa gio hang cu. Gia tien duoc tinh toan tu phia server dua tren basePrice, size va toppings tu collection products.

### Request Body

```json
{
  "userId": "user_001",
  "storeId": "store_001",
  "foodId": "prod_001",
  "size": "M",
  "toppings": [
    { "name": "Tran chau", "price": 5000.0 },
    { "name": "Thach", "price": 3000.0 }
  ],
  "note": "Khong them hau",
  "quantity": 2
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |
| `storeId` | String | **Co** | ID cua hang chua mon an | `store_001` |
| `foodId` | String | **Co** | ID san pham (mon an) can them vao gio | `prod_001` |
| `size` | String | Khong | Kich thuoc da chon (VD: M, L). Co the null neu san pham khong co tuy chon size. | `M` |
| `toppings` | List<ToppingOption> | Khong | Danh sach topping da chon | Xem vi du |
| `toppings[].name` | String | **Co** (neu toppings != null) | Ten topping | `Tran chau` |
| `toppings[].price` | Double | **Co** (neu toppings != null) | Gia cua topping (VND) | `5000.0` |
| `note` | String | Khong | Ghi chu cho cua hang ve mon nay | `Khong them hau` |
| `quantity` | Integer | **Co** | So luong mon them vao gio (lon hon 0) | `2` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Da them mon vao gio hang thanh cong.",
  "data": {
    "id": "cart_item_003",
    "userId": "user_001",
    "storeId": "store_001",
    "foodId": "prod_001",
    "name": "Com tam suon bi cha",
    "price": 106000.0,
    "quantity": 2,
    "size": "M",
    "sizePrice": 5000.0,
    "toppings": [
      { "name": "Tran chau", "price": 5000.0 },
      { "name": "Thach", "price": 3000.0 }
    ],
    "note": "Khong them hau",
    "imageUrl": "https://images.unsplash.com/photo-xxx",
    "createdAt": "2026-05-26T10:00:00Z",
    "updatedAt": "2026-05-26T10:00:00Z"
  }
}
```

### Response (That bai - 400 - Mon an het hang)

```json
{
  "success": false,
  "code": 400,
  "message": "Mon an dang het hang. Vui long chon mon khac."
}
```

### Response (That bai - 400 - Vi pham quy tac mot cua hang)

```json
{
  "success": false,
  "code": 400,
  "message": "Gio hang hien tai thuoc cua hang [store_001]. Vui long xoa gio hang de chon mon tu cua hang [store_002]."
}
```

### Response (That bai - 404)

```json
{
  "success": false,
  "code": 404,
  "message": "San pham khong ton tai trong he thong."
}
```

### Gia Tri Tra Ve (Tinh Toan Phia Server)

Gia tong cua mon = (basePrice + sizePrice + tongGiaToppings) * quantity

| Thanh Phan | Vi du |
|---|---|
| basePrice (gia co so) | `45000.0` |
| sizePrice (phu phi size M) | `5000.0` |
| tongGiaToppings (5000 + 3000) | `8000.0` |
| Don gia (45000 + 5000 + 8000) | `58000.0` |
| So luong | `2` |
| **Tong gia (58000 * 2)** | `116000.0` |

---

## 2. Cap Nhat So Luong Mon

**Endpoint:** `PUT /api/cart/{itemId}/quantity`

**Mo ta:** Cap nhat so luong cua mot mon trong gio hang cua khach hang. So luong phai lon hon 0.

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `itemId` | String | ID cua mon trong gio hang (cartItemId) | `cart_item_001` |

### Request Body

```json
{
  "userId": "user_001",
  "quantity": 5
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |
| `quantity` | Integer | **Co** | So luong moi (lon hon 0) | `5` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Cap nhat so luong mon thanh cong.",
  "data": null
}
```

### Response (That bai - 400)

```json
{
  "success": false,
  "code": 400,
  "message": "So luong khong hop le."
}
```

### Response (That bai - 404)

```json
{
  "success": false,
  "code": 404,
  "message": "Mon khong ton tai trong gio hang."
}
```

---

## 3. Xoa Mot Mon Khoi Gio Hang

**Endpoint:** `DELETE /api/cart/{itemId}`

**Mo ta:** Xoa mot mon an khoi gio hang cua khach hang. Phuong thuc nay la idempotent - tra ve thanh cong ke ca khi mon khong ton tai trong gio hang.

### Path Parameters

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `itemId` | String | ID cua mon trong gio hang (cartItemId) | `cart_item_001` |

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Da xoa mon khoi gio hang thanh cong.",
  "data": null
}
```

---

## 4. Xoa Toan Bo Gio Hang

**Endpoint:** `DELETE /api/cart`

**Mo ta:** Xoa tat ca cac mon trong gio hang cua khach hang. Su dung WriteBatch de toi uu so lan goi API len Firebase.

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Da xoa toan bo gio hang thanh cong.",
  "data": null
}
```

---

## Bieu Do Quy Trinh Gio Hang

```
Nguoi dung xem thuc don (StoreDetailView)
       |
       v
  Bam "+" tren mon an
       |
       v
  +----+----+
  |         |
  |    Mo BottomSheet tuy chon (size, topping, ghi chu)
  |         |
  |         v
  |    Chon size (VD: M)
  |    Chon toppings (VD: Tran chau, Thach)
  |    Nhap ghi chu (VD: "Khong duong")
  |         |
  |         v
  |    Bam "Them vao gio"
  |         |
  +----+----+
       |
       v
  POST /api/cart/add
       |
       v
  +----+----+
  |         |
  OK       400 (Quy tac mot cua hang bi vi pham)
  |
  v
  Gio hang duoc cap nhat
  Icon gio hang hien thi badge so luong

========================================

Nguoi dung mo CartView
       |
       v
  Danh sach mon trong gio (StreamBuilder)
       |
       +-- Bam "+" / "-" tren item
       |    PUT /api/cart/{itemId}/quantity
       |
       +-- Vuot trai tren item (Dismissible)
       |    DELETE /api/cart/{itemId}
       |
       +-- Bam "Xoa toan bo"
            DELETE /api/cart
```

---

## Quy Tac Quan Trong

### Quy tac mot cua hang

- Gio hang chi chua mon tu mot cua hang duy nhat.
- Neu nguoi dung da co mon cua hang A trong gio, them mon cua hang B se tra ve loi 400.
- Nguoi dung phai xoa gio hang cu (DELETE /api/cart) truoc khi them mon tu cua hang moi.

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
