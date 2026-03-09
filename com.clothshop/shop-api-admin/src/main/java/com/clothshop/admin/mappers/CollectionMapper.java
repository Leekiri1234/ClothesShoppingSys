package com.clothshop.admin.mappers;

import com.clothshop.admin.dtos.request.marketing.CollectionSaveRequest;
import com.clothshop.admin.dtos.response.marketing.CollectionResponse;
import com.clothshop.domain.entities.marketing.Collection;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CollectionMapper {

    // 1. Entity -> Response
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
    Collection toEntity(CollectionSaveRequest request);

    // 3. Request -> Entity (Update)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(CollectionSaveRequest request, @MappingTarget Collection collection);
}