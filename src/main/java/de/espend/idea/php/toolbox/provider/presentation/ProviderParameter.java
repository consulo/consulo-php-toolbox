package de.espend.idea.php.toolbox.provider.presentation;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ProviderParameter {
    public enum TYPE {
        INTEGER, BOOLEAN, STRING
    };

    private final String name;

    private final TYPE type;

    public ProviderParameter(String name, TYPE type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public TYPE getType() {
        return type;
    }
}
