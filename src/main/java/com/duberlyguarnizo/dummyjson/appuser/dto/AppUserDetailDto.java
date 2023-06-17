package com.duberlyguarnizo.dummyjson.appuser.dto;

import com.duberlyguarnizo.dummyjson.appuser.AppUser;
import com.duberlyguarnizo.dummyjson.appuser.AppUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link AppUser}
 */
@Value
public class AppUserDetailDto implements Serializable {
    Long createdBy;
    Long modifiedBy;
    LocalDateTime createdDate;
    LocalDateTime modifiedDate;
    Long id;
    @NotBlank
    String names;
    String email;
    @NotBlank
    String idCard;
    @NotNull
    AppUserRole role;
    boolean isActive;
    @Serial
    private static final long serialVersionUID = 989L;
    @NotBlank
    String username;
    boolean isLocked;
}