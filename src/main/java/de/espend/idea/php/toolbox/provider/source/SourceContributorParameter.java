package de.espend.idea.php.toolbox.provider.source;

import consulo.project.Project;
import de.espend.idea.php.toolbox.completion.dict.PhpToolboxCompletionContributorParameter;
import de.espend.idea.php.toolbox.dict.json.JsonProvider;
import de.espend.idea.php.toolbox.dict.json.JsonProviderSource;

import org.jspecify.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SourceContributorParameter {

    private final PhpToolboxCompletionContributorParameter parameter;

    private final JsonProvider jsonProvider;

    public SourceContributorParameter(PhpToolboxCompletionContributorParameter parameter, JsonProvider jsonProvider) {
        this.parameter = parameter;
        this.jsonProvider = jsonProvider;
    }

    public JsonProvider getJsonProvider() {
        return jsonProvider;
    }

    public PhpToolboxCompletionContributorParameter getContributor() {
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
