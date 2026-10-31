package de.espend.idea.php.toolbox.dict;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public class MapObjectBag {

    private final Map<String, Object> parameters;

    public MapObjectBag(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Boolean getParameterBool(String key, Boolean defaultValue) {
        if(!parameters.containsKey(key)) {
            return defaultValue;
        }

        Object o = parameters.get(key);
        return (o instanceof Boolean) ? (Boolean) o : defaultValue;
    }

    @Nullable
    public Boolean getParameterBool(String key) {
        if(!parameters.containsKey(key)) {
            return null;
        }

        Object o = parameters.get(key);
        return (o instanceof Boolean) ? (Boolean) o : null;
    }

    @Nullable
    public String getParameterString(String key) {
        return getParameterString(key, null);
    }

    @Nullable
    public String getParameterString(String key, @Nullable String defaultValue) {
        if(!parameters.containsKey(key)) {
            return defaultValue;
        }

        Object o = parameters.get(key);
        return (o instanceof String) ? (String) o : defaultValue;
    }
}
