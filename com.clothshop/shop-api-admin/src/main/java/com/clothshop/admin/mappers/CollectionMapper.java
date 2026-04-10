package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.request.marketing.CollectionSaveRequest;
import com.clothshop.admin.dtos.response.marketing.CollectionResponse;
import com.clothshop.domain.models.marketing.Collection;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CollectionMapper {

    // 1. Entity -> Response
    @Mapping(source = "bannerUrl", target = "imageUrl")
    @Mapping(target = "itemCount", ignore = true)
    CollectionResponse toResponse(Collection collection);

    // 2. Request -> Entity (Create)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "bannerUrl", ignore = true) // Tạm thời ignore nếu chưa dùng đến trong form này
    @Mapping(target = "imageUrl", ignore = true)  //Service tự set sau khi upload file
    Collection toEntity(CollectionSaveRequest request);

    // 3. Request -> Entity (Update)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "imageUrl", ignore = true) //Tránh việc MapStruct ghi đè giá trị null vào link ảnh cũ
    void updateEntityFromRequest(CollectionSaveRequest request, @MappingTarget Collection collection);
}