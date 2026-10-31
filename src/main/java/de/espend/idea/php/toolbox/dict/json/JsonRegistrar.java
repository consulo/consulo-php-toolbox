package de.espend.idea.php.toolbox.dict.json;

import de.espend.idea.php.toolbox.utils.JsonParseUtil;

import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class JsonRegistrar {

    private Collection<String> signature = new HashSet<>();

    private Collection<JsonSignature> signatures = new ArrayList<>();

    @Nullable
    private Collection<JsonSignature> mySignatures = null;

    @Nullable
    private String provider;

    @Nullable
    private String language;

    private Map<String, Object> parameters = new HashMap<>();

    private boolean references = false ;

    public JsonRegistrar() {
    }

    @Nullable
    public String getProvider() {
        return provider;
    }

    @Nullable
    public String getLanguage() {
        return language;
    }

    public Collection<JsonSignature> getSignatures() {
        if(this.mySignatures != null) {
            return this.mySignatures;
        }

        Collection<JsonSignature> result = new ArrayList<>(signatures);

        if(this.signature.size() > 0) {
            result.addAll(JsonParseUtil.createSignaturesFromStrings(this.signature));
        }

        return this.mySignatures = result;
    }

    public boolean isReferences() {
        return references;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

}

