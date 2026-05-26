package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.FeaturedProductResponse;
import com.example.be_foodgo.dto.PaginationInfo;
import com.example.be_foodgo.dto.ProductDTO;
import com.example.be_foodgo.model.Product;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.repository.ProductRepository;
import com.example.be_foodgo.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    public List<Product> getAllProducts(String storeId) throws ExecutionException, InterruptedException {
        return productRepository.findAll(storeId);
    }

    public Product getProductById(String id) throws ExecutionException, InterruptedException {
        return productRepository.findById(id);
    }

    public String createProduct(ProductDTO dto) throws ExecutionException, InterruptedException {
        Product product = new Product();
        mapDtoToEntity(dto, product);
        product.setId(generateNextProductId());
        return productRepository.save(product);
    }

    private String generateNextProductId() throws ExecutionException, InterruptedException {
        List<String> ids = productRepository.getAllProductIds();
        int maxId = 0;
        for (String id : ids) {
            if (id != null && id.startsWith("prod_")) {
                try {
                    int num = Integer.parseInt(id.substring(5));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return String.format("prod_%03d", maxId + 1);
    }

    public String updateProduct(String id, ProductDTO dto) throws ExecutionException, InterruptedException {
        Product existing = productRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("Món ăn không tồn tại");
        }
        mapDtoToEntity(dto, existing);
        existing.setId(id);
        return productRepository.save(existing);
    }

    public String deleteProduct(String id) throws ExecutionException, InterruptedException {
        return productRepository.delete(id);
    }

    private void mapDtoToEntity(ProductDTO dto, Product product) {
        product.setStoreId(dto.getStoreId());
        product.setCategoryId(dto.getCategoryId());
        product.setCategoryName(dto.getCategoryName());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setImageUrl(dto.getImageUrl());
        product.setOutOfStock(dto.isOutOfStock());
        product.setFeatured(dto.isFeatured());

        if (dto.getOptionGroups() != null) {
            List<Product.ProductOptionGroup> groups = dto.getOptionGroups().stream().map(g -> {
                Product.ProductOptionGroup group = new Product.ProductOptionGroup();
                group.setName(g.getName());
                group.setRequired(g.isRequired());
                group.setMaxChoices(g.getMaxChoices());
                if (g.getOptions() != null) {
                    group.setOptions(g.getOptions().stream().map(o -> {
                        Product.ProductOption opt = new Product.ProductOption();
                        opt.setName(o.getName());
                        opt.setPrice(o.getPrice());
                        return opt;
                    }).collect(Collectors.toList()));
                }
                return group;
            }).collect(Collectors.toList());
            product.setOptionGroups(groups);
        } else {
            product.setOptionGroups(null);
        }
    }

    public Map<String, Object> getFeaturedProducts(int limit, String categoryId) throws ExecutionException, InterruptedException {
        List<Product> products = productRepository.findFeatured(categoryId);

        List<String> storeIds = products.stream()
                .map(Product::getStoreId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Store> storeMap = new LinkedHashMap<>();
        for (String storeId : storeIds) {
            Store store = storeRepository.getStoreById(storeId);
            if (store != null) {
                storeMap.put(storeId, store);
            }
        }

        List<FeaturedProductResponse> responses = products.stream().limit(limit).map(product -> {
            FeaturedProductResponse.StoreSummary storeSummary = null;
            Store store = storeMap.get(product.getStoreId());
            if (store != null) {
                storeSummary = FeaturedProductResponse.StoreSummary.builder()
                        .id(store.getId())
                        .name(store.getName())
                        .rating(store.getRating())
                        .avtUrl(store.getAvtUrl())
                        .deliveryFee(store.getDeliveryFee())
                        .deliveryTime(store.getDeliveryTime())
                        .build();
            }
            return FeaturedProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .basePrice(product.getBasePrice())
                    .imageUrl(product.getImageUrl())
                    .isOutOfStock(product.isOutOfStock())
                    .store(storeSummary)
                    .build();
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", responses);
        result.put("pagination", PaginationInfo.builder()
                .limit(limit)
                .returned(responses.size())
                .total(responses.size())
                .build());

        return result;
    }
}
