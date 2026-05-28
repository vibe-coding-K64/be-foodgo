# API - Danh Gia (Review)

## Thong Tin Chung

| Property | Value |
|---|---|
| **Base URL** | `/api/reviews` |
| **Authentication** | Khong can Bearer Token |
| **Content-Type** | `application/json` |

---

## 1. Tao Danh Gia

**Endpoint:** `POST /api/reviews`

**Mo ta:** Cho phep khach hang tao mot danh gia cho don hang da nhan. Chi cho phep danh gia khi don hang o trang thai [Hoan thanh] (status = 3). Mot don hang chi duoc phep danh gia mot lan.

### Request Body

```json
{
  "orderId": "order_002",
  "storeId": "store_002",
  "userId": "user_001",
  "userName": "Khoi",
  "userAvatarUrl": "https://example.com/avatar/user001.jpg",
  "starRating": 5,
  "comment": "Do an rat ngon, giao hang nhanh, dong goi ky luong.",
  "imageUrls": [
    "https://example.com/review/rev001_1.jpg",
    "https://example.com/review/rev001_2.jpg"
  ]
}
```

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `orderId` | String | **Co** | ID don hang da nhan | `order_002` |
| `storeId` | String | **Co** | ID cua hang | `store_002` |
| `userId` | String | **Co** | ID nguoi dung khach hang | `user_001` |
| `userName` | String | **Co** | Ho ten nguoi danh gia | `Khoi` |
| `userAvatarUrl` | String | Khong | URL avatar nguoi danh gia | `https://example.com/avatar/user001.jpg` |
| `starRating` | Integer | **Co** | So sao danh gia (1 - 5) | `5` |
| `comment` | String | **Co** | Noi dung binh luan | `Do an rat ngon, giao hang nhanh.` |
| `imageUrls` | List<String> | Khong | Danh sach URL hinh anh danh gia | `["https://...", "https://..."]` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Tao danh gia thanh cong.",
  "data": {
    "id": "rev_new_001",
    "orderId": "order_002",
    "storeId": "store_002",
    "userId": "user_001",
    "userName": "Khoi",
    "userAvatarUrl": "https://example.com/avatar/user001.jpg",
    "starRating": 5,
    "comment": "Do an rat ngon, giao hang nhanh, dong goi ky luong.",
    "imageUrls": [
      "https://example.com/review/rev001_1.jpg",
      "https://example.com/review/rev001_2.jpg"
    ],
    "createdAt": "2026-05-26T14:00:00Z",
    "updatedAt": "2026-05-26T14:00:00Z"
  }
}
```

### Side Effect - Cap Nhat Diem So Cua Hang

Sau khi tao danh gia, he thong tu dong cap nhat diem so (rating) va so luong danh gia (reviewCount) cua cua hang:

```
newRating = (oldRating * oldReviewCount + newStarRating) / (oldReviewCount + 1)
newReviewCount = oldReviewCount + 1
```

| Vi du | Gia tri |
|---|---|
| Rating cu | `4.8` |
| ReviewCount cu | `500` |
| Sao danh gia moi | `5` |
| Rating moi | `(4.8 * 500 + 5) / 501 = 4.802` (lam tron 1 chu so) |
| ReviewCount moi | `501` |

### Response (That bai - 400 - Don hang da danh gia roi)

```json
{
  "success": false,
  "code": 400,
  "message": "Don hang da duoc danh gia truoc do."
}
```

### Response (That bai - 400 - Trang thai khong cho phep danh gia)

```json
{
  "success": false,
  "code": 400,
  "message": "Don hang chua duoc giao. Chi co the danh gia khi don hang hoan thanh (trang thai = 3)."
}
```

### Response (That bai - 403 - Khong phai chu so huu)

```json
{
  "success": false,
  "code": 403,
  "message": "Ban khong co quyen danh gia don hang nay."
}
```

### Response (That bai - 404)

```json
{
  "success": false,
  "code": 404,
  "message": "Don hang khong ton tai."
}
```

---

## 2. Lay Danh Sach Danh Gia Theo Cua Hang

**Endpoint:** `GET /api/reviews`

**Mo ta:** Lay tat ca danh gia cua mot cua hang theo storeId.

### Query Parameters

| Thuoc tinh | Kieu du lieu | Bat buoc | Mo ta | Vi du |
|---|---|---|---|---|
| `storeId` | String | **Co** | ID cua hang | `store_001` |

### Response (Thanh cong - 200)

```json
{
  "success": true,
  "code": 200,
  "message": "Lay danh sach danh gia thanh cong.",
  "data": [
    {
      "id": "rev_001",
      "orderId": "order_001",
      "storeId": "store_001",
      "userId": "user_001",
      "userName": "Khoi",
      "userAvatarUrl": "https://example.com/avatar/user001.jpg",
      "starRating": 5,
      "comment": "Do an rat ngon, giao hang nhanh, dong goi ky luong.",
      "imageUrls": [
        "https://example.com/review/rev001_1.jpg",
        "https://example.com/review/rev001_2.jpg"
      ],
      "createdAt": "2026-05-25T14:00:00Z",
      "updatedAt": "2026-05-25T14:00:00Z"
    },
    {
      "id": "rev_002",
      "orderId": "order_005",
      "storeId": "store_001",
      "userId": "user_002",
      "userName": "Quan Tri Vien",
      "userAvatarUrl": "https://example.com/avatar/admin.jpg",
      "starRating": 4,
      "comment": "Mon an ngon, nhung giao hang tre hon 15 phut.",
      "imageUrls": [],
      "createdAt": "2026-05-24T14:00:00Z",
      "updatedAt": "2026-05-24T14:00:00Z"
    }
  ]
}
```

---

## Bang Dieu Kien Tao Danh Gia

| Dieu kien | Trang thai don hang | Ket qua |
|---|---|---|
| Don hang ton tai | Bat ky | Tiep tuc |
| Nguoi dung la chu so huu don hang | Bat ky | Tiep tuc |
| Don hang o trang thai Hoan thanh (3) | `status == 3` | Tiep tuc |
| Don hang chua duoc danh gia | Chua co review | Tiep tuc |
| Don hang o trang thai khac | `status != 3` | **Loi 400** |
| Nguoi dung khong phai chu so huu | Nguoi dung khac | **Loi 403** |
| Don hang da danh gia roi | Co review roi | **Loi 400** |

---

## Bang Sao Danh Gia

| Gia tri | Hinh anh | Mo ta |
|---|---|---|
| `1` | Mot sao | Rat kem |
| `2` | Hai sao | Kem |
| `3` | Ba sao | Trung binh |
| `4` | Bon sao | Tot |
| `5` | Nam sao | Rat tot |

---

## Bieu Do Quy Trinh Danh Gia

```
Nguoi dung xem chi tiet don hang (OrderDetailView)
       |
       v
  Kiem tra trang thai don hang:
       |
       +-- status == 3 (Hoan thanh)
       |    |
       |    v
       |    Kiem tra da danh gia chua:
       |    |
       |    +-- Chua danh gia
       |    |    Hien thi nut "Danh gia mon an"
       |    |
       |    +-- Da danh gia
       |         Hien thi thong tin danh gia da co
       |
       +-- status != 3
            |
            v
            Khong hien thi phan danh gia

========================================

Nguoi dung bam "Danh gia mon an"
       |
       v
  Mo man hinh/nhap thong tin danh gia:
  - Chon so sao (1-5)
  - Nhap binh luan
  - (Tuy chon) Them hinh anh
       |
       v
  Bam "Gui danh gia"
       |
       v
  POST /api/reviews
       |
       v
  +----+----+
  |         |
  OK       400 (Da danh gia roi / Trang thai khong hop le)
  |         |
  |         +-- 403 (Khong phai chu so huu)
  |         |
  |         +-- 404 (Don hang khong ton tai)
  |
  v
  He thong cap nhat diem so cua hang (tu dong)
  Cap nhat giao dien
```

---

## Lich Su Thay Doi

| Phien ban | Ngay | Mo ta |
|---|---|---|
| 1.0 | 2026-05-26 | Phien ban dau tien |
