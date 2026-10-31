package de.espend.idea.php.toolbox.provider.presentation;

import org.jspecify.annotations.Nullable;

import consulo.ui.image.Image;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public abstract class ProviderPresentation {
    @Nullable
    public Image getIcon() {
        return null;
    }

    @Nullable
    public String getDescription() {
        return null;
    }

    public ProviderParameter @Nullable [] getParameter() {
        return null;
    }
}
