package org.kopuz.transfer_zone.entity;

public enum TwitterUser {
    FABRIZIO_ROMANO("330262748"),
    SABUN_YAGIZCIOGLU("70744910"),
    GIANLUCA_DI_MARZIO("251690031");

    public static String[] authorNames = {"FABRIZIO_ROMANO", "SABUN_YAGIZCIOGLU", "GIANLUCA_DI_MARZIO"};

    private final String userId;

    TwitterUser(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
