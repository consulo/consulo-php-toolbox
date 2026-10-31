package de.espend.idea.php.toolbox.provider.source.contributor.utils;

import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.util.lang.Pair;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import de.espend.idea.php.toolbox.dict.json.JsonRawLookupElement;
import de.espend.idea.php.toolbox.utils.JsonParseUtil;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import consulo.component.util.Iconable;
import consulo.util.lang.StringUtil;
import consulo.language.icon.IconDescriptorUpdaters;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ReturnSourceUtil {

    /**
     * Extract valid class method syntax
     * "Foo::bar, Foo:car"
     */
    public static Collection<Pair<String, String>> extractParameter(String parameters) {
        Collection<Pair<String, String>> methods = new ArrayList<>();

        String[] sourceParameter = parameters.split(",");
        for (String s : sourceParameter) {
            String[] split = s.trim().replaceAll("(:)\\1", "$1").split(":");
            if(split.length < 2 || StringUtil.isEmptyOrSpaces(split[0]) || StringUtil.isEmpty(split[1])) {
                continue;
            }

            methods.add(Pair.create(split[0], split[1]));
        }

        return methods;
    }

    public static LookupElementBuilder buildLookupElement(Method method, String contents, @Nullable JsonRawLookupElement jsonRawLookupElement) {
        LookupElementBuilder lookupElement = LookupElementBuilder.create(contents);
        PhpClass phpClass = method.getContainingClass();

        if(phpClass != null) {
            lookupElement = lookupElement.withTypeText(phpClass.getPresentableFQN(), true);
            lookupElement = lookupElement.withIcon(IconDescriptorUpdaters.getIcon(phpClass, Iconable.ICON_FLAG_VISIBILITY));
        }

        return JsonParseUtil.getDecoratedLookupElementBuilder(
            lookupElement,
            jsonRawLookupElement
        );
    }
}
