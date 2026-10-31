package de.espend.idea.php.toolbox.navigation;

import consulo.language.editor.navigation.GotoDeclarationHandler;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiInvalidElementAccessException;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.twig.TwigFileType;
import com.jetbrains.twig.TwigTokenTypes;
import de.espend.idea.php.toolbox.dict.json.JsonRegistrar;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;
import de.espend.idea.php.toolbox.utils.RegistrarMatchUtil;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class PhpToolboxGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement psiElement, int i, Editor editor) {
        if(psiElement == null) {
            return new PsiElement[0];
        }

        PsiFile containingFile;
        try {
            containingFile = psiElement.getContainingFile();
        }
        catch (PsiInvalidElementAccessException e) {
            return new PsiElement[0];
        }

        if(containingFile == null) {
            return new PsiElement[0];
        }

        FileType fileType = containingFile.getFileType();
        if(!(fileType instanceof PhpFileType) && !(fileType instanceof TwigFileType)) {
            return new PsiElement[0];
        }

        String selectedItem = null;
        if(psiElement.getNode().getElementType() == TwigTokenTypes.STRING_TEXT) {
            // twig language
            selectedItem = psiElement.getText();
        }
        else {
            // php language

            PsiElement stringLiteral = psiElement.getParent();
            if(stringLiteral instanceof StringLiteralExpression) {
                selectedItem = ((StringLiteralExpression) stringLiteral).getContents();
            }
        }

        if(StringUtils.isBlank(selectedItem)) {
            return new PsiElement[0];
        }

        Map<PhpToolboxProviderInterface, Set<JsonRegistrar>> providers = RegistrarMatchUtil.getProviders(psiElement);
        if(providers.size() == 0) {
            return new PsiElement[0];
        }

        PhpToolboxDeclarationHandlerParameter parameter = new PhpToolboxDeclarationHandlerParameter(psiElement, selectedItem, fileType);

        Collection<PsiElement> targets = new HashSet<>();
        for (Map.Entry<PhpToolboxProviderInterface, Set<JsonRegistrar>> provider : providers.entrySet()) {
            targets.addAll(provider.getKey().getPsiTargets(parameter));
        }

        return targets.toArray(PsiElement.EMPTY_ARRAY);
    }
}
