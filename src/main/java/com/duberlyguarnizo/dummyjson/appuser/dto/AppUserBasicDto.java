package com.duberlyguarnizo.dummyjson.appuser.dto;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link AppUser}
 */
@Value
public class AppUserBasicDto implements Serializable {
    Long id;
    @NotBlank
    String names;
    @NotNull
    AppUserRole role;
    boolean isActive;
}