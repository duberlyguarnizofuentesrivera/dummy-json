/*
 * dummy-json
 * Copyright (c) 2023 Duberly Guarnizo Fuentes Rivera <duberlygfr@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.duberlyguarnizo.dummyjson.jsoncontent.dto;

import com.duberlyguarnizo.dummyjson.jsoncontent.JsonContent;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface JsonContentMapper {
    //@Mapping(target = "createdDate", source = "jsonContentCreationDto.createdDate", qualifiedByName = "toLocalDateTime")
    JsonContent toEntity(JsonContentCreationDto jsonContentCreationDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    JsonContent partialUpdate(JsonContentCreationDto jsonContentCreationDto, @MappingTarget JsonContent jsonContent);

    @Mapping(target = "createdDate", source = "jsonContent.createdDate", qualifiedByName = "toZonedDateTime")
    @Mapping(target = "modifiedDate", source = "jsonContent.modifiedDate", qualifiedByName = "toZonedDateTime")
    JsonContentDetailDto toDetailDto(JsonContent jsonContent);

    JsonContentBasicDto toBasicDto(JsonContent jsonContent);

    @Named("toZonedDateTime")
    default ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        return ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
    }

    @Named("toLocalDateTime")
    default LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalDateTime();
    }

}