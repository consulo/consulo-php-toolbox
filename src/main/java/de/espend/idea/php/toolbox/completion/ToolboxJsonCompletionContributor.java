package de.espend.idea.php.toolbox.completion;

import com.jetbrains.php.completion.PhpLookupElement;
import com.jetbrains.php.completion.insert.PhpReferenceInsertHandler;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.ui.image.Image;
import de.espend.idea.php.toolbox.utils.JsonParseUtil;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionUtilCore;
import org.jspecify.annotations.Nullable;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.application.util.matcher.PrefixMatcher;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.*;
import consulo.document.util.TextRange;
import consulo.language.pattern.PatternCondition;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.pattern.PsiElementPattern;
import consulo.language.pattern.PsiFilePattern;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import consulo.language.editor.completion.CompletionProvider;

import java.util.Map;
import de.espend.idea.php.toolbox.icon.PhpToolboxIconGroup;
import de.espend.idea.php.toolbox.completion.dict.ToolboxJsonFileCompletionArguments;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;
import de.espend.idea.php.toolbox.extension.SourceContributorInterface;
import de.espend.idea.php.toolbox.extension.ToolboxJsonFileCompletion;
import de.espend.idea.php.toolbox.provider.presentation.ProviderParameter;
import de.espend.idea.php.toolbox.provider.presentation.ProviderPresentation;
import de.espend.idea.php.toolbox.utils.ExtensionProviderUtil;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class ToolboxJsonCompletionContributor extends CompletionContributor {

    public ToolboxJsonCompletionContributor() {

        // "provider":"twig_ext"
        extend(CompletionType.BASIC, getNextToPropertyPattern("provider"), new CompletionProvider() {
            @Override
            public void addCompletions(CompletionParameters completionParameters, ProcessingContext processingContext, CompletionResultSet completionResultSet) {

                for (PhpToolboxProviderInterface provider : ExtensionProviderUtil.getProviders(completionParameters.getPosition().getProject())) {

                    LookupElementBuilder lookupElement = LookupElementBuilder
                        .create(provider.getName());

                    ProviderPresentation presentation = provider.getPresentation();
                    String description = null;
                    if(presentation != null) {
                        Image icon = presentation.getIcon();
                        if(icon != null) {
                            lookupElement = lookupElement.withIcon(icon);
                        }
                        description = presentation.getDescription();
                    }
                    else {
                        lookupElement = lookupElement.withIcon(PhpToolboxIconGroup.toolbox());
                    }

                    // Overwrite with class name
                    if(description == null) {
                        String s = provider.getClass().toString();
                        int i = s.lastIndexOf(".");
                        if(i > 0) {
                            lookupElement = lookupElement.withTypeText(s.substring(i + 1), true);
                        }
                    }

                    if(description != null) {
                        lookupElement = lookupElement.withTypeText(description, true);
                    }

                    completionResultSet.addElement(lookupElement);
                }
            }
        });

        // "language":"twig"
        extend(
            CompletionType.BASIC,
            getNextToPropertyPattern("language"),
            new MyStringCompletionProvider(ToolboxJsonFileCompletionArguments.TYPE.LANGUAGE, "php", "twig")
        );

        // "function":"date"
        extend(CompletionType.BASIC, getNextToPropertyPattern("function"), new CompletionProvider() {
            @Override
            public void addCompletions(CompletionParameters completionParameters, ProcessingContext processingContext, CompletionResultSet completionResultSet) {
                for (String s : PhpIndex.getInstance(completionParameters.getPosition().getProject()).getAllFunctionNames(PrefixMatcher.ALWAYS_TRUE)) {
                    completionResultSet.addElement(LookupElementBuilder.create(s).withIcon(com.jetbrains.php.PhpIcons.METHOD));
                }
            }
        });

        // "class":"date"
        extend(CompletionType.BASIC, getNextToPropertyPattern("class"), new CompletionProvider() {
            @Override
            public void addCompletions(CompletionParameters completionParameters, ProcessingContext processingContext, CompletionResultSet resultSet) {
                PhpIndex phpIndex = PhpIndex.getInstance(completionParameters.getPosition().getProject());
                for (String className : phpIndex.getAllClassNames(resultSet.getPrefixMatcher())) {

                    for(PhpClass phpClass: phpIndex.getClassesByName(className)) {
                        resultSet.addElement(new MyPhpLookupElement(phpClass));
                    }

                    for(PhpClass phpClass: phpIndex.getInterfacesByName(className)) {
                        resultSet.addElement(new MyPhpLookupElement(phpClass));
                    }
                }
            }
        });

        // "registrar" => "parameters" => "foo"
        extend(CompletionType.BASIC, getAfterPropertyAndInsideObjectPattern("parameters"), new CompletionProvider() {
            @Override
            public void addCompletions(CompletionParameters completionParameters, ProcessingContext processingContext, CompletionResultSet resultSet) {
                // @TODO: filter for provider name
                for (PhpToolboxProviderInterface provider : ExtensionProviderUtil.getProviders(completionParameters.getPosition().getProject())) {
                    ProviderPresentation presentation = provider.getPresentation();
                    if(presentation == null) {
                        continue;
                    }

                    ProviderParameter[] parameter = presentation.getParameter();
                    if(parameter == null) {
                        continue;
                    }

                    for (ProviderParameter providerParameter : parameter) {
                        resultSet.addElement(
                            LookupElementBuilder.create(providerParameter.getName()).withTypeText(providerParameter.getType().toString().toLowerCase(), true)
                        );
                    }
                }
            }
        });

        // "icon":"com.jetbrains.php.PhpIcons"
        extend(CompletionType.BASIC, getNextToPropertyPattern("icon"), new MyIconCompletionProvider());

        // "contributor":"date"
        extend(CompletionType.BASIC, getNextToPropertyPattern("contributor"), new CompletionProvider() {
            @Override
            public void addCompletions(CompletionParameters completionParameters, ProcessingContext processingContext, CompletionResultSet resultSet) {
                for (SourceContributorInterface sourceContributor : ExtensionProviderUtil.getSourceContributors(completionParameters.getPosition().getProject())) {
                    resultSet.addElement(
                        LookupElementBuilder.create(sourceContributor.getName()).withIcon(PhpToolboxIconGroup.toolbox())
                    );
                }
            }
        });

        // "type":"date"
        extend(
            CompletionType.BASIC,
            getNextToPropertyPattern("type"),
            new MyStringCompletionProvider(ToolboxJsonFileCompletionArguments.TYPE.SIGNATURE_TYPE, "default", "return", "array_key", "type")
        );

        // "signatures":[{"date": "foo"}]
        extend(
            CompletionType.BASIC,
            getAfterPropertyAndInsideArrayObjectPattern("signatures"),
            new MyStringCompletionProvider(ToolboxJsonFileCompletionArguments.TYPE.SIGNATURE_KEY, "index", "type", "array", "function", "class", "method", "field")
        );

        // "defaults|items":[{"date": "foo"}]
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(getAfterPropertyAndInsideArrayObjectPattern("defaults"), getAfterPropertyAndInsideArrayObjectPattern("items")),
            new MyStringCompletionProvider("lookup_string", "presentable_text", "tail_text", "type_text", "type", "icon", "target")
        );

        // "providers":[{"date": "foo"}]
        extend(
            CompletionType.BASIC,
            getAfterPropertyAndInsideArrayObjectPattern("providers"),
            new MyStringCompletionProvider("name", "items", "defaults", "source", "lookup_strings")
        );

        // "providers":[{"date": "foo"}]
        extend(CompletionType.BASIC,
            getAfterPropertyAndInsideArrayObjectPattern("registrar"),
            new MyStringCompletionProvider(ToolboxJsonFileCompletionArguments.TYPE.PROVIDER_KEY, "signatures", "provider", "language", "signature", "parameters")
        );

        // root: {"providers": ..., "registrar": ...}
        extend(
            CompletionType.BASIC,
            getPropertyAfterRootPattern(),
            new MyStringCompletionProvider("providers", "registrar")
        );

        // "source":[{"date": "foo"}]
        extend(CompletionType.BASIC,
            getAfterPropertyAndInsideObjectPattern("source"),
            new MyStringCompletionProvider("contributor", "parameter")
        );
    }

    @Override
    public Language getLanguage() {
        return JsonLanguage.INSTANCE;
    }

    private void addKnownIconClasses(CompletionResultSet resultSet) {
        String[] strings = {
            "com.jetbrains.php.PhpIcons",
            "com.jetbrains.twig.icons.TwigIcons",
            "fr.adrienbrault.idea.symfony2plugin.Symfony2Icons",
            "fr.adrienbrault.idea.symfony2plugin.icon.SymfonyIconGroup",
            "consulo.platform.base.icon.PlatformIconGroup",
            "de.espend.idea.php.toolbox.icon.PhpToolboxIconGroup",
        };

        for (String clazz : strings) {
            if(JsonParseUtil.findClass(clazz) == null) {
                continue;
            }

            resultSet.addElement(LookupElementBuilder.create(clazz).withIcon(PhpToolboxIconGroup.toolbox()));
        }
    }

    /**
     * "key": "<caret>"
     */
    public PsiElementPattern.Capture<PsiElement> getNextToPropertyPattern(String key) {
        return PlatformPatterns.psiElement().inFile(getMetadataFilePattern()).withParent(
            PlatformPatterns.psiElement(JsonStringLiteral.class).withParent(
                PlatformPatterns.psiElement(JsonProperty.class).withFirstChild(
                    PlatformPatterns.psiElement(JsonStringLiteral.class).withText("\"" + key + "\"")
                )
            )
        );
    }

    private PsiFilePattern.Capture<PsiFile> getMetadataFilePattern() {
        return PlatformPatterns.psiFile().withName(PlatformPatterns.string().endsWith("toolbox.metadata.json"));
    }

    /**
     * foo:[{"key": "<caret>"}]
     */
    public PsiElementPattern.Capture<PsiElement> getAfterPropertyAndInsideArrayObjectPattern(String key) {
        return PlatformPatterns.psiElement().inFile(getMetadataFilePattern()).withParent(
            PlatformPatterns.psiElement(JsonStringLiteral.class).with(FirstItemInTreePatternCondition.getInstance()).withParent(
                PlatformPatterns.psiElement(JsonProperty.class).withParent(
                    PlatformPatterns.psiElement(JsonObject.class).withParent(
                        PlatformPatterns.psiElement(JsonArray.class).withParent(
                            PlatformPatterns.psiElement(JsonProperty.class).withFirstChild(
                                PlatformPatterns.psiElement(JsonStringLiteral.class).withText("\"" + key + "\"")
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * foo:{"key": "<caret>"}
     */
    public PsiElementPattern.Capture<PsiElement> getAfterPropertyAndInsideObjectPattern(String key) {
        return PlatformPatterns.psiElement().inFile(getMetadataFilePattern()).withParent(
            PlatformPatterns.psiElement(JsonStringLiteral.class).with(FirstItemInTreePatternCondition.getInstance()).withParent(
                PlatformPatterns.psiElement(JsonProperty.class).withParent(
                    PlatformPatterns.psiElement(JsonObject.class).withParent(

                            PlatformPatterns.psiElement(JsonProperty.class).withFirstChild(
                                PlatformPatterns.psiElement(JsonStringLiteral.class).withText("\"" + key + "\"")
                            )

                    )
                )
            )
        );
    }

    /**
     * foo:[{"key": "<caret>"}]
     */
    public PsiElementPattern.Capture<PsiElement> getPropertyAfterRootPattern() {
        return PlatformPatterns.psiElement().inFile(getMetadataFilePattern()).withParent(
            PlatformPatterns.psiElement(JsonStringLiteral.class).withParent(
                PlatformPatterns.psiElement(JsonProperty.class).withParent(
                    PlatformPatterns.psiElement(JsonObject.class).withParent(
                        PlatformPatterns.psiElement(JsonFile.class)
                    )
                )
            )
        );
    }

    private static class MyPhpLookupElement extends PhpLookupElement {
        public MyPhpLookupElement(PhpNamedElement namedElement) {
            super(namedElement);
            this.handler = PhpReferenceTrimBackslashInsertHandler.getInstance();
        }
    }

    private static class PhpReferenceTrimBackslashInsertHandler implements InsertHandler<LookupElement> {

        private static final PhpReferenceTrimBackslashInsertHandler instance = new PhpReferenceTrimBackslashInsertHandler();

        public void handleInsert(InsertionContext context, LookupElement lookupElement) {

            // reuse core class + namespace insertHandler
            PhpReferenceInsertHandler.getInstance().handleInsert(context, lookupElement);

            // phpstorm8: remove leading backslash on PhpReferenceInsertHandler
            String backslash = context.getDocument().getText(new TextRange(context.getStartOffset(), context.getStartOffset() + 1));
            if("\\".equals(backslash)) {
                context.getDocument().deleteString(context.getStartOffset(), context.getStartOffset() + 1);
            }
        }

        public static PhpReferenceTrimBackslashInsertHandler getInstance(){
            return instance;
        }

    }

    private static String removeIdeaRuleHack(String value) {
        // wtf: ???
        // looks like current cursor position is marked :)
        return value.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "").trim();
    }

    private static class FirstItemInTreePatternCondition extends PatternCondition<PsiElement> {

        private static final FirstItemInTreePatternCondition INSTANCE = new FirstItemInTreePatternCondition();

        public static FirstItemInTreePatternCondition getInstance() {
            return INSTANCE;
        }

        private FirstItemInTreePatternCondition() {
            super("FirstItemInTree");
        }

        @Override
        public boolean accepts(PsiElement psiElement, ProcessingContext processingContext) {
            return psiElement.getPrevSibling() == null;
        }
    }

    private class MyIconCompletionProvider implements CompletionProvider {
        @Override
        public void addCompletions(CompletionParameters completionParameters, ProcessingContext processingContext, CompletionResultSet resultSet) {

            PsiElement position = completionParameters.getPosition();
            PsiElement parent = position.getParent();
            if(!(parent instanceof JsonStringLiteral)) {
                return;
            }

            String value = removeIdeaRuleHack(((JsonStringLiteral) parent).getValue());

            if(value.length() == 0) {
                addKnownIconClasses(resultSet);
                return;
            }

            String[] split = value.split("\\.");
            if(split.length == 0) {
                return;
            }

            // strip fields / methods
            // com.jetbrains.php.PhpIcons."ABS"
            String clazz = value;
            int lastDot = clazz.lastIndexOf(".");
            if(lastDot > 0 && JsonParseUtil.findClass(clazz) == null) {
                clazz = clazz.substring(0, lastDot);
            }

            // strip empty namespace
            // com.jetbrains.php.PhpIcons.
            if(clazz.endsWith(".")) {
                clazz = clazz.substring(0, clazz.length() - 1);
            }

            Class<?> iconClass = JsonParseUtil.findClass(clazz);
            if(iconClass == null) {
                return;
            }

            for (Map.Entry<String, Image> entry : JsonParseUtil.getIcons(iconClass).entrySet()) {
                Image image = entry.getValue();
                int iconWidth = image.getWidth();
                int iconHeight = image.getHeight();

                LookupElementBuilder lookupElement = LookupElementBuilder.create(clazz + "." + entry.getKey())
                    .withTypeText(String.format("(%sx%s)", iconWidth, iconHeight), true);

                // dont explode completion window; 32 for "@x2"
                if(iconWidth <= 32 && iconHeight <= 32) {
                    lookupElement = lookupElement.withIcon(image);
                }

                resultSet.addElement(lookupElement);
            }
        }
    }

    private static class MyStringCompletionProvider implements CompletionProvider {
        private final ToolboxJsonFileCompletionArguments.@Nullable TYPE type;
        private final String[] strings;

        public MyStringCompletionProvider(ToolboxJsonFileCompletionArguments.TYPE type, String... strings) {
            this.type = type;
            this.strings = strings;
        }

        public MyStringCompletionProvider(String... strings) {
            this.type = null;
            this.strings = strings;
        }

        @Override
        public void addCompletions(CompletionParameters completionParameters, ProcessingContext processingContext, CompletionResultSet resultSet) {
            for (String s : strings) {
                resultSet.addElement(
                    LookupElementBuilder.create(s).withIcon(PhpToolboxIconGroup.toolbox())
                );
            }

            if(type == null) {
                return;
            }

            ToolboxJsonFileCompletionArguments arguments = new ToolboxJsonFileCompletionArguments(
                type,
                resultSet
            );

            completionParameters.getPosition().getProject().getApplication().getExtensionPoint(ToolboxJsonFileCompletion.class)
                .forEachExtensionSafe(ext -> ext.addCompletions(arguments));
        }
    }
}
