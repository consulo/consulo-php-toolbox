package de.espend.idea.php.toolbox.provider;

import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiElement;
import de.espend.idea.php.toolbox.completion.dict.PhpToolboxCompletionContributorParameter;
import de.espend.idea.php.toolbox.dict.json.JsonProvider;
import de.espend.idea.php.toolbox.dict.json.JsonProviderSource;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderAbstract;
import de.espend.idea.php.toolbox.extension.SourceContributorInterface;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.provider.source.SourceContributorDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.provider.source.SourceContributorParameter;
import de.espend.idea.php.toolbox.utils.ExtensionProviderUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SourceProvider extends PhpToolboxProviderAbstract {

    private final String name;
    private final Collection<JsonProvider> sourceProviders;

    public SourceProvider(String name, Collection<JsonProvider> sourceProviders) {
        this.name = name;
        this.sourceProviders = sourceProviders;
    }

    @Override
    public Collection<LookupElement> getLookupElements(PhpToolboxCompletionContributorParameter parameter) {

        Collection<LookupElement> lookupElements = new ArrayList<>();
        SourceContributorParameter params = null;
        for (JsonProvider sourceProvider : sourceProviders) {
            JsonProviderSource source = sourceProvider.getSource();
            String contributor = source != null ? source.getContributor() : null;
            if(contributor == null) {
                continue;
            }

            SourceContributorInterface sourceContributor = ExtensionProviderUtil.getSourceContributor(parameter.getProject(), contributor);
            if(sourceContributor == null) {
                continue;
            }

            if(params == null) {
                params = new SourceContributorParameter(parameter, sourceProvider);
            }

            lookupElements.addAll(sourceContributor.getLookupElements(params));
        }

        return lookupElements;
    }

    @Override
    public Collection<PsiElement> getPsiTargets(PhpToolboxDeclarationHandlerParameter parameter) {

        Collection<PsiElement> targets = new ArrayList<>();
        SourceContributorDeclarationHandlerParameter params = null;
        for (JsonProvider sourceProvider : sourceProviders) {
            JsonProviderSource source = sourceProvider.getSource();
            String contributor = source != null ? source.getContributor() : null;
            if(contributor == null) {
                continue;
            }

            SourceContributorInterface sourceContributor = ExtensionProviderUtil.getSourceContributor(parameter.getProject(), contributor);
            if(sourceContributor == null) {
                continue;
            }

            if(params == null) {
                params = new SourceContributorDeclarationHandlerParameter(parameter, sourceProvider);
            }

            targets.addAll(sourceContributor.getPsiTargets(params));
        }

        return targets;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
