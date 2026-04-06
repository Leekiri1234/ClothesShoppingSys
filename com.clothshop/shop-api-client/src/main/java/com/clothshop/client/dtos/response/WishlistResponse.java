package com.clothshop.client.dtos.response;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WishlistResponse {
    private List<WishlistItemResponse> items;
    private int totalItems;
}
