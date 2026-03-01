package com.clothshop.common.dtos.response;

import lombok.*;

import java.util.List;

/**
 * Paginated response wrapper for API responses.
 * Contains paginated data along with pagination metadata.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
    private boolean first;
    private boolean last;
}
