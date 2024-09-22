package blade.utils.blade;

public class ConfigKey<T> {
    public static <T> ConfigKey<T> key(String name, T defaultValue) {
        return new ConfigKey<>(name, defaultValue);
    }

    private final String name;
    private final T defaultValue;

    protected ConfigKey(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConfigKey<?> key)) return false;
        return name.equals(key.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "config[" + name + "]";
    }

    public T getDefaultValue() {
        return defaultValue;
    }
}
