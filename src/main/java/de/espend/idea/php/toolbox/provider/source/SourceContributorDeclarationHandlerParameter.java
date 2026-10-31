package de.espend.idea.php.toolbox.provider.source;

import consulo.project.Project;
import de.espend.idea.php.toolbox.dict.json.JsonProvider;
import de.espend.idea.php.toolbox.dict.json.JsonProviderSource;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;

import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SourceContributorDeclarationHandlerParameter {

    private final PhpToolboxDeclarationHandlerParameter parameter;

    private final JsonProvider jsonProvider;

    public SourceContributorDeclarationHandlerParameter(PhpToolboxDeclarationHandlerParameter parameter, JsonProvider jsonProvider) {
        this.parameter = parameter;
        this.jsonProvider = jsonProvider;
    }

    public JsonProvider getJsonProvider() {
        return jsonProvider;
    }

    public PhpToolboxDeclarationHandlerParameter getHandlerParameter() {
        return parameter;
    }

    public Project getProject() {
        return parameter.getProject();
    }

    @Nullable
    public JsonProviderSource getSource() {
        return jsonProvider.getSource();
    }

    @Nullable
    public String getSourceParameter() {
        return getSource() != null ? getSource().getParameter() : null;
    }
}
