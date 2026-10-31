package de.espend.idea.php.toolbox.completion.dict;

import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementPresentation;
import de.espend.idea.php.toolbox.dict.json.JsonRawLookupElement;
import de.espend.idea.php.toolbox.utils.JsonParseUtil;
import consulo.util.lang.StringUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class JsonLookupElement extends LookupElement {

    private final JsonRawLookupElement jsonLookupElement;

    public JsonLookupElement(JsonRawLookupElement jsonLookupElement) {
        this.jsonLookupElement = jsonLookupElement;
    }

    @Override
    public String getLookupString() {
        return StringUtil.notNullize(jsonLookupElement.getLookupString());
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        JsonParseUtil.decorateLookupElement(presentation, jsonLookupElement);
        presentation.setItemText(getLookupString());
    }

}