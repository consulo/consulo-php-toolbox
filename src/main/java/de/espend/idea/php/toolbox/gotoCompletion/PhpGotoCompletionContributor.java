package de.espend.idea.php.toolbox.gotoCompletion;

import com.jetbrains.php.lang.PhpLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionType;

/**
 * Completion part of {@link PhpGotoDeclarationHandler}; split out because Consulo registers one extension per class.
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class PhpGotoCompletionContributor extends CompletionContributor {
    public PhpGotoCompletionContributor() {
        for (GotoCompletionContributor<?> contributor : PhpGotoDeclarationHandler.CONTRIBUTORS) {
            extend(CompletionType.BASIC, contributor.getPattern(), contributor::addCompletions);
        }
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
