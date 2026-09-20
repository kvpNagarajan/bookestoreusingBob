package com.ibm.capstone.ecom.repository;

import com.ibm.capstone.ecom.entity.Product;
import com.ibm.capstone.ecom.entity.Product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Always JOIN FETCH category to avoid LazyInitializationException in toResponse()
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.status = :status")
    Page<Product> findByStatus(@Param("status") ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId AND p.status = :status")
    Page<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId,
                                            @Param("status") ProductStatus status,
                                            Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.status = 'ACTIVE' AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.publisher) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.status = 'ACTIVE' AND p.category.id = :categoryId " +
           "ORDER BY p.averageRating DESC")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    List<Product> findLatestProducts(Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    java.util.Optional<Product> findByIdWithCategory(@Param("id") Long id);

    boolean existsByIsbn(String isbn);
}
