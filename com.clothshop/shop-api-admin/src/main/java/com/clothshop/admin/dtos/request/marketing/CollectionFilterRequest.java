package com.clothshop.admin.dtos.request.marketing;

import lombok.Data;

@Data
public class CollectionFilterRequest {
    private String keyword;
    private Boolean status;
}

