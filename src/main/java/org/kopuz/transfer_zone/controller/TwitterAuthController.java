package org.kopuz.transfer_zone.controller;

import com.github.scribejava.core.model.OAuth2AccessToken;
import org.kopuz.transfer_zone.service.TwitterApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TwitterAuthController {
    private final TwitterApiService twitterApiService;

    public TwitterAuthController(TwitterApiService twitterApiService) {
        this.twitterApiService = twitterApiService;
    }

    @GetMapping("/auth/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {
        // Verify the state parameter to prevent CSRF attacks
        if (!"state".equals(state)) { // Replace with your stored state
            return ResponseEntity.badRequest().body("Invalid state parameter");
        }

        try {
            return ResponseEntity.ok(code);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error exchanging code: " + e.getMessage());
        }
    }
}
