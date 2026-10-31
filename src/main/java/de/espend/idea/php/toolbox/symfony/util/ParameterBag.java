package de.espend.idea.php.toolbox.symfony.util;

import org.jspecify.annotations.Nullable;
import consulo.language.psi.PsiElement;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ParameterBag {

    private int index;
    private PsiElement psiElement;

    public ParameterBag(int index, PsiElement psiElement) {
        this.index = index;
        this.psiElement = psiElement;
    }

    public int getIndex() {
        return index;
    }

    @Nullable
    public String getValue() {
        return PsiElementUtils.getMethodParameter(psiElement);
    }

    public PsiElement getElement() {
        return this.psiElement;
    }

}
