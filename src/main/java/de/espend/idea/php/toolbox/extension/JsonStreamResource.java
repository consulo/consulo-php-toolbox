package de.espend.idea.php.toolbox.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface JsonStreamResource {
    /**
     * Should provider streams eg of "getResourceAsStream",
     * so that plugins can provider valid json files
     */

    Collection<InputStream> getInputStreams();
}
