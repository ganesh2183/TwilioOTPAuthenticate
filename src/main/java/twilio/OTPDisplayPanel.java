// OTPDisplayPanel.java
package twilio;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.Preferences;
import utils.ConfigurationParser;
import utils.RuleType;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OTPDisplayPanel extends JPanel {
    private final MontoyaApi api;
    private final Preferences preferences;

    private final JTextField otpField;
    private JLabel statusLabel;

    private final JPasswordField accountSidField;
    private final JPasswordField authTokenField;
    private final JTextField fromNumberField;
    private final JTextField toNumberField;
    private final JComboBox<RuleType> ruleTypeComboBox;
    private final JTextField parameterNameField;

    private final OTPHandler otpHandler;
    private final ConfigurationParser configParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OTPDisplayPanel(MontoyaApi api, OTPHandler otpHandler, ConfigurationParser configParser) {
        this.api = api;
        this.otpHandler = otpHandler;
        this.configParser = configParser;

        preferences = api.persistence().preferences();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        otpField = new JTextField(20);
        otpField.setEditable(false);
        otpField.setFont(new Font("Arial", Font.PLAIN, 16));
        otpField.setBackground(new Color(211, 211, 211));
        otpField.setForeground(Color.BLACK);
        otpField.setCaretColor(Color.BLACK);
        otpField.setBorder(BorderFactory.createLineBorder(new Color(169, 169, 169)));

        accountSidField = new JPasswordField(loadPreference("accountSid", ""), 25);
        authTokenField = new JPasswordField(loadPreference("authToken", ""), 25);
        fromNumberField = new JTextField(loadPreference("fromNumber", ""), 25);
        toNumberField = new JTextField(loadPreference("toNumber", ""), 25);

        ruleTypeComboBox = new JComboBox<>(RuleType.values());
        ruleTypeComboBox.setSelectedItem(RuleType.valueOf(loadPreference("ruleType", "HEADER")));
        parameterNameField = new JTextField(loadPreference("parameterName", ""), 20);

        JPanel unifiedPanel = new JPanel();
        unifiedPanel.setLayout(new BoxLayout(unifiedPanel, BoxLayout.Y_AXIS));
        unifiedPanel.add(createSectionPanel("Fetch OTP", createMainPanel()));
        unifiedPanel.add(createSectionPanel("Twilio Settings", createSettingsPanel()));
        unifiedPanel.add(createSectionPanel("Configure", createConfigurePanel()));

        add(unifiedPanel, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createSectionPanel(String title, JPanel content) {
        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBorder(BorderFactory.createTitledBorder(title));
        sectionPanel.add(content, BorderLayout.CENTER);
        return sectionPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Latest OTP:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(otpField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JButton fetchOtpButton = new JButton("Fetch OTP");
        fetchOtpButton.addActionListener(e -> fetchAndUpdateOTP());
        mainPanel.add(fetchOtpButton, gbc);

        return mainPanel;
    }

    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(new JLabel("Account SID:"), gbc);
        gbc.gridx = 1;
        settingsPanel.add(createMaskedField(accountSidField), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        settingsPanel.add(new JLabel("Auth Token:"), gbc);
        gbc.gridx = 1;
        settingsPanel.add(createMaskedField(authTokenField), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        settingsPanel.add(new JLabel("From Number:"), gbc);
        gbc.gridx = 1;
        settingsPanel.add(fromNumberField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        settingsPanel.add(new JLabel("To Number:"), gbc);
        gbc.gridx = 1;
        settingsPanel.add(toNumberField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveButton = new JButton("Save");
        JButton importButton = new JButton("Import");
        JButton exportButton = new JButton("Export");
        JButton clearButton = new JButton("Clear");

        saveButton.addActionListener(e -> saveTwilioSettings());
        importButton.addActionListener(e -> importSettingsFromFile());
        exportButton.addActionListener(e -> exportSettingsToFile());
        clearButton.addActionListener(e -> clearTwilioSettings());

        buttonPanel.add(saveButton);
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(clearButton);
        settingsPanel.add(buttonPanel, gbc);

        return settingsPanel;
    }

    private JPanel createConfigurePanel() {
        JPanel configurePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        configurePanel.add(new JLabel("Rule Type:"), gbc);
        gbc.gridx = 1;
        configurePanel.add(ruleTypeComboBox, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        configurePanel.add(new JLabel("Parameter Name:"), gbc);
        gbc.gridx = 1;
        configurePanel.add(parameterNameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton generateConfigButton = new JButton("Generate Config");
        generateConfigButton.addActionListener(e -> saveConfigurationSettings());
        configurePanel.add(generateConfigButton, gbc);

        return configurePanel;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Status: Ready", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerPanel.add(statusLabel, BorderLayout.WEST);

        JLabel creditLabel = new JLabel("Created by: Ganesh Babu", SwingConstants.RIGHT);
        creditLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerPanel.add(creditLabel, BorderLayout.EAST);

        return footerPanel;
    }

    private JPanel createMaskedField(JPasswordField field) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        field.setEchoChar((char) 0);
        fieldPanel.add(field, BorderLayout.CENTER);

        JButton toggleButton = new JButton("Hide");
        toggleButton.setFont(new Font("Arial", Font.PLAIN, 10));
        toggleButton.addActionListener(e -> {
            if (field.getEchoChar() == (char) 0) {
                field.setEchoChar('*');
                toggleButton.setText("Show");
            } else {
                field.setEchoChar((char) 0);
                toggleButton.setText("Hide");
            }
        });
        fieldPanel.add(toggleButton, BorderLayout.EAST);
        return fieldPanel;
    }

    private void fetchAndUpdateOTP() {
        statusLabel.setText("Status: Fetching OTP...");
        otpHandler.getLatestOTPAsync()
                .thenAccept(this::updateOtpDisplay)
                .exceptionally(ex -> {
                    statusLabel.setText("Status: Error fetching OTP.");
                    api.logging().logToError("Error fetching OTP: " + ex.getMessage());
                    return null;
                });
    }

    public void updateOtpDisplay(String otp) {
        otpField.setText(otp);
    }

    private void saveTwilioSettings() {
        String accountSid = new String(accountSidField.getPassword());
        String authToken = new String(authTokenField.getPassword());
        String fromNumber = fromNumberField.getText().trim();
        String toNumber = toNumberField.getText().trim();

        otpHandler.updateSettings(accountSid, authToken, fromNumber, toNumber);

        savePreference("accountSid", accountSid);
        savePreference("authToken", authToken);
        savePreference("fromNumber", fromNumber);
        savePreference("toNumber", toNumber);

        statusLabel.setText("Twilio settings saved successfully.");
        api.logging().logToOutput("Twilio settings updated and saved");
    }

    private void importSettingsFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try (InputStream input = new FileInputStream(fileChooser.getSelectedFile())) {
            Map<String, String> map = objectMapper.readValue(input, Map.class);
            accountSidField.setText(map.getOrDefault("accountSid", ""));
            authTokenField.setText(map.getOrDefault("authToken", ""));
            fromNumberField.setText(map.getOrDefault("fromNumber", ""));
            toNumberField.setText(map.getOrDefault("toNumber", ""));
            statusLabel.setText("Imported settings from file.");
        } catch (IOException ex) {
            statusLabel.setText("Failed to import settings.");
            api.logging().logToError("Error importing settings: " + ex.getMessage());
        }
    }

    private void exportSettingsToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        Map<String, String> map = new HashMap<>();
        map.put("accountSid", new String(accountSidField.getPassword()));
        map.put("authToken", new String(authTokenField.getPassword()));
        map.put("fromNumber", fromNumberField.getText().trim());
        map.put("toNumber", toNumberField.getText().trim());

        try (OutputStream output = new FileOutputStream(fileChooser.getSelectedFile())) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(output, map);
            statusLabel.setText("Settings exported successfully.");
        } catch (IOException ex) {
            statusLabel.setText("Failed to export settings.");
            api.logging().logToError("Error exporting settings: " + ex.getMessage());
        }
    }

    private void clearTwilioSettings() {
        accountSidField.setText("");
        authTokenField.setText("");
        fromNumberField.setText("");
        toNumberField.setText("");

        savePreference("accountSid", "");
        savePreference("authToken", "");
        savePreference("fromNumber", "");
        savePreference("toNumber", "");

        statusLabel.setText("Cleared Twilio credentials.");
        api.logging().logToOutput("Twilio credentials cleared.");
    }

    private void saveConfigurationSettings() {
        Object selectedRuleTypeObj = ruleTypeComboBox.getSelectedItem();

        if (selectedRuleTypeObj == null) {
            statusLabel.setText("Status: Rule Type selection is required.");
            api.logging().logToError("Rule Type not selected.");
            return;
        }

        String selectedRuleType = selectedRuleTypeObj.toString();
        String parameterName = parameterNameField.getText().trim();

        if (parameterName.isEmpty()) {
            statusLabel.setText("Status: Parameter Name is required.");
            api.logging().logToError("Parameter Name is empty.");
            return;
        }

        configParser.saveToPreferences(selectedRuleType, parameterName);
        statusLabel.setText(String.format("Config Saved: RuleType=%s, ParameterName=%s", selectedRuleType, parameterName));
        api.logging().logToOutput(String.format("Configuration Saved: RuleType=%s, ParameterName=%s", selectedRuleType, parameterName));
    }

    private void savePreference(String key, String value) {
        preferences.setString(key, value);
    }

    private String loadPreference(String key, String defaultValue) {
        String value = preferences.getString(key);
        return (value != null) ? value : defaultValue;
    }
}
