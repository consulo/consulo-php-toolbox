package de.espend.idea.php.toolbox.matcher.php;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;
import de.espend.idea.php.toolbox.dict.matcher.LanguageMatcherParameter;
import de.espend.idea.php.toolbox.extension.LanguageRegistrarMatcherInterface;
import de.espend.idea.php.toolbox.matcher.php.container.ContainerConditions;
import de.espend.idea.php.toolbox.matcher.php.util.PhpMatcherUtil;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class PhpFunctionRegistrarMatcher implements LanguageRegistrarMatcherInterface {

    @Override
    public boolean matches(LanguageMatcherParameter parameter) {

        PsiElement parent = parameter.getElement().getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return false;
        }

        Collection<JsonSignature> signatures = ContainerUtil.filter(
            parameter.getRegistrar().getSignatures(),
            ContainerConditions.DEFAULT_TYPE_FILTER
        );

        if(signatures.size() == 0) {
            return false;
        }

        return PhpMatcherUtil.matchesArraySignature(parent, signatures);
    }

    public boolean supports(FileType fileType) {
        return fileType == PhpFileType.INSTANCE;
    }

}
