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

    public List<CategoryDTO> getSystemCategories() throws ExecutionException, InterruptedException {
        List<Category> categories = categoryRepository.findAllSystemCategories();
        List<CategoryDTO> dtos = new ArrayList<>();
        for (Category category : categories) {
            dtos.add(mapToDTO(category));
        }
        return dtos;
    }

    public List<CategoryDTO> getStoreCategories(String storeId) throws ExecutionException, InterruptedException {
        List<Category> categories = categoryRepository.findAllStoreCategories(storeId);
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
        boolean isSystem = dto.getStoreId() == null || dto.getStoreId().isEmpty();

        if (isSystem) {
            Category existing = categoryRepository.findSystemCategoryByOrder(dto.getOrder());
            if (existing != null) {
                throw new IllegalArgumentException("Vị trí " + dto.getOrder() + " đã tồn tại trong danh mục hệ thống. Vui lòng chọn vị trí khác.");
            }
        } else {
            Category existing = categoryRepository.findStoreCategoryByOrder(dto.getStoreId(), dto.getOrder());
            if (existing != null) {
                throw new IllegalArgumentException("Vị trí " + dto.getOrder() + " đã tồn tại trong danh mục cửa hàng. Vui lòng chọn vị trí khác.");
            }
        }

        Category category = new Category();
        category.setId(generateNextCategoryId(isSystem));
        category.setStoreId(dto.getStoreId());
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setOrder(dto.getOrder());
        category.setImageUrl(dto.getImageUrl());
        category.setCreatedAt(Timestamp.now());
        category.setUpdatedAt(Timestamp.now());

        return categoryRepository.save(category);
    }

    private String generateNextCategoryId(boolean isSystem) throws ExecutionException, InterruptedException {
        List<String> ids = isSystem
                ? categoryRepository.getAllSystemCategoryIds()
                : categoryRepository.getAllStoreCategoryIds();
        int maxId = 0;
        String prefix = isSystem ? "syscate_" : "stocate_";
        for (String id : ids) {
            if (id != null && id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(prefix.length()));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return String.format("%s%03d", prefix, maxId + 1);
    }

    public String updateCategory(String id, CategoryDTO dto) throws ExecutionException, InterruptedException {
        Category category = categoryRepository.findById(id);
        if (category != null) {
            boolean newIsSystem = dto.getStoreId() == null || dto.getStoreId().isEmpty();

            if (!category.getOrder().equals(dto.getOrder())) {
                Category existing;
                if (newIsSystem) {
                    existing = categoryRepository.findSystemCategoryByOrder(dto.getOrder());
                } else {
                    existing = categoryRepository.findStoreCategoryByOrder(dto.getStoreId(), dto.getOrder());
                }
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
