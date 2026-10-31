package de.espend.idea.php.toolbox.completion.dict;

import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import de.espend.idea.php.toolbox.icon.PhpToolboxIconGroup;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ToolboxJsonFileCompletionArguments {

    public enum TYPE {
        LANGUAGE, SIGNATURE_TYPE,
        SIGNATURE_KEY, PROVIDER_KEY
    }

    private final TYPE type;

    private final CompletionResultSet resultSet;

    public ToolboxJsonFileCompletionArguments(TYPE type, CompletionResultSet resultSet) {
        this.type = type;
        this.resultSet = resultSet;
    }

    public CompletionResultSet getResultSet() {
        return resultSet;
    }

    public void addLookupString(String lookup) {
        resultSet.addElement(LookupElementBuilder.create(lookup).withIcon(PhpToolboxIconGroup.toolbox()));
    }

    public TYPE getType() {
        return type;
    }
}
