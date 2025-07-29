package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import twilio.OTPHandler;
import twilio.OTPDisplayPanel;
import utils.ConfigurationParser;

public class BurpExtender implements BurpExtension {
    private static final String EXTENSION_NAME = "Twilio OTP Authenticate";

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName(EXTENSION_NAME);

        try {
            // Initialize components
            ConfigurationParser configParser = new ConfigurationParser(api);
            OTPHandler otpHandler = new OTPHandler(api);
            OTPDisplayPanel otpDisplayPanel = new OTPDisplayPanel(api, otpHandler, configParser);

            // Register the session handling action
            SessionHandlingAction sessionHandlingAction = new MySessionHandlingAction(api, otpHandler, configParser);
            api.http().registerSessionHandlingAction(sessionHandlingAction);

            // Register the UI tab for Burp Suite Pro
            api.userInterface().registerSuiteTab(EXTENSION_NAME, otpDisplayPanel);

            // Register unloading handler
            api.extension().registerUnloadingHandler(() ->
                    api.logging().logToOutput(EXTENSION_NAME + " unloaded.")
            );

            // Log success message
            api.logging().logToOutput(EXTENSION_NAME + " loaded successfully.");
        } catch (Exception e) {
            api.logging().logToError(EXTENSION_NAME + " - Initialization error: " + e.getMessage());
        }
    }
}
