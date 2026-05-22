# Cau truc thu muc du an - Be FoodGo

Du an backend Spring Boot ket noi Firebase Firestore lam co so du lieu.

---

## Cau truc tong the

```
be-foodgo/
│
├── .mvn/                              # Maven wrapper
├── .vscode/                           # Cau hinh VS Code
├── docs/                              # Tai lieu du an
│   ├── firebase_collections.md        # Cau truc Firestore
│   └── project_structure.md           # Tai lieu nay
├── src/                               # Source code
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/be_foodgo/
│   │   │       ├── BeFoodgoApplication.java   # Entry point
│   │   │       ├── config/                   # Cau hinh
│   │   │       ├── constant/                 # Enum & hang so
│   │   │       ├── controller/               # REST API
│   │   │       ├── dto/                     # Data Transfer Object
│   │   │       ├── exception/               # Xy ly loi tap trung
│   │   │       ├── model/                   # Firestore document
│   │   │       ├── repository/              # Giao tiep Firestore
│   │   │       ├── seeder/                  # Khoi tao du lieu mau
│   │   │       └── service/                 # Logic nghiep vu
│   │   └── resources/
│   │       ├── application.properties        # Cau hinh Spring
│   │       ├── firebase-service-account.json # Firebase credentials
│   │       ├── static/                      # Static files
│   │       └── templates/                   # Template files
│   └── test/                               # Unit tests
│       └── java/com/example/be_foodgo/
│
├── pom.xml                             # Maven dependencies
├── mvnw / mvnw.cmd                     # Maven wrapper scripts
├── .gitignore                          # Git ignore
└── README.md                           # Huong dan su dung
```

---

## Kien truc phan lop (Layered Architecture)

```
Controller
    │ goi
    ▼
Service
    │ goi
    ▼
Repository
    │ goi
    ▼
Firebase Firestore
```

### Chi tiet tung lop

| Package         | Chuc nang                                                                                              |
| --------------- | ------------------------------------------------------------------------------------------------------ |
| `config`        | Cau hinh he thong: Firebase SDK, Security, Swagger, CORS...                                            |
| `constant`      | Enum (trang thai, role...) va hang so dung chung toan he thong                                          |
| `controller`    | REST API endpoints. Nhan request tu client, tra response. Khong chua logic nghip vu.                    |
| `dto`           | Data Transfer Object. Dong goi du lieu gui/nhan qua API (request/response).                              |
| `exception`     | Global Exception Handler. Xy ly loi tap trung, tra ve message thong nhat cho client.                    |
| `model`         | Entity/document mapping. Cac class dong nhu Firestore document (annotation @DocumentReference...).          |
| `repository`    | Truy van Firestore. Dung Firebase Admin SDK (Firestore, CollectionReference...) de doc/ghi du lieu.     |
| `seeder`        | Khoi tao du lieu mau. Chay 1 lan khi ung dung len, seed du lieu test vao Firestore neu chua co.           |
| `service`       | Logic nghip vu. Xy ly cac tac vu nghiep vu phuc tap, goi repository de truy van du lieu.                |

### Moi quan he giua cac lop

```
Client (HTTP Request)
       │
       ▼
┌─────────────────┐
│   Controller     │  Nhan request, goi service tuong ung
└────────┬────────┘
         │ call
         ▼
┌─────────────────┐
│    Service      │  Logic nghip vu, goi repository
└────────┬────────┘
         │ call
         ▼
┌─────────────────┐
│  Repository     │  Truy van Firestore
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

## Quy tac dat ten

| Loai              | Quy tac                | Vi du                          |
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

## Quy tac commit Git

| Prefix | Moi truong su dung                                    |
| ------ | ----------------------------------------------------- |
| `feat` | Them chuc nang moi                                     |
| `fix`  | Sua loi                                               |
| `docs` | Cap nhat tai lieu                                     |
| `refactor` | Tái cau truc code, khong thay doi chuc nang          |
| `chore` | Cap nhat phu thuoc, build script                      |

---

## Chu thich

- File `.gitkeep` trong moi package dam bao Git tracking ca khi package chua co file nao.
- File `firebase-service-account.json` chua credentials Firebase, **khong** duoc push len Git (da co trong `.gitignore`).
- Khi tao class trong `model`, su dung `@DocumentReference` hoac tuong tu de map voi Firestore document.
