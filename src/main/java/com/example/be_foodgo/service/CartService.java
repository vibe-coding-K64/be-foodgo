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
        final List<CartItem.ToppingItem> toppingItemsFinal = toppingItems;

        List<CartItem> gioHienTai;
        try {
            gioHienTai = cartRepository.layTatCaMonTrongGio(request.getUserId());
        } catch (Exception e) {
            log.error("Loi khi lay gio hang nguoi dung [{}]: {}", request.getUserId(), e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay thong tin gio hang.");
        }

        CartItem.ToppingItem[] toppingItemsArr = toppingItems.toArray(new CartItem.ToppingItem[0]);
        CartItem itemTrung = timItemTrung(gioHienTai, request.getFoodId(), request.getSize(), toppingItemsArr);

        log.info("=== [themMonVaoGio] Ket qua timItemTrung: {} ===",
                itemTrung != null ? "TIM THAY (se GOP)" : "KHONG TIM THAY (se TAO DONG MOI)");

        if (itemTrung != null) {
            List<CartItem.ToppingItem> mergedToppings = toppingItemsFinal.isEmpty() ? null : toppingItemsFinal;
            Integer soLuongMoi = itemTrung.getQuantity() + request.getQuantity();
            Double giaMoi = donGia * soLuongMoi;
            itemTrung.setQuantity(soLuongMoi);
            itemTrung.setPrice(giaMoi);
            itemTrung.setSizePrice(sizePrice);
            itemTrung.setToppings(mergedToppings);
            cartRepository.capNhatCartItem(request.getUserId(), itemTrung);
            log.info("Tang so luong mon [{}] tu {} len {} - Gia moi: {}",
                    request.getFoodId(), itemTrung.getQuantity() - request.getQuantity(), soLuongMoi, giaMoi);
            return itemTrung;
        }

        log.info("=== [themMonVaoGio] Tao dong moi trong gio hang ===");
        Double tongGia = donGia * request.getQuantity();
        CartItem item = CartItem.builder()
                .storeId(request.getStoreId())
                .foodId(request.getFoodId())
                .name((String) sanPhamDoc.get("name"))
                .price(tongGia)
                .quantity(request.getQuantity())
                .size(request.getSize())
                .sizePrice(sizePrice)
                .toppings(toppingItemsFinal.isEmpty() ? null : toppingItemsFinal)
                .note(request.getNote())
                .imageUrl((String) sanPhamDoc.get("imageUrl"))
                .build();

        String cartItemId = cartRepository.themMonVaoGio(request.getUserId(), item);
        item.setId(cartItemId);

        log.info("Da them mon [{}] vao gio hang thanh cong voi cartItemId: {}", request.getFoodId(), cartItemId);
        return item;
    }

    private CartItem timItemTrung(List<CartItem> gioHienTai, String foodId, String size, CartItem.ToppingItem[] toppingItems) {
        log.info("=== [timItemTrung] Bat dau tim kiem trung lap ===");
        log.info("[timItemTrung] Can tim: foodId={}, size={}, toppings={}",
                foodId, size,
                toppingItems != null
                        ? java.util.Arrays.stream(toppingItems).map(t -> t.getName() + "(" + t.getPrice() + ")").toList()
                        : null);

        for (int i = 0; i < gioHienTai.size(); i++) {
            CartItem item = gioHienTai.get(i);
            log.info("[timItemTrung] Kiem tra item[{}]: foodId={}, size={}, toppings={}",
                    i, item.getFoodId(), item.getSize(),
                    item.getToppings() != null
                            ? item.getToppings().stream().map(t -> t.getName() + "(" + t.getPrice() + ")").toList()
                            : null);

            if (!item.getFoodId().equals(foodId)) {
                log.info("[timItemTrung]   -> foodId khac '{}' != '{}' -> CONTINUE", item.getFoodId(), foodId);
                continue;
            }
            if (!java.util.Objects.equals(size, item.getSize())) {
                log.info("[timItemTrung]   -> size khac '{}' != '{}' -> CONTINUE",
                        item.getSize(), size);
                continue;
            }

            List<CartItem.ToppingItem> toppingList = toppingItems == null || toppingItems.length == 0
                    ? null : java.util.Arrays.asList(toppingItems);
            boolean cungTopping = item.coCungTopping(toppingList);
            log.info("[timItemTrung]   -> foodId OK, size OK, coCungTopping={} -> {}",
                    cungTopping, cungTopping ? "TIM THAY TRUNG!" : "toppings khac -> CONTINUE");

            if (cungTopping) {
                log.info("[timItemTrung] === TIM THAY ITEM TRUNG: id={} ===", item.getId());
                return item;
            }
        }

        log.info("[timItemTrung] === KHONG TIM THAY item trung -> tra ve null ===");
        return null;
    }

    public CartResponse layGioHang(String userId) {
        log.info("Bat dau lay gio hang - Nguoi dung: {}", userId);

        List<CartItem> items;
        try {
            items = cartRepository.layTatCaMonTrongGio(userId);
        } catch (Exception e) {
            log.error("Loi khi truy van gio hang cua nguoi dung [{}]: {}", userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay thong tin gio hang.");
        }

        if (items.isEmpty()) {
            log.info("Gio hang cua nguoi dung [{}] dang trong.", userId);
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
            log.warn("Khong the lay thong tin cua hang [{}]: {}", storeId, e.getMessage());
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

        log.info("Lay gio hang cua nguoi dung [{}] thanh cong - {} mon.", userId, itemResponses.size());
        return CartResponse.builder()
                .items(itemResponses)
                .storeId(storeId)
                .storeName(storeName)
                .storeImageUrl(storeImageUrl)
                .build();
    }

    public void capNhatSoLuongMon(String userId, String itemId, Integer quantity) {
        log.info("Bat dau xu ly cap nhat so luong - Nguoi dung: {}, Mon: {}, So luong moi: {}",
                userId, itemId, quantity);

        if (quantity == null || quantity <= 0) {
            log.warn("So luong khong hop le: {}", quantity);
            throw new IllegalArgumentException("So luong khong hop le.");
        }

        try {
            CartItem item = cartRepository.layMotMonTrongGio(userId, itemId);
            if (item == null) {
                log.warn("Mon voi itemId [{}] khong ton tai trong gio hang cua nguoi dung {}", itemId, userId);
                throw BusinessException.cartItemKhongTimThay(itemId);
            }

            cartRepository.capNhatSoLuongMon(userId, itemId, quantity);
            log.info("Cap nhat so luong mon [{}] thanh cong.", itemId, quantity);

        } catch (BusinessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Loi khi cap nhat so luong mon [{}]: {}", itemId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the cap nhat so luong mon.");
        }
    }

    public void xoaMotMon(String userId, String itemId) {
        log.info("Bat dau xoa mon [{}] khoi gio hang - Nguoi dung: {}", itemId, userId);

        try {
            cartRepository.xoaMotMonTrongGio(userId, itemId);
            log.info("Da xoa mon [{}] khoi gio hang cua nguoi dung {}", itemId, userId);

        } catch (Exception e) {
            log.error("Loi khi xoa mon [{}]: {}", itemId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the xoa mon khoi gio hang.");
        }
    }

    public void xoaToanBoGioHang(String userId) {
        log.info("Bat dau xoa toan bo gio hang - Nguoi dung: {}", userId);

        try {
            cartRepository.xoaTatCaMonTrongGio(userId);
            log.info("Da xoa toan bo gio hang cua nguoi dung {}", userId);

        } catch (Exception e) {
            log.error("Loi khi xoa toan bo gio hang: {}", e.getMessage());
            throw BusinessException.loiHeThong("Khong the xoa gio hang.");
        }
    }

    private Double tinhGiaSize(String foodId, String size, FirestoreDocument sanPhamDoc) {
        if (size == null || size.isBlank()) {
            log.info("San pham [{}] khong chon size, gia size = 0", foodId);
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
            log.warn("Khong the parse optionGroups cua san pham [{}]: {}", foodId, e.getMessage());
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
                            log.info("Tim thay size [{}] voi gia: {}", size, gia);
                            return gia;
                        }
                    }
                }
            }
        }

        log.info("Size [{}] khong ton tai trong cau hinh san pham [{}], gia = 0", size, foodId);
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
        log.info("Tong gia {} topping: {}", toppings.size(), tong);
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
