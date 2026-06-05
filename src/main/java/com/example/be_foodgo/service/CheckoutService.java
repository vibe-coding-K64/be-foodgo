package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.CheckoutRequestV2;
import com.example.be_foodgo.dto.CheckoutResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.model.Address;
import com.example.be_foodgo.model.MyVoucher;
import com.example.be_foodgo.model.PaymentMethod;
import com.example.be_foodgo.model.Product;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.repository.AddressRepository;
import com.example.be_foodgo.repository.PaymentRepository;
import com.example.be_foodgo.repository.ProductRepository;
import com.example.be_foodgo.repository.StoreRepository;
import com.example.be_foodgo.repository.VoucherRepository;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);
    private static final double BAN_KINH_TRAI_DAT = 6371.0;
    private static final double GIOI_HAN_KHOANG_CACH_KM = 10.0;
    private static final double PHI_SHIP_CO_BAN = 15000.0;

    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final VoucherRepository voucherRepository;
    private final Firestore firestore;
    private final NotificationService notificationService;

    public CheckoutService(
            AddressRepository addressRepository,
            PaymentRepository paymentRepository,
            ProductRepository productRepository,
            StoreRepository storeRepository,
            VoucherRepository voucherRepository,
            Firestore firestore,
            NotificationService notificationService
    ) {
        this.addressRepository = addressRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.voucherRepository = voucherRepository;
        this.firestore = firestore;
        this.notificationService = notificationService;
    }

    public CheckoutResponse thucHienDatHang(CheckoutRequestV2 request, String authenticatedUserId) {
        String userId = request.getUserId();
        String addressId = request.getAddressId();
        String storeId = request.getStoreId();
        log.info("Bat dau xu ly dat hang - userId: {}, addressId: {}, storeId: {}, paymentMethod: {}, discountVoucher: {}, shopVoucher: {}, freeshpVoucher: {}",
                userId, addressId, storeId, request.getPaymentMethod(),
                request.getDiscountVoucherId(), request.getShopVoucherId(), request.getFreeshpVoucherId());

        if (authenticatedUserId == null || !authenticatedUserId.equals(userId)) {
            log.warn("UserId khong khop - request: {}, authenticated: {}", userId, authenticatedUserId);
            throw BusinessException.userIdKhongKhop(userId);
        }

        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            String existingOrderId = kiemTraIdempotencyKey(authenticatedUserId, request.getIdempotencyKey());
            if (existingOrderId != null) {
                log.info("Idempotency key [{}] da duoc su dung, tra ve orderId cu: {}",
                        request.getIdempotencyKey(), existingOrderId);
                throw BusinessException.donHangDaTonTai(existingOrderId);
            }
        }

        List<CheckoutRequestV2.CheckoutItem> requestItems = request.getItems();
        if (requestItems == null || requestItems.isEmpty()) {
            log.warn("Danh sach items cua nguoi dung [{}] dang rong.", userId);
            throw BusinessException.gioHangRong();
        }
        log.info("Tong so {} mon trong yeu cau checkout cua nguoi dung [{}].", requestItems.size(), userId);

        Address diaChi;
        try {
            diaChi = addressRepository.layMotDiaChi(userId, addressId);
        } catch (Exception e) {
            log.error("Loi khi truy van dia chi [{}] cua nguoi dung [{}]: {}", addressId, userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the truy van thong tin dia chi.");
        }

        if (diaChi == null) {
            log.warn("Khong tim thay dia chi [{}] cua nguoi dung [{}].", addressId, userId);
            throw BusinessException.diaChiKhongTimThay(addressId);
        }
        log.info("Tim thay dia chi giao hang: [{}] - {}, {}.",
                diaChi.getName(), diaChi.getAddress(), diaChi.getReceiverName());

        Store cuaHang;
        try {
            cuaHang = storeRepository.getStoreById(storeId);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Loi khi truy van cua hang [{}]: {}", storeId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the truy van thong tin cua hang.");
        }

        if (cuaHang == null) {
            log.error("Khong tim thay cua hang voi ID [{}].", storeId);
            throw BusinessException.loiHeThong("Khong the truy van thong tin cua hang.");
        }
        log.info("Tim thay cua hang: [{}] - [{}].", cuaHang.getId(), cuaHang.getName());

        kiemTraKhoangCach(cuaHang, diaChi);

        List<String> foodIds = requestItems.stream().map(CheckoutRequestV2.CheckoutItem::getFoodId).toList();
        Map<String, Boolean> trangThaiTonKho = kiemTraTonKhoSanPham(foodIds);
        for (CheckoutRequestV2.CheckoutItem item : requestItems) {
            Boolean conHang = trangThaiTonKho.get(item.getFoodId());
            if (conHang == null || conHang) {
                log.info("San pham [{}] con hang trong he thong, kiem tra thanh cong.", item.getFoodId());
            } else {
                log.warn("San pham [{}] - [{}] da het hang.", item.getFoodId(), item.getName());
                throw BusinessException.monAnHetHangTrongGio(item.getFoodId());
            }
        }

        double tongTienHang = tinhTongTienTuItems(requestItems);
        double phiShip = PHI_SHIP_CO_BAN;
        List<VoucherInfo> voucherInfos = new ArrayList<>();

        double discountAmountVal = 0.0;
        double shopDiscountAmountVal = 0.0;
        double freeshipDiscountAmountVal = 0.0;

        if (request.getDiscountVoucherId() != null && !request.getDiscountVoucherId().isBlank()) {
            VoucherInfo info = kiemTraVaXuLyVoucher(userId, request.getDiscountVoucherId(), tongTienHang);
            voucherInfos.add(info);
            discountAmountVal = tinhSoTienGiam(info, tongTienHang, phiShip);
            log.info("Ap dung discount voucher [{}] - giam: {}.", request.getDiscountVoucherId(), discountAmountVal);
        }

        if (request.getShopVoucherId() != null && !request.getShopVoucherId().isBlank()) {
            VoucherInfo info = kiemTraVaXuLyVoucher(userId, request.getShopVoucherId(), tongTienHang);
            voucherInfos.add(info);
            shopDiscountAmountVal = tinhSoTienGiam(info, tongTienHang, phiShip);
            log.info("Ap dung shop voucher [{}] - giam: {}.", request.getShopVoucherId(), shopDiscountAmountVal);
        }

        if (request.getFreeshpVoucherId() != null && !request.getFreeshpVoucherId().isBlank()) {
            VoucherInfo info = kiemTraVaXuLyVoucher(userId, request.getFreeshpVoucherId(), tongTienHang);
            voucherInfos.add(info);
            freeshipDiscountAmountVal = tinhSoTienGiam(info, tongTienHang, phiShip);
            log.info("Ap dung freeshp voucher [{}] - giam: {}.", request.getFreeshpVoucherId(), freeshipDiscountAmountVal);
        }

        double tongSoTienGiam = discountAmountVal + shopDiscountAmountVal + freeshipDiscountAmountVal;
        double tongThanhToan = tongTienHang + phiShip - tongSoTienGiam;
        if (tongThanhToan < 0) {
            tongThanhToan = 0;
        }

        log.info("Tinh toan chi phi - Tong tien hang: {}, Phi ship: {}, Giam discount: {}, Giam shop: {}, Giam freeship: {}, Tong giam: {}, Tong phai tra: {}.",
                tongTienHang, phiShip, discountAmountVal, shopDiscountAmountVal, freeshipDiscountAmountVal, tongSoTienGiam, tongThanhToan);

        CheckoutResponse.OrderItemData[] orderItems = chuanBiOrderItems(requestItems);
        String orderCode = String.format("FG-%s-%s",
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                "");  // tạm placeholder, sẽ gán sau khi có orderId
        String orderId = taoDonHangAtomic(
                userId, storeId, cuaHang.getName(), diaChi, request,
                orderItems, tongTienHang, phiShip, discountAmountVal, shopDiscountAmountVal, freeshipDiscountAmountVal, tongThanhToan,
                voucherInfos, request.getIdempotencyKey(), orderCode
        );
        orderCode = String.format("FG-%s-%s",
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                orderId.substring(orderId.length() - 3).toUpperCase());

        String paymentMethodName = request.getPaymentMethod();
        try {
            PaymentMethod pm = paymentRepository.layMotPhuongThuc(userId, request.getPaymentMethod());
            if (pm != null) {
                paymentMethodName = pm.getName();
            }
        } catch (Exception e) {
            log.warn("Khong the lay payment method [{}] tu Firestore, tra ve ID goc", request.getPaymentMethod());
        }

        log.info("Dat hang thanh cong - orderId: [{}], orderCode: [{}].", orderId, orderCode);

        String receiverName = diaChi.getReceiverName() != null ? diaChi.getReceiverName() : "Khách hàng";
        int itemCount = request.getItems().stream().mapToInt(CheckoutRequestV2.CheckoutItem::getQuantity).sum();
        com.example.be_foodgo.dto.NotificationDTO notif = com.example.be_foodgo.dto.NotificationDTO.builder()
                .type(21)
                .title("Đơn hàng mới từ " + receiverName)
                .body(orderCode + " · " + itemCount + " món · " + String.format("%,.0f", tongThanhToan) + "đ")
                .orderId(orderId)
                .referenceId(orderId)
                .build();
        notificationService.notifyMerchantByStoreId(storeId, notif);

        return CheckoutResponse.builder()
                .orderId(orderId)
                .orderCode(orderCode)
                .storeId(storeId)
                .storeName(cuaHang.getName())
                .userId(userId)
                .items(java.util.Arrays.asList(orderItems))
                .totalAmount(tongTienHang)
                .deliveryFee(phiShip)
                .discountAmount(discountAmountVal)
                .shopDiscountAmount(shopDiscountAmountVal)
                .freeshipDiscountAmount(freeshipDiscountAmountVal)
                .finalAmount(tongThanhToan)
                .paymentMethod(paymentMethodName)
                .deliveryAddress(diaChi.getAddress())
                .status(0)
                .paymentStatus(1)
                .createdAt(Instant.now())
                .note(request.getNote())
                .build();
    }

    private void kiemTraKhoangCach(Store cuaHang, Address diaChi) {
        Double storeLat = cuaHang.getLat();
        Double storeLng = cuaHang.getLng();
        Double customerLat = diaChi.getLat();
        Double customerLng = diaChi.getLng();

        if (storeLat == null || storeLng == null || customerLat == null || customerLng == null) {
            log.warn("Thieu toa do cua hang hoac dia chi giao hang. Bo qua kiem tra khoang cach.");
            return;
        }

        double khoangCach = tinhKhoangCachHaversine(storeLat, storeLng, customerLat, customerLng);
        log.info("Khoang cach tu cua hang den dia chi giao hang: {:.1f} km.", Double.valueOf(khoangCach));

        if (khoangCach > GIOI_HAN_KHOANG_CACH_KM) {
            log.warn("Khoang cach {:.1f} km vuot qua gioi han {:.1f} km.", Double.valueOf(khoangCach), Double.valueOf(GIOI_HAN_KHOANG_CACH_KM));
            throw BusinessException.khoangCachVuotGioiHan(khoangCach, GIOI_HAN_KHOANG_CACH_KM);
        }

        log.info("Khoang cach nam trong gioi han cho phep.");
    }

    private double tinhKhoangCachHaversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BAN_KINH_TRAI_DAT * c;
    }

    private Map<String, Boolean> kiemTraTonKhoSanPham(List<String> foodIds) {
        Map<String, Boolean> ketQua = new HashMap<>();
        for (String foodId : foodIds) {
            try {
                var product = productRepository.findById(foodId);
                if (product != null) {
                    ketQua.put(foodId, !product.getIsOutOfStock());
                } else {
                    log.warn("San pham [{}] khong ton tai trong collection products.", foodId);
                    ketQua.put(foodId, false);
                }
            } catch (Exception e) {
                log.error("Loi khi kiem tra ton kho san pham [{}]: {}", foodId, e.getMessage());
                ketQua.put(foodId, false);
            }
        }
        return ketQua;
    }

    private VoucherInfo kiemTraVaXuLyVoucher(String userId, String voucherId, double tongTienHang) {
        VoucherInfo info = new VoucherInfo();
        info.setVoucherId(voucherId);

        try {
            Voucher systemVoucher = voucherRepository.getVoucher(voucherId);
            log.info("Ket qua truy van voucher he thong [{}]: {}", voucherId,
                    systemVoucher != null ? "TIM THAY" : "KHONG TIM THAY");
            if (systemVoucher != null) {
                info.setLoaiVoucher(LoaiVoucher.SYSTEM);
                info.setSystemVoucher(systemVoucher);
                kiemTraVoucherHeThong(systemVoucher, voucherId, tongTienHang);
                log.info("Voucher he thong [{}] hop le va duoc chap nhan.", voucherId);
                return info;
            }
        } catch (BusinessException e) {
            log.warn("BusinessException khi xu ly voucher he thong [{}]: {}", voucherId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception khi truy van voucher he thong [{}]: {} - {}", voucherId, e.getClass().getName(), e.getMessage());
        }

        try {
            MyVoucher caNhanVoucher = voucherRepository.layVoucherCaNhan(userId, voucherId);
            log.info("Ket qua truy van voucher ca nhan [{}] cua user [{}]: {}", voucherId, userId,
                    caNhanVoucher != null ? "TIM THAY" : "KHONG TIM THAY");
            if (caNhanVoucher != null) {
                info.setLoaiVoucher(LoaiVoucher.CA_NHAN);
                info.setMyVoucher(caNhanVoucher);
                kiemTraVoucherCaNhan(caNhanVoucher, voucherId, tongTienHang);
                log.info("Voucher ca nhan [{}] hop le va duoc chap nhan.", voucherId);
                return info;
            }
        } catch (BusinessException e) {
            log.warn("BusinessException khi xu ly voucher ca nhan [{}]: {}", voucherId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception khi truy van voucher ca nhan [{}]: {} - {}", voucherId, e.getClass().getName(), e.getMessage());
        }

        log.warn("Khong tim thay voucher [{}] trong ca vouchers va my_vouchers.", voucherId);
        throw BusinessException.voucherKhongTimThay(voucherId);
    }

    private void kiemTraVoucherHeThong(Voucher voucher, String voucherId, double tongTienHang) {
        int remaining = getRemainingFromSystemVoucher(voucher);
        if (remaining <= 0) {
            log.warn("Voucher he thong [{}] da het so luong (remaining: {}).", voucherId, remaining);
            throw BusinessException.voucherDaHetSoLuong(voucherId);
        }

        double minOrderValue = getMinOrderValueFromSystemVoucher(voucher);
        if (minOrderValue > 0 && tongTienHang < minOrderValue) {
            log.warn("Voucher [{}] yeu cau don hang toi thieu {} VND, nhung gia tri hien tai la {} VND.",
                    voucherId, minOrderValue, tongTienHang);
            throw BusinessException.voucherKhongDatDonToiThieu(voucherId, minOrderValue);
        }

        log.info("Voucher he thong [{}] hop le - remaining: {}, minOrderValue: {}.",
                voucherId, remaining, minOrderValue);
    }

    private void kiemTraVoucherCaNhan(MyVoucher voucher, String voucherId, double tongTienHang) {
        if (!voucher.isActive()) {
            log.warn("Voucher ca nhan [{}] chua duoc kich hoat (isActive: false).", voucherId);
            throw BusinessException.voucherInactive(voucherId);
        }

        if (voucher.getExpiryDate() != null) {
            Date now = new Date();
            if (now.after(voucher.getExpiryDate())) {
                log.warn("Voucher ca nhan [{}] da het han (ngay het han: {}).", voucherId, voucher.getExpiryDate());
                throw BusinessException.voucherDaHetHan(voucherId);
            }
        }

        double minOrderValue = voucher.getMinOrderValue();
        if (minOrderValue > 0 && tongTienHang < minOrderValue) {
            log.warn("Voucher [{}] yeu cau don hang toi thieu {} VND, nhung gia tri hien tai la {} VND.",
                    voucherId, minOrderValue, tongTienHang);
            throw BusinessException.voucherKhongDatDonToiThieu(voucherId, minOrderValue);
        }

        log.info("Voucher ca nhan [{}] hop le - expiryDate: {}, minOrderValue: {}.",
                voucherId, voucher.getExpiryDate(), minOrderValue);
    }

    private int getRemainingFromSystemVoucher(Voucher voucher) {
        int remaining = voucher.getRemaining();
        log.info("Doc truong remaining tu Voucher: {}", remaining);
        return remaining;
    }

    private double getMinOrderValueFromSystemVoucher(Voucher voucher) {
        double minOrderValue = voucher.getMinOrderValue();
        log.info("Doc truong minOrderValue tu Voucher: {}", minOrderValue);
        return minOrderValue;
    }

    private double tinhSoTienGiam(VoucherInfo info, double tongTienHang, double phiShip) {
        if (info.getLoaiVoucher() == LoaiVoucher.SYSTEM) {
            Voucher v = info.getSystemVoucher();
            int type = v.getType();
            double voucherValue = v.getValue();
            boolean isFreeship = v.getIsFreeship();
            log.info("Tinh giam voucher he thong - type: {}, value: {}, isFreeship: {}, tongTienHang: {}, phiShip: {}", type, voucherValue, isFreeship, tongTienHang, phiShip);

            if (isFreeship) {
                double soTienGiam = phiShip;
                log.info("Voucher he thong freeship - giam {} VND.", soTienGiam);
                return soTienGiam;
            }
            if (type == 1) {
                double soTienGiam = tongTienHang * (voucherValue / 100.0);
                log.info("Voucher he thong phan tram - giam {}% tuong duong {} VND.", voucherValue, soTienGiam);
                return soTienGiam;
            } else {
                double soTienGiam = voucherValue;
                if (soTienGiam > tongTienHang) {
                    soTienGiam = tongTienHang;
                }
                log.info("Voucher he thong tien mat - giam {} VND.", soTienGiam);
                return soTienGiam;
            }
        } else {
            MyVoucher v = info.getMyVoucher();
            int type = v.getType();
            double voucherValue = v.getValue();
            boolean isFreeship = v.isFreeship();
            log.info("Tinh giam voucher ca nhan - type: {}, value: {}, isFreeship: {}, tongTienHang: {}, phiShip: {}", type, voucherValue, isFreeship, tongTienHang, phiShip);

            if (isFreeship) {
                double soTienGiam = phiShip;
                log.info("Voucher ca nhan freeship - giam {} VND.", soTienGiam);
                return soTienGiam;
            }
            if (type == 1) {
                double soTienGiam = tongTienHang * (voucherValue / 100.0);
                log.info("Voucher ca nhan phan tram - giam {}% tuong duong {} VND.", voucherValue, soTienGiam);
                return soTienGiam;
            } else {
                double soTienGiam = voucherValue;
                if (soTienGiam > tongTienHang) {
                    soTienGiam = tongTienHang;
                }
                log.info("Voucher ca nhan tien mat - giam {} VND.", soTienGiam);
                return soTienGiam;
            }
        }
    }

    private double tinhTongSoTienGiam(List<VoucherInfo> voucherInfos, double tongTienHang, double phiShip) {
        double tongSoTienGiam = 0.0;
        double phiShipConLai = phiShip;

        for (VoucherInfo info : voucherInfos) {
            double soTienGiam = tinhSoTienGiam(info, tongTienHang, phiShipConLai);
            tongSoTienGiam += soTienGiam;

            if (info.getLoaiVoucher() == LoaiVoucher.SYSTEM) {
                Voucher v = info.getSystemVoucher();
                if (v.getIsFreeship()) {
                    phiShipConLai = 0.0;
                }
            } else {
                MyVoucher v = info.getMyVoucher();
                if (v.isFreeship()) {
                    phiShipConLai = 0.0;
                }
            }
        }

        return tongSoTienGiam;
    }

    private CheckoutResponse.OrderItemData[] chuanBiOrderItems(List<CheckoutRequestV2.CheckoutItem> requestItems) {
        List<CheckoutResponse.OrderItemData> items = new ArrayList<>();
        for (CheckoutRequestV2.CheckoutItem cartItem : requestItems) {
            Product product = null;
            try {
                product = productRepository.findById(cartItem.getFoodId());
            } catch (Exception e) {
                log.warn("Khong the lay san pham [{}] khi chuan bi order items", cartItem.getFoodId());
            }

            String selectedSize = null;
            List<CheckoutResponse.ItemOption> toppingOptions = null;

            if (cartItem.getSelectedOptions() != null && !cartItem.getSelectedOptions().isEmpty()) {
                List<CheckoutResponse.ItemOption> allToppings = new ArrayList<>();
                for (CheckoutRequestV2.SelectedOptionGroup group : cartItem.getSelectedOptions()) {
                    if (group.getName() == null) continue;
                    String groupNameLower = group.getName().toLowerCase();

                    if (groupNameLower.contains("kich") && groupNameLower.contains("thuoc")
                            || groupNameLower.contains("size")) {
                        if (group.getOptions() != null && !group.getOptions().isEmpty()) {
                            CheckoutRequestV2.SelectedOption sizeOpt = group.getOptions().get(0);
                            selectedSize = sizeOpt.getName();
                        }
                    } else {
                        if (group.getOptions() != null) {
                            for (CheckoutRequestV2.SelectedOption selOpt : group.getOptions()) {
                                double toppingPrice = 0.0;
                                if (product != null) {
                                    toppingPrice = layGiaOptionTheoTen(product, selOpt.getName());
                                }
                                allToppings.add(CheckoutResponse.ItemOption.builder()
                                        .name(selOpt.getName())
                                        .price(toppingPrice)
                                        .build());
                            }
                        }
                    }
                }
                if (!allToppings.isEmpty()) {
                    toppingOptions = allToppings;
                }
            }

            double donGia = tinhDonGiaMotMon(cartItem, product, selectedSize);

            items.add(CheckoutResponse.OrderItemData.builder()
                    .foodId(cartItem.getFoodId())
                    .name(cartItem.getName())
                    .price(donGia)
                    .quantity(cartItem.getQuantity())
                    .imageUrl(cartItem.getImageUrl())
                    .size(selectedSize)
                    .options(toppingOptions)
                    .build());
        }
        return items.toArray(new CheckoutResponse.OrderItemData[0]);
    }

    private double tinhDonGiaMotMon(CheckoutRequestV2.CheckoutItem item, Product product, String selectedSize) {
        double tongDonGia = 0.0;
        if (product != null && product.getBasePrice() > 0) {
            tongDonGia = product.getBasePrice();
        }

        if (selectedSize != null && !selectedSize.isBlank() && product != null) {
            double giaSize = layGiaOptionTheoTen(product, selectedSize);
            tongDonGia += giaSize;
            log.info("Gia size [{}] cho san pham [{}]: {}", selectedSize, item.getFoodId(), giaSize);
        }

        if (item.getSelectedOptions() != null && product != null) {
            for (CheckoutRequestV2.SelectedOptionGroup group : item.getSelectedOptions()) {
                if (group.getName() == null) continue;
                String groupNameLower = group.getName().toLowerCase();
                boolean isSizeGroup = groupNameLower.contains("kich") && groupNameLower.contains("thuoc")
                        || groupNameLower.contains("size");
                if (isSizeGroup) continue;

                if (group.getOptions() != null) {
                    for (CheckoutRequestV2.SelectedOption selOpt : group.getOptions()) {
                        double gia = layGiaOptionTheoTen(product, selOpt.getName());
                        tongDonGia += gia;
                    }
                }
            }
        }
        return tongDonGia;
    }

    private double layGiaOptionTheoTen(Product product, String optionName) {
        if (product.getOptionGroups() == null) {
            return 0.0;
        }
        for (Product.ProductOptionGroup group : product.getOptionGroups()) {
            if (group.getOptions() == null) continue;
            for (Product.ProductOption option : group.getOptions()) {
                if (option.getName() != null && option.getName().equalsIgnoreCase(optionName)) {
                    log.info("Tim thay option [{}] trong optionGroups voi gia: {}", optionName, option.getPrice());
                    return option.getPrice();
                }
            }
        }
        log.warn("Khong tim thay option [{}] trong optionGroups cua san pham [{}], gia = 0",
                optionName, product.getId());
        return 0.0;
    }

    private double tinhTongTienTuItems(List<CheckoutRequestV2.CheckoutItem> requestItems) {
        double tong = 0.0;
        for (CheckoutRequestV2.CheckoutItem item : requestItems) {
            Product product = null;
            String selectedSize = null;
            try {
                product = productRepository.findById(item.getFoodId());
            } catch (Exception e) {
                log.warn("Khong the lay san pham [{}] khi tinh tong tien", item.getFoodId());
            }

            if (item.getSelectedOptions() != null && !item.getSelectedOptions().isEmpty()) {
                for (CheckoutRequestV2.SelectedOptionGroup group : item.getSelectedOptions()) {
                    if (group.getName() == null) continue;
                    String groupNameLower = group.getName().toLowerCase();
                    boolean isSizeGroup = groupNameLower.contains("kich") && groupNameLower.contains("thuoc")
                            || groupNameLower.contains("size");
                    if (isSizeGroup && group.getOptions() != null && !group.getOptions().isEmpty()) {
                        selectedSize = group.getOptions().get(0).getName();
                    }
                }
            }

            double donGia = tinhDonGiaMotMon(item, product, selectedSize);
            tong += donGia * item.getQuantity();
        }
        return tong;
    }

    private String taoDonHangAtomic(
            String userId,
            String storeId,
            String storeName,
            Address diaChi,
            CheckoutRequestV2 request,
            CheckoutResponse.OrderItemData[] orderItems,
            double tongTienHang,
            double phiShip,
            double discountAmount,
            double shopDiscountAmount,
            double freeshipDiscountAmount,
            double tongThanhToan,
            List<VoucherInfo> voucherInfos,
            String idempotencyKey,
            String orderCode
    ) {
        WriteBatch batch = firestore.batch();
        log.info("Bat dau tao don hang atomi cho nguoi dung [{}].", userId);

        String orderId;
        DocumentReference orderDocRef = firestore.collection("orders").document();
        orderId = orderDocRef.getId();

        List<Map<String, Object>> itemsData = new ArrayList<>();
        for (CheckoutResponse.OrderItemData item : orderItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("foodId", item.getFoodId());
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("imageUrl", item.getImageUrl() != null ? item.getImageUrl() : "");
            itemMap.put("size", item.getSize() != null ? item.getSize() : "");
            if (item.getOptions() != null) {
                List<Map<String, Object>> toppingMaps = new ArrayList<>();
                for (CheckoutResponse.ItemOption t : item.getOptions()) {
                    toppingMaps.add(Map.of("name", t.getName() != null ? t.getName() : "",
                            "price", t.getPrice() != null ? t.getPrice() : 0.0));
                }
                itemMap.put("options", toppingMaps);
            } else {
                itemMap.put("options", new ArrayList<>());
            }
            itemsData.add(itemMap);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("code", orderCode);
        orderData.put("userId", userId);
        orderData.put("storeId", storeId);
        orderData.put("storeName", storeName != null ? storeName : "");
        orderData.put("items", itemsData);
        orderData.put("totalAmount", tongTienHang);
        orderData.put("deliveryFee", phiShip);
        orderData.put("discountAmount", discountAmount);
        orderData.put("shopDiscountAmount", shopDiscountAmount);
        orderData.put("freeshipDiscountAmount", freeshipDiscountAmount);
        orderData.put("finalAmount", tongThanhToan);
        orderData.put("paymentMethod", request.getPaymentMethod());
        orderData.put("deliveryAddress", diaChi.getAddress());
        orderData.put("addressId", diaChi.getId());
        orderData.put("deliveryLat", diaChi.getLat() != null ? diaChi.getLat() : 0.0);
        orderData.put("deliveryLng", diaChi.getLng() != null ? diaChi.getLng() : 0.0);
        orderData.put("receiverName", diaChi.getReceiverName() != null ? diaChi.getReceiverName() : "");
        orderData.put("receiverPhone", diaChi.getReceiverPhone() != null ? diaChi.getReceiverPhone() : "");
        orderData.put("status", 0);
        orderData.put("paymentStatus", 1);
        orderData.put("note", request.getNote() != null ? request.getNote() : "");
        orderData.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
        orderData.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
        orderData.put("deletedAt", null);

        // Luu idempotency key de chan dat hang trung lap
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            orderData.put("idempotencyKey", idempotencyKey);
        }

        batch.set(orderDocRef, orderData);
        log.info("Them thao tac tao document don hang [{}] vao WriteBatch.", orderId);

        if (voucherInfos != null && !voucherInfos.isEmpty()) {
            for (VoucherInfo voucherInfo : voucherInfos) {
                String voucherId = voucherInfo.getVoucherId();
                if (voucherInfo.getLoaiVoucher() == LoaiVoucher.SYSTEM) {
                    DocumentReference voucherDocRef = firestore.collection("vouchers").document(voucherId);
                    batch.update(voucherDocRef, "remaining",
                            com.google.cloud.firestore.FieldValue.increment(-1));
                    log.info("Them thao tac giam remaining voucher he thong [{}] vao WriteBatch.", voucherId);
                } else {
                    DocumentReference myVoucherDocRef = firestore
                            .collection("customer_profiles")
                            .document(userId)
                            .collection("my_vouchers")
                            .document(voucherId);
                    batch.delete(myVoucherDocRef);
                    log.info("Them thao tac xoa voucher ca nhan [{}] khoi my_vouchers vao WriteBatch.", voucherId);
                }
            }
        }

        // Chi xoa nhung cart item trung voi cac mon da dat (cung foodId + cung selectedOptions)
        CollectionReference cartRef = firestore
                .collection("customer_profiles")
                .document(userId)
                .collection("cart");
        int cartItemCount = 0;
        try {
            ApiFuture<QuerySnapshot> cartQuery = cartRef.get();
            QuerySnapshot cartSnapshot = cartQuery.get();
            for (DocumentSnapshot cartDoc : cartSnapshot.getDocuments()) {
                String cartFoodId = cartDoc.getString("foodId");
                List<Map<String, Object>> cartSelectedOptions = (List<Map<String, Object>>) cartDoc.get("selectedOptions");

                boolean matchesAnOrderedItem = request.getItems().stream().anyMatch(item -> {
                    if (!item.getFoodId().equals(cartFoodId)) {
                        return false;
                    }
                    List<CheckoutRequestV2.SelectedOptionGroup> itemOptions = item.getSelectedOptions();
                    return coCungSelectedOptions(cartSelectedOptions, itemOptions);
                });

                if (matchesAnOrderedItem) {
                    batch.delete(cartDoc.getReference());
                    cartItemCount++;
                    log.debug("Xoa cart item [{}] - foodId: {} - trung voi item da dat", cartDoc.getId(), cartFoodId);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Khong the truy van gio hang de xoa (van tiep tuc tao don): {}", e.getMessage());
        }
        if (cartItemCount > 0) {
            log.info("Them thao tac xoa {} mon da dat khoi gio hang cua nguoi dung [{}] vao WriteBatch.", cartItemCount, userId);
        } else {
            log.info("Khong co cart item nao trung voi cac mon da dat trong gio hang cua nguoi dung [{}].", userId);
        }

        try {
            batch.commit();
            log.info("WriteBatch commit thanh cong - orderId: [{}].", orderId);
        } catch (Exception e) {
            log.error("Loi khi commit WriteBatch: {}", e.getMessage(), e);
            throw BusinessException.loiHeThong("Khong the tao don hang. Vui long thu lai sau.");
        }

        return orderId;
    }

    private String kiemTraIdempotencyKey(String userId, String idempotencyKey) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("orders")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("idempotencyKey", idempotencyKey)
                    .limit(1)
                    .get();
            QuerySnapshot snapshot = future.get();
            if (!snapshot.isEmpty()) {
                return snapshot.getDocuments().get(0).getId();
            }
        } catch (Exception e) {
            log.warn("Loi khi kiem tra idempotency key [{}]: {}", idempotencyKey, e.getMessage());
        }
        return null;
    }

    private boolean coCungSelectedOptions(
            List<Map<String, Object>> cartOptions,
            List<CheckoutRequestV2.SelectedOptionGroup> itemOptions
    ) {
        boolean cartEmpty = cartOptions == null || cartOptions.isEmpty();
        boolean itemEmpty = itemOptions == null || itemOptions.isEmpty();

        if (cartEmpty && itemEmpty) {
            return true;
        }
        if (cartEmpty || itemEmpty) {
            return false;
        }

        if (cartOptions.size() != itemOptions.size()) {
            return false;
        }

        for (Map<String, Object> cartGroupMap : cartOptions) {
            String cartGroupName = (String) cartGroupMap.get("name");
            CheckoutRequestV2.SelectedOptionGroup matchingItemGroup = itemOptions.stream()
                    .filter(g -> java.util.Objects.equals(g.getName(), cartGroupName))
                    .findFirst()
                    .orElse(null);

            if (matchingItemGroup == null) {
                return false;
            }

            List<CheckoutRequestV2.SelectedOption> itemOpts = matchingItemGroup.getOptions();
            List<Map<String, Object>> cartOpts = (List<Map<String, Object>>) cartGroupMap.get("options");

            boolean cartOptsEmpty = cartOpts == null || cartOpts.isEmpty();
            boolean itemOptsEmpty = itemOpts == null || itemOpts.isEmpty();

            if (cartOptsEmpty && itemOptsEmpty) {
                continue;
            }
            if (cartOptsEmpty || itemOptsEmpty) {
                return false;
            }
            if (cartOpts.size() != itemOpts.size()) {
                return false;
            }

            List<String> cartOptNames = cartOpts.stream()
                    .map(o -> (String) o.get("name"))
                    .sorted()
                    .toList();
            List<String> itemOptNames = itemOpts.stream()
                    .map(CheckoutRequestV2.SelectedOption::getName)
                    .sorted()
                    .toList();

            if (!cartOptNames.equals(itemOptNames)) {
                return false;
            }
        }

        return true;
    }

    private enum LoaiVoucher {
        SYSTEM,
        CA_NHAN
    }

    private static class VoucherInfo {
        private LoaiVoucher loaiVoucher;
        private String voucherId;
        private Voucher systemVoucher;
        private MyVoucher myVoucher;

        public LoaiVoucher getLoaiVoucher() { return loaiVoucher; }
        public void setLoaiVoucher(LoaiVoucher loaiVoucher) { this.loaiVoucher = loaiVoucher; }
        public String getVoucherId() { return voucherId; }
        public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
        public Voucher getSystemVoucher() { return systemVoucher; }
        public void setSystemVoucher(Voucher systemVoucher) { this.systemVoucher = systemVoucher; }
        public MyVoucher getMyVoucher() { return myVoucher; }
        public void setMyVoucher(MyVoucher myVoucher) { this.myVoucher = myVoucher; }
    }
}
