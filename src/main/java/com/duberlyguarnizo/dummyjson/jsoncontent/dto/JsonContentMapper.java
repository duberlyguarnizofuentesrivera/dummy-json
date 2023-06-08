package com.duberlyguarnizo.dummyjson.jsoncontent.dto;

import com.duberlyguarnizo.dummyjson.jsoncontent.JsonContent;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface JsonContentMapper {
    JsonContent toEntity(JsonContentCreationDto jsonContentCreationDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    JsonContent partialUpdate(JsonContentCreationDto jsonContentCreationDto, @MappingTarget JsonContent jsonContent);

    JsonContentDetailDto toDetailDto(JsonContent jsonContent);

    JsonContentBasicDto toBasicDto(JsonContent jsonContent);

}