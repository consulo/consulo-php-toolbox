package de.espend.idea.php.toolbox.navigation.locator.utils;

import consulo.language.psi.PsiElement;
import de.espend.idea.php.toolbox.extension.PhpToolboxTargetLocator;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.navigation.locator.TargetLocatorParameter;

import java.util.Collection;
import java.util.HashSet;

public class TargetLocatorUtil {

    public static Collection<PsiElement> getTargetsBySignature(PhpToolboxDeclarationHandlerParameter parameter, String target) {

        Collection<PsiElement> psiElements = new HashSet<>();
        TargetLocatorParameter locatorParameter = new TargetLocatorParameter(parameter, target);
        parameter.getProject().getApplication().getExtensionPoint(PhpToolboxTargetLocator.class)
            .forEachExtensionSafe(locator -> psiElements.addAll(locator.getTargets(locatorParameter)));

        return psiElements;
    }

}
