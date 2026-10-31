/**
 * @author VISTALL
 */
import org.jspecify.annotations.NullMarked;

@NullMarked
module de.espend.idea.php.toolbox {
    requires consulo.application.api;
    requires consulo.code.editor.api;
    requires consulo.component.api;
    requires consulo.container.api;
    requires consulo.datacontext.api;
    requires consulo.document.api;
    requires consulo.language.api;
    requires consulo.language.editor.api;
    requires consulo.language.editor.refactoring.api;
    requires consulo.localize.api;
    requires consulo.logging.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.util.collection;
    requires consulo.util.io;
    requires consulo.util.lang;
    requires consulo.util.xml.serializer;
    requires consulo.virtual.file.system.api;

    requires consulo.php.api;
    requires consulo.twig;
    requires consulo.json.api;

    requires com.google.gson;
    requires org.apache.commons.lang3;


    exports de.espend.idea.php.toolbox;
    exports de.espend.idea.php.toolbox.completion;
    exports de.espend.idea.php.toolbox.completion.dict;
    exports de.espend.idea.php.toolbox.dict;
    exports de.espend.idea.php.toolbox.dict.json;
    exports de.espend.idea.php.toolbox.dict.matcher;
    exports de.espend.idea.php.toolbox.extension;
    exports de.espend.idea.php.toolbox.extension.cache;
    exports de.espend.idea.php.toolbox.gotoCompletion;
    exports de.espend.idea.php.toolbox.gotoCompletion.contributor;
    exports de.espend.idea.php.toolbox.matcher.php;
    exports de.espend.idea.php.toolbox.matcher.php.container;
    exports de.espend.idea.php.toolbox.matcher.php.docTag;
    exports de.espend.idea.php.toolbox.matcher.php.util;
    exports de.espend.idea.php.toolbox.matcher.twig;
    exports de.espend.idea.php.toolbox.navigation;
    exports de.espend.idea.php.toolbox.navigation.dict;
    exports de.espend.idea.php.toolbox.navigation.locator;
    exports de.espend.idea.php.toolbox.navigation.locator.utils;
    exports de.espend.idea.php.toolbox.provider;
    exports de.espend.idea.php.toolbox.provider.php;
    exports de.espend.idea.php.toolbox.provider.presentation;
    exports de.espend.idea.php.toolbox.provider.source;
    exports de.espend.idea.php.toolbox.provider.source.contributor;
    exports de.espend.idea.php.toolbox.provider.source.contributor.utils;
    exports de.espend.idea.php.toolbox.refactoring;
    exports de.espend.idea.php.toolbox.symfony;
    exports de.espend.idea.php.toolbox.symfony.dic;
    exports de.espend.idea.php.toolbox.symfony.util;
    exports de.espend.idea.php.toolbox.symfony.utils;
    exports de.espend.idea.php.toolbox.type;
    exports de.espend.idea.php.toolbox.type.utils;
    exports de.espend.idea.php.toolbox.utils;
    exports de.espend.idea.php.toolbox.icon;

    opens de.espend.idea.php.toolbox to consulo.util.xml.serializer;
    opens de.espend.idea.php.toolbox.dict.json to com.google.gson;
}
