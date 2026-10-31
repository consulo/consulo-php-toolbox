package de.espend.idea.php.toolbox.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiElement;
import de.espend.idea.php.toolbox.provider.source.SourceContributorDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.provider.source.SourceContributorParameter;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface SourceContributorInterface {

    Collection<LookupElement> getLookupElements(SourceContributorParameter parameter);

    Collection<PsiElement> getPsiTargets(SourceContributorDeclarationHandlerParameter parameter);

    String getName();

}
