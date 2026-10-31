package de.espend.idea.php.toolbox.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import de.espend.idea.php.toolbox.completion.dict.ToolboxJsonFileCompletionArguments;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface ToolboxJsonFileCompletion {
    void addCompletions(ToolboxJsonFileCompletionArguments arguments);
}
