package de.espend.idea.php.toolbox.provider.php;

import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.util.lang.StringUtil;
import consulo.language.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import de.espend.idea.php.toolbox.completion.dict.PhpToolboxCompletionContributorParameter;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderAbstract;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.type.PhpToolboxTypeProviderArguments;
import de.espend.idea.php.toolbox.type.PhpToolboxTypeProviderInterface;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
abstract class PhpIndexAbstractProviderAbstract extends PhpToolboxProviderAbstract implements PhpToolboxTypeProviderInterface {

    public Collection<LookupElement> getLookupElements(PhpToolboxCompletionContributorParameter parameter) {
        PhpIndex instance = PhpIndex.getInstance(parameter.getProject());

        Collection<LookupElement> lookupElements = new ArrayList<>();
        for (String className : getClasses(parameter)) {
            // strip double backslash
            className = className.replaceAll("\\\\+", "\\\\");

            for (PhpClass phpClass : getPhpClassesForLookup(instance, className)) {
                lookupElements.add(
                    LookupElementBuilder.create(phpClass.getPresentableFQN()).withIcon(phpClass.getIcon())
                );
            }
        }

        return lookupElements;
    }

    protected Collection<String> getClasses(PhpToolboxCompletionContributorParameter parameter) {
        return PhpIndex.getInstance(parameter.getProject()).getAllClassNames(parameter.getCompletionResultSet().getPrefixMatcher());
    }

    protected abstract Collection<PhpClass> getPhpClassesForLookup(PhpIndex phpIndex, String className);

    protected Collection<PhpClass> resolveParameter(PhpIndex phpIndex, String parameter) {
        return phpIndex.getAnyByFQN(parameter);
    }

    @Override
    public Collection<PsiElement> getPsiTargets(PhpToolboxDeclarationHandlerParameter parameter) {
        String contents = parameter.getContents();
        if(!StringUtil.startsWithChar(contents, '\\')) {
            contents = "\\" + contents;
        }

        return new ArrayList<>(
            resolveParameter(PhpIndex.getInstance(parameter.getProject()), contents)
        );
    }

    @Nullable
    @Override
    public Collection<PhpNamedElement> resolveParameter(PhpToolboxTypeProviderArguments args) {
        String type = args.getParameter().replaceAll("\\\\+", "\\\\");

        return new HashSet<>(
            PhpIndex.getInstance(args.getProject()).getAnyByFQN(type)
        );
    }

}
