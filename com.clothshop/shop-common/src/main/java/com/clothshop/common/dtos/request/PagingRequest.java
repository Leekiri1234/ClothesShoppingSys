package com.clothshop.common.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // @Data đã bao gồm @Getter và @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagingRequest {
    private int pageNumber = 0;

    private int pageSize = 10;

    private String sortBy;
    private String sortDirection = "DESC";

    public void validate() {
        if (pageNumber < 0) pageNumber = 0;
        if (pageSize <= 0) pageSize = 10;
        if (sortDirection == null) sortDirection = "DESC";
    }
}