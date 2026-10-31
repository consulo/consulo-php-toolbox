package de.espend.idea.php.toolbox.provider.presentation;

import de.espend.idea.php.toolbox.icon.PhpToolboxIconGroup;
import org.jspecify.annotations.Nullable;

import consulo.ui.image.Image;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SimpleProviderPresentation extends ProviderPresentation {
    private static final SimpleProviderPresentation INSTANCE = new SimpleProviderPresentation();

    private SimpleProviderPresentation() {
    }

    @Nullable
    @Override
    public Image getIcon() {
        return PhpToolboxIconGroup.toolbox();
    }

    public static SimpleProviderPresentation getInstance() {
        return INSTANCE;
    }
}
