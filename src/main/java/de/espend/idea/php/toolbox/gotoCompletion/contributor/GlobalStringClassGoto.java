package de.espend.idea.php.toolbox.gotoCompletion.contributor;

import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.codeEditor.Editor;
import consulo.project.Project;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.util.ProcessingContext;
import consulo.util.collection.ContainerUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.toolbox.gotoCompletion.GotoCompletionContributor;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * 'Foo::foo'
 * 'Foo'
 */
public class GlobalStringClassGoto implements GotoCompletionContributor {

    @Override
    public void addCompletions(CompletionParameters completionParameters, ProcessingContext context, CompletionResultSet resultSet) {
    }

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {
        PsiElement parent = psiElement.getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return new PsiElement[0];
        }

        // only class names
        String contents = ((StringLiteralExpression) parent).getContents();
        if(contents.length() == 0 || contents.length() > 255) {
            return new PsiElement[0];
        }

        return getPsiElements(psiElement.getProject(), contents);
    }

    public static PsiElement[] getPsiElements(Project project, String contents) {

        Collection<PsiElement> psiElements = new HashSet<>();
        contents = contents.replaceAll("\\\\+", "\\\\");

        // DateTime
        // date
        Matcher matcher = Pattern.compile("^([\\w\\\\-]+)$").matcher(contents);
        if (matcher.find()) {
            PhpIndex phpIndex = PhpIndex.getInstance(project);

            ContainerUtil.addAllNotNull(psiElements, phpIndex.getAnyByFQN(contents));
            ContainerUtil.addAllNotNull(psiElements, phpIndex.getFunctionsByName(contents));

            return psiElements.toArray(new PsiElement[psiElements.size()]);
        }

        // DateTime:format
        // DateTime::format
        matcher = Pattern.compile("^([\\w\\\\-]+):+([\\w_\\-]+)$").matcher(contents);
        if (matcher.find()) {
            for (PhpClass phpClass : PhpIndex.getInstance(project).getAnyByFQN(matcher.group(1))) {
                ContainerUtil.addIfNotNull(psiElements, phpClass.findMethodByName(matcher.group(2)));
            }
            return psiElements.toArray(new PsiElement[psiElements.size()]);
        }

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    @Override
    public ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.psiElement().withParent(StringLiteralExpression.class);
    }
}
