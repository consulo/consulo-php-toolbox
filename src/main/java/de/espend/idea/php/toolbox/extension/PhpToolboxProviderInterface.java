package de.espend.idea.php.toolbox.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiElement;
import de.espend.idea.php.toolbox.completion.dict.PhpToolboxCompletionContributorParameter;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.provider.presentation.ProviderPresentation;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface PhpToolboxProviderInterface {

    Collection<LookupElement> getLookupElements(PhpToolboxCompletionContributorParameter parameter);

    Collection<PsiElement> getPsiTargets(PhpToolboxDeclarationHandlerParameter parameter);

    String getName();

    @Nullable
    ProviderPresentation getPresentation();
}
