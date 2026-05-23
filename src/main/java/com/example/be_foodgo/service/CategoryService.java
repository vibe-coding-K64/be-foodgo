package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.CategoryDTO;
import com.example.be_foodgo.model.Category;
import com.example.be_foodgo.repository.CategoryRepository;
import com.google.cloud.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories(String storeId) throws ExecutionException, InterruptedException {
        List<Category> categories = categoryRepository.findAll(storeId);
        List<CategoryDTO> dtos = new ArrayList<>();
        for (Category category : categories) {
            dtos.add(mapToDTO(category));
        }
        return dtos;
    }

    public CategoryDTO getCategoryById(String id) throws ExecutionException, InterruptedException {
        Category category = categoryRepository.findById(id);
        if (category != null) {
            return mapToDTO(category);
        }
        return null;
    }

    public String createCategory(CategoryDTO dto) throws ExecutionException, InterruptedException {
        Category existing = categoryRepository.findByOrder(dto.getStoreId(), dto.getOrder());
        if (existing != null) {
            throw new IllegalArgumentException("Vị trí " + dto.getOrder() + " đã tồn tại. Vui lòng chọn vị trí khác.");
        }

        Category category = new Category();
        category.setId(generateNextCategoryId());
        category.setStoreId(dto.getStoreId());
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setOrder(dto.getOrder());
        category.setImageUrl(dto.getImageUrl());
        category.setCreatedAt(Timestamp.now());
        category.setUpdatedAt(Timestamp.now());
        
        return categoryRepository.save(category);
    }

    private String generateNextCategoryId() throws ExecutionException, InterruptedException {
        List<String> ids = categoryRepository.getAllCategoryIds();
        int maxId = 0;
        for (String id : ids) {
            if (id != null && id.startsWith("cate_")) {
                try {
                    int num = Integer.parseInt(id.substring(5));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return String.format("cate_%03d", maxId + 1);
    }

    public String updateCategory(String id, CategoryDTO dto) throws ExecutionException, InterruptedException {
        Category category = categoryRepository.findById(id);
        if (category != null) {
            if (!category.getOrder().equals(dto.getOrder())) {
                Category existing = categoryRepository.findByOrder(category.getStoreId(), dto.getOrder());
                if (existing != null && !existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Vị trí " + dto.getOrder() + " đã tồn tại. Vui lòng chọn vị trí khác.");
                }
            }

            category.setName(dto.getName());
            category.setIcon(dto.getIcon());
            category.setOrder(dto.getOrder());
            category.setImageUrl(dto.getImageUrl());
            category.setUpdatedAt(Timestamp.now());
            return categoryRepository.save(category);
        }
        return "Not found";
    }

    public String deleteCategory(String id) throws ExecutionException, InterruptedException {
        return categoryRepository.delete(id);
    }

    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setStoreId(category.getStoreId());
        dto.setName(category.getName());
        dto.setIcon(category.getIcon());
        dto.setOrder(category.getOrder());
        dto.setImageUrl(category.getImageUrl());
        if (category.getCreatedAt() != null) {
            dto.setCreatedAt(category.getCreatedAt().toDate());
        }
        if (category.getUpdatedAt() != null) {
            dto.setUpdatedAt(category.getUpdatedAt().toDate());
        }
        return dto;
    }
}
