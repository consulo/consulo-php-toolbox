package de.espend.idea.php.toolbox.utils;

import com.google.gson.*;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.LookupElementPresentation;
import de.espend.idea.php.toolbox.PhpToolboxApplicationService;
import de.espend.idea.php.toolbox.dict.json.*;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;

import consulo.ui.image.Image;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class JsonParseUtil {

    @Nullable
    public static JsonConfigFile getDeserializeConfig(String contents) {

        JsonObject jsonObject = null;
        try {
            jsonObject = JsonParser.parseString(contents).getAsJsonObject();
        }
        catch (JsonSyntaxException | IllegalStateException | JsonIOException ignored) {
        }

        if(jsonObject == null) {
            PhpToolboxApplicationService.LOG.warn("Invalid json string found");
            return null;
        }

        return getJsonConfigFile(jsonObject);
    }

    @Nullable
    public static JsonConfigFile getDeserializeConfig(File file) {

        if(!file.exists()) {
            return null;
        }

        JsonObject jsonObject = null;
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        }
        catch (IOException | JsonIOException | IllegalStateException | JsonSyntaxException ignored) {
        }

        if(jsonObject == null) {
            PhpToolboxApplicationService.LOG.warn(String.format("Invalid file '%s'", file.getAbsolutePath()));
            return null;
        }

        return getJsonConfigFile(jsonObject);
    }

    @Nullable
    public static JsonConfigFile getJsonConfigFile(JsonObject jsonObject) {
        try {
            return new Gson().fromJson(jsonObject, JsonConfigFile.class);
        }
        catch (JsonIOException | IllegalStateException | JsonSyntaxException ignored) {
        }

        return null;
    }

    public static Map<String, Collection<JsonRawLookupElement>> getProviderJsonRawLookupElements(Collection<JsonProvider> jsonProviders) {

        Map<String, Collection<JsonRawLookupElement>> jsonLookupElements = new HashMap<>();

        for (JsonProvider provider: jsonProviders) {

            String providerName = provider.getName();
            if(providerName == null || provider.getItems().size() == 0) {
                continue;
            }

            Collection<JsonRawLookupElement> lookupItems = provider.getItems();
            if(lookupItems != null && lookupItems.size() > 0) {

                if(!jsonLookupElements.containsKey(providerName)) {
                    jsonLookupElements.put(providerName, new ArrayList<>());
                }

                // merge default values
                JsonRawLookupElement defaults = provider.getDefaults();
                if(defaults != null) {
                    for(JsonRawLookupElement element: lookupItems) {
                        element.setDefaultOptions(defaults);
                    }
                }

                jsonLookupElements.get(providerName).addAll(lookupItems);
            }

        }

        return jsonLookupElements;
    }

    public static class JsonFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith("-toolbox.metadata.json");
        }
    }

    public static void decorateLookupElement(LookupElementPresentation lookupElement, JsonRawLookupElement jsonLookup) {

        if(jsonLookup.getTailText() != null) {
            lookupElement.setTailText(jsonLookup.getTailText(), true);
        }

        if(jsonLookup.getTypeText() != null) {
            lookupElement.setTypeText(jsonLookup.getTypeText());
            lookupElement.setTypeGrayed(true);
        }

        String iconString = jsonLookup.getIcon();
        if(iconString != null) {
            Image icon = getLookupIconOnString(iconString);
            if(icon != null) {
                lookupElement.setIcon(icon);
            }
        }

    }

    public static LookupElementBuilder getDecoratedLookupElementBuilder(LookupElementBuilder lookupElement, @Nullable JsonRawLookupElement jsonLookup) {
        if(jsonLookup == null) {
            return lookupElement;
        }

        if(jsonLookup.getTailText() != null) {
            lookupElement = lookupElement.withTailText(jsonLookup.getTailText(), true);
        }

        if(jsonLookup.getTypeText() != null) {
            lookupElement = lookupElement.withTypeText(jsonLookup.getTypeText(), true);
        }

        String iconString = jsonLookup.getIcon();
        if(iconString != null) {
            Image icon = getLookupIconOnString(iconString);
            if(icon != null) {
                lookupElement = lookupElement.withIcon(icon);
            }
        }

        return lookupElement;
    }

    /**
     * Resolves an icon reference like "com.jetbrains.php.PhpIcons.METHOD" or
     * "consulo.platform.base.icon.PlatformIconGroup.nodesClass": public static field or no-arg method of type Image
     * or public static no-arg method returning Image.
     */
    @Nullable
    public static Image getLookupIconOnString(String icon) {
        int endIndex = icon.lastIndexOf(".");
        if(endIndex < 0 || icon.length() - endIndex < 1) {
            return null;
        }

        String className = icon.substring(0, endIndex);

        Class<?> iconClass = findClass(className);
        if(iconClass == null) {
            return null;
        }

        String member = icon.substring(endIndex + 1);

        try {
            Field field = iconClass.getField(member);
            return toImage(field.get(null));
        }
        catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
        }

        try {
            Method method = iconClass.getMethod(member);
            if(Modifier.isStatic(method.getModifiers())) {
                return toImage(method.invoke(null));
            }
        }
        catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
        }

        return null;
    }

    /**
     * All public static icons of the given class: fields of type Image and no-arg methods returning Image
     */
    public static Map<String, Image> getIcons(Class<?> iconClass) {
        Map<String, Image> icons = new LinkedHashMap<>();

        try {
            for (Field field : iconClass.getFields()) {
                if(!Modifier.isStatic(field.getModifiers()) || !(Image.class.isAssignableFrom(field.getType()))) {
                    continue;
                }

                Image image = toImage(field.get(null));
                if(image != null) {
                    icons.put(field.getName(), image);
                }
            }

            for (Method method : iconClass.getMethods()) {
                if(!Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0 || !Image.class.isAssignableFrom(method.getReturnType())) {
                    continue;
                }

                Image image = toImage(method.invoke(null));
                if(image != null) {
                    icons.put(method.getName(), image);
                }
            }
        }
        catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
        }

        return icons;
    }

    @Nullable
    private static Image toImage(@Nullable Object value) {
        if(value instanceof Image image) {
            return image;
        }

        return null;
    }

    /**
     * Provide support for some more user friendly use cases
     *
     * com.jetbrains.php.PhpIcons$Foo.Bar -> com.jetbrains.php.PhpIcons.Foo.Bar
     */
    @Nullable
    public static Class<?> findClass(String className) {
        Class<?> clazz = loadClass(className);
        if(clazz != null) {
            return clazz;
        }

        int i = className.lastIndexOf(".");
        if(i <= 0) {
            return null;
        }

        return loadClass(className.substring(0, i) + "$" + className.substring(i + 1));
    }

    @Nullable
    private static Class<?> loadClass(String className) {
        // icon classes may live in other plugins; try our own classloader and then the thread context one
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException | LinkageError ignored) {
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if(contextClassLoader != null) {
            try {
                return Class.forName(className, false, contextClassLoader);
            }
            catch (ClassNotFoundException | LinkageError ignored) {
            }
        }

        return null;
    }

    public static Collection<JsonSignature> createSignaturesFromStrings(Collection<String> signatures) {

        Collection<JsonSignature> jsonSignatures = new ArrayList<>();

        for (String signature : signatures) {
            if(signature == null || StringUtils.isBlank(signature)) {
                continue;
            }

            // foo:car but not foo:11
            Matcher matcher = Pattern.compile("^([\\w\\\\-]+):+(\\d*[a-zA-Z_-][a-zA-Z_\\-\\d]*)$").matcher(signature);
            if (matcher.find()) {
                jsonSignatures.add(JsonSignature.createClassMethod(matcher.group(1), matcher.group(2), 0));
                continue;
            }

            // foo:car:1
            matcher = Pattern.compile("^([\\w\\\\-]+):+(\\w+):+(\\d+)$").matcher(signature);
            if (matcher.find()) {
                try {
                    jsonSignatures.add(JsonSignature.createClassMethod(matcher.group(1), matcher.group(2), Integer.parseInt(matcher.group(3))));
                }
                catch (NumberFormatException ignored) {
                }
                continue;
            }

            // foo
            matcher = Pattern.compile("^([\\w\\\\-]+)$").matcher(signature);
            if (matcher.find()) {
                jsonSignatures.add(JsonSignature.createFunction(matcher.group(1), 0));
                continue;
            }

            // foo:1
            matcher = Pattern.compile("^([\\w\\\\-]+):+(\\d+)$").matcher(signature);
            if (matcher.find()) {
                try {
                    jsonSignatures.add(JsonSignature.createFunction(matcher.group(1), Integer.parseInt(matcher.group(2))));
                }
                catch (NumberFormatException ignored) {
                }
                continue;
            }
        }

        return jsonSignatures;
    }
}
