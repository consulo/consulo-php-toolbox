package de.espend.idea.php.toolbox.provider.php;

import consulo.annotation.component.ExtensionImpl;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.toolbox.completion.dict.PhpToolboxCompletionContributorParameter;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class TraitProvider extends PhpIndexAbstractProviderAbstract {

    protected Collection<String> getClasses(PhpToolboxCompletionContributorParameter parameter) {
        return PhpIndex.getInstance(parameter.getProject()).getAllTraitNames();
    }

    @Override
    protected Collection<PhpClass> getPhpClassesForLookup(PhpIndex phpIndex, String name) {
        return phpIndex.getTraitsByName(name);
    }

    protected Collection<PhpClass> resolveParameter(PhpIndex phpIndex, String parameter) {
        return phpIndex.getTraitsByFQN(parameter);
    }

    @Override
    public String getName() {
        return "trait";
    }
}

