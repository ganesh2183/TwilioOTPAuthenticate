package utils;

import burp.api.montoya.MontoyaApi;

import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public class ConfigurationParser {
    private static final String PREFERENCES_NODE = "TwilioOTPAuthenticate";
    private static final String RULE_TYPE_KEY = "ruleType";
    private static final String PARAMETER_NAME_KEY = "parameterName";

    private String parameterName;
    private RuleType ruleType;
    private Pattern replacementPattern;

    private final MontoyaApi api;
    private final Preferences preferences;

    public ConfigurationParser(MontoyaApi api) {
        this.api = api;
        this.preferences = Preferences.userRoot().node(PREFERENCES_NODE);

        loadFromPreferences();
    }

    /**
     * Loads the saved configuration from persistent storage.
     */
    private void loadFromPreferences() {
        String savedRuleType = preferences.get(RULE_TYPE_KEY, null);
        String savedParameterName = preferences.get(PARAMETER_NAME_KEY, null);

        if (savedRuleType == null || savedParameterName == null || savedParameterName.isEmpty()) {
            api.logging().logToError("Configuration not found in preferences.");
            return;
        }

        try {
            ruleType = RuleType.valueOf(savedRuleType.toUpperCase());
            parameterName = savedParameterName.trim();

            api.logging().logToOutput("Loaded configuration from Preferences:");
            api.logging().logToOutput("Configured RuleType: " + ruleType);
            api.logging().logToOutput("Configured ParameterName: " + parameterName);

            initializeReplacementPattern();
        } catch (IllegalArgumentException e) {
            api.logging().logToError("Invalid configuration format in preferences: " + e.getMessage());
        }
    }

    /**
     * Saves the configuration to persistent storage.
     */
    public void saveToPreferences(String ruleType, String parameterName) {
        preferences.put(RULE_TYPE_KEY, ruleType.toUpperCase());
        preferences.put(PARAMETER_NAME_KEY, parameterName.trim());

        api.logging().logToOutput("Configuration saved to Preferences:");
        api.logging().logToOutput("Saved RuleType: " + ruleType);
        api.logging().logToOutput("Saved ParameterName: " + parameterName);

        loadFromPreferences();
    }

    /**
     * Initializes the replacement pattern if the RuleType is BODY_REGEX.
     */
    private void initializeReplacementPattern() {
        if (ruleType == RuleType.BODY_REGEX && parameterName != null && !parameterName.isEmpty()) {
            replacementPattern = Pattern.compile(
                    "\"" + Pattern.quote(parameterName) + "\"\\s*:\\s*\"(.*?)\"",
                    Pattern.DOTALL | Pattern.MULTILINE
            );
            api.logging().logToOutput("Initialized replacement pattern: " + replacementPattern.pattern());
        }
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Pattern getReplacementPattern() {
        return replacementPattern;
    }

}
