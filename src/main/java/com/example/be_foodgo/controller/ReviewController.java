package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.ReviewDTO;
import com.example.be_foodgo.dto.ReviewRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.ReviewService;
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

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Danh gia", description = "Cac API lien quan den danh gia cua khach hang")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
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
