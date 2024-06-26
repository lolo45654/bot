package blade.util;

public enum ComboType implements EnumStringIdentifier {
    AUTO("bot:auto"),
    S_TAP("bot:s_tap"),
    WASD("bot:wasd"),

    ;

    private final String identifier;

    ComboType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String asString() {
        return identifier;
    }
}
