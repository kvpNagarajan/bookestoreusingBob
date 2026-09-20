package com.ibm.capstone.ecom.service;

import com.ibm.capstone.ecom.dto.response.ProductResponse;
import com.ibm.capstone.ecom.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    Page<ProductResponse> getAllProducts(Pageable pageable);

    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);

    Page<ProductResponse> searchProducts(String keyword, Pageable pageable);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getRelatedProducts(Long productId);

    List<ProductResponse> getLatestProducts(int limit);

    ProductResponse createProduct(Product product);

    ProductResponse updateProduct(Long id, Product product);

    void deleteProduct(Long id);
}
