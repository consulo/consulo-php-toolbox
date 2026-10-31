package de.espend.idea.php.toolbox.provider.php;

import consulo.annotation.component.ExtensionImpl;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class ClassProvider extends PhpIndexAbstractProviderAbstract {

    @Override
    protected Collection<PhpClass> getPhpClassesForLookup(PhpIndex phpIndex, String className) {
        return phpIndex.getClassesByName(className);
    }

    @Override
    public String getName() {
        return "class";
    }
}

