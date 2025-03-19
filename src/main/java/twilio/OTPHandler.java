package twilio;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.Preferences;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Message;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPHandler {
    private final MontoyaApi api;
    private final Preferences preferences;
    private final Pattern otpRegex;

    // Declare Twilio settings as Strings
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
        loadSettings();
    }

    // Load Twilio settings from Preferences
    private void loadSettings() {
        this.accountSid = preferences.getString(PREF_ACCOUNT_SID);
        this.authToken = preferences.getString(PREF_AUTH_TOKEN);
        this.fromNumber = preferences.getString(PREF_FROM_NUMBER);
        this.toNumber = preferences.getString(PREF_TO_NUMBER);

        if (accountSid != null && authToken != null) {
            try {
                Twilio.init(this.accountSid, this.authToken);
                api.logging().logToOutput("Twilio client initialized successfully with saved settings.");
            } catch (Exception e) {
                api.logging().logToError("Failed to initialize Twilio client with saved settings: " + e.getMessage());
            }
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

        try {
            Twilio.init(this.accountSid, this.authToken);
            api.logging().logToOutput("Twilio client initialized successfully with updated settings.");
        } catch (Exception e) {
            api.logging().logToError("Failed to initialize Twilio client: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Fetch the latest OTP from Twilio messages
    public CompletableFuture<String> getLatestOTPAsync() {
        if (accountSid == null || authToken == null || fromNumber == null || toNumber == null) {
            throw new IllegalStateException("Twilio settings are not configured.");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                api.logging().logToOutput("Fetching messages from Twilio...");

                ResourceSet<Message> messages = Message.reader()
                        .setFrom(new com.twilio.type.PhoneNumber(fromNumber))
                        .setTo(new com.twilio.type.PhoneNumber(toNumber))
                        .limit(1)
                        .read();

                if (messages.iterator().hasNext()) {
                    Message message = messages.iterator().next();
                    api.logging().logToOutput("Message retrieved: " + message.getBody());

                    Matcher matcher = otpRegex.matcher(message.getBody());
                    if (matcher.find()) {
                        String otp = matcher.group();
                        api.logging().logToOutput("OTP retrieved: " + otp);
                        return otp;
                    }
                }

                throw new Exception("No valid OTP found in recent messages.");

            } catch (Exception e) {
                api.logging().logToError("Failed to retrieve OTP: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}