package twilio;

import burp.api.montoya.MontoyaApi;
import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Message;

import java.util.prefs.Preferences;
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
        this.preferences = Preferences.userRoot().node("TwilioOTPAuthenticate");
        this.otpRegex = Pattern.compile("\\b\\d{4,6}\\b");
        loadSettings();
    }

    // Load Twilio settings from Preferences
    private void loadSettings() {
        this.accountSid = preferences.get(PREF_ACCOUNT_SID, "");
        this.authToken = preferences.get(PREF_AUTH_TOKEN, "");
        this.fromNumber = preferences.get(PREF_FROM_NUMBER, "");
        this.toNumber = preferences.get(PREF_TO_NUMBER, "");

        if (!accountSid.isEmpty() && !authToken.isEmpty()) {
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
        preferences.put(PREF_ACCOUNT_SID, accountSid);
        preferences.put(PREF_AUTH_TOKEN, authToken);
        preferences.put(PREF_FROM_NUMBER, fromNumber);
        preferences.put(PREF_TO_NUMBER, toNumber);

        try {
            Twilio.init(this.accountSid, this.authToken);
            api.logging().logToOutput("Twilio client initialized successfully with updated settings.");
        } catch (Exception e) {
            api.logging().logToError("Failed to initialize Twilio client: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Fetch the latest OTP from Twilio messages
    public String getLatestOTP() throws Exception {
        if (accountSid.isEmpty() || authToken.isEmpty() || fromNumber.isEmpty() || toNumber.isEmpty()) {
            throw new IllegalStateException("Twilio settings are not configured.");
        }

        try {
            api.logging().logToOutput("Fetching messages from Twilio...");

            Thread.sleep(2000);

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

        } catch (InterruptedException e) {
            api.logging().logToError("Thread interrupted during wait time");
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            api.logging().logToError("Failed to retrieve OTP: " + e.getMessage());
            throw e;
        }
    }
}
