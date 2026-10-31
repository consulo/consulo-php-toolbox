package de.espend.idea.php.toolbox.matcher.php.docTag;

import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.codeEditor.Editor;
import java.util.function.Predicate;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.ProcessingContext;
import consulo.util.collection.ContainerUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.toolbox.completion.dict.PhpToolboxCompletionContributorParameter;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;
import de.espend.idea.php.toolbox.gotoCompletion.GotoCompletionContributor;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.utils.ExtensionProviderUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpDocTagGotoCompletionContributor implements GotoCompletionContributor {
    @Override
    public void addCompletions(CompletionParameters completionParameters, ProcessingContext context, CompletionResultSet resultSet) {

        PsiElement psiElement = completionParameters.getPosition().getParent();
        if(psiElement == null) {
            return;
        }

        Collection<PhpDocParamTag> docTags = PhpDocUtil.getDocTagsForScope(psiElement);
        if(docTags == null || docTags.size() == 0) {
            return;
        }

        PhpToolboxCompletionContributorParameter parameter = null;

        for(PhpDocParamTag phpDocParamTag: docTags) {
            String providerName = extractProviderName(phpDocParamTag.getText());
            if(providerName == null) {
                continue;
            }

            List<PhpToolboxProviderInterface> filter = getProvidersByName(
                psiElement,
                providerName
            );

            if(filter.size() == 0) {
                continue;
            }

            if(parameter == null) {
                parameter = new PhpToolboxCompletionContributorParameter(completionParameters, context, resultSet, new HashSet<>());
            }

            for (PhpToolboxProviderInterface provider : filter) {
                resultSet.addAllElements(provider.getLookupElements(parameter));
            }
        }
    }

    /**
     * #Foo, #Foo, #Foo.Foo
     */
    @Nullable
    private String extractProviderName(String text) {
        Matcher matcher = Pattern.compile("\\s*#([\\.\\w-]+)\\s*").matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private List<PhpToolboxProviderInterface> getProvidersByName(PsiElement psiElement, String providerName) {
        // find provider on name or interface alias
        return ContainerUtil.filter(
            ExtensionProviderUtil.getProviders(psiElement.getProject()),
            new PhpToolboxProviderInterfaceCondition(providerName)
        );
    }

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {

        PsiElement element = psiElement.getParent();
        if (!(element instanceof StringLiteralExpression)) {
            return new PsiElement[0];
        }

        String contents = ((StringLiteralExpression) element).getContents();
        if (StringUtils.isBlank(contents)) {
            return new PsiElement[0];
        }

        PhpToolboxDeclarationHandlerParameter args = null;

        Collection<PsiElement> targets = new ArrayList<>();

        Collection<PhpDocParamTag> docTags = PhpDocUtil.getDocTagsForScope(psiElement);
        if (docTags == null || docTags.size() == 0) {
            return new PsiElement[0];
        }

        for (PhpDocParamTag phpDocParamTag : docTags) {
            String providerName = extractProviderName(phpDocParamTag.getText());
            if (providerName == null) {
                continue;
            }

            List<PhpToolboxProviderInterface> filter = getProvidersByName(
                psiElement,
                providerName
            );

            if (filter.size() == 0) {
                continue;
            }

            if (args == null) {
                PsiFile containingFile = psiElement.getContainingFile();
                if (containingFile == null) {
                    break;
                }

                args = new PhpToolboxDeclarationHandlerParameter(psiElement, contents, containingFile.getFileType());
            }

            for (PhpToolboxProviderInterface provider : filter) {
                targets.addAll(provider.getPsiTargets(args));
            }
        }

        return targets.toArray(new PsiElement[targets.size()]);
    }

    @Override
    public ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression.class)
                .withParent(ParameterList.class)
        );
    }

    private static class PhpToolboxProviderInterfaceCondition implements Predicate<PhpToolboxProviderInterface> {
        private final String providerName;

        public PhpToolboxProviderInterfaceCondition(String providerName) {
            this.providerName = providerName;
        }

        @Override
        public boolean test(PhpToolboxProviderInterface provider) {
            return providerName.equalsIgnoreCase(provider.getName()) || (
                provider instanceof ToolboxDocTagAliasInterface &&
                ArrayUtils.contains(((ToolboxDocTagAliasInterface) provider).getInlineStrings(), providerName)
            );
        }
    }
}
