package de.espend.idea.php.toolbox.tests;

import consulo.application.Application;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.InjectingRecord;
import de.espend.idea.php.toolbox.PhpToolboxApplicationService;
import de.espend.idea.php.toolbox.extension.JsonStreamResource;
import de.espend.idea.php.toolbox.extension.LanguageRegistrarMatcherInterface;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;
import de.espend.idea.php.toolbox.extension.PhpToolboxTargetLocator;
import de.espend.idea.php.toolbox.extension.SourceContributorInterface;
import de.espend.idea.php.toolbox.navigation.locator.GlobalClassTargetLocator;
import de.espend.idea.php.toolbox.navigation.locator.ProjectFileTargetLocator;
import de.espend.idea.php.toolbox.provider.InternalJsonStreamResource;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the toolbox extension points and their bundled implementations are registered.
 */
@NullUnmarked
@ExtendWith(ConsuloProjectLoader.class)
public class PhpToolboxExtensionsTest {
    public record Context(Application application) implements InjectingRecord {
    }

    @Test
    public void testProvidersRegistered(Context context) {
        Set<String> names = new HashSet<>();
        context.application().getExtensionPoint(PhpToolboxProviderInterface.class).forEachExtensionSafe(provider -> names.add(provider.getName()));

        assertTrue(names.contains("php.functions"), names.toString());
        assertTrue(names.contains("class"), names.toString());
        assertTrue(names.contains("interface"), names.toString());
        assertTrue(names.contains("trait"), names.toString());
        assertTrue(names.contains("class_interface"), names.toString());
    }

    @Test
    public void testOtherExtensionsRegistered(Context context) {
        assertNotNull(context.application().getExtensionPoint(PhpToolboxTargetLocator.class).findExtension(ProjectFileTargetLocator.class));
        assertNotNull(context.application().getExtensionPoint(PhpToolboxTargetLocator.class).findExtension(GlobalClassTargetLocator.class));
        assertNotNull(context.application().getExtensionPoint(JsonStreamResource.class).findExtension(InternalJsonStreamResource.class));

        Set<String> contributors = new HashSet<>();
        context.application().getExtensionPoint(SourceContributorInterface.class).forEachExtensionSafe(contributor -> contributors.add(contributor.getName()));
        assertTrue(contributors.contains("return"), contributors.toString());
        assertTrue(contributors.contains("return_array"), contributors.toString());
        assertTrue(contributors.contains("sub_classes"), contributors.toString());

        int[] matchers = {0};
        context.application().getExtensionPoint(LanguageRegistrarMatcherInterface.class).forEachExtensionSafe(matcher -> matchers[0]++);
        assertEquals(8, matchers[0]);

        assertNotNull(context.application().getInstance(PhpToolboxApplicationService.class));
    }
}
