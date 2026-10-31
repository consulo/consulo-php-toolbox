package de.espend.idea.php.toolbox.gotoCompletion;

import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.codeEditor.Editor;
import consulo.language.pattern.ElementPattern;
import consulo.language.psi.PsiElement;
import consulo.language.util.ProcessingContext;

import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface GotoCompletionContributor<V extends CompletionParameters> {
    void addCompletions(CompletionParameters completionParameters, ProcessingContext context, CompletionResultSet resultSet);

    PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor);

    ElementPattern<? extends PsiElement> getPattern();
}
