# Màn Hình - Danh Mục Chính (Main Navigation)

## 1. Màn Hình Chính (MainView)

**File:** `lib/features/main/views/main_view.dart`

**Class:** `MainView`

**Mục đích chính:** Màn hình container chính của ứng dụng, chứa 5 tab điều hướng ở bottom navigation bar.

---

**Các dữ liệu cần hiển thị (Data Displayed):**

- **BottomNavigationBar (5 tab, Fixed):**
  - Tab 1: icon home_outlined / home, nhãn "Trang chủ"
  - Tab 2: icon receipt_long_outlined / receipt_long, nhãn "Hoạt động"
  - Tab 3: icon local_offer_outlined / local_offer, nhãn "Ưu đãi"
  - Tab 4: icon notifications_outlined / notifications, nhãn "Thông báo"
  - Tab 5: icon person_outlined / person, nhãn "Tài khoản"
- **Nội dung hiển thị theo tab (IndexedStack):**
  - Tab 0: HomeView
  - Tab 1: ActivityView
  - Tab 2: RewardsView
  - Tab 3: NotificationsView
  - Tab 4: ProfileView
- **Màu sắc:** Tab được chọn: AppColors.primary (xanh lá), Tab chưa chọn: Colors.grey

---

**Các hành động của người dùng (User Actions):**

- Bấm 1 trong 5 tab ở BottomNavigationBar để chuyển đổi nội dung hiển thị (setState _currentIndex)
- Cuộn giữa các tab được giữ nguyên trạng thái (IndexedStack)
