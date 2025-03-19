package twilio;

import burp.api.montoya.MontoyaApi;
import utils.ConfigurationParser;
import utils.RuleType;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

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

    public OTPDisplayPanel(MontoyaApi api, OTPHandler otpHandler, ConfigurationParser configParser) {
        this.otpHandler = otpHandler;
        this.api = api;
        this.configParser = configParser;

        preferences = Preferences.userRoot().node("TwilioOTPAuthenticate");

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        otpField = new JTextField(20);
        otpField.setEditable(false);
        otpField.setFont(new Font("Arial", Font.PLAIN, 16));
        otpField.setBackground(Color.LIGHT_GRAY);

        accountSidField = new JPasswordField(loadPreference("accountSid", ""), 25);
        authTokenField = new JPasswordField(loadPreference("authToken", ""), 25);
        fromNumberField = new JTextField(loadPreference("fromNumber", ""), 25);
        toNumberField = new JTextField(loadPreference("toNumber", ""), 25);

        ruleTypeComboBox = new JComboBox<>(RuleType.values());
        ruleTypeComboBox.setSelectedItem(RuleType.valueOf(loadPreference("ruleType", "HEADER")));
        parameterNameField = new JTextField(loadPreference("parameterName", ""), 20);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Fetch OTP", createMainPanel());
        tabbedPane.addTab("Twilio Settings", createSettingsPanel());
        tabbedPane.addTab("Configure", createConfigurePanel());
        tabbedPane.addTab("About", createAboutPanel());

        add(tabbedPane, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel otpLabel = new JLabel("Latest OTP:");
        otpLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        mainPanel.add(otpLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(otpField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JButton fetchOtpButton = new JButton("Fetch OTP");
        fetchOtpButton.setFont(new Font("Arial", Font.BOLD, 14));
        fetchOtpButton.addActionListener(e -> fetchAndUpdateOTP());
        mainPanel.add(fetchOtpButton, gbc);

        return mainPanel;
    }

    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
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
        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(e -> saveTwilioSettings());
        settingsPanel.add(saveButton, gbc);

        return settingsPanel;
    }

    private JPanel createConfigurePanel() {
        JPanel configurePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
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

    private JPanel createAboutPanel() {
        JPanel aboutPanel = new JPanel(new BorderLayout());
        aboutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Content for the About tab, rendered as HTML
        String aboutContent = """
            <html>
                <body style='font-family: Arial, sans-serif;'>
                    <h1 style='text-align: center; color: #333;'>Twilio OTP Authenticate</h2>
                    <p style='text-align: center; font-size: 12px; font-style: italic;'>
                        Twilio OTP integration for BurpSuite Automation
                    </p>
                    <div style='text-align: left; margin-top: 10px; font-size: 12px;'>
                        <b>Created by:</b> Ganesh Babu<br><br>
                        Twilio OTP Authenticate utilizes session handling rules to provide a Twilio OTP code to outgoing requests.<br>
                        It can be used in both <b>BurpSuite Pro</b> and <b>BurpSuite Community edition</b>.
                    </div>
                    <hr>
                     <div style='margin-top: 10px; font-size: 12px;'>
                        <h3 style='text-align: left; font-size: 14px; color: #000;'>How to Configure:</h3>
                         <ol>
                              <li>Load the extension into <b>Extensions > Installed > Add > Extension Type: Java > Choose the jar file</b></li>
                              <li>Go to <b>Settings > Search > Sessions</b></li>
                              <li>Under <b>Session handling rules</b>, go to <b>Add > Rule actions > Add > Invoke a Burp extension</b>,<br>
                                  select '<b>Twilio OTP Authenticate</b>' from the dropdown list available and click OK.</li>
                              <li>Click across to the <b>Scope</b> tab, ensuring that the <b>Tools scope > Scanner, Repeater</b> box is checked.</li>
                              <li>Configure the URL scope appropriately.</li>
                              <li>Click OK.</li>
                              <li>Now you can perform security testing in Burp Suite Professional.</li>
                         </ol>
                     </div>
                </body>
            </html>
            """;

        // Use JEditorPane for proper HTML rendering
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(aboutContent);
        editorPane.setEditable(false);
        editorPane.setOpaque(false);

        // Add JEditorPane to the panel
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        aboutPanel.add(scrollPane, BorderLayout.CENTER);

        return aboutPanel;
    }

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Status: Ready", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerPanel.add(statusLabel, BorderLayout.CENTER);
        return footerPanel;
    }

    private JPanel createMaskedField(JPasswordField field) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        field.setEchoChar((char) 0); // Unmask by default
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
        try {
            String latestOtp = otpHandler.getLatestOTP();
            updateOtpDisplay(latestOtp);
            statusLabel.setText("Status: OTP fetched successfully.");
        } catch (Exception e) {
            statusLabel.setText("Status: Error fetching OTP.");
        }
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

        statusLabel.setText(
                String.format("Config Saved: RuleType=%s, ParameterName=%s", selectedRuleType, parameterName)
        );
        api.logging().logToOutput(
                String.format("Configuration Saved: RuleType=%s, ParameterName=%s", selectedRuleType, parameterName)
        );
    }

    private void savePreference(String key, String value) {
        preferences.put(key, value);
    }

    private String loadPreference(String key, String defaultValue) {
        return preferences.get(key, defaultValue);
    }
}
