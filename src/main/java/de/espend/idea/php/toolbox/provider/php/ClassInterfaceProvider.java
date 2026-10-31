package de.espend.idea.php.toolbox.provider.php;

import consulo.annotation.component.ExtensionImpl;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.toolbox.matcher.php.docTag.ToolboxDocTagAliasInterface;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class ClassInterfaceProvider extends PhpIndexAbstractProviderAbstract implements ToolboxDocTagAliasInterface {

    @Override
    public String getName() {
        return "class_interface";
    }

    @Override
    protected Collection<PhpClass> getPhpClassesForLookup(final PhpIndex phpIndex, final String className) {
        return new HashSet<PhpClass>() {{
            addAll(phpIndex.getClassesByName(className));
            addAll(phpIndex.getInterfacesByName(className));
        }};
    }

    @Override
    public String[] getInlineStrings() {
        return new String[] {"ClassInterface"};
    }
}
