package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.CartRequest;
import com.example.be_foodgo.dto.CartResponse;
import com.example.be_foodgo.dto.CartResponse.CartItemResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.model.CartItem;
import com.example.be_foodgo.repository.CartRepository;
import com.example.be_foodgo.repository.CartRepository.FirestoreDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartItem themMonVaoGio(CartRequest request) {
        log.info("Bat dau xu ly them mon vao gio - Nguoi dung: {}, San pham: {}, So luong: {}",
                request.getUserId(), request.getFoodId(), request.getQuantity());

        FirestoreDocument sanPhamDoc;
        try {
            sanPhamDoc = cartRepository.layThongTinSanPham(request.getFoodId());
        } catch (Exception e) {
            log.error("Loi khi truy van san pham [{}]: {}", request.getFoodId(), e.getMessage());
            throw BusinessException.loiHeThong("Khong the truy van thong tin san pham.");
        }

        if (sanPhamDoc == null) {
            log.warn("San pham [{}] khong ton tai trong he thong", request.getFoodId());
            throw BusinessException.sanPhamKhongTimThay(request.getFoodId());
        }

        Boolean isOutOfStock = toBoolean(sanPhamDoc.get("isOutOfStock"));
        if (Boolean.TRUE.equals(isOutOfStock)) {
            log.warn("San pham [{}] dang het hang", request.getFoodId());
            throw BusinessException.monAnHetHang(request.getFoodId());
        }

        Double basePrice = toDouble(sanPhamDoc.get("basePrice"));
        Double sizePrice = tinhGiaSize(request.getFoodId(), request.getSize(), sanPhamDoc);
        Double toppingPrice = tinhTongGiaTopping(request.getToppings());

        Double donGia = basePrice + sizePrice + toppingPrice;

        List<CartItem.ToppingItem> toppingItems = new ArrayList<>();
        if (request.getToppings() != null) {
            for (CartRequest.ToppingOption t : request.getToppings()) {
                toppingItems.add(CartItem.ToppingItem.builder()
                        .name(t.getName())
                        .price(t.getPrice())
                        .build());
            }
        }

        List<CartItem> gioHienTai;
        try {
            gioHienTai = cartRepository.layTatCaMonTrongGio(request.getUserId());
        } catch (Exception e) {
            log.error("Loi khi lay gio hang nguoi dung [{}]: {}", request.getUserId(), e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay thong tin gio hang.");
        }

        CartItem.ToppingItem[] toppingItemsArr = toppingItems.toArray(new CartItem.ToppingItem[0]);
        CartItem itemTrung = timItemTrung(gioHienTai, request.getFoodId(), request.getSize(), toppingItemsArr);

        if (itemTrung != null) {
            Integer soLuongMoi = itemTrung.getQuantity() + request.getQuantity();
            Double giaMoi = donGia * soLuongMoi;
            itemTrung.setQuantity(soLuongMoi);
            itemTrung.setPrice(giaMoi);
            cartRepository.capNhatSoLuongVaGia(request.getUserId(), itemTrung.getId(), soLuongMoi, giaMoi);
            log.info("Tang so luong mon [{}] tu {} len {} - Gia moi: {}",
                    request.getFoodId(), itemTrung.getQuantity() - request.getQuantity(), soLuongMoi, giaMoi);
            return itemTrung;
        }

        Double tongGia = donGia * request.getQuantity();
        CartItem item = CartItem.builder()
                .storeId(request.getStoreId())
                .foodId(request.getFoodId())
                .name((String) sanPhamDoc.get("name"))
                .price(tongGia)
                .quantity(request.getQuantity())
                .size(request.getSize())
                .sizePrice(sizePrice)
                .toppings(toppingItems.isEmpty() ? null : toppingItems)
                .note(request.getNote())
                .imageUrl((String) sanPhamDoc.get("imageUrl"))
                .build();

        String cartItemId = cartRepository.themMonVaoGio(request.getUserId(), item);
        item.setId(cartItemId);

        log.info("Da them mon [{}] vao gio hang thanh cong voi cartItemId: {}", request.getFoodId(), cartItemId);
        return item;
    }

    private CartItem timItemTrung(List<CartItem> gioHienTai, String foodId, String size, CartItem.ToppingItem[] toppingItems) {
        for (CartItem item : gioHienTai) {
            if (!item.getFoodId().equals(foodId)) continue;
            if (!java.util.Objects.equals(size, item.getSize())) continue;
            if (!item.coCungTopping(toppingItems == null || toppingItems.length == 0
                    ? null : java.util.Arrays.asList(toppingItems))) continue;
            log.info("Tim thay mon trung trong gio - cartItemId: {}, foodId: {}, size: {}, toppings: {}",
                    item.getId(), foodId, size, toppingItems);
            return item;
        }
        return null;
    }

    public CartResponse layGioHang(String userId) {
        log.info("Bắt đầu lấy giỏ hàng - Người dùng: {}", userId);

        List<CartItem> items;
        try {
            items = cartRepository.layTatCaMonTrongGio(userId);
        } catch (Exception e) {
            log.error("Lỗi khi truy vấn giỏ hàng của người dùng [{}]: {}", userId, e.getMessage());
            throw BusinessException.loiHeThong("Không thể lấy thông tin giỏ hàng.");
        }

        if (items.isEmpty()) {
            log.info("Giỏ hàng của người dùng [{}] đang trống.", userId);
            return CartResponse.builder()
                    .items(List.of())
                    .storeId(null)
                    .storeName(null)
                    .storeImageUrl(null)
                    .build();
        }

        String storeId = items.get(0).getStoreId();
        String resolvedStoreName = null;
        String resolvedStoreImageUrl = null;

        try {
            FirestoreDocument storeDoc = cartRepository.layThongTinCuaHang(storeId);
            if (storeDoc != null) {
                resolvedStoreName = (String) storeDoc.get("name");
                resolvedStoreImageUrl = (String) storeDoc.get("avtUrl");
            }
        } catch (Exception e) {
            log.warn("Không thể lấy thông tin cửa hàng [{}]: {}", storeId, e.getMessage());
        }

        final String storeName = resolvedStoreName;
        final String storeImageUrl = resolvedStoreImageUrl;

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .userId(userId)
                        .storeId(item.getStoreId())
                        .storeName(storeName)
                        .storeImageUrl(storeImageUrl)
                        .foodId(item.getFoodId())
                        .name(item.getName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .size(item.getSize())
                        .sizePrice(item.getSizePrice())
                        .toppings(item.getToppings())
                        .note(item.getNote())
                        .imageUrl(item.getImageUrl())
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .toList();

        log.info("Lấy giỏ hàng của người dùng [{}] thành công - {} món.", userId, itemResponses.size());
        return CartResponse.builder()
                .items(itemResponses)
                .storeId(storeId)
                .storeName(storeName)
                .storeImageUrl(storeImageUrl)
                .build();
    }

    public void capNhatSoLuongMon(String userId, String itemId, Integer quantity) {
        log.info("Bắt đầu xử lý cập nhật số lượng - Người dùng: {}, Món: {}, Số lượng mới: {}",
                userId, itemId, quantity);

        if (quantity == null || quantity <= 0) {
            log.warn("Số lượng không hợp lệ: {}", quantity);
            throw new IllegalArgumentException("Số lượng không hợp lệ.");
        }

        try {
            CartItem item = cartRepository.layMotMonTrongGio(userId, itemId);
            if (item == null) {
                log.warn("Món với itemId [{}] không tồn tại trong giỏ hàng của người dùng {}", itemId, userId);
                throw BusinessException.cartItemKhongTimThay(itemId);
            }

            cartRepository.capNhatSoLuongMon(userId, itemId, quantity);
            log.info("Cập nhật số lượng món [{}] thành {} thành công.", itemId, quantity);

        } catch (BusinessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật số lượng món [{}]: {}", itemId, e.getMessage());
            throw BusinessException.loiHeThong("Không thể cập nhật số lượng món.");
        }
    }

    public void xoaMotMon(String userId, String itemId) {
        log.info("Bắt đầu xóa món [{}] khỏi giỏ hàng - Người dùng: {}", itemId, userId);

        try {
            cartRepository.xoaMotMonTrongGio(userId, itemId);
            log.info("Đã xóa món [{}] khỏi giỏ hàng của người dùng {}", itemId, userId);

        } catch (Exception e) {
            log.error("Lỗi khi xóa món [{}]: {}", itemId, e.getMessage());
            throw BusinessException.loiHeThong("Không thể xóa món khỏi giỏ hàng.");
        }
    }

    public void xoaToanBoGioHang(String userId) {
        log.info("Bắt đầu xóa toàn bộ giỏ hàng - Người dùng: {}", userId);

        try {
            cartRepository.xoaTatCaMonTrongGio(userId);
            log.info("Đã xóa toàn bộ giỏ hàng của người dùng {}", userId);

        } catch (Exception e) {
            log.error("Lỗi khi xóa toàn bộ giỏ hàng: {}", e.getMessage());
            throw BusinessException.loiHeThong("Không thể xóa giỏ hàng.");
        }
    }

    private Double tinhGiaSize(String foodId, String size, FirestoreDocument sanPhamDoc) {
        if (size == null || size.isBlank()) {
            log.info("Sản phẩm [{}] không chọn size, giá size = 0", foodId);
            return 0.0;
        }

        Object optionGroupsObj = sanPhamDoc.get("optionGroups");
        if (optionGroupsObj == null) {
            return 0.0;
        }

        List<?> optionGroups;
        try {
            optionGroups = (List<?>) optionGroupsObj;
        } catch (Exception e) {
            log.warn("Không thể parse optionGroups của sản phẩm [{}]: {}", foodId, e.getMessage());
            return 0.0;
        }

        for (Object group : optionGroups) {
            if (!(group instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> groupMap = (Map<String, Object>) group;
            String groupName = (String) groupMap.get("name");
            if (groupName == null) continue;

            if (groupName.equalsIgnoreCase("Kich thuoc") || groupName.equalsIgnoreCase("Size")) {
                Object optionsObj = groupMap.get("options");
                if (optionsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> options = (List<Map<String, Object>>) optionsObj;
                    for (Map<String, Object> option : options) {
                        String optName = (String) option.get("name");
                        if (optName != null && optName.equalsIgnoreCase(size)) {
                            Double gia = toDouble(option.get("price"));
                            log.info("Tìm thấy size [{}] với giá: {}", size, gia);
                            return gia;
                        }
                    }
                }
            }
        }

        log.info("Size [{}] không tồn tại trong cấu hình sản phẩm [{}], giá = 0", size, foodId);
        return 0.0;
    }

    private Double tinhTongGiaTopping(List<CartRequest.ToppingOption> toppings) {
        if (toppings == null || toppings.isEmpty()) {
            return 0.0;
        }
        Double tong = 0.0;
        for (CartRequest.ToppingOption t : toppings) {
            Double gia = t.getPrice() != null ? t.getPrice() : 0.0;
            tong += gia;
        }
        log.info("Tổng giá {} topping: {}", toppings.size(), tong);
        return tong;
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return false;
    }
}
