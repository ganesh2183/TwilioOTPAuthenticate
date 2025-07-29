package utils;
import burp.api.montoya.http.message.params.HttpParameterType;

public enum RuleType
{
    HEADER,
    URL,
    COOKIE,
    BODY_PARAM,
    BODY_REGEX;

    public HttpParameterType toParameterType() {
        return switch (this) {
            case URL -> HttpParameterType.URL;
            case COOKIE -> HttpParameterType.COOKIE;
            case BODY_PARAM -> HttpParameterType.BODY;
            default -> throw new UnsupportedOperationException("RuleType '" + this + "' cannot be converted to HttpParameterType");
        };
    }
}
