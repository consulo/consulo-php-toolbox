package de.espend.idea.php.toolbox.gotoCompletion.contributor;

import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.codeEditor.Editor;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.pattern.PsiElementPattern;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.php.toolbox.gotoCompletion.GotoCompletionContributor;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 *
 * [$this, '<caret>']
 * [$foo, '<caret>']
 */
public class PhpArrayCallbackGotoCompletion implements GotoCompletionContributor {

    @Override
    public void addCompletions(CompletionParameters completionParameters, ProcessingContext context, CompletionResultSet resultSet) {
        PsiElement position = completionParameters.getPosition();

        PhpClass phpClass = findClassCallback(position);
        if(phpClass == null) {
            return;
        }

        for (Method method : phpClass.getMethods()) {
            String name = method.getName();

            // __construct
            if(name.startsWith("__")) {
                continue;
            }

            LookupElementBuilder lookupElement = LookupElementBuilder.create(name).withIcon(method.getIcon());

            PhpClass containingClass = method.getContainingClass();
            if(containingClass != null) {
                lookupElement = lookupElement.withTypeText(containingClass.getPresentableFQN(), true);
            }

            resultSet.addElement(lookupElement);
        }
    }

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {

        Collection<PsiElement> psiElements = new HashSet<>();

        PsiElement parent = psiElement.getParent();
        if(parent instanceof StringLiteralExpression) {
            String contents = ((StringLiteralExpression) parent).getContents();
            if(StringUtils.isNotBlank(contents)) {
                PhpClass phpClass = findClassCallback(psiElement);
                if(phpClass != null) {
                    Method method = phpClass.findMethodByName(contents);
                    if(method != null) {
                        psiElements.add(method);
                    }
                }
            }
        }

        return psiElements.toArray(new PsiElement[psiElements.size()]);
    }

    /**
     * [$this, '']
     * array($this, '')
     */

    @Override
    public PsiElementPattern.Capture<PsiElement> getPattern() {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(
                PlatformPatterns.psiElement().withElementType(PhpElementTypes.ARRAY_VALUE).afterLeafSkipping(
                    PlatformPatterns.psiElement(PsiWhiteSpace.class),
                    PlatformPatterns.psiElement().withText(",")
                ).afterSiblingSkipping(
                    PlatformPatterns.psiElement(PsiWhiteSpace.class),
                    PlatformPatterns.psiElement().withElementType(PhpElementTypes.ARRAY_VALUE).withFirstNonWhitespaceChild(
                        PlatformPatterns.psiElement(Variable.class)
                    ).afterLeafSkipping(
                        PlatformPatterns.psiElement(PsiWhiteSpace.class),
                        PlatformPatterns.psiElement().withText(PlatformPatterns.string().oneOf("[", "("))
                    )
                )
            )
        );
    }

    @Nullable
    private PhpClass findClassCallback(PsiElement position) {
        ArrayCreationExpression arrayCreation = PsiTreeUtil.getParentOfType(position, ArrayCreationExpression.class);
        if(arrayCreation == null) {
            return null;
        }

        PhpPsiElement arrayValue = arrayCreation.getFirstPsiChild();
        if(arrayValue == null || arrayValue.getNode().getElementType() != PhpElementTypes.ARRAY_VALUE) {
            return null;
        }

        PhpPsiElement variable = arrayValue.getFirstPsiChild();
        if(!(variable instanceof Variable)) {
            return null;
        }

        PsiElement resolve = ((Variable) variable).resolve();
        if(resolve instanceof PhpClass) {
            return (PhpClass) resolve;
        }

        for (String s : ((Variable) variable).getType().filterPrimitives().getTypes()) {
            Collection<PhpClass> anyByFQN = PhpIndex.getInstance(position.getProject()).getAnyByFQN(s);
            if(anyByFQN.size() > 0) {
                return anyByFQN.iterator().next();
            }
        }

        return null;
    }
}
