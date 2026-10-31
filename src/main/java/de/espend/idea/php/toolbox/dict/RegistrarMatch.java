package de.espend.idea.php.toolbox.dict;

import de.espend.idea.php.toolbox.dict.json.JsonRegistrar;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class RegistrarMatch {

    private final PhpToolboxProviderInterface provider;
    private final JsonRegistrar registrar;

    public RegistrarMatch(PhpToolboxProviderInterface provider, JsonRegistrar registrar) {
        this.provider = provider;
        this.registrar = registrar;
    }

    public PhpToolboxProviderInterface getProvider() {
        return provider;
    }

    public JsonRegistrar getRegistrar() {
        return registrar;
    }

}
