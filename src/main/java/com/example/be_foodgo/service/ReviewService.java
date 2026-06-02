package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.NotificationDTO;
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

    @Autowired
    private NotificationService notificationService;

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

        // Thông báo cho Quán ăn
        NotificationDTO merchantNotif = new NotificationDTO();
        merchantNotif.setTitle("Đánh giá mới");
        String userName = (request.getUserName() != null && !request.getUserName().trim().isEmpty()) ? request.getUserName() : "Khách hàng";
        String orderCode = getOrderCodeDisplay(donHang);
        merchantNotif.setBody(userName + " vừa đánh giá " + request.getStarRating() + " sao cho đơn hàng " + orderCode + ".");
        merchantNotif.setType(3); // 3 = review
        merchantNotif.setOrderId(request.getOrderId());
        merchantNotif.setReferenceId(reviewId);
        notificationService.notifyMerchantByStoreId(request.getStoreId(), merchantNotif);

        return convertToDTO(review);
    }

    private String getOrderCodeDisplay(Order order) {
        if (order.getCode() != null && !order.getCode().trim().isEmpty()) {
            return order.getCode();
        }
        if (order.getId() != null && order.getId().length() >= 6) {
            return order.getId().substring(order.getId().length() - 6).toUpperCase();
        }
        return "ORDER";
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
                .map(review -> {
                    Order order = null;
                    try {
                        order = orderRepository.findById(review.getOrderId());
                    } catch (Exception e) {
                        log.error("Loi lay thong tin don hang", e);
                    }
                    return convertToDTO(review, order);
                })
                .collect(Collectors.toList());
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

    public List<ReviewDTO> layTatCaDanhGia() throws Exception {
        List<Review> reviews = reviewRepository.findAllReviews();
        return reviews.stream()
                .map(review -> {
                    Order order = null;
                    try {
                        order = orderRepository.findById(review.getOrderId());
                    } catch (Exception e) {
                        log.error("Loi lay thong tin don hang", e);
                    }
                    return convertToDTO(review, order);
                })
                .collect(Collectors.toList());
    }

    public ReviewDTO convertToDTO(Review review) {
        return convertToDTO(review, null);
    }

    private ReviewDTO convertToDTO(Review review, Order order) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setOrderId(review.getOrderId());
        if (order != null) {
            dto.setOrderCode(order.getCode());
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                String itemsStr = order.getItems().stream()
                        .map(item -> item.getName() + " (x" + item.getQuantity() + ")")
                        .collect(Collectors.joining(", "));
                dto.setOrderItems(itemsStr);
            }
        }
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
