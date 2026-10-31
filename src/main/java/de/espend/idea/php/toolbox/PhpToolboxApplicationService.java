package de.espend.idea.php.toolbox;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.container.boot.ContainerPathManager;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.xml.serializer.XmlSerializerUtil;
import de.espend.idea.php.toolbox.utils.JsonParseUtil;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

import java.io.File;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@Singleton
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
@State(name = "PHP Toolbox", storages = @Storage("php-toolbox.xml"))
public class PhpToolboxApplicationService implements PersistentStateComponent<PhpToolboxApplicationService> {

    public static final Logger LOG = Logger.getInstance("Toolbox-Plugin");

    public static PhpToolboxApplicationService getInstance(Project project) {
        return project.getApplication().getInstance(PhpToolboxApplicationService.class);
    }

    public File[] getApplicationJsonFiles() {
        File file = new File(getApplicationFolder());
        if (!file.isDirectory()) {
            return new File[0];
        }

        File[] files = file.listFiles(new JsonParseUtil.JsonFileFilter());
        return files != null ? files : new File[0];
    }

    public static String getApplicationFolder() {
        return ContainerPathManager.get().getConfigPath() + "/php-toolbox";
    }

    @Nullable
    @Override
    public PhpToolboxApplicationService getState() {
        return this;
    }

    @Override
    public void loadState(PhpToolboxApplicationService applicationService) {
        XmlSerializerUtil.copyBean(applicationService, this);
    }
}
