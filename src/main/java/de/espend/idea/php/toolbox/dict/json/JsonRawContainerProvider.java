package de.espend.idea.php.toolbox.dict.json;

import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import de.espend.idea.php.toolbox.completion.dict.JsonLookupElement;
import de.espend.idea.php.toolbox.completion.dict.PhpToolboxCompletionContributorParameter;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderAbstract;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.navigation.locator.utils.TargetLocatorUtil;
import de.espend.idea.php.toolbox.type.PhpToolboxTypeProviderArguments;
import de.espend.idea.php.toolbox.type.PhpToolboxTypeProviderInterface;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class JsonRawContainerProvider extends PhpToolboxProviderAbstract implements PhpToolboxTypeProviderInterface {

    private final String name;
    private final Collection<JsonRawLookupElement> items;

    public JsonRawContainerProvider(String name, Collection<JsonRawLookupElement> items) {
        this.name = name;
        this.items = items;
    }

    @Override
    public Collection<LookupElement> getLookupElements(PhpToolboxCompletionContributorParameter parameter) {

        Collection<LookupElement> elements = new ArrayList<>();

        for (JsonRawLookupElement item : items) {
            elements.add(new JsonLookupElement(item));
        }

        return elements;
    }

    @Override
    public Collection<PsiElement> getPsiTargets(PhpToolboxDeclarationHandlerParameter parameter) {
        Collection<PsiElement> psiElements = new ArrayList<>();

        for (JsonRawLookupElement item : items) {
            String target = item.getTarget();
            if(target == null) {
                continue;
            }

            String lookupString = item.getLookupString();
            if(lookupString != null && lookupString.equals(parameter.getContents())) {
                psiElements.addAll(TargetLocatorUtil.getTargetsBySignature(parameter, target));
            }
        }

        return psiElements;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Collection<PhpNamedElement> resolveParameter(PhpToolboxTypeProviderArguments args) {

        Collection<PhpNamedElement> elements = new ArrayList<>();

        for (JsonRawLookupElement jsonRawLookupElement: args.getLookupElements()) {
            String type = jsonRawLookupElement.getType();
            if(type == null || StringUtils.isBlank(type)) {
                continue;
            }

            type = type.replaceAll("\\\\+", "\\\\");

            // internal fully fqn needed by converter since phpstorm9;
            // we normalize it on our side for a unique collection
            if(!type.startsWith("\\")) {
                type = "\\" + type;
            }

            if(args.getParameter().equals(jsonRawLookupElement.getLookupString())) {
                elements.addAll(PhpIndex.getInstance(args.getProject()).getAnyByFQN(type));
            }
        }

        return elements;
    }
}
