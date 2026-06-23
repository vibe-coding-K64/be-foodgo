package com.example.be_foodgo.controller;

import com.example.be_foodgo.dto.CategoryDTO;
import com.example.be_foodgo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category Management", description = "Quản lý danh mục")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lấy danh sách danh mục", description = "Lấy tất cả danh mục")
    public ResponseEntity<?> getAllCategories(@RequestParam String storeId) {
        try {
            List<CategoryDTO> list = categoryService.getAllCategories(storeId);
            return ResponseEntity.ok(createResponse(true, "Success", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/system")
    @Operation(summary = "Lấy danh mục hệ thống", description = "Lấy các danh mục mặc định của hệ thống")
    public ResponseEntity<?> getSystemCategories() {
        try {
            List<CategoryDTO> list = categoryService.getSystemCategories();
            return ResponseEntity.ok(createResponse(true, "Success", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/store")
    @Operation(summary = "Lấy danh mục của cửa hàng", description = "Lấy các danh mục thuộc về một cửa hàng cụ thể")
    public ResponseEntity<?> getStoreCategories(@RequestParam String storeId) {
        try {
            List<CategoryDTO> list = categoryService.getStoreCategories(storeId);
            return ResponseEntity.ok(createResponse(true, "Success", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin danh mục", description = "Lấy chi tiết danh mục theo ID")
    public ResponseEntity<?> getCategoryById(@PathVariable String id) {
        try {
            CategoryDTO category = categoryService.getCategoryById(id);
            if (category != null) {
                return ResponseEntity.ok(createResponse(true, "Success", category));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createResponse(false, "Category not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo danh mục mới", description = "Thêm một danh mục mới")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            String id = categoryService.createCategory(categoryDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createResponse(true, "Created successfully with id: " + id, id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật danh mục", description = "Cập nhật thông tin danh mục")
    public ResponseEntity<?> updateCategory(@PathVariable String id, @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            String result = categoryService.updateCategory(id, categoryDTO);
            if ("Not found".equals(result)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponse(false, "Category not found", null));
            }
            return ResponseEntity.ok(createResponse(true, "Updated successfully", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa danh mục", description = "Xóa danh mục khỏi hệ thống")
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        try {
            String result = categoryService.deleteCategory(id);
            return ResponseEntity.ok(createResponse(true, result, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }
}
