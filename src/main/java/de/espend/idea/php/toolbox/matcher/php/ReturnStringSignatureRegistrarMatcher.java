package de.espend.idea.php.toolbox.matcher.php;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.language.psi.PsiElement;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.php.toolbox.dict.matcher.LanguageMatcherParameter;
import de.espend.idea.php.toolbox.extension.LanguageRegistrarMatcherInterface;
import de.espend.idea.php.toolbox.matcher.php.util.PhpMatcherUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class ReturnStringSignatureRegistrarMatcher implements LanguageRegistrarMatcherInterface {
    @Override
    public boolean matches(LanguageMatcherParameter parameter) {

        PsiElement parent = parameter.getElement().getParent();
        if(!(parent instanceof StringLiteralExpression)) {
            return false;
        }

        PsiElement phpReturn = parent.getParent();
        if(!(phpReturn instanceof PhpReturn)) {
            return false;
        }

        return PhpMatcherUtil.isMachingReturnArray(parameter.getSignatures(), phpReturn);
    }

    @Override
    public boolean supports(FileType fileType) {
        return fileType == PhpFileType.INSTANCE;
    }

}
