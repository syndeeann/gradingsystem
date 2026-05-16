package com.gradingsystem.mapper;

import com.gradingsystem.dto.subject.SubjectRequest;
import com.gradingsystem.dto.subject.SubjectResponse;
import com.gradingsystem.entity.Subject;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    SubjectResponse toResponse(Subject subject);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Subject toEntity(SubjectRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(SubjectRequest request, @MappingTarget Subject subject);
}
