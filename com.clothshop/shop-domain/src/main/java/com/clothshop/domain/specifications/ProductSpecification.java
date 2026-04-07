package com.clothshop.domain.specifications;

import com.clothshop.domain.entities.product.Category;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.entities.product.ProductVariant;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterProducts(String keyword, List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice, List<String> colors, List<String> sizes) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Keyword search (Name, Desc, and SKU)
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("productName").as(String.class)), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("productDesc").as(String.class)), pattern);

                Join<Product, ProductVariant> keywordVariantJoin = root.join("variants", JoinType.LEFT);
                Predicate skuMatch = cb.like(cb.lower(keywordVariantJoin.get("sku").as(String.class)), pattern);

                Join<Product, Category> keywordCategoryJoin = root.join("category", JoinType.LEFT);
                Predicate categoryMatch = cb.like(cb.lower(keywordCategoryJoin.get("categoryName").as(String.class)), pattern);

                predicates.add(cb.or(nameMatch, descMatch, skuMatch, categoryMatch));
            }

            // 2. Category filtering - Include hierarchy (Parent or exact exact match)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
                Predicate exactCategory = categoryJoin.get("id").in(categoryIds);
                Predicate childCategory = categoryJoin.get("parent").get("id").in(categoryIds);
                predicates.add(cb.or(exactCategory, childCategory));
            }

            // 3. Variant filters (Price, Color, Size)
            if (minPrice != null || maxPrice != null || (colors != null && !colors.isEmpty()) || (sizes != null && !sizes.isEmpty())) {
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);

                if (minPrice != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("retailPrice"), minPrice));
                }

                if (maxPrice != null) {
                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("retailPrice"), maxPrice));
                }

                if (colors != null && !colors.isEmpty()) {
                    predicates.add(variantJoin.get("color").in(colors));
                }

                if (sizes != null && !sizes.isEmpty()) {
                    predicates.add(variantJoin.get("sizeValue").in(sizes));
                }
            }

            // Must be active
            predicates.add(cb.isTrue(root.get("isActive")));

            // Distinct is needed when joining collections to return unique products
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
