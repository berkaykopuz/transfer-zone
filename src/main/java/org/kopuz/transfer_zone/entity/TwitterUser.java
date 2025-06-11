package org.kopuz.transfer_zone.entity;

public enum TwitterUser {
    FABRIZIO_ROMANO("330262748"),
    SABUN_YAGIZCIOGLU("70744910"),
    GIANLUCA_DI_MARZIO("251690031");

    private final String userId;

    TwitterUser(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
