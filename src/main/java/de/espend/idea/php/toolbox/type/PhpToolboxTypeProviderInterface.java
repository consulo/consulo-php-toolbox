package de.espend.idea.php.toolbox.type;

import com.jetbrains.php.lang.psi.elements.PhpNamedElement;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface PhpToolboxTypeProviderInterface {
    @Nullable
    Collection<PhpNamedElement> resolveParameter(PhpToolboxTypeProviderArguments args);
}
