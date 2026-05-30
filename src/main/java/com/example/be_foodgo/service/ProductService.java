package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.FeaturedProductResponse;
import com.example.be_foodgo.dto.FeaturedProductResponse.OptionDTO;
import com.example.be_foodgo.dto.FeaturedProductResponse.OptionGroupDTO;
import com.example.be_foodgo.dto.PaginationInfo;
import com.example.be_foodgo.dto.ProductDTO;
import com.example.be_foodgo.model.Product;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.repository.ProductRepository;
import com.example.be_foodgo.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

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
        product.setIsOutOfStock(dto.getIsOutOfStock());
        product.setIsFeatured(dto.getIsFeatured());

        if (dto.getOptionGroups() != null) {
            List<Product.ProductOptionGroup> groups = dto.getOptionGroups().stream().map(g -> {
                Product.ProductOptionGroup group = new Product.ProductOptionGroup();
                group.setName(g.getName());
                group.setIsSingleSelect(g.getIsSingleSelect());
                group.setIsSingleSelect(g.getIsSingleSelect());
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
        log.info("Found {} featured products: {}", products.size(),
                products.stream().map(p -> p.getId() + " (storeId=" + p.getStoreId() + ")").toList());

        List<String> storeIds = products.stream()
                .map(Product::getStoreId)
                .filter(storeId -> storeId != null && !storeId.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Map<String, Store> storeMap = new LinkedHashMap<>();
        for (String storeId : storeIds) {
            Store store = storeRepository.getStoreById(storeId);
            if (store != null) {
                storeMap.put(storeId, store);
                log.debug("Store loaded: id={}, name={}", storeId, store.getName());
            } else {
                log.warn("Store NOT found for storeId={}. Check if document ID '{}' exists in 'stores' collection.", storeId, storeId);
            }
        }

        List<FeaturedProductResponse> responses = products.stream().limit(limit).map(product -> {
            Store store = storeMap.get(product.getStoreId());

            // Map optionGroups sang DTO
            List<OptionGroupDTO> optionGroupDTOs = new ArrayList<>();
            if (product.getOptionGroups() != null) {
                optionGroupDTOs = product.getOptionGroups().stream().map(group -> {
                    List<OptionDTO> optionDTOs = new ArrayList<>();
                    if (group.getOptions() != null) {
                        optionDTOs = group.getOptions().stream().map(opt -> {
                            return OptionDTO.builder()
                                    .name(opt.getName())
                                    .price(opt.getPrice())
                                    .build();
                        }).collect(Collectors.toList());
                    }
                    return OptionGroupDTO.builder()
                            .name(group.getName())
                            .isSingleSelect(group.getIsSingleSelect())
                            .isSingleSelect(group.getIsSingleSelect())
                            .options(optionDTOs)
                            .build();
                }).collect(Collectors.toList());
            }

            return FeaturedProductResponse.builder()
                    .id(product.getId())
                    .storeId(product.getStoreId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .basePrice(product.getBasePrice())
                    .imageUrl(product.getImageUrl())
                    .isOutOfStock(product.getIsOutOfStock())
                    .isFeatured(product.getIsFeatured())
                    .categoryName(product.getCategoryName())
                    .optionGroups(optionGroupDTOs)
                    .storeName(store != null ? store.getName() : null)
                    .storeAvtUrl(store != null ? store.getAvtUrl() : null)
                    .rating(store != null ? store.getRating() : null)
                    .reviewCount(store != null ? store.getReviewCount() : null)
                    .isOpen(store != null ? store.isOpen() : null)
                    .deliveryTime(store != null ? store.getDeliveryTime() : null)
                    .deliveryFee(store != null ? store.getDeliveryFee() : null)
                    .address(store != null ? store.getAddress() : null)
                    .build();
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("featuredDishes", responses);
        result.put("pagination", PaginationInfo.builder()
                .limit(limit)
                .returned(responses.size())
                .total(responses.size())
                .build());

        return result;
    }
}
