package com.duberlyguarnizo.dummyjson.appuser.dto;

import com.duberlyguarnizo.dummyjson.appuser.AppUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppUserRegistrationDto {
    @NotBlank
    private String names;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String email;
    @NotBlank
    private String idCard;
    @NotNull
    private AppUserRole role;
}
