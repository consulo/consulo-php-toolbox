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
import de.espend.idea.php.toolbox.symfony.util.MethodMatcher;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class MethodParameterRegistrarMatcher implements LanguageRegistrarMatcherInterface {
    @Override
    public boolean matches(LanguageMatcherParameter parameter) {

        PsiElement parent = parameter.getElement().getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return false;
        }

        Collection<JsonSignature> signatures = ContainerUtil.filter(
            parameter.getSignatures(),
            ContainerConditions.DEFAULT_TYPE_FILTER
        );

        for (JsonSignature signature : signatures) {

            if(StringUtils.isBlank(signature.getMethod()) || StringUtils.isBlank(signature.getClassName())) {
                continue;
            }

            if(signature.getMethod().equals("__construct") &&
               new MethodMatcher.NewExpressionParameterMatcher(parent, signature.getIndex()).withSignature(signature.getClassName(), signature.getMethod()).match() != null
               )
            {
                return true;
            }

            if (MethodMatcher.getMatchedSignatureWithDepth(parent, new MethodMatcher.CallToSignature[]{new MethodMatcher.CallToSignature(signature.getClassName(), signature.getMethod())}, signature.getIndex()) != null) {
                return true;
            }
        }

        return false;
    }

    public boolean supports(FileType fileType) {
        return fileType == PhpFileType.INSTANCE;
    }
}
