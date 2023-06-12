package com.duberlyguarnizo.dummyjson.appuser;

public enum AppUserRole {
    ADMIN("enums.AppUserRole.ADMIN"),
    SUPERVISOR("enums.AppUserRole.SUPERVISOR"),
    USER("enums.AppUserRole.USER");
    private String label;

    AppUserRole(String LocalizedLabel) {
        this.label = LocalizedLabel;
    }

    @Override
    public String toString() {
        return label;
    }
}
