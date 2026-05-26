# API - Tim Kiem (Search)

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/search` |
| **Authentication** | Khong can Bearer Token |
| **Content-Type** | `application/json` |

---

## 1. Tim Kiem Mon An Va Quan An

**Endpoint:** `GET /api/search`

**Mo ta:** Tim kiem mon an hoac quan an theo tu khoa, loc theo khoang cach toi da 10km tu vi tri nguoi dung, luu lich su tim kiem va sap xep ket qua.

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `query` | String | **Co** | Tu khoa tim kiem (ten mon an hoac ten quan an) | `com tam` |
| `userLat` | Double | **Co** | Vi do cua dia chi giao hang | `10.8500` |
| `userLng` | Double | **Co** | Kinh do cua dia chi giao hang | `106.7900` |
| `sortBy` | String | Khong | Chieu sap xep ket qua | Xem bang duoi |
| `userId` | String | Khong | ID nguoi dung de luu lich su tim kiem | `user_001` |

### Cac Tuy Chon sortBy

| Gia tri | Mo ta | Vi du |
|---|---|---|
| `priceAsc` | Sap xep theo gia tang dan | Mon 20.000 -> 30.000 -> 45.000 |
| `priceDesc` | Sap xep theo gia giam dan | Mon 45.000 -> 30.000 -> 20.000 |
| `ratingDesc` | Sap xep theo danh gia giam dan | Cua hang 5 sao -> 4.8 sao -> 4.5 sao |
| `null` (khong truyen) | Khong sap xep | Tra ve theo thu tu mac dinh |

### Vi Du Request

```
GET /api/search?query=com%20tam&userLat=10.8500&userLng=106.7900&sortBy=ratingDesc&userId=user_001
```

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Tim thay 3 ket qua phu hop.",
  "data": [
    {
      "productId": "prod_001",
      "productName": "Com tam suon bi cha",
      "storeId": "store_001",
      "storeName": "Com tam Phuc Loc Tho",
      "price": 45000.0,
      "rating": 4.8,
      "reviewCount": 500,
      "distance": 2.1,
      "imageUrl": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"
    },
    {
      "productId": "prod_013",
      "productName": "Com suon tron",
      "storeId": "store_001",
      "storeName": "Com tam Phuc Loc Tho",
      "price": 48000.0,
      "rating": 4.8,
      "reviewCount": 500,
      "distance": 2.1,
      "imageUrl": "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&q=80"
    },
    {
      "productId": "prod_002",
      "productName": "Com tam ga xoi mo",
      "storeId": "store_001",
      "storeName": "Com tam Phuc Loc Tho",
      "price": 50000.0,
      "rating": 4.8,
      "reviewCount": 500,
      "distance": 2.1,
      "imageUrl": "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80"
    }
  ]
}
```

| Thuoc tinh | Kieu du lieu | Mo ta | Vi du |
|---|---|---|---|
| `productId` | String | ID cua san pham (mon an) | `prod_001` |
| `productName` | String | Ten mon an | `Com tam suon bi cha` |
| `storeId` | String | ID cua cua hang | `store_001` |
| `storeName` | String | Ten cua hang | `Com tam Phuc Loc Tho` |
| `price` | Double | Gia co so cua mon an (VND) | `45000.0` |
| `rating` | Double | Diem danh gia trung binh cua cua hang (0.0 - 5.0) | `4.8` |
| `reviewCount` | Integer | Tong so danh gia cua cua hang | `500` |
| `distance` | Double | Khoang cach tu vi tri nguoi dung den cua hang (km) | `2.1` |
| `imageUrl` | String | URL hinh anh mon an | `https://images.unsplash.com/...` |

### Response (Khong tim thay - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Khong tim thay mon an hoac quan an nao phu hop.",
  "data": []
}
```

### Response (That bai - 400 - Tu khoa rong)

```json
{
  "success": false,
  "code": 400,
  "message": "Tu khoa tim kiem khong duoc de trong."
}
```

### Response (That bai - 400 - Toa do khong hop le)

```json
{
  "success": false,
  "code": 400,
  "message": "Toa do nguoi dung (userLat, userLng) khong hop le."
}
```

---

## Quy Trinh Xu Ly Phia Server

```
1. Lay userId + query tu tham so
       |
       v
2. Luu lich su tim kiem (neu co userId)
   -> Collection: users/{userId}/search_history
   -> Document: { keyword, createdAt }
       |
       v
3. Lay tat ca cua hang tu Firestore (stores)
       |
       v
4. Loc cua hang trong pham vi 10km (Haversine)
   -> Tinh khoang cach tu (userLat, userLng) den (storeLat, storeLng)
   -> Chi giu lai cua hang co khoang cach <= 10km
       |
       v
5. Lay tat ca san pham tu Firestore (products)
       |
       v
6. Loc san pham:
   - Chi giu lai san pham thuoc cua hang trong pham vi
   - Tim kiem theo tu khoa (khong phan biet dau tieng Viet):
        - Ten mon an chua tu khoa?
        - Ten cua hang chua tu khoa?
   -> Su dung xu ly chuoi: lowercase + bo dau tieng Viet
       |
       v
7. Map ket qua (ket hop thong tin san pham + cua hang)
       |
       v
8. Sap xep ket qua (neu co sortBy)
   -> priceAsc: gia tang dan
   -> priceDesc: gia giam dan
   -> ratingDesc: danh gia giam dan
       |
       v
9. Tra ve danh sach ket qua
```

---

## Xu Ly Chuoi Tim Kiem

He thong chuan hoa tu khoa tim kiem:

1. Chuyen thanh chu thuong (lowercase)
2. Loai bo dau tieng Viet (Normalizer NFD + regex)

| Tu khoa nhap | Tu khoa chuan hoa |
|---|---|
| `Com Tam` | `com tam` |
| `Trà Sữa` | `tra sua` |
| `Bún Bò` | `bun bo` |
| `Cơm Tấm` | `com tam` |

---

## Gioi Han Khoang Cach

- Ban kinh trai dat: 6371 km
- Gioi han khoang cach: 10 km
- Chi tra ve san pham tu cua hang trong pham vi 10km

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
