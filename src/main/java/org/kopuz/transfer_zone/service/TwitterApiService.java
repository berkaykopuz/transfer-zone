package org.kopuz.transfer_zone.service;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsOAuth2;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.auth.TwitterOAuth20Service;
import com.twitter.clientlib.model.Get2UsersIdTweetsResponse;
import com.twitter.clientlib.model.Tweet;
import org.kopuz.transfer_zone.entity.TwitterUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TwitterApiService {

    private TwitterApi apiInstance;

    private final TwitterCredentialsOAuth2 credentials;

    private final TokenStorageService tokenStorageService;

    private final Environment env;

    private final String clientId;

    private final String clientSecret;
    private int sequence = 0;


    public TwitterApiService(TokenStorageService tokenStorageService, Environment env) {
        this.env = env;
        this.tokenStorageService = tokenStorageService;

        // Get client id and secret from application.properties
        clientId = this.env.getProperty("twitter.client.id");
        clientSecret = this.env.getProperty("twitter.client.secret");


        this.credentials = new TwitterCredentialsOAuth2(
                clientId,
                clientSecret,
                tokenStorageService.getAccessToken(),
                tokenStorageService.getRefreshToken()
        );


        OAuth2AccessToken accessToken = getAccessToken(credentials);
        if (accessToken == null) {
            return;
        }

        // Set new access and refresh token in credentials
        credentials.setTwitterOauth2AccessToken(accessToken.getAccessToken());
        credentials.setTwitterOauth2RefreshToken(accessToken.getRefreshToken());

        // Backup the access and refresh token
        tokenStorageService.updateTokens(accessToken.getAccessToken(), accessToken.getRefreshToken());

        apiInstance = new TwitterApi(credentials);
    }

    public OAuth2AccessToken getAccessToken(TwitterCredentialsOAuth2 credentials) {

        TwitterOAuth20Service service = new TwitterOAuth20Service(
                credentials.getTwitterOauth2ClientId(),
                credentials.getTwitterOAuth2ClientSecret(),
                env.getProperty("twitter.oauth.callback.url"),
                env.getProperty("twitter.oauth.scopes")
        );

        OAuth2AccessToken accessToken = null;
        try {
            final Scanner in = new Scanner(System.in, "UTF-8");
            System.out.println("Fetching the Authorization URL...");

            final String secretState = "state";
            PKCE pkce = new PKCE();
            pkce.setCodeChallenge("challenge");
            pkce.setCodeChallengeMethod(PKCECodeChallengeMethod.PLAIN);
            pkce.setCodeVerifier("challenge");
            String authorizationUrl = service.getAuthorizationUrl(pkce, secretState);

            System.out.println("Go to the Authorization URL and authorize your App:\n" +
                    authorizationUrl + "\nAfter that paste the authorization code here\n>>");
            final String code = in.nextLine();
            System.out.println("\nTrading the Authorization Code for an Access Token...");
            accessToken = service.getAccessToken(pkce, code);

            System.out.println("Access token: " + accessToken.getAccessToken());
            System.out.println("Refresh token: " + accessToken.getRefreshToken());
        } catch (Exception e) {
            System.err.println("Error while getting the access token:\n " + e);
            e.printStackTrace();
        }
        return accessToken;
    }

    /*public OAuth2AccessToken exchangeCodeForAccessToken(String code) {
        TwitterOAuth20Service service = new TwitterOAuth20Service(
                credentials.getTwitterOauth2ClientId(),
                credentials.getTwitterOAuth2ClientSecret(),
                env.getProperty("twitter.oauth.callback.url"),
                env.getProperty("twitter.oauth.scopes")
        );
        PKCE pkce = new PKCE();
        // Use dynamic PKCE values
        String codeVerifier = generateCodeVerifier(); // Implement this method
        pkce.setCodeChallenge(generateCodeChallenge(codeVerifier));
        pkce.setCodeChallengeMethod(PKCECodeChallengeMethod.PLAIN); // Or S256
        pkce.setCodeVerifier(codeVerifier);

        try {
            OAuth2AccessToken accessToken = service.getAccessToken(pkce, code);
            tokenStorageService.updateTokens(accessToken.getAccessToken(), accessToken.getRefreshToken());
            credentials.setTwitterOauth2AccessToken(accessToken.getAccessToken());
            credentials.setTwitterOauth2RefreshToken(accessToken.getRefreshToken());
            return accessToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange code for access token", e);
        }
    }

    private String generateCodeVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    private String generateCodeChallenge(String codeVerifier) {
        return codeVerifier; // For PLAIN method; use SHA-256 for S256
    }*/

    public String findTweetForFavTeams(List<String> favTeamList){

        String outputMessage = "";

        TwitterUser user = TwitterUser.valueOf(TwitterUser.authorNames[sequence]);


            Get2UsersIdTweetsResponse result = getTweetByUser(user.getUserId());

            if(result != null && result.getData() != null){

                String author = user.name() + ": ";

                outputMessage += "✈\uD83D\uDD25 İşte Güncel Transfer Söylentileri\n\n";

                List<Tweet> filteredTweets = result.getData().stream().filter(tweet -> {
                    String tweetText = tweet.getText().toLowerCase();

                    return favTeamList.stream().anyMatch(team -> tweetText.contains(team));
                }).collect(Collectors.toList());

                if(filteredTweets.isEmpty()) return null; // Check if any tweet is found for specified teams.

                List<String> filteredTweetTexts = filteredTweets.stream().map(tweet -> {
                    return tweet.getText();
                }).collect(Collectors.toList());



                for (String filteredTweetText:
                        filteredTweetTexts) {
                    outputMessage += author  + filteredTweetText + "\n\n";
                }
            }

        if(sequence == TwitterUser.authorNames.length - 1) sequence = 0;
        else sequence++;

        return outputMessage;
    }

    private Get2UsersIdTweetsResponse getTweetByUser(String userId){

        String id = userId;

        Integer maxResults = 5;

        // Set valid RFC3339 time format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneId.of("UTC"));

        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC"));
        String startTime = now.minusMinutes(59).format(formatter); // 15 minutes ago
        String endTime = now.format(formatter); // Current time


        try {
            Get2UsersIdTweetsResponse result = apiInstance.tweets().usersIdTweets(id)
                    .maxResults(maxResults)
                    .startTime(OffsetDateTime.parse(startTime))
                    .endTime(OffsetDateTime.parse(endTime))
                    .execute();


            System.out.println(result);
            return result;
        }
        catch (ApiException e){
            System.err.println("Exception when calling TweetsApi#usersIdTweets");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }

        return null;
    }

    private Set<String> toBeSearchedKeywords(List<String> favTeamList){
        Set<String> tweetFields = new HashSet<>(favTeamList);

        return tweetFields;
    }
}
