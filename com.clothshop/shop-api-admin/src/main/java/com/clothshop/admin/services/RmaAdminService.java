package com.clothshop.admin.services;

import com.clothshop.admin.dtos.request.order.RmaStatusUpdateRequest;
import com.clothshop.admin.dtos.response.order.RmaAdminResponse;
import com.clothshop.admin.mappers.RmaAdminMapper;
import com.clothshop.common.dtos.request.PagingRequest;
import com.clothshop.common.dtos.response.PageResponse;
import com.clothshop.common.exceptions.BusinessException;
import com.clothshop.common.exceptions.ErrorCode;
import com.clothshop.domain.entities.order.RmaRequest;
import com.clothshop.domain.enums.RmaStatus;
import com.clothshop.domain.repositories.order.RmaRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RmaAdminService {

    private final RmaRequestRepository rmaRepository;
    private final RmaAdminMapper rmaMapper;

    /**
     * Lấy tất cả yêu cầu RMA phân trang
     */
    @Transactional(readOnly = true)
    public PageResponse<RmaAdminResponse> getAllRmaRequests(PagingRequest pagingRequest) {
        pagingRequest.validate();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingRequest.getSortDirection()),
                pagingRequest.getSortBy() != null ? pagingRequest.getSortBy() : "createdAt");
        Pageable pageable = PageRequest.of(pagingRequest.getPageNumber(), pagingRequest.getPageSize(), sort);

        Page<RmaRequest> rmaPage = rmaRepository.findAll(pageable);
        List<RmaAdminResponse> content = rmaMapper.toResponseList(rmaPage.getContent());

        return PageResponse.<RmaAdminResponse>builder()
                .content(content)
                .pageNumber(rmaPage.getNumber())
                .pageSize(rmaPage.getSize())
                .totalElements(rmaPage.getTotalElements())
                .totalPages(rmaPage.getTotalPages())
                .build();
    }

    /**
     * Cập nhật trạng thái RMA (Approve, Reject, Receive, Complete)
     */
    @Transactional
    public RmaAdminResponse updateRmaStatus(Long rmaId, RmaStatusUpdateRequest request) {
        RmaRequest rmaRequest = rmaRepository.findById(rmaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy yêu cầu RMA"));

        log.info("Updating RMA status: ID={}, NewStatus={}", rmaId, request.getStatus());

        // Cập nhật các thông tin từ Admin
        rmaRequest.setStatus(request.getStatus());
        if (request.getAdminNote() != null) {
            rmaRequest.setAdminNote(request.getAdminNote());
        }
        if (request.getRefundAmount() != null) {
            rmaRequest.setRefundAmount(request.getRefundAmount());
        }

        RmaRequest savedRma = rmaRepository.save(rmaRequest);
        return rmaMapper.toResponse(savedRma);
    }

    /**
     * Lấy chi tiết 1 yêu cầu RMA
     */
    @Transactional(readOnly = true)
    public RmaAdminResponse getRmaById(Long rmaId) {
        return rmaRepository.findById(rmaId)
                .map(rmaMapper::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}