package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import twilio.OTPHandler;
import twilio.OTPDisplayPanel;
import utils.ConfigurationParser;

import javax.swing.*;

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

            // Log success message
            api.logging().logToOutput(EXTENSION_NAME + " loaded successfully.");
        } catch (Exception e) {
            api.logging().logToError(EXTENSION_NAME + " - Initialization error: " + e.getMessage());
        }
    }

    // Main method for UI testing purposes
    public static void main(String[] args) {
        JFrame mainFrame = new JFrame(EXTENSION_NAME);
        OTPHandler dummyHandler = new OTPHandler(null); // Pass `null` as MontoyaApi for standalone testing
        ConfigurationParser dummyConfigParser = new ConfigurationParser(null);
        OTPDisplayPanel otpDisplayPanel = new OTPDisplayPanel(null, dummyHandler, dummyConfigParser);

        mainFrame.setSize(400, 300);
        mainFrame.add(otpDisplayPanel);
        mainFrame.pack();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }
}
