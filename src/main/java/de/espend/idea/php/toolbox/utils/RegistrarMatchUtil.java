package de.espend.idea.php.toolbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import consulo.virtualFileSystem.fileType.FileType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.util.collection.ContainerUtil;
import java.util.HashMap;
import java.util.HashSet;
import de.espend.idea.php.toolbox.PhpToolboxApplicationService;
import de.espend.idea.php.toolbox.dict.json.JsonRegistrar;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;
import de.espend.idea.php.toolbox.dict.matcher.LanguageMatcherParameter;
import de.espend.idea.php.toolbox.extension.LanguageRegistrarMatcherInterface;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class RegistrarMatchUtil {

    public static Map<PhpToolboxProviderInterface, Set<JsonRegistrar>> getProviders(PsiElement psiElement) {

        Collection<JsonRegistrar> registrars = ExtensionProviderUtil.getRegistrar(psiElement.getProject(), PhpToolboxApplicationService.getInstance(psiElement.getProject()));
        if(registrars.size() == 0) {
            return Collections.emptyMap();
        }

        PsiFile containingFile = psiElement.getContainingFile();
        if(containingFile == null) {
            return Collections.emptyMap();
        }

        FileType fileType = containingFile.getFileType();
        String fileTypeName = fileType.getId();

        Collection<PhpToolboxProviderInterface> providers = null;

        Map<PhpToolboxProviderInterface, Set<JsonRegistrar>> providerMatches = new HashMap<>();

        for (JsonRegistrar registrar : registrars) {

            if(registrar == null) {
                continue;
            }

            Collection<JsonSignature> signatures = registrar.getSignatures();
            if(!fileTypeName.equalsIgnoreCase(registrar.getLanguage()) || registrar.getProvider() == null || signatures.size() == 0) {
                continue;
            }

            // init providers
            if(providers == null) {
                providers = ExtensionProviderUtil.getProviders(psiElement.getProject());
            }

            Collection<PhpToolboxProviderInterface> matchedProviders = new ArrayList<>();
            for (PhpToolboxProviderInterface provider : providers) {
                String name = provider.getName();
                if (name.equals(registrar.getProvider())) {
                    matchedProviders.add(provider);
                }
            }

            if(matchedProviders.size() == 0) {
                continue;
            }

            LanguageMatcherParameter parameter = new LanguageMatcherParameter(psiElement, registrar);

            LanguageRegistrarMatcherInterface matched = psiElement.getProject().getApplication()
                .getExtensionPoint(LanguageRegistrarMatcherInterface.class)
                .findFirstSafe(matcher -> matcher.supports(fileType) && matcher.matches(parameter));

            if(matched != null) {
                for (PhpToolboxProviderInterface matchedProvider : matchedProviders) {
                    providerMatches.computeIfAbsent(matchedProvider, k -> new HashSet<>()).add(registrar);
                }
            }

        }

        return providerMatches;
    }

}
