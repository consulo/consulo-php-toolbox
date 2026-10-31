package de.espend.idea.php.toolbox.dict.matcher;

import consulo.language.psi.PsiElement;
import de.espend.idea.php.toolbox.dict.json.JsonRegistrar;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class LanguageMatcherParameter {

    private final PsiElement psiElement;

    private final JsonRegistrar registrar;

    public LanguageMatcherParameter(PsiElement psiElement, JsonRegistrar registrar) {
        this.psiElement = psiElement;
        this.registrar = registrar;
    }

    public PsiElement getElement() {
        return psiElement;
    }

    public JsonRegistrar getRegistrar() {
        return registrar;
    }

    public Collection<JsonSignature> getSignatures() {
        return getRegistrar().getSignatures();
    }
}
