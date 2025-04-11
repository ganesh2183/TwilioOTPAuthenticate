package twilio;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.Preferences;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.HttpMode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPHandler {
    private final MontoyaApi api;
    private final Preferences preferences;
    private final Pattern otpRegex;
    private final ObjectMapper objectMapper;

    // Twilio settings
    private String accountSid;
    private String authToken;
    private String fromNumber;
    private String toNumber;

    // Preference Keys
    private static final String PREF_ACCOUNT_SID = "accountSid";
    private static final String PREF_AUTH_TOKEN = "authToken";
    private static final String PREF_FROM_NUMBER = "fromNumber";
    private static final String PREF_TO_NUMBER = "toNumber";

    public OTPHandler(MontoyaApi api) {
        this.api = api;
        this.preferences = api.persistence().preferences();
        this.otpRegex = Pattern.compile("\\b\\d{4,6}\\b");
        this.objectMapper = new ObjectMapper();
        loadSettings();
    }

    // Load Twilio settings from Preferences
    private void loadSettings() {
        this.accountSid = preferences.getString(PREF_ACCOUNT_SID);
        this.authToken = preferences.getString(PREF_AUTH_TOKEN);
        this.fromNumber = preferences.getString(PREF_FROM_NUMBER);
        this.toNumber = preferences.getString(PREF_TO_NUMBER);

        if (accountSid == null || authToken == null || fromNumber == null || toNumber == null) {
            api.logging().logToError("Twilio settings are not fully configured.");
        }
    }

    // Update Twilio settings and save them to Preferences
    public void updateSettings(String accountSid, String authToken, String fromNumber, String toNumber) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.toNumber = toNumber;

        // Save settings to preferences
        preferences.setString(PREF_ACCOUNT_SID, accountSid);
        preferences.setString(PREF_AUTH_TOKEN, authToken);
        preferences.setString(PREF_FROM_NUMBER, fromNumber);
        preferences.setString(PREF_TO_NUMBER, toNumber);

        api.logging().logToOutput("Twilio settings updated successfully.");
    }

    // Fetch the latest OTP from Twilio messages using Montoya API
    public CompletableFuture<String> getLatestOTPAsync() {
        if (accountSid == null || authToken == null || fromNumber == null || toNumber == null) {
            throw new IllegalStateException("Twilio settings are not configured.");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                api.logging().logToOutput("Fetching messages from Twilio...");

                String credentials = accountSid + ":" + authToken;
                String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

                HttpService twilioService = HttpService.httpService("api.twilio.com", 443, true);
                String path = String.format("/2010-04-01/Accounts/%s/Messages.json?From=%s&To=%s&PageSize=1", accountSid, fromNumber, toNumber);

                HttpRequest request = HttpRequest.httpRequest()
                        .withService(twilioService)
                        .withMethod("GET")
                        .withPath(path)
                        .withAddedHeader("Host", "api.twilio.com")
                        .withAddedHeader("Authorization", "Basic " + encodedCredentials);

                HttpResponse response = api.http().sendRequest(request, HttpMode.HTTP_1).response();

                if (response.statusCode() == 200) {
                    JsonNode rootNode = objectMapper.readTree(response.body().toString());
                    JsonNode messages = rootNode.path("messages");

                    if (messages.isArray() && messages.size() > 0) {
                        String body = messages.get(0).path("body").asText();
                        api.logging().logToOutput("Message retrieved: " + body);

                        Matcher matcher = otpRegex.matcher(body);
                        if (matcher.find()) {
                            String otp = matcher.group();
                            api.logging().logToOutput("OTP retrieved: " + otp);
                            return otp;
                        }
                    }
                } else {
                    api.logging().logToError("Failed to retrieve messages: " + response.statusCode() + " " + response.reasonPhrase());
                }

                throw new Exception("No valid OTP found in recent messages.");

            } catch (Exception e) {
                api.logging().logToError("Failed to retrieve OTP: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
