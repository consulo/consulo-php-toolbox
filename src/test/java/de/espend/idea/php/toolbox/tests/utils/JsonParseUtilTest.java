package de.espend.idea.php.toolbox.tests.utils;

import consulo.util.collection.ContainerUtil;
import de.espend.idea.php.toolbox.dict.json.JsonConfigFile;
import de.espend.idea.php.toolbox.dict.json.JsonProvider;
import de.espend.idea.php.toolbox.dict.json.JsonRawLookupElement;
import de.espend.idea.php.toolbox.dict.json.JsonRegistrar;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;
import de.espend.idea.php.toolbox.utils.JsonParseUtil;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@NullUnmarked
public class JsonParseUtilTest {
    private static JsonConfigFile loadConfig() {
        try (InputStream stream = JsonParseUtilTest.class.getResourceAsStream("ide-toolbox.metadata.json")) {
            assertNotNull(stream);
            JsonConfigFile config = JsonParseUtil.getDeserializeConfig(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            assertNotNull(config);
            return config;
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void testGetProviderJsonFromFile() {
        Map<String, Collection<JsonRawLookupElement>> elements = JsonParseUtil.getProviderJsonRawLookupElements(loadConfig().getProviders());
        assertTrue(elements.containsKey("date_format"));

        JsonRawLookupElement dateFormat = elements.get("date_format").iterator().next();

        assertEquals("d", dateFormat.getLookupString());

        // defaults
        assertEquals("com.jetbrains.php.PhpIcons.METHOD", dateFormat.getIcon());
        assertEquals("(TailTest)", dateFormat.getTailText());
        assertEquals("test", dateFormat.getTarget());

        JsonRawLookupElement source2Item = elements.get("source_2").iterator().next();
        assertEquals("d", source2Item.getLookupString());
    }

    @Test
    public void testGetRegistrarJsonFromFile() {
        JsonRegistrar next = loadConfig().getRegistrar().iterator().next();

        assertTrue(ContainerUtil.filter(next.getSignatures(), function("foo")).size() > 0);
        assertTrue(ContainerUtil.filter(next.getSignatures(), function("date")).size() > 0);
        assertTrue(ContainerUtil.filter(next.getSignatures(), signature -> "DateTime".equals(signature.getClassName())).size() > 0);
    }

    @Test
    public void testGetRegistrarJsonFromFileWithShortcut() {
        JsonRegistrar next = loadConfig().getRegistrar().iterator().next();

        JsonSignature object = ContainerUtil.find(next.getSignatures(), function("apple"));

        assertNotNull(object);
        assertEquals(object.hashCode(), ContainerUtil.find(next.getSignatures(), function("apple")).hashCode());

        assertTrue(ContainerUtil.filter(next.getSignatures(), signature -> "apple".equals(signature.getClassName()) && "car".equals(signature.getMethod())).size() > 0);
    }

    @Test
    public void testConfigDeserialize() {
        List<JsonProvider> providers = new ArrayList<>(loadConfig().getProviders());

        assertEquals("date_format", providers.get(0).getName());

        Collection<JsonRawLookupElement> dateFormatProvider = providers.get(0).getItems();
        JsonRawLookupElement item = ContainerUtil.find(dateFormatProvider, lookupString("d"));
        assertNotNull(item);
        assertEquals(item.hashCode(), ContainerUtil.find(dateFormatProvider, lookupString("d")).hashCode());

        assertNotNull(ContainerUtil.find(dateFormatProvider, lookupString("car")));
        assertNotNull(ContainerUtil.find(dateFormatProvider, lookupString("apple")));

        assertEquals("source_1", providers.get(1).getName());
        assertEquals("return", providers.get(1).getSource().getContributor());
        assertEquals("\\Twig_Environment::getExtension", providers.get(1).getSource().getParameter());

        assertEquals("source_2", providers.get(2).getName());
        assertTrue(providers.get(2).getItems().size() > 0);
    }

    @Test
    public void testCreateSignaturesFromStrings() {
        JsonSignature sign = first("\\Foo\\Bar:foo");
        assertEquals("\\Foo\\Bar", sign.getClassName());
        assertEquals(0, sign.getIndex());
        assertEquals("foo", sign.getMethod());

        sign = first("PHPUnit_Framework_MockObject_Generator:getMock");
        assertEquals("PHPUnit_Framework_MockObject_Generator", sign.getClassName());
        assertEquals("getMock", sign.getMethod());

        sign = first("PHPUnit_Framework_MockObject_Generator:getMock:1");
        assertEquals("PHPUnit_Framework_MockObject_Generator", sign.getClassName());
        assertEquals("getMock", sign.getMethod());
        assertEquals(1, sign.getIndex());

        sign = first("\\Foo\\Bar:f_oo");
        assertEquals("\\Foo\\Bar", sign.getClassName());

        sign = first("\\Foo\\Bar:fo-o");
        assertEquals("\\Foo\\Bar", sign.getClassName());

        sign = first("Foo\\B__--ar:foo");
        assertEquals("Foo\\B__--ar", sign.getClassName());
        assertEquals("foo", sign.getMethod());

        sign = first("\\Foo\\Bar:::::Fo_-o2");
        assertEquals("\\Foo\\Bar", sign.getClassName());
        assertEquals("Fo_-o2", sign.getMethod());
        assertEquals(0, sign.getIndex());

        sign = first("\\Foo\\Bar:foo:1");
        assertEquals("\\Foo\\Bar", sign.getClassName());
        assertEquals(1, sign.getIndex());
        assertEquals("foo", sign.getMethod());

        sign = first("Foo\\B__--ar:foo:1");
        assertEquals("Foo\\B__--ar", sign.getClassName());
        assertEquals(1, sign.getIndex());
        assertEquals("foo", sign.getMethod());

        sign = first("foo_bar");
        assertEquals("foo_bar", sign.getFunction());
        assertEquals(0, sign.getIndex());

        sign = first("foo_bar:1");
        assertEquals("foo_bar", sign.getFunction());
        assertEquals(1, sign.getIndex());

        assertEquals(0, JsonParseUtil.createSignaturesFromStrings(Collections.singletonList("")).size());
        assertEquals(0, JsonParseUtil.createSignaturesFromStrings(Collections.singletonList("a:")).size());

        List<String> signatures = new ArrayList<>();
        signatures.add(null);
        assertEquals(0, JsonParseUtil.createSignaturesFromStrings(signatures).size());
    }

    @Test
    public void testEmptyString() {
        assertNull(JsonParseUtil.getDeserializeConfig(""));
        assertNull(JsonParseUtil.getDeserializeConfig("{"));
    }

    @Test
    public void testParameterRegistrarMappedAsObject() {
        JsonRegistrar next = loadConfig().getRegistrar().iterator().next();

        Map<String, Object> parameters = next.getParameters();

        assertTrue((Boolean) parameters.get("foo"));
        assertEquals(1.0, parameters.get("bar"));
        assertEquals("bobby car", parameters.get("car"));
    }

    private static JsonSignature first(String signature) {
        return JsonParseUtil.createSignaturesFromStrings(Collections.singletonList(signature)).iterator().next();
    }

    private static Predicate<JsonSignature> function(String function) {
        return signature -> function.equals(signature.getFunction());
    }

    private static Predicate<JsonRawLookupElement> lookupString(String string) {
        return element -> string.equals(element.getLookupString());
    }
}
