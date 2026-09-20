package com.ibm.capstone.ecom.dto.response;

import com.ibm.capstone.ecom.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class ProductResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stockQuantity;
    private String imageUrl;
    private String publisher;
    private Integer publicationYear;
    private Integer tentativeDeliveryDays;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Product.ProductStatus status;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
}
