package de.espend.idea.php.toolbox.gotoCompletion;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.navigation.GotoDeclarationHandler;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import de.espend.idea.php.toolbox.gotoCompletion.contributor.GlobalStringClassGoto;
import de.espend.idea.php.toolbox.gotoCompletion.contributor.PhpArrayCallbackGotoCompletion;
import de.espend.idea.php.toolbox.matcher.php.docTag.PhpDocTagGotoCompletionContributor;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class PhpGotoDeclarationHandler implements GotoDeclarationHandler {
    static final GotoCompletionContributor<?>[] CONTRIBUTORS = new GotoCompletionContributor<?>[]{
        new PhpArrayCallbackGotoCompletion(),
        new GlobalStringClassGoto(),
        new PhpDocTagGotoCompletionContributor(),
    };

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement psiElement, int i, Editor editor) {
        if (psiElement == null) {
            return null;
        }

        Set<PsiElement> psiElements = new HashSet<>();
        for (GotoCompletionContributor<?> contributor : CONTRIBUTORS) {
            if (!contributor.getPattern().accepts(psiElement)) {
                continue;
            }

            PsiElement[] targets = contributor.getGotoDeclarationTargets(psiElement, i, editor);
            if (targets != null && targets.length > 0) {
                ContainerUtil.addAllNotNull(psiElements, targets);
            }
        }

        return psiElements.toArray(PsiElement.EMPTY_ARRAY);
    }
}
