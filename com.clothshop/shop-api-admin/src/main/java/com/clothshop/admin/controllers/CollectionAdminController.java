package com.clothshop.admin.controllers;

import com.clothshop.admin.dtos.request.marketing.CollectionSaveRequest;
import com.clothshop.admin.dtos.response.marketing.CollectionResponse;
import com.clothshop.admin.services.FeaturedCollectionService;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.marketing.Collection;
import com.clothshop.domain.entities.marketing.CollectionItem;
import com.clothshop.domain.entities.product.Product;
import com.clothshop.domain.repositories.marketing.CollectionItemRepository;
import com.clothshop.domain.repositories.marketing.CollectionRepository;
import com.clothshop.domain.repositories.product.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/collections")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MARKETING_STAFF')")
public class CollectionAdminController {

    private final FeaturedCollectionService featuredCollectionService;
    private final CollectionRepository collectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final ProductRepository productRepository;

    /**
     * Hiển thị danh sách các bộ sưu tập (Có phân trang và tìm kiếm)
     */
    @GetMapping
    public String listCollections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CollectionResponse> collectionPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            collectionPage = featuredCollectionService.searchCollectionsByName(keyword.trim(), pageable);
        } else {
            collectionPage = featuredCollectionService.getAllCollectionsWithCount(pageable);
        }

        model.addAttribute("collections", collectionPage);
        model.addAttribute("keyword", keyword);
        return "admin/collections/list";
    }

    /**
     * Màn hình Form dùng chung cho Thêm mới và Chỉnh sửa
     */
    @GetMapping("/form")
    public String showForm(@RequestParam(required = false) Long id, Model model) {
        CollectionSaveRequest request = new CollectionSaveRequest();

        // Nếu có ID truyền vào -> Chế độ Edit
        if (id != null) {
            Collection collection = collectionRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy bộ sưu tập"));

            request.setId(collection.getId());
            request.setName(collection.getName());
            request.setDescription(collection.getDescription());
            request.setIsActive(collection.getIsActive());
            model.addAttribute("pageTitle", "Chỉnh sửa Bộ sưu tập");
        } else {
            // Chế độ Create
            request.setIsActive(true); // Mặc định tạo mới là active
            model.addAttribute("pageTitle", "Thêm Bộ sưu tập mới");
        }

        model.addAttribute("collectionDTO", request);
        return "admin/collections/form";
    }

    /**
     * Xử lý submit lưu Form (Tạo hoặc Sửa)
     */
    @PostMapping("/save")
    public String saveCollection(
            @Valid @ModelAttribute("collectionDTO") CollectionSaveRequest request,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validate lỗi từ DTO (ví dụ: Tên trống, quá độ dài)
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", request.getId() == null ? "Thêm Bộ sưu tập mới" : "Chỉnh sửa Bộ sưu tập");
            return "admin/collections/form";
        }

        // Validate trùng lặp tên
        boolean isNameExist = request.getId() == null
                ? collectionRepository.existsByName(request.getName())
                : collectionRepository.existsByNameAndIdNot(request.getName(), request.getId());

        if (isNameExist) {
            bindingResult.rejectValue("name", "error.collectionDTO", "Tên bộ sưu tập đã tồn tại!");
            model.addAttribute("pageTitle", request.getId() == null ? "Thêm Bộ sưu tập mới" : "Chỉnh sửa Bộ sưu tập");
            return "admin/collections/form";
        }

        // Gọi Service lưu
        featuredCollectionService.saveCollection(request, principal.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Lưu bộ sưu tập thành công!");
        return "redirect:/admin/collections";
    }

    /**
     * Xóa mềm bộ sưu tập
     */
    @PostMapping("/{id}/delete")
    public String deleteCollection(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        featuredCollectionService.deleteCollection(id, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bộ sưu tập!");
        return "redirect:/admin/collections";
    }

    /**
     * ==========================================
     * QUẢN LÝ SẢN PHẨM TRONG BỘ SƯU TẬP
     * ==========================================
     */

    /**
     * Màn hình Gán Sản Phẩm (Hiển thị 2 bảng: Sản phẩm đang có và Sản phẩm có thể thêm)
     */
    @GetMapping("/{id}/assign")
    public String showAssignPage(@PathVariable Long id, Model model) {
        // Lấy thông tin Collection
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy bộ sưu tập"));

        // Lấy danh sách sản phẩm ĐÃ nằm trong bộ sưu tập (JOIN FETCH Product để tránh LazyInitializationException)
        List<CollectionItem> currentItems = collectionItemRepository.findActiveItemsWithProductByCollectionId(id);

        // Lấy danh sách TOÀN BỘ sản phẩm đang kinh doanh để Marketing chọn.
        // JOIN FETCH images, category, variants để tránh LazyInitializationException
        Pageable top100 = PageRequest.of(0, 100);
        List<Product> availableProducts = productRepository.findTop100ActiveProductsWithDetails(top100);

        model.addAttribute("collection", collection);
        model.addAttribute("currentItems", currentItems);
        model.addAttribute("availableProducts", availableProducts);

        return "admin/collections/assign-products";
    }

    /**
     * Xử lý khi Admin chọn nhiều sản phẩm và nhấn "Thêm vào Bộ Sưu Tập"
     */
    @PostMapping("/{id}/assign")
    public String processAssignProducts(
            @PathVariable Long id,
            @RequestParam(value = "productIds", required = false) List<Long> productIds,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (productIds == null || productIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 sản phẩm!");
            return "redirect:/admin/collections/" + id + "/assign";
        }

        // Gọi Service xử lý Bulk Insert
        featuredCollectionService.addProductsToCollection(id, productIds, principal.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Đã gán " + productIds.size() + " sản phẩm vào bộ sưu tập!");
        return "redirect:/admin/collections/" + id + "/assign";
    }

    /**
     * Xóa 1 sản phẩm khỏi bộ sưu tập (Xóa mềm bản ghi trung gian)
     */
    @PostMapping("/{collectionId}/remove-item/{itemId}")
    public String removeProductFromCollection(
            @PathVariable Long collectionId,
            @PathVariable Long itemId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        CollectionItem item = collectionItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy liên kết"));

        item.setIsActive(false);
        item.setUpdatedBy(principal.getName());
        collectionItemRepository.save(item);

        redirectAttributes.addFlashAttribute("successMessage", "Đã loại bỏ sản phẩm khỏi bộ sưu tập!");
        return "redirect:/admin/collections/" + collectionId + "/assign";
    }
}