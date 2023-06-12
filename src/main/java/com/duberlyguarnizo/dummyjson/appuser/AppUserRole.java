package com.duberlyguarnizo.dummyjson.appuser;

public enum AppUserRole {
    ADMIN("Admin", "Administrador", "Administrador"),
    SUPERVISOR("Supervisor", "Supervisor", "Supervisor"),
    USER("User", "Usuario", "Usuario");
    private final String labelEn;
    private final String labelEs;
    private final String labelPt;

    AppUserRole(String enLabel, String esLabel, String ptLabel) {
        this.labelEn = enLabel;
        this.labelEs = esLabel;
        this.labelPt = ptLabel;
    }

    public String getLabel(String language) {
        return switch (language) {
            case "es" -> labelEs;
            case "pt" -> labelPt;
            default -> labelEn;
        };
    }
}
