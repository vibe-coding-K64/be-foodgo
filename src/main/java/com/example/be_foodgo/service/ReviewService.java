package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.ReviewDTO;
import com.example.be_foodgo.dto.ReviewRequest;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.model.Order;
import com.example.be_foodgo.model.Review;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.repository.OrderRepository;
import com.example.be_foodgo.repository.ReviewRepository;
import com.example.be_foodgo.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        List<Review> danhSachDanhGiaHienTai = reviewRepository.findByOrderId(request.getOrderId());
        if (!danhSachDanhGiaHienTai.isEmpty()) {
            log.warn("Don hang [{}] da duoc danh gia roi", request.getOrderId());
            throw BusinessException.donHangDaDuocDanhGia(request.getOrderId());
        }

        Review review = new Review();
        review.setOrderId(request.getOrderId());
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
        log.info("Da luu danh gia [{}] cho don hang [{}]", reviewId, request.getOrderId());

        capNhatDiemSoCuaHang(request.getStoreId(), request.getStarRating());

        return convertToDTO(review);
    }

    private void capNhatDiemSoCuaHang(String storeId, int starRatingMoi) throws Exception {
        Store cuaHang = storeRepository.getStoreById(storeId);
        if (cuaHang == null) {
            log.warn("Cua hang [{}] khong ton tai, khong the cap nhat diem so", storeId);
            return;
        }

        int reviewCountCu = 0;
        double ratingCu = 0.0;

        if (cuaHang.getReviewCount() != null) {
            reviewCountCu = cuaHang.getReviewCount();
        }
        if (cuaHang.getRating() != null) {
            ratingCu = cuaHang.getRating();
        }

        int reviewCountMoi = reviewCountCu + 1;
        double ratingMoi;
        if (reviewCountCu == 0) {
            ratingMoi = starRatingMoi;
        } else {
            double tongDiem = ratingCu * reviewCountCu + starRatingMoi;
            ratingMoi = tongDiem / reviewCountMoi;
        }

        ratingMoi = Math.round(ratingMoi * 10.0) / 10.0;

        log.info("Cap nhat diem so cua hang [{}]: reviewCount {} -> {}, rating {} -> {}",
                storeId, reviewCountCu, reviewCountMoi, ratingCu, ratingMoi);

        reviewRepository.capNhatStoreRating(storeId, ratingMoi, reviewCountMoi);
    }

    public List<ReviewDTO> layDanhSachDanhGiaCuaHang(String storeId) throws Exception {
        List<Review> reviews = reviewRepository.findByStoreId(storeId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setOrderId(review.getOrderId());
        dto.setStoreId(review.getStoreId());
        dto.setUserId(review.getUserId());
        dto.setUserName(review.getUserName());
        dto.setUserAvatarUrl(review.getUserAvatarUrl());
        dto.setStarRating(review.getStarRating());
        dto.setComment(review.getComment());
        dto.setImageUrls(review.getImageUrls());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}
