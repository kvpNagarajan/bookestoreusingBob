package com.ibm.capstone.ecom.service.impl;

import com.ibm.capstone.ecom.dto.response.ProductResponse;
import com.ibm.capstone.ecom.entity.Product;
import com.ibm.capstone.ecom.entity.Product.ProductStatus;
import com.ibm.capstone.ecom.exception.BusinessException;
import com.ibm.capstone.ecom.exception.ResourceNotFoundException;
import com.ibm.capstone.ecom.repository.ProductRepository;
import com.ibm.capstone.ecom.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatus(categoryId, ProductStatus.ACTIVE, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable).map(this::toResponse);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return toResponse(findActive(id));
    }

    @Override
    public List<ProductResponse> getRelatedProducts(Long productId) {
        Product product = findActive(productId);
        if (product.getCategory() == null) {
            return List.of();
        }
        return productRepository
                .findRelatedProducts(product.getCategory().getId(), PageRequest.of(0, 6))
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getLatestProducts(int limit) {
        return productRepository.findLatestProducts(PageRequest.of(0, limit))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse createProduct(Product product) {
        if (product.getIsbn() != null && productRepository.existsByIsbn(product.getIsbn())) {
            throw new BusinessException("A product with this ISBN already exists");
        }
        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, Product updated) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        existing.setTitle(updated.getTitle());
        existing.setAuthor(updated.getAuthor());
        existing.setIsbn(updated.getIsbn());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setOriginalPrice(updated.getOriginalPrice());
        existing.setStockQuantity(updated.getStockQuantity());
        existing.setImageUrl(updated.getImageUrl());
        existing.setPublisher(updated.getPublisher());
        existing.setPublicationYear(updated.getPublicationYear());
        existing.setTentativeDeliveryDays(updated.getTentativeDeliveryDays());
        existing.setCategory(updated.getCategory());
        existing.setStatus(updated.getStatus());
        return toResponse(productRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    private Product findActive(Long id) {
        return productRepository.findByIdWithCategory(id)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .author(p.getAuthor())
                .isbn(p.getIsbn())
                .description(p.getDescription())
                .price(p.getPrice())
                .originalPrice(p.getOriginalPrice())
                .stockQuantity(p.getStockQuantity())
                .imageUrl(p.getImageUrl())
                .publisher(p.getPublisher())
                .publicationYear(p.getPublicationYear())
                .tentativeDeliveryDays(p.getTentativeDeliveryDays())
                .averageRating(p.getAverageRating())
                .reviewCount(p.getReviewCount())
                .status(p.getStatus())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
