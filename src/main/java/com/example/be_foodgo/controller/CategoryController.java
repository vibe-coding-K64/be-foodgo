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
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllCategories(@RequestParam String storeId) {
        try {
            List<CategoryDTO> list = categoryService.getAllCategories(storeId);
            return ResponseEntity.ok(createResponse(true, "Success", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
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
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        try {
            String result = categoryService.deleteCategory(id);
            return ResponseEntity.ok(createResponse(true, result, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, e.getMessage(), null));
        }
    }

    // Helper method de tao response chuan
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
