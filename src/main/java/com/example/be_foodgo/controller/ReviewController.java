package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.BatchReviewRequest;
import com.example.be_foodgo.dto.BatchReviewResponse;
import com.example.be_foodgo.dto.ReviewDTO;
import com.example.be_foodgo.dto.ReviewRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Danh gia", description = "Cac API lien quan den danh gia cua khach hang")
public class ReviewController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/batch")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Tạo đánh giá hàng loạt (multipart/form-data)",
            description = "Cho phép khách hàng gửi nhiều đánh giá cùng lúc kèm ảnh chụp món ăn. " +
                    "Request gồm part 'metadata' (JSON) và part 'images' (file ảnh). " +
                    "Chỉ cho phép đánh giá khi đơn hàng ở trạng thái [Hoàn thành] (status = 3). " +
                    "Sản phẩm đã được đánh giá sẽ bị bỏ qua."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tạo đánh giá hàng loạt thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc đơn hàng không cho phép đánh giá",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Người dùng không phải chủ sở hữu đơn hàng",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy đơn hàng",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<BatchReviewResponse>> taoDanhGiaBatch(
            @Parameter(description = "JSON metadata chứa thông tin đánh giá", required = true)
            @RequestPart("metadata") String metadataJson,
            @Parameter(description = "Danh sách ảnh đính kèm (JPEG, PNG, WEBP, tối đa 10MB/ảnh)")
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws Exception {

        BatchReviewRequest request = objectMapper.readValue(metadataJson, BatchReviewRequest.class);
        ReviewService.BatchReviewResult result = reviewService.taoDanhGiaBatch(request, images);

        BatchReviewResponse response = BatchReviewResponse.builder()
                .count(result.count)
                .reviewIds(result.reviewIds)
                .build();
        return ResponseEntity.ok(ApiResponse.thatSuccess(response, "Đánh giá đã được gửi thành công"));
    }
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Tao danh gia",
            description = "Cho phep khach hang tao mot danh gia cho don hang da nhan. Chi cho phep danh gia khi don hang o trang thai [Hoan thanh] (status = 3). Mot don hang chi duoc phep danh gia mot lan."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tao danh gia thanh cong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu khong hop le hoac don hang khong cho phep danh gia (trang thai khac 3 hoac da danh gia roi)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Nguoi dung khong phai chu so huu don hang",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Khong tim thay don hang",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<ReviewDTO>> taoDanhGia(
            @Parameter(description = "Thong tin danh gia tu khach hang", required = true)
            @Valid @RequestBody ReviewRequest request) throws Exception {

        ReviewDTO reviewDTO = reviewService.taoDanhGia(request);
        return ResponseEntity.ok(ApiResponse.thatSuccess(reviewDTO, "Tao danh gia thanh cong."));
    }

    @GetMapping
    @Operation(
            summary = "Lay danh sach danh gia theo cua hang",
            description = "Lay tat ca danh gia cua mot cua hang theo storeId."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay danh sach danh gia thanh cong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> layDanhSachDanhGia(
            @Parameter(description = "ID cua hang", required = true)
            @RequestParam String storeId) throws Exception {

        List<ReviewDTO> reviews = reviewService.layDanhSachDanhGiaCuaHang(storeId);
        return ResponseEntity.ok(ApiResponse.thatSuccess(reviews, "Lay danh sach danh gia thanh cong."));
    }

    @GetMapping("/product/{foodId}")
    @Operation(
            summary = "Lay danh sach danh gia theo san pham",
            description = "Lay tat ca danh gia cua mot san pham theo foodId."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay danh sach danh gia thanh cong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> layDanhSachDanhGiaSanPham(
            @Parameter(description = "ID san pham", required = true)
            @PathVariable String foodId) throws Exception {

        List<ReviewDTO> reviews = reviewService.layDanhSachDanhGiaSanPham(foodId);
        return ResponseEntity.ok(ApiResponse.thatSuccess(reviews, "Lay danh sach danh gia san pham thanh cong."));
    }

    @PutMapping("/{id}/reply")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Phản hồi đánh giá", description = "Cho phép chủ gian hàng phản hồi đánh giá của khách hàng")
    public ResponseEntity<ApiResponse<ReviewDTO>> replyReview(
            @PathVariable String id,
            @Valid @RequestBody com.example.be_foodgo.dto.ReviewReplyRequest request) throws Exception {
        ReviewDTO reviewDTO = reviewService.replyReview(id, request.getReplyComment());
        return ResponseEntity.ok(ApiResponse.thatSuccess(reviewDTO, "Đã phản hồi đánh giá."));
    }
}
