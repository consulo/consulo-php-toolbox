package de.espend.idea.php.toolbox.matcher.php;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileType;
import java.util.function.Predicate;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.patterns.PhpPatterns;
import com.jetbrains.php.lang.psi.elements.*;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;
import de.espend.idea.php.toolbox.dict.matcher.LanguageMatcherParameter;
import de.espend.idea.php.toolbox.extension.LanguageRegistrarMatcherInterface;
import de.espend.idea.php.toolbox.matcher.php.container.ContainerConditions;
import de.espend.idea.php.toolbox.symfony.utils.PhpElementsUtil;
import de.espend.idea.php.toolbox.symfony.util.MethodMatcher;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class ArrayKeyValueSignatureRegistrarMatcher implements LanguageRegistrarMatcherInterface {
    @Override
    public boolean matches(LanguageMatcherParameter parameter) {

        PsiElement psiElement = parameter.getElement().getParent();
        if(!(psiElement instanceof StringLiteralExpression)) {
            return false;
        }

        Collection<JsonSignature> signatures = ContainerUtil.filter(
            parameter.getSignatures(),
            new MyValidJsonSignatureCondition()
        );

        for (JsonSignature signature : signatures) {

            // filtered before; check again for nullable
            if(signature.getArray() == null) {
                continue;
            }

            // @TODO: find ArrayCreationExpression once and check for key
            ArrayCreationExpression arrayCreationExpression = findArrayCreationExpression((StringLiteralExpression) psiElement, signature.getArray());
            if(arrayCreationExpression == null) {
                continue;
            }

            PsiElement parameterList = arrayCreationExpression.getParent();
            if(!(parameterList instanceof ParameterList)) {
                continue;
            }

            // new Foo(['foo' => '<caret>'])
            PsiElement parent = parameterList.getParent();
            if(parent instanceof NewExpression) {
                String className = signature.getClassName();
                String method = signature.getMethod();
                if (method == null || !"__construct".equals(method) || className == null || StringUtils.isBlank(className)) {
                    continue;
                }

                if (new MethodMatcher.NewExpressionParameterMatcher(arrayCreationExpression, signature.getIndex())
                    .withSignature(className, method)
                    .match() != null
                    ) {
                    return true;
                }

            }
            else if(parent instanceof MethodReference) {
                if (StringUtils.isBlank(signature.getClassName()) || StringUtils.isBlank(signature.getMethod())) {
                    continue;
                }

                if (MethodMatcher.getMatchedSignatureWithDepth(arrayCreationExpression, new MethodMatcher.CallToSignature[]{new MethodMatcher.CallToSignature(signature.getClassName(), signature.getMethod())}, signature.getIndex()) != null) {
                    return true;
                }
            }
            else if(parent instanceof FunctionReference) {
                if(StringUtils.isNotBlank(signature.getFunction())) {
                    if(PhpElementsUtil.isFunctionReference(arrayCreationExpression, signature.getIndex(), signature.getFunction())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private ArrayCreationExpression findArrayCreationExpression(StringLiteralExpression psiElement, String key) {

        // value inside array
        // $menu->addChild(array(
        //   'route' => 'foo',
        // ));
        if(PhpPatterns.psiElement(PhpElementTypes.ARRAY_VALUE).accepts(psiElement.getContext())) {
            PsiElement arrayValue = psiElement.getContext();
            if(arrayValue != null) {
                PsiElement arrayHashElement = arrayValue.getContext();
                if(arrayHashElement instanceof ArrayHashElement) {
                    PhpPsiElement arrayKey = ((ArrayHashElement) arrayHashElement).getKey();
                    if(arrayKey instanceof StringLiteralExpression && ((StringLiteralExpression) arrayKey).getContents().equals(key)) {
                        PsiElement arrayCreationExpression = arrayHashElement.getContext();
                        if(arrayCreationExpression instanceof ArrayCreationExpression) {
                            if(!(arrayCreationExpression.getParent() instanceof ParameterList)) {
                                return null;
                            }

                            return (ArrayCreationExpression) arrayCreationExpression;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean supports(FileType fileType) {
        return fileType == PhpFileType.INSTANCE;
    }

    private static class MyValidJsonSignatureCondition implements Predicate<JsonSignature> {
        @Override
        public boolean test(JsonSignature signature) {
            return
                ContainerConditions.DEFAULT_TYPE_FILTER.test(signature) &&
                signature.getArray() != null && StringUtils.isNotBlank(signature.getArray()) && (
                 (
                     signature.getClassName() != null && StringUtils.isNotBlank(signature.getClassName()) &&
                     signature.getMethod() != null && StringUtils.isNotBlank(signature.getMethod())
                 ) ||
                   signature.getFunction() != null && StringUtils.isNotBlank(signature.getFunction())
                )
                ;
        }
    }
}
