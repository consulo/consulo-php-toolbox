package de.espend.idea.php.toolbox.provider.source.contributor;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.php.toolbox.dict.json.JsonProvider;
import de.espend.idea.php.toolbox.extension.SourceContributorInterface;
import de.espend.idea.php.toolbox.provider.source.SourceContributorDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.provider.source.SourceContributorParameter;
import de.espend.idea.php.toolbox.provider.source.contributor.utils.ReturnSourceUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class ArrayReturnSourceContributor implements SourceContributorInterface {

    @Override
    public Collection<LookupElement> getLookupElements(SourceContributorParameter parameter) {

        Collection<LookupElement> lookupElements = new ArrayList<>();

        ReturnVisitor visitor = null;

        String sourceParameter = parameter.getSourceParameter();
        if(sourceParameter == null) {
            return Collections.emptyList();
        }

        for (Pair<String, String> s : ReturnSourceUtil.extractParameter(sourceParameter)) {
            if(visitor == null) {
                visitor = new MyReturnLookupVisitor(parameter.getJsonProvider(), lookupElements);
            }

            visitReturnElements(parameter.getProject(), s.getFirst(), s.getSecond(), visitor);
        }

        return lookupElements;
    }

    @Override
    public Collection<PsiElement> getPsiTargets(SourceContributorDeclarationHandlerParameter parameter) {

        String contents = parameter.getHandlerParameter().getContents();
        if(StringUtils.isBlank(contents)) {
            return Collections.emptyList();
        }

        String sourceParameter = parameter.getSourceParameter();
        if(sourceParameter == null) {
            return Collections.emptyList();
        }

        final Collection<PsiElement> psiElements = new ArrayList<>();

        ReturnVisitor visitor = null;

        for (Pair<String, String> s : ReturnSourceUtil.extractParameter(sourceParameter)) {
            if(visitor == null) {
                visitor = new MyReturnTargetVisitor(contents, psiElements);
            }

            visitReturnElements(parameter.getProject(), s.getFirst(), s.getSecond(), visitor);
        }

        return psiElements;
    }

    private void visitReturnElements(Project project, String className, String methodName, final ReturnVisitor visitor) {

        for (PhpClass phpClass : PhpIndex.getInstance(project).getAllSubclasses(className)) {

            final Method method = phpClass.findOwnMethodByName(methodName);
            if(method == null) {
                continue;
            }

            method.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(PsiElement element) {

                    if(!(element instanceof PhpReturn)) {
                        super.visitElement(element);
                        return;
                    }

                    PsiElement firstChild = ((PhpReturn) element).getFirstPsiChild();
                    if(!(firstChild instanceof ArrayCreationExpression)) {
                        return;
                    }

                    for (PsiElement arrayValue : firstChild.getChildren()) {

                        if(arrayValue.getNode().getElementType() != PhpElementTypes.ARRAY_VALUE) {
                            continue;
                        }

                        PsiElement stringLiteral = arrayValue.getFirstChild();
                        if(!(stringLiteral instanceof StringLiteralExpression)) {
                            continue;
                        }

                        String contents = ((StringLiteralExpression) stringLiteral).getContents();
                        if(StringUtils.isNotBlank(contents)) {
                            visitor.visit(method, (StringLiteralExpression) stringLiteral, contents);
                        }

                    }

                    super.visitElement(element);
                }
            });

        }
    }

    private interface ReturnVisitor {
        void visit(Method method, StringLiteralExpression psiElement, String contents);
    }

    @Override
    public String getName() {
        return "return_array";
    }

    private static class MyReturnTargetVisitor implements ReturnVisitor {

        private final String contents;
        private final Collection<PsiElement> psiElements;

        public MyReturnTargetVisitor(String contents, Collection<PsiElement> psiElements) {
            this.contents = contents;
            this.psiElements = psiElements;
        }

        @Override
        public void visit(Method method, StringLiteralExpression psiElement, String returnValue) {
            if (contents.equals(returnValue)) {
                psiElements.add(method);
            }
        }
    }

    private static class MyReturnLookupVisitor implements ReturnVisitor {

        private final JsonProvider provider;
        private final Collection<LookupElement> lookupElements;

        public MyReturnLookupVisitor(JsonProvider provider, Collection<LookupElement> lookupElements) {
            this.provider = provider;
            this.lookupElements = lookupElements;
        }

        @Override
        public void visit(Method method, StringLiteralExpression psiElement, String contents) {
            LookupElementBuilder decoratedLookupElementBuilder = ReturnSourceUtil.buildLookupElement(method, contents, provider.getDefaults());
            lookupElements.add(decoratedLookupElementBuilder);
        }
    }
}
