package de.espend.idea.php.toolbox.type;

import consulo.project.Project;
import de.espend.idea.php.toolbox.dict.json.JsonRawLookupElement;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpToolboxTypeProviderArguments {

    final private Project project;
    final private String parameter;
    final private Collection<JsonRawLookupElement> lookupElements;

    public PhpToolboxTypeProviderArguments(Project project, String parameter, Collection<JsonRawLookupElement> lookupElements) {
        this.project = project;
        this.parameter = parameter;
        this.lookupElements = lookupElements;
    }

    public String getParameter() {
        return parameter;
    }

    public Project getProject() {
        return project;
    }

    public Collection<JsonRawLookupElement> getLookupElements() {
        return lookupElements;
    }
}
