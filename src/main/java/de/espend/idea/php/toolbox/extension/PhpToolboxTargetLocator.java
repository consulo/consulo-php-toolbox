package de.espend.idea.php.toolbox.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.language.psi.PsiElement;
import de.espend.idea.php.toolbox.navigation.locator.TargetLocatorParameter;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface PhpToolboxTargetLocator {

    Collection<PsiElement> getTargets(TargetLocatorParameter parameter);
}
