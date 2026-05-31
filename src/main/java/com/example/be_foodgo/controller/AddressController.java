package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.AddressRequest;
import com.example.be_foodgo.exception.ApiResponse;
import com.example.be_foodgo.model.Address;
import com.example.be_foodgo.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@Tag(name = "Address", description = "API quan ly dia chi giao hang cho phan he Khach hang")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @Operation(
            summary = "Them dia chi moi",
            description = "Them mot dia chi giao hang moi cho khach hang. " +
                    "Neu request gui isDefault = true, he thong se tu dong bo mac dinh cac dia chi cu. " +
                    "Dia chi duoc luu vao sub-collection customer_profiles/{userId}/addresses."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Them dia chi thanh cong",
                    content = @Content(schema = @Schema(implementation = AddressSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu dau vao khong hop le - thieu truong bat buoc hoac dinh dang sai",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Loi he thong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<Address>> themDiaChi(
            @Valid
            @RequestBody
            @Parameter(description = "Thong tin dia chi can them")
            AddressRequest request
    ) {
        log.info("Nhan yeu cau them dia chi - userId: {}, nhan: {}, isDefault: {}",
                request.getUserId(), request.getName(), request.getIsDefault());

        Address address = addressService.themDiaChi(request);

        log.info("Them dia chi [{}] thanh cong cho nguoi dung {}", address.getId(), request.getUserId());
        return ResponseEntity.ok(ApiResponse.thatSuccess(address, "Da them dia chi thanh cong."));
    }

    @GetMapping
    @Operation(
            summary = "Lay danh sach dia chi",
            description = "Lay tat ca dia chi giao hang cua khach hang tu sub-collection " +
                    "customer_profiles/{userId}/addresses."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay danh sach dia chi thanh cong",
                    content = @Content(schema = @Schema(implementation = AddressListSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Loi he thong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<List<Address>>> layDanhSachDiaChi(
            @RequestParam
            @Parameter(description = "ID nguoi dung khach hang", example = "user_001")
            String userId
    ) {
        log.info("Nhan yeu cau lay danh sach dia chi - userId: {}", userId);

        List<Address> addresses = addressService.layTatCaDiaChi(userId);

        log.info("Da lay {} dia chi cua nguoi dung {}", addresses.size(), userId);
        return ResponseEntity.ok(ApiResponse.thatSuccess(addresses, "Da lay danh sach dia chi thanh cong."));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lay thong tin mot dia chi",
            description = "Lay thong tin chi tiet cua mot dia chi giao hang cu the theo addressId."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lay dia chi thanh cong",
                    content = @Content(schema = @Schema(implementation = AddressSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Dia chi khong ton tai",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Loi he thong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<Address>> layMotDiaChi(
            @PathVariable
            @Parameter(description = "ID dia chi can lay", example = "addr_001")
            String id,
            @RequestParam
            @Parameter(description = "ID nguoi dung khach hang", example = "user_001")
            String userId
    ) {
        log.info("Nhan yeu cau lay dia chi - addressId: {}, userId: {}", id, userId);

        Address address = addressService.layMotDiaChi(userId, id);

        log.info("Da lay dia chi [{}] thanh cong", id);
        return ResponseEntity.ok(ApiResponse.thatSuccess(address, "Da lay thong tin dia chi thanh cong."));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Cap nhat dia chi",
            description = "Cap nhat thong tin dia chi giao hang cua khach hang. " +
                    "Neu isDefault = true, he thong se tu dong bo mac dinh cac dia chi cu."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cap nhat dia chi thanh cong",
                    content = @Content(schema = @Schema(implementation = AddressSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Du lieu dau vao khong hop le",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Dia chi khong ton tai",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Loi he thong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<Address>> suaDiaChi(
            @PathVariable
            @Parameter(description = "ID dia chi can cap nhat", example = "addr_001")
            String id,
            @Valid
            @RequestBody
            @Parameter(description = "Thong tin dia chi cap nhat")
            AddressRequest request
    ) {
        log.info("Nhan yeu cau cap nhat dia chi - addressId: {}, userId: {}, nhan: {}",
                id, request.getUserId(), request.getName());

        Address address = addressService.suaDiaChi(request.getUserId(), id, request);

        log.info("Cap nhat dia chi [{}] thanh cong", id);
        return ResponseEntity.ok(ApiResponse.thatSuccess(address, "Da cap nhat dia chi thanh cong."));
    }

    @PutMapping("/{id}/default")
    @Operation(
            summary = "Dat dia chi lam mac dinh",
            description = "Dat mot dia chi giao hang lam dia chi mac dinh cho khach hang. " +
                    "He thong se quet tat ca dia chi cua nguoi dung, bo flag isDefault cua cac dia chi cu, " +
                    "sau do dat flag isDefault = true cho dia chi duoc yeu cau."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dat dia chi mac dinh thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Dia chi khong ton tai",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Loi he thong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> datDiaChiMacDinh(
            @PathVariable
            @Parameter(description = "ID dia chi can dat lam mac dinh", example = "addr_001")
            String id,
            @RequestParam
            @Parameter(description = "ID nguoi dung khach hang", example = "user_001")
            String userId
    ) {
        log.info("Nhan yeu cau dat dia chi [{}] lam mac dinh - userId: {}", id, userId);

        addressService.datDiaChiMacDinh(userId, id);

        log.info("Dat dia chi [{}] lam mac dinh thanh cong", id);
        return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Da dat dia chi lam mac dinh thanh cong."));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xoa dia chi",
            description = "Xoa mot dia chi giao hang cua khach hang. " +
                    "Phuong thuc nay la idempotent - tra ve thanh cong ke ca khi dia chi khong ton tai."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xoa dia chi thanh cong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Loi he thong",
                    content = @Content(schema = @Schema(implementation = ApiResponseSchema.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> xoaDiaChi(
            @PathVariable
            @Parameter(description = "ID dia chi can xoa", example = "addr_001")
            String id,
            @RequestParam
            @Parameter(description = "ID nguoi dung khach hang", example = "user_001")
            String userId
    ) {
        log.info("Nhan yeu cau xoa dia chi - addressId: {}, userId: {}", id, userId);

        addressService.xoaDiaChi(userId, id);

        log.info("Xoa dia chi [{}] thanh cong", id);
        return ResponseEntity.ok(ApiResponse.thatSuccess(null, "Da xoa dia chi thanh cong."));
    }

    @Schema(name = "AddressSchema", description = "Schema cho Address trong phan hoi thanh cong")
    public static class AddressSchema extends Address {
    }

    @Schema(name = "AddressListSchema", description = "Schema cho danh sach Address")
    public static class AddressListSchema {
        private List<Address> addresses;
    }

    @Schema(name = "ApiResponseSchema", description = "Schema co ban cho ApiResponse")
    public static class ApiResponseSchema extends ApiResponse<Void> {
    }
}
