package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.ProductDTO;
import com.example.be_foodgo.model.Product;
import com.example.be_foodgo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@Tag(name = "Product Management", description = "Quản lý món ăn của cửa hàng")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(summary = "Lấy danh sách món ăn", description = "Lấy tất cả món ăn của một cửa hàng")
    public ResponseEntity<Map<String, Object>> getAllProducts(@RequestParam String storeId) {
        try {
            List<Product> products = productService.getAllProducts(storeId);
            return ResponseEntity.ok(createResponse(true, "Lấy danh sách thành công", products));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết món ăn", description = "Lấy thông tin món ăn theo ID")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable String id) {
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                return ResponseEntity.ok(createResponse(true, "Lấy thành công", product));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponse(false, "Không tìm thấy món ăn", null));
            }
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping
    @Operation(summary = "Tạo món ăn mới", description = "Thêm một món ăn mới vào cửa hàng")
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            String updateTime = productService.createProduct(productDTO);
            return ResponseEntity.ok(createResponse(true, "Tạo món ăn thành công lúc: " + updateTime, null));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin món ăn", description = "Cập nhật thông tin món ăn theo ID")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable String id, @Valid @RequestBody ProductDTO productDTO) {
        try {
            String updateTime = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(createResponse(true, "Cập nhật thành công lúc: " + updateTime, null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse(false, e.getMessage(), null));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa món ăn", description = "Xóa một món ăn khỏi hệ thống")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable String id) {
        try {
            String updateTime = productService.deleteProduct(id);
            return ResponseEntity.ok(createResponse(true, "Xóa thành công lúc: " + updateTime, null));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/featured")
    @Operation(summary = "Lấy danh sách món ăn nổi bật", description = "Lấy danh sách các món ăn nổi bật (featured)")
    public ResponseEntity<Map<String, Object>> getFeaturedProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String categoryId) {
        try {
            Map<String, Object> response = productService.getFeaturedProducts(limit, categoryId);
            return ResponseEntity.ok(response);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}
