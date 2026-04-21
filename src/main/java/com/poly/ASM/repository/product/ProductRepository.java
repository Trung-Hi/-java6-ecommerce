package com.poly.ASM.repository.product;

import com.poly.ASM.entity.product.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where p.isDelete = false")
    List<Product> findAllWithCategory();

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where p.id = :id and p.isDelete = false")
    Optional<Product> findByIdWithCategory(@Param("id") Integer id);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByIsDeleteFalse(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    List<Product> findTop8ByIsDeleteFalseOrderByCreateDateDesc();

    @EntityGraph(attributePaths = "category")
    List<Product> findTop8ByDiscountGreaterThanAndIsDeleteFalseOrderByDiscountDesc(BigDecimal discount);

    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryIdAndIsDeleteFalse(String categoryId);

    @EntityGraph(attributePaths = "category")
    List<Product> findTop4ByCategoryIdAndIdNotAndIsDeleteFalse(String categoryId, Integer id);

    @Query("""
            select p
            from Product p
            join p.orderDetails od
            join fetch p.category c
            where p.isDelete = false
            group by p, c.id, c.name
            order by sum(od.quantity) desc
            """)
    List<Product> findBestSeller(Pageable pageable);

    @Query("""
            select p
            from Product p
            join fetch p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and p.isDelete = false
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) >= :minPrice)
              and (:maxPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) <= :maxPrice)
            """)
    List<Product> search(@Param("keyword") String keyword,
                         @Param("categoryId") String categoryId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);

    @Query(value = """
            select p
            from Product p
            join fetch p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and p.isDelete = false
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) >= :minPrice)
              and (:maxPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) <= :maxPrice)
            """, countQuery = """
            select count(p)
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and p.isDelete = false
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) >= :minPrice)
              and (:maxPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) <= :maxPrice)
            """)
    Page<Product> searchPage(@Param("keyword") String keyword,
                             @Param("categoryId") String categoryId,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             Pageable pageable);

    @Query("""
            select p
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and p.isDelete = false
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) >= :minPrice)
              and (:maxPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) <= :maxPrice)
            order by (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) asc
            """)
    List<Product> searchOrderByPriceAsc(@Param("keyword") String keyword,
                                        @Param("categoryId") String categoryId,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice);

    @Query("""
            select p
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and p.isDelete = false
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) >= :minPrice)
              and (:maxPrice is null or (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) <= :maxPrice)
            order by (p.price - (p.price * coalesce(p.discount, 0) / 100.0)) desc
            """)
    List<Product> searchOrderByPriceDesc(@Param("keyword") String keyword,
                                         @Param("categoryId") String categoryId,
                                         @Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice);
}
