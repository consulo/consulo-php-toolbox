package de.espend.idea.php.toolbox.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.virtualFileSystem.fileType.FileType;
import de.espend.idea.php.toolbox.dict.matcher.LanguageMatcherParameter;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface LanguageRegistrarMatcherInterface {
    boolean matches(LanguageMatcherParameter parameter);

    boolean supports(FileType fileType);
}
