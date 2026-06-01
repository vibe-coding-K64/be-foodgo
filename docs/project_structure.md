# Cấu trúc thư mục dự án - Be FoodGo

Dự án backend Spring Boot kết nối Firebase Firestore làm cơ sở dữ liệu.

---

## Cấu trúc tổng thể

```
be-foodgo/
│
├── .mvn/                              # Maven wrapper
├── .vscode/                           # Cấu hình VS Code
├── docs/                              # Tài liệu dự án
│   ├── firebase_collections.md        # Cấu trúc Firestore
│   └── project_structure.md           # Tài liệu này
├── src/                               # Source code
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/be_foodgo/
│   │   │       ├── BeFoodgoApplication.java   # Entry point
│   │   │       ├── config/                   # Cấu hình
│   │   │       ├── constant/                 # Enum & hằng số
│   │   │       ├── controller/               # REST API
│   │   │       ├── dto/                     # Data Transfer Object
│   │   │       ├── exception/               # Xử lý lỗi tập trung
│   │   │       ├── model/                   # Firestore document
│   │   │       ├── repository/              # Giao tiếp Firestore
│   │   │       ├── seeder/                  # Khởi tạo dữ liệu mẫu
│   │   │       └── service/                 # Logic nghiệp vụ
│   │   └── resources/
│   │       ├── application.properties        # Cấu hình Spring
│   │       ├── firebase-service-account.json # Firebase credentials
│   │       ├── static/                      # Static files
│   │       └── templates/                   # Template files
│   └── test/                               # Unit tests
│       └── java/com/example/be_foodgo/
│
├── pom.xml                             # Maven dependencies
├── mvnw / mvnw.cmd                     # Maven wrapper scripts
├── .gitignore                          # Git ignore
└── README.md                           # Hướng dẫn sử dụng
```

---

## Kiến trúc phân lớp (Layered Architecture)

```
Controller
    │ gọi
    ▼
Service
    │ gọi
    ▼
Repository
    │ gọi
    ▼
Firebase Firestore
```

### Chi tiết từng lớp

| Package         | Chức năng                                                                                              |
| --------------- | ------------------------------------------------------------------------------------------------------ |
| `config`        | Cấu hình hệ thống: Firebase SDK, Security, Swagger, CORS...                                            |
| `constant`      | Enum (trạng thái, role...) và hằng số dùng chung toàn hệ thống                                          |
| `controller`    | REST API endpoints. Nhận request từ client, trả response. Không chứa logic nghiệp vụ.                    |
| `dto`           | Data Transfer Object. Đóng gói dữ liệu gửi/nhận qua API (request/response).                              |
| `exception`     | Global Exception Handler. Xử lý lỗi tập trung, trả về message thống nhất cho client.                    |
| `model`         | Entity/document mapping. Các class đóng như Firestore document (annotation @DocumentReference...).          |
| `repository`    | Truy vấn Firestore. Dùng Firebase Admin SDK (Firestore, CollectionReference...) để đọc/ghi dữ liệu.     |
| `seeder`        | Khởi tạo dữ liệu mẫu. Chạy 1 lần khi ứng dụng lên, seed dữ liệu test vào Firestore nếu chưa có.           |
| `service`       | Logic nghiệp vụ. Xử lý các tác vụ nghiệp vụ phức tạp, gọi repository để truy vấn dữ liệu.                |

### Mối quan hệ giữa các lớp

```
Client (HTTP Request)
       │
       ▼
┌─────────────────┐
│   Controller     │  Nhận request, gọi service tương ứng
└────────┬────────┘
         │ call
         ▼
┌─────────────────┐
│    Service      │  Logic nghiệp vụ, gọi repository
└────────┬────────┘
         │ call
         ▼
┌─────────────────┐
│  Repository     │  Truy vấn Firestore
└────────┬────────┘
         │ CRUD
         ▼
┌─────────────────┐
│    Model        │  Object mapping Firestore document
└────────┬────────┘
         │
         ▼
  Firebase Firestore
```

---

## Quy tắc đặt tên

| Loại              | Quy tắc                | Ví dụ                          |
| ----------------- | ---------------------- | ------------------------------ |
| Class Java        | PascalCase             | `UserController.java`          |
| Interface         | PascalCase + suffix    | `UserService.java`              |
| Method            | camelCase              | `getUserById()`                 |
| Variable          | camelCase              | `userId`, `isActive`            |
| Package           | lowercase              | `controller`, `dto`            |
| Enum              | PascalCase             | `OrderStatus.java`              |
| Enum constant     | SCREAMING_SNAKE_CASE   | `PENDING`, `COMPLETED`          |
| Firestore field   | camelCase              | `createdAt`, `userId`           |

---

## Quy tắc commit Git

| Prefix | Môi trường sử dụng                                    |
| ------ | ----------------------------------------------------- |
| `feat` | Thêm chức năng mới                                     |
| `fix`  | Sửa lỗi                                               |
| `docs` | Cập nhật tài liệu                                     |
| `refactor` | Tái cấu trúc code, không thay đổi chức năng          |
| `chore` | Cập nhật phụ thuộc, build script                      |

Áp dụng format: `<type>: <short description>`

Ví dụ:
- `feat: implement JWT authentication`
- `fix: resolve token validation bug`

---

## GITHUB (BẮT BUỘC)

### Repository

- Mỗi nhóm tạo **01 repository** trên GitHub
- Repository phải:
  - **Public** (hoặc Private nhưng add giảng viên)
  - Có file `README.md` mô tả:
    - Tên đề tài
    - Thành viên nhóm
    - Mô tả chức năng hệ thống
    - Hướng dẫn chạy project
    - Link Swagger UI

### Quy định chia nhánh

#### 1. `main`

- Chứa code ổn định, production-ready
- Chỉ merge từ `develop`
- **Không commit trực tiếp**

#### 2. `dev`

- Nhánh phát triển chính
- Merge từ các `feature/*` hoặc `defect/*`
- Dùng để test tích hợp trước khi lên `main`

#### 3. `feature/xxx`

- Phát triển tính năng mới
- Ví dụ: `feature/authentication`
- Sau khi hoàn thành, tạo **Pull Request** vào `develop`

#### 4. `defect/xxx`

- Sửa lỗi
- Ví dụ: `defect/login-bug`

### Quy trình làm việc chuẩn

1. Tạo branch từ `develop`
2. Code + commit
3. Push lên GitHub
4. Tạo **Pull Request (PR)**
5. Thành viên khác review
6. Merge vào `develop`
7. Khi hoàn chỉnh, merge `develop` → `main`

> **Lưu ý:** Không được commit trực tiếp vào `main`

### Quy chuẩn commit message

Áp dụng format: `<type>: <short description>`

Ví dụ:
- `feat: implement JWT authentication`
- `fix: resolve token validation bug`

---

## Chú thích

- File `.gitkeep` trong mỗi package đảm bảo Git tracking cả khi package chưa có file nào.
- File `firebase-service-account.json` chứa credentials Firebase, **không** được push lên Git (đã có trong `.gitignore`).
- Khi tạo class trong `model`, sử dụng `@DocumentReference` hoặc tương tự để map với Firestore document.
