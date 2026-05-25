package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.SearchResultResponse;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
@Tag(name = "Tim kiem", description = "API tim kiem mon an va quan an cho phan he Khach hang")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @Operation(
            summary = "Tim kiem mon an va quan an",
            description = "Tim kiem mon an hoac quan an theo tu khoa, loc theo khoang cach toi da 10km tu vi tri nguoi dung, luu lich su tim kiem va sap xep ket qua."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tim kiem thanh cong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SearchResultSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Tham so dau vao khong hop le",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Loi he thong",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<SearchResultResponse>>> timKiem(
            @Parameter(description = "Tu khoa tim kiem (ten mon an hoac ten quan an)", required = true)
            @RequestParam("query") String query,

            @Parameter(description = "Vi do cua dia chi giao hang", example = "10.8500")
            @RequestParam("userLat") Double userLat,

            @Parameter(description = "Kinh do cua dia chi giao hang", example = "106.7900")
            @RequestParam("userLng") Double userLng,

            @Parameter(description = "Chieu sap xep: priceAsc (gia tang dan), priceDesc (gia giam dan), ratingDesc (danh gia giam dan)", example = "ratingDesc")
            @RequestParam(value = "sortBy", required = false) String sortBy,

            @Parameter(description = "ID nguoi dung de luu lich su tim kiem (tuy chon)", example = "user_001")
            @RequestParam(value = "userId", required = false) String userId
    ) {
        log.info("Nhan yeu cau tim kiem - query: '{}', userLat: {}, userLng: {}, sortBy: '{}', userId: '{}'",
                query, userLat, userLng, sortBy, userId);

        if (query == null || query.trim().isEmpty()) {
            log.warn("Tu khoa tim kiem rong hoac null");
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, "Tu khoa tim kiem khong duoc de trong.")
            );
        }

        if (userLat == null || userLng == null) {
            log.warn("Toa do nguoi dung khong hop le - userLat: {}, userLng: {}", userLat, userLng);
            return ResponseEntity.badRequest().body(
                    ApiResponse.thatError(400, "Toa do nguoi dung (userLat, userLng) khong hop le.")
            );
        }

        try {
            List<SearchResultResponse> ketQua = searchService.search(query, userLat, userLng, sortBy, userId);
            log.info("Tim kiem thanh cong - tra ve {} ket qua", ketQua.size());
            return ResponseEntity.ok(
                    ApiResponse.thatSuccess(ketQua,
                            ketQua.isEmpty()
                                    ? "Khong tim thay mon an hoac quan an nao phu hop."
                                    : "Tim thay " + ketQua.size() + " ket qua phu hop.")
            );
        } catch (ExecutionException | InterruptedException e) {
            log.error("Loi khi truy van Firestore trong qua trinh tim kiem: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(
                    ApiResponse.thatError(500, "Loi he thong khi tim kiem. Vui long thu lai sau.")
            );
        }
    }

    @Schema(name = "SearchResultSchema", description = "Schema cho ApiResponse chua danh sach SearchResultResponse")
    public static class SearchResultSchema extends ApiResponse<List<SearchResultResponse>> {
    }
}
