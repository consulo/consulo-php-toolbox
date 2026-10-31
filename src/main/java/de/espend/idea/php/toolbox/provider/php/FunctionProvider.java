package de.espend.idea.php.toolbox.provider.php;

import consulo.ui.image.Image;
import consulo.annotation.component.ExtensionImpl;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;


import org.jspecify.annotations.Nullable;
import consulo.application.util.matcher.PrefixMatcher;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.language.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import de.espend.idea.php.toolbox.completion.dict.PhpToolboxCompletionContributorParameter;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.provider.presentation.ProviderPresentation;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class FunctionProvider implements PhpToolboxProviderInterface {

    @Override
    public Collection<LookupElement> getLookupElements(PhpToolboxCompletionContributorParameter parameter) {
        return PhpIndex.getInstance(parameter.getProject())
            .getAllFunctionNames(PrefixMatcher.ALWAYS_TRUE).stream().map(
                s -> LookupElementBuilder.create(s).withIcon(PlatformIconGroup.nodesFunction())
            )
            .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Collection<PsiElement> getPsiTargets(PhpToolboxDeclarationHandlerParameter parameter) {
        return new HashSet<PsiElement>() {{
            addAll(PhpIndex.getInstance(parameter.getProject()).getFunctionsByFQN(parameter.getContents()));
        }};
    }

    @Override
    public String getName() {
        return "php.functions";
    }

    @Nullable
    @Override
    public ProviderPresentation getPresentation() {
        return new ProviderPresentation() {
            @Nullable
            @Override
            public Image getIcon() {
                return PlatformIconGroup.nodesFunction();
            }

            @Nullable
            @Override
            public String getDescription() {
                return "Functions";
            }
        };
    }
}
