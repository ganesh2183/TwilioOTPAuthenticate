package burp;

import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.sessions.ActionResult;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import burp.api.montoya.http.sessions.SessionHandlingActionData;
import burp.api.montoya.MontoyaApi;
import twilio.OTPHandler;
import utils.ConfigurationParser;
import utils.RuleType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static burp.api.montoya.http.message.HttpHeader.httpHeader;
import static burp.api.montoya.http.message.params.HttpParameter.parameter;

public class MySessionHandlingAction implements SessionHandlingAction {
    private final MontoyaApi api;
    private final OTPHandler otpHandler;
    private final ConfigurationParser configParser;

    public MySessionHandlingAction(MontoyaApi api, OTPHandler otpHandler, ConfigurationParser configParser) {
        this.api = api;
        this.otpHandler = otpHandler;
        this.configParser = configParser;
    }

    @Override
    public String name() {
        return "Twilio OTP Authenticate";
    }

    @Override
    public ActionResult performAction(SessionHandlingActionData actionData) {
        RuleType ruleType = configParser.getRuleType();
        String parameterName = configParser.getParameterName();
        Pattern replacementPattern = configParser.getReplacementPattern();

        if (ruleType == null || parameterName == null){
            api.logging().logToError("Invalid configuration: RuleType or ParameterName is missing.");
            return ActionResult.actionResult(actionData.request());
        }

        HttpRequest request = actionData.request();
        String latestOtp;

        try {
            latestOtp = otpHandler.getLatestOTPAsync().get();
            api.logging().logToOutput("Generated OTP: " + latestOtp);
        } catch (Exception e) {
            api.logging().logToError("Failed to generate OTP: " + e.getMessage());
            return ActionResult.actionResult(request);
        }

        HttpRequest newRequest = switch (ruleType) {
            case HEADER -> updateOrAddTokenInHeader(request, parameterName, latestOtp);
            case URL -> updateOrAddTokenInParameter(request, HttpParameterType.URL, parameterName, latestOtp);
            case COOKIE -> updateOrAddTokenInParameter(request, HttpParameterType.COOKIE, parameterName, latestOtp);
            case BODY_PARAM -> updateOrAddTokenInParameter(request, HttpParameterType.BODY, parameterName, latestOtp);
            case BODY_REGEX -> updateTokenInBody(request, parameterName, latestOtp, replacementPattern);
        };

        return ActionResult.actionResult(newRequest);
    }

    private HttpRequest updateOrAddTokenInHeader(HttpRequest request, String parameterName, String latestOtp) {
        return isParameterNamePresentInHeaders(request, parameterName)
                ? request.withUpdatedHeader(httpHeader(parameterName, latestOtp))
                : request.withAddedHeader(httpHeader(parameterName, latestOtp));
    }

    private HttpRequest updateOrAddTokenInParameter(HttpRequest request, HttpParameterType parameterType, String parameterName, String latestOtp) {
        return isParameterNamePresentInParameters(request, parameterType, parameterName)
                ? request.withUpdatedParameters(parameter(parameterName, latestOtp, parameterType))
                : request.withAddedParameters(parameter(parameterName, latestOtp, parameterType));
    }

    private HttpRequest updateTokenInBody(HttpRequest request, String parameterName ,String latestOtp, Pattern replacementPattern) {
        String body = request.bodyToString();

        if (replacementPattern == null) {
            api.logging().logToError("Replacement pattern not initialized.");
            return request;
        }

        Matcher matcher = replacementPattern.matcher(body);

        if (matcher.find()) {
            String updatedBody = matcher.replaceFirst("\"" + parameterName + "\":\"" + latestOtp + "\"");
            api.logging().logToOutput("Updated request body with OTP: " + updatedBody);
            return request.withBody(updatedBody);
        } else {
            api.logging().logToError("Parameter '" + parameterName + "' not found in the body.");
            return request;
        }
    }

    private boolean isParameterNamePresentInHeaders(HttpRequest request, String parameterName) {
        for (HttpHeader h : request.headers()) {
            if (h.name().equalsIgnoreCase(parameterName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isParameterNamePresentInParameters(HttpRequest request, HttpParameterType parameterType, String parameterName) {
        for (ParsedHttpParameter p : request.parameters()) {
            if (p.type().equals(parameterType) && p.name().equalsIgnoreCase(parameterName)) {
                return true;
            }
        }
        return false;
    }
}