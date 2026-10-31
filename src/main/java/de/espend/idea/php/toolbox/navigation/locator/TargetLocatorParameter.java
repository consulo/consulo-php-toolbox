package de.espend.idea.php.toolbox.navigation.locator;

import consulo.project.Project;
import de.espend.idea.php.toolbox.navigation.dict.PhpToolboxDeclarationHandlerParameter;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class TargetLocatorParameter {

    private final PhpToolboxDeclarationHandlerParameter parameter;

    private final String target;

    public TargetLocatorParameter(PhpToolboxDeclarationHandlerParameter parameter, String target) {
        this.parameter = parameter;
        this.target = target;
    }

    public PhpToolboxDeclarationHandlerParameter getDeclarationParameter() {
        return parameter;
    }

    public String getTarget() {
        return target;
    }

    public Project getProject() {
        return parameter.getProject();
    }
}
