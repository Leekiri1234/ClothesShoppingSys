package com.clothshop.admin.dtos.request.marketing;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class FeaturedProductListRequest {
    private List<FeaturedProductRequest> featuredProducts;
}