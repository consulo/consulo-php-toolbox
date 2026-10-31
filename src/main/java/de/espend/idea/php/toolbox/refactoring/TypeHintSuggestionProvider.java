package de.espend.idea.php.toolbox.refactoring;

import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.refactoring.rename.NameSuggestionProvider;
import consulo.language.editor.refactoring.rename.SuggestedNameInfo;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class TypeHintSuggestionProvider implements NameSuggestionProvider {
    private static final String[] TRIM_STRINGS = new String[]{
        "interface", "abstract", "extension", "test", "resolver",
        "factory", "loader", "provider", "manager", "service",
        "logger", "plugin",
    };

    @Nullable
    @Override
    public SuggestedNameInfo getSuggestedNames(PsiElement psiElement, @Nullable PsiElement nameSuggestionContext, Set<String> set) {
        if (!(psiElement instanceof Parameter parameter)) {
            return null;
        }

        List<String> filter = variableNameByType(parameter);
        if (filter.isEmpty()) {
            return null;
        }

        for (String item : filter) {
            for (String end : TRIM_STRINGS) {
                // ending
                if (item.toLowerCase().endsWith(end)) {
                    item = item.substring(0, item.length() - end.length());
                }

                // remove starting
                if (item.toLowerCase().startsWith(end)) {
                    item = StringUtils.uncapitalize(item.substring(end.length()));
                }
            }

            if (StringUtils.isNotBlank(item)) {
                set.add(item);
            }
        }

        return null;
    }

    /**
     * Local replacement for PhpStorm's {@code PhpNameSuggestionUtil.variableNameByType(Parameter, Project, boolean)}:
     * "\Foo\BarFactoryInterface" -> "barFactoryInterface", "factoryInterface", "interface"
     */
    // TODO consulo-php: PhpNameSuggestionUtil missing
    private static List<String> variableNameByType(Parameter parameter) {
        Set<String> names = new LinkedHashSet<>();

        for (String type : parameter.getDeclaredType().getTypes()) {
            String className = StringUtil.trimEnd(type, "[]");
            if (PhpType.isPrimitiveType(className)) {
                continue;
            }

            String shortName = StringUtil.getShortName(className, '\\');
            if (shortName.isEmpty()) {
                continue;
            }

            List<String> words = new ArrayList<>();
            for (String word : StringUtils.splitByCharacterTypeCamelCase(shortName)) {
                if (!word.isEmpty() && Character.isLetterOrDigit(word.charAt(0))) {
                    words.add(word);
                }
            }

            for (int i = 0; i < words.size(); i++) {
                String name = StringUtils.uncapitalize(String.join("", words.subList(i, words.size())));
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }

        return new ArrayList<>(names);
    }
}
