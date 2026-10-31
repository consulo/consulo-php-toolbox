package de.espend.idea.php.toolbox.provider.source.contributor;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.toolbox.extension.SourceContributorInterface;
import de.espend.idea.php.toolbox.provider.source.SourceContributorDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.provider.source.SourceContributorParameter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class SubClassesSourceContributor implements SourceContributorInterface {

    @Override
    public Collection<LookupElement> getLookupElements(SourceContributorParameter parameter) {
        String sourceParameter = parameter.getSourceParameter();
        if(sourceParameter == null) {
            return Collections.emptyList();
        }

        Collection<LookupElement> lookupElements = new ArrayList<>();
        for (PhpClass phpClass : PhpIndex.getInstance(parameter.getProject()).getAllSubclasses(sourceParameter)) {
            lookupElements.add(
                LookupElementBuilder.create(phpClass.getPresentableFQN()).withIcon(phpClass.getIcon())
            );
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

        for (PhpClass phpClass : PhpIndex.getInstance(parameter.getProject()).getAllSubclasses(sourceParameter)) {
            if(StringUtils.stripStart(contents, "\\").equalsIgnoreCase(StringUtils.stripStart(phpClass.getPresentableFQN(), "\\"))) {
                psiElements.add(phpClass);
            }
        }

        return psiElements;
    }

    @Override
    public String getName() {
        return "sub_classes";
    }
}
