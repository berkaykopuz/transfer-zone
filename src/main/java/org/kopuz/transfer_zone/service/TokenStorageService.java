package org.kopuz.transfer_zone.service;

import org.springframework.stereotype.Service;

@Service
public class TokenStorageService {

    private String accessToken;
    private String refreshToken;

    public TokenStorageService() {
        this.accessToken = "";
        this.refreshToken = "";
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }


    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
