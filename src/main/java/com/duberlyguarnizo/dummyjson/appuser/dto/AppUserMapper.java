package com.duberlyguarnizo.dummyjson.appuser.dto;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppUserMapper {
//    AppUserMapper INSTANCE = Mappers.getMapper(AppUserMapper.class);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AppUser toEntity(AppUserRegistrationDto registrationDto);

    AppUserBasicDto toBasicDto(AppUser appUser);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AppUser partialUpdate(AppUserRegistrationDto appUserRegistrationDto, @MappingTarget AppUser appUser);


    AppUserDetailDto toDetailDto(AppUser appUser);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AppUser partialUpdate(AppUserDetailDto appUserDetailDto, @MappingTarget AppUser appUser);
}
