package de.espend.idea.php.toolbox.provider.source.contributor.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import consulo.ide.IconDescriptorUpdaters;
import de.espend.idea.php.toolbox.dict.json.JsonRawLookupElement;
import de.espend.idea.php.toolbox.utils.JsonParseUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ReturnSourceUtil {

    /**
     * Extract valid class method syntax
     * "Foo::bar, Foo:car"
     */
    public static Collection<Pair<String, String>> extractParameter(@NotNull String parameters) {
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

    public static LookupElementBuilder buildLookupElement(@NotNull Method method, @NotNull String contents, @Nullable JsonRawLookupElement jsonRawLookupElement) {
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
