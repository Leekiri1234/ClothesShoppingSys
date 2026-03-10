package com.clothshop.client.controllers;

import com.clothshop.client.dtos.response.CollectionResponse;
import com.clothshop.client.services.CollectionClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các yêu cầu liên quan đến Bộ sưu tập (Collections) phía khách hàng.
 */
@Controller
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionClientService collectionService;

    /**
     * Hiển thị trang chi tiết một bộ sưu tập dựa trên slug.
     * URL ví dụ: http://localhost:8080/collections/bo-suu-tap-mua-he
     */
    @GetMapping("/{slug}")
    public String getCollectionDetail(@PathVariable String slug, Model model) {
        // 1. Gọi service lấy dữ liệu (đã được map sang CollectionResponse)
        CollectionResponse collection = collectionService.getCollectionBySlug(slug);

        // 2. Đẩy dữ liệu sang cho Thymeleaf
        model.addAttribute("collection", collection);
        model.addAttribute("pageTitle", collection.getName());

        // 3. Trả về đường dẫn file HTML (sẽ tạo ở bước tiếp theo)
        return "client/collections/detail";
    }

    /**
     * (Tùy chọn) Hiển thị trang danh sách tất cả các bộ sưu tập đang có.
     * URL: http://localhost:8080/collections
     */
    @GetMapping
    public String getAllCollections(Model model) {
        model.addAttribute("collections", collectionService.getAllActiveCollections());
        return "client/collections/list";
    }
}