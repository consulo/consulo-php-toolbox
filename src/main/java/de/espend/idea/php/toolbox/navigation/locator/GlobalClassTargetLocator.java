package de.espend.idea.php.toolbox.navigation.locator;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.espend.idea.php.toolbox.extension.PhpToolboxTargetLocator;
import de.espend.idea.php.toolbox.gotoCompletion.contributor.GlobalStringClassGoto;

import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class GlobalClassTargetLocator implements PhpToolboxTargetLocator {

    @Override
    public Collection<PsiElement> getTargets(TargetLocatorParameter parameter) {
        return new HashSet<>(Arrays.asList(
            GlobalStringClassGoto.getPsiElements(parameter.getProject(), parameter.getTarget()))
        );
    }
}
