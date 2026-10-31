package de.espend.idea.php.toolbox.provider.source.contributor;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.language.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.toolbox.dict.json.JsonProvider;
import de.espend.idea.php.toolbox.extension.SourceContributorInterface;
import de.espend.idea.php.toolbox.provider.source.SourceContributorDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.provider.source.SourceContributorParameter;
import de.espend.idea.php.toolbox.provider.source.contributor.utils.ReturnSourceUtil;
import de.espend.idea.php.toolbox.symfony.utils.PhpElementsUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class StringReturnSourceContributor implements SourceContributorInterface {

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

    private void visitReturnElements(Project project, String className, String methodName, ReturnVisitor visitor) {

        for (PhpClass phpClass : PhpIndex.getInstance(project).getAllSubclasses(className)) {

            Method method = phpClass.findOwnMethodByName(methodName);
            if(method == null) {
                continue;
            }

            String string = PhpElementsUtil.getMethodReturnAsString(method);
            if(string != null) {
                visitor.visit(method, string);
            }
        }
    }

    private interface ReturnVisitor {
        void visit(Method method, String contents);
    }

    @Override
    public String getName() {
        return "return";
    }

    private static class MyReturnTargetVisitor implements ReturnVisitor {

        private final String contents;
        private final Collection<PsiElement> psiElements;

        public MyReturnTargetVisitor(String contents, Collection<PsiElement> psiElements) {
            this.contents = contents;
            this.psiElements = psiElements;
        }

        @Override
        public void visit(Method method, String returnValue) {
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
        public void visit(Method method, String contents) {
            LookupElementBuilder decoratedLookupElementBuilder = ReturnSourceUtil.buildLookupElement(method, contents, provider.getDefaults());
            lookupElements.add(decoratedLookupElementBuilder);
        }
    }
}
