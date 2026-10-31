package de.espend.idea.php.toolbox.matcher.twig;

import consulo.annotation.component.ExtensionImpl;
import java.util.HashSet;
import java.util.Set;

import consulo.virtualFileSystem.fileType.FileType;
import com.jetbrains.twig.TwigFileType;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;
import de.espend.idea.php.toolbox.dict.matcher.LanguageMatcherParameter;
import de.espend.idea.php.toolbox.extension.LanguageRegistrarMatcherInterface;
import de.espend.idea.php.toolbox.utils.TwigUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class TwigBlockRegistrarMatcher implements LanguageRegistrarMatcherInterface {

    @Override
    public boolean matches(LanguageMatcherParameter parameter) {
        Set<String> functions = new HashSet<>();

        for (JsonSignature signature : parameter.getSignatures()) {
            if(StringUtils.isBlank(signature.getFunction())) {
                continue;
            }

            functions.add(signature.getFunction());
        }

        return TwigUtil.getPrintBlockFunctionPattern(functions.toArray(new String[functions.size()])).accepts(parameter.getElement());
    }

    @Override
    public boolean supports(FileType fileType) {
        return fileType == TwigFileType.INSTANCE;
    }
}
