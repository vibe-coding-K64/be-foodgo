package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.BatchReviewRequest;
import com.example.be_foodgo.dto.ReviewDTO;
import com.example.be_foodgo.dto.ReviewRequest;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.model.Order;
import com.example.be_foodgo.model.Product;
import com.example.be_foodgo.model.Review;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.repository.OrderRepository;
import com.example.be_foodgo.repository.ProductRepository;
import com.example.be_foodgo.repository.ReviewRepository;
import com.example.be_foodgo.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private static final int TRANG_THAI_HOAN_THANH = 3;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public ReviewDTO taoDanhGia(ReviewRequest request) throws Exception {
        log.info("Bat dau tao danh gia cho don hang [{}] tu nguoi dung [{}]", request.getOrderId(), request.getUserId());

        Order donHang = orderRepository.findById(request.getOrderId());

        if (donHang == null) {
            log.warn("Don hang [{}] khong ton tai", request.getOrderId());
            throw BusinessException.donHangKhongTimThay(request.getOrderId());
        }

        String chuSoHuu = donHang.getUserId();
        if (chuSoHuu == null || !chuSoHuu.equals(request.getUserId())) {
            log.warn("Nguoi dung [{}] khong phai chu so huu don hang [{}]", request.getUserId(), request.getOrderId());
            throw BusinessException.khongPhaiChuDonHang(request.getOrderId());
        }

        int trangThaiHienTai = donHang.getStatusValue();
        if (trangThaiHienTai != TRANG_THAI_HOAN_THANH) {
            log.warn("Don hang [{}] dang o trang thai [{}], chi cho phep danh gia khi trang thai = 3 (Hoan thanh)",
                    request.getOrderId(), trangThaiHienTai);
            throw BusinessException.trangThaiDonHangKhongChoPhepDanhGia(request.getOrderId(), trangThaiHienTai);
        }

        List<Review> danhSachDanhGiaHienTai = reviewRepository.findByOrderIdAndItemId(
                request.getOrderId(), request.getItemId());
        if (!danhSachDanhGiaHienTai.isEmpty()) {
            log.warn("Mon an [{}] trong don hang [{}] da duoc danh gia roi",
                    request.getItemId(), request.getOrderId());
            throw BusinessException.donHangDaDuocDanhGia(request.getOrderId());
        }

        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setItemId(request.getItemId());
        review.setFoodId(request.getFoodId());
        review.setStoreId(request.getStoreId());
        review.setUserId(request.getUserId());
        review.setUserName(request.getUserName());
        review.setUserAvatarUrl(request.getUserAvatarUrl());
        review.setStarRating(request.getStarRating());
        review.setComment(request.getComment());
        review.setImageUrls(request.getImageUrls());
        review.setCreatedAt(new Date());
        review.setUpdatedAt(new Date());

        String reviewId = reviewRepository.save(review);
        review.setId(reviewId);
        log.info("Da luu danh gia [{}] cho mon [{}] trong don hang [{}]",
                reviewId, request.getItemId(), request.getOrderId());

        capNhatDiemSoCuaHang(request.getStoreId(), request.getStarRating());
        capNhatDiemSoSanPham(request.getFoodId(), request.getStarRating());

        return convertToDTO(review);
    }

    private void capNhatDiemSoCuaHang(String storeId, int starRatingMoi) throws Exception {
        Store cuaHang = storeRepository.getStoreById(storeId);
        if (cuaHang == null) {
            log.warn("Cua hang [{}] khong ton tai, khong the cap nhat diem so", storeId);
            return;
        }

        int reviewCountCu = cuaHang.getReviewCount() != null ? cuaHang.getReviewCount() : 0;
        double ratingCu = cuaHang.getRating() != null ? cuaHang.getRating() : 0.0;

        int reviewCountMoi = reviewCountCu + 1;
        double ratingMoi = (ratingCu * reviewCountCu + starRatingMoi) / reviewCountMoi;
        ratingMoi = Math.round(ratingMoi * 10.0) / 10.0;

        log.info("Cap nhat diem so cua hang [{}]: reviewCount {} -> {}, rating {} -> {}",
                storeId, reviewCountCu, reviewCountMoi, ratingCu, ratingMoi);
        reviewRepository.capNhatStoreRating(storeId, ratingMoi, reviewCountMoi);
    }

    private void capNhatDiemSoSanPham(String foodId, int starRatingMoi) throws Exception {
        Product sanPham = productRepository.findById(foodId);
        if (sanPham == null) {
            log.warn("San pham [{}] khong ton tai, khong the cap nhat diem so", foodId);
            return;
        }

        int reviewCountCu = sanPham.getReviewCount() != null ? sanPham.getReviewCount() : 0;
        double ratingCu = sanPham.getRating() != null ? sanPham.getRating() : 0.0;

        int reviewCountMoi = reviewCountCu + 1;
        double ratingMoi = (ratingCu * reviewCountCu + starRatingMoi) / reviewCountMoi;
        ratingMoi = Math.round(ratingMoi * 10.0) / 10.0;

        log.info("Cap nhat diem so san pham [{}]: reviewCount {} -> {}, rating {} -> {}",
                foodId, reviewCountCu, reviewCountMoi, ratingCu, ratingMoi);
        productRepository.capNhatProductRating(foodId, ratingMoi, reviewCountMoi);
    }

    private void capNhatDiemSoSanPhamSauKhiXoa(String foodId, int starRatingDaXoa) throws Exception {
        Product sanPham = productRepository.findById(foodId);
        if (sanPham == null) {
            log.warn("San pham [{}] khong ton tai", foodId);
            return;
        }

        int reviewCountCu = sanPham.getReviewCount() != null ? sanPham.getReviewCount() : 0;
        double ratingCu = sanPham.getRating() != null ? sanPham.getRating() : 0.0;

        if (reviewCountCu <= 1) {
            log.info("San pham [{}] chi con 1 review, reset ve 0", foodId);
            productRepository.capNhatProductRating(foodId, 0.0, 0);
            return;
        }

        double tongDiemCu = ratingCu * reviewCountCu;
        double tongDiemMoi = tongDiemCu - starRatingDaXoa;
        int reviewCountMoi = reviewCountCu - 1;
        double ratingMoi = tongDiemMoi / reviewCountMoi;
        ratingMoi = Math.round(ratingMoi * 10.0) / 10.0;

        log.info("Cap nhat diem so san pham [{}] sau xoa: reviewCount {} -> {}, rating {} -> {}",
                foodId, reviewCountCu, reviewCountMoi, ratingCu, ratingMoi);
        productRepository.capNhatProductRating(foodId, ratingMoi, reviewCountMoi);
    }

    private void capNhatDiemSoCuaHangSauBatch(String storeId, int tongSaoCu, int tongSaoMoi, int soLuongCu, int soLuongMoi) throws Exception {
        Store cuaHang = storeRepository.getStoreById(storeId);
        if (cuaHang == null) {
            log.warn("Cua hang [{}] khong ton tai, khong the cap nhat diem so", storeId);
            return;
        }

        int reviewCountCu = cuaHang.getReviewCount() != null ? cuaHang.getReviewCount() : 0;
        double ratingCu = cuaHang.getRating() != null ? cuaHang.getRating() : 0.0;

        double tongDiemCu = ratingCu * reviewCountCu;
        double tongDiemMoi = tongDiemCu - tongSaoCu + tongSaoMoi;
        int reviewCountMoi = reviewCountCu - soLuongCu + soLuongMoi;

        double ratingMoi = reviewCountMoi > 0 ? tongDiemMoi / reviewCountMoi : 0.0;
        ratingMoi = Math.round(ratingMoi * 10.0) / 10.0;

        log.info("Cap nhat diem so cua hang [{}] sau batch: reviewCount {} -> {}, rating {} -> {}",
                storeId, reviewCountCu, reviewCountMoi, ratingCu, ratingMoi);
        reviewRepository.capNhatStoreRating(storeId, ratingMoi, reviewCountMoi);
    }

    public List<ReviewDTO> layDanhSachDanhGiaCuaHang(String storeId) throws Exception {
        Store cuaHang = storeRepository.getStoreById(storeId);
        if (cuaHang == null) {
            log.warn("Cua hang [{}] khong ton tai", storeId);
            throw BusinessException.cuaHangKhongTonTai(storeId);
        }
        List<Review> reviews = reviewRepository.findByStoreId(storeId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewDTO> layDanhSachDanhGiaSanPham(String foodId) throws Exception {
        List<Review> reviews = reviewRepository.findByFoodId(foodId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BatchReviewResult taoDanhGiaBatch(
            BatchReviewRequest request,
            List<MultipartFile> images) throws Exception {
        log.info("Bat dau tao danh gia batch cho don hang [{}] tu nguoi dung [{}], so mon: {}, so anh: {}",
                request.getOrderId(), request.getUserId(), request.getItems().size(),
                images != null ? images.size() : 0);

        Order donHang = orderRepository.findById(request.getOrderId());
        if (donHang == null) {
            log.warn("Don hang [{}] khong ton tai", request.getOrderId());
            throw BusinessException.donHangKhongTimThay(request.getOrderId());
        }

        String chuSoHuu = donHang.getUserId();
        if (chuSoHuu == null || !chuSoHuu.equals(request.getUserId())) {
            log.warn("Nguoi dung [{}] khong phai chu so huu don hang [{}]", request.getUserId(), request.getOrderId());
            throw BusinessException.khongPhaiChuDonHang(request.getOrderId());
        }

        int trangThaiHienTai = donHang.getStatusValue();
        if (trangThaiHienTai != TRANG_THAI_HOAN_THANH) {
            log.warn("Don hang [{}] dang o trang thai [{}], chi cho phep danh gia khi trang thai = 3 (Hoan thanh)",
                    request.getOrderId(), trangThaiHienTai);
            throw BusinessException.trangThaiDonHangKhongChoPhepDanhGia(request.getOrderId(), trangThaiHienTai);
        }

        if (request.getItems().size() > 20) {
            throw new IllegalArgumentException("Tối đa 20 món đánh giá mỗi request.");
        }

        List<String> reviewIds = new ArrayList<>();
        int count = 0;
        int imageCursor = 0;
        int tongSaoStoreCu = 0;

        List<MultipartFile> allImages = (images != null) ? images : new ArrayList<>();

        int soLuongCu = 0;
        for (int itemIdx = 0; itemIdx < request.getItems().size(); itemIdx++) {
            BatchReviewRequest.BatchReviewItem item = request.getItems().get(itemIdx);

            List<Review> danhSachHienTai = reviewRepository.findByOrderIdAndItemId(
                    request.getOrderId(), item.getProductId());
            if (!danhSachHienTai.isEmpty()) {
                Review cu = danhSachHienTai.get(0);
                Integer saoCu = cu.getStarRating();
                reviewRepository.deleteById(cu.getId());
                log.info("Da xoa danh gia cu [{}] cua mon [{}] trong don hang [{}]",
                        cu.getId(), item.getProductId(), request.getOrderId());
                if (saoCu != null) {
                    capNhatDiemSoSanPhamSauKhiXoa(item.getProductId(), saoCu);
                    tongSaoStoreCu += saoCu;
                    soLuongCu++;
                }
            }

            List<String> anhDanhGia = new ArrayList<>();
            List<String> itemImageUrls = item.getImageUrls();
            int soAnhItem = 0;
            if (itemImageUrls != null && !itemImageUrls.isEmpty()) {
                soAnhItem = itemImageUrls.size();
            } else if (!allImages.isEmpty()) {
                soAnhItem = (int) Math.ceil((double) allImages.size() / request.getItems().size());
            }
            for (int imgIdx = 0; imgIdx < soAnhItem; imgIdx++) {
                if (imageCursor >= allImages.size()) {
                    break;
                }
                MultipartFile imageFile = allImages.get(imageCursor);
                String url = cloudinaryService.uploadReviewImage(
                        imageFile, request.getOrderId(), itemIdx, imgIdx);
                anhDanhGia.add(url);
                imageCursor++;
            }

            Review review = new Review();
            review.setOrderId(request.getOrderId());
            review.setItemId(item.getProductId());
            review.setFoodId(item.getProductId());
            review.setStoreId(request.getStoreId());
            review.setUserId(request.getUserId());
            review.setUserName(request.getUserName());
            review.setUserAvatarUrl(request.getUserAvatarUrl());
            review.setStarRating(item.getStarRating());
            review.setComment(item.getComment() != null ? item.getComment() : "");
            review.setImageUrls(anhDanhGia);
            review.setCreatedAt(new Date());
            review.setUpdatedAt(new Date());

            String reviewId = reviewRepository.save(review);
            review.setId(reviewId);
            log.info("Da luu danh gia [{}] cho mon [{}] trong don hang [{}]",
                    reviewId, item.getProductId(), request.getOrderId());

            reviewIds.add(reviewId);
            count++;
        }

        int tongSaoStoreMoi = 0;
        for (BatchReviewRequest.BatchReviewItem item : request.getItems()) {
            tongSaoStoreMoi += item.getStarRating();
        }

        if (count > 0) {
            capNhatDiemSoCuaHangSauBatch(request.getStoreId(), tongSaoStoreCu, tongSaoStoreMoi, soLuongCu, count);
            for (BatchReviewRequest.BatchReviewItem item : request.getItems()) {
                capNhatDiemSoSanPham(item.getProductId(), item.getStarRating());
            }
        }

        log.info("Hoan tat tao danh gia batch cho don hang [{}], so danh gia tao thanh cong: {}",
                request.getOrderId(), count);
        return new BatchReviewResult(count, reviewIds);
    }

    public static class BatchReviewResult {
        public final int count;
        public final List<String> reviewIds;

        public BatchReviewResult(int count, List<String> reviewIds) {
            this.count = count;
            this.reviewIds = reviewIds;
        }
    }

    public ReviewDTO replyReview(String reviewId, String replyComment) throws Exception {
        Review review = reviewRepository.findById(reviewId);
        if (review == null) {
            log.warn("Không tìm thấy đánh giá [{}]", reviewId);
            throw new Exception("Không tìm thấy đánh giá");
        }
        review.setReplyComment(replyComment);
        review.setRepliedAt(new Date());
        review.setUpdatedAt(new Date());
        reviewRepository.updateReview(review);
        log.info("Đã phản hồi đánh giá [{}]", reviewId);
        return convertToDTO(review);
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setOrderId(review.getOrderId());
        dto.setItemId(review.getItemId());
        dto.setFoodId(review.getFoodId());
        dto.setStoreId(review.getStoreId());
        dto.setUserId(review.getUserId());
        dto.setUserName(review.getUserName());
        dto.setUserAvatarUrl(review.getUserAvatarUrl());
        dto.setStarRating(review.getStarRating());
        dto.setComment(review.getComment());
        dto.setImageUrls(review.getImageUrls());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        dto.setReplyComment(review.getReplyComment());
        dto.setRepliedAt(review.getRepliedAt());
        return dto;
    }
}
