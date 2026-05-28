package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.ProductDTO;
import com.example.be_foodgo.model.Product;
import com.example.be_foodgo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

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
}
