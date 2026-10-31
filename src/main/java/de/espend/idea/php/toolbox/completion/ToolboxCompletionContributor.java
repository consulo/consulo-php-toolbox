package de.espend.idea.php.toolbox.completion;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionType;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.ProcessingContext;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.twig.TwigFileType;
import de.espend.idea.php.toolbox.completion.dict.PhpToolboxCompletionContributorParameter;
import de.espend.idea.php.toolbox.dict.json.JsonRegistrar;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;
import de.espend.idea.php.toolbox.utils.RegistrarMatchUtil;

import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class ToolboxCompletionContributor extends CompletionContributor {

    public ToolboxCompletionContributor() {

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), (completionParameters, processingContext, completionResultSet) -> {
            PsiElement position = completionParameters.getPosition();

            PsiFile containingFile = position.getContainingFile();
            if(containingFile == null) {
                return;
            }

            FileType fileType = containingFile.getFileType();
            if(!(fileType instanceof PhpFileType) && !(fileType instanceof TwigFileType)) {
                return;
            }

            Map<PhpToolboxProviderInterface, Set<JsonRegistrar>> providers = RegistrarMatchUtil.getProviders(position);
            if(providers.size() == 0) {
                return;
            }

            PhpToolboxCompletionContributorParameter parameter = null;
            for (Map.Entry<PhpToolboxProviderInterface, Set<JsonRegistrar>> provider : providers.entrySet()) {

                if(parameter == null) {
                    parameter = new PhpToolboxCompletionContributorParameter(
                        completionParameters,
                        processingContext,
                        completionResultSet,
                        provider.getValue()
                    );
                }

                completionResultSet.addAllElements(provider.getKey().getLookupElements(parameter));
            }
        });
    }

    @Override
    public Language getLanguage() {
        return Language.ANY;
    }
}
