package de.espend.idea.php.toolbox.navigation.locator;

import consulo.annotation.component.ExtensionImpl;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import de.espend.idea.php.toolbox.extension.PhpToolboxTargetLocator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class ProjectFileTargetLocator implements PhpToolboxTargetLocator {

    @Override
    public Collection<PsiElement> getTargets(TargetLocatorParameter parameter) {
        if(!parameter.getTarget().startsWith("file://")) {
            return Collections.emptyList();
        }

        String projectFile = parameter.getTarget().substring("file://".length());
        projectFile = StringUtil.trimStart(projectFile.replaceAll("\\\\+", "/").replaceAll("/+", "/"), "/");

        VirtualFile relativeFile = VirtualFileUtil.findRelativeFile(parameter.getProject().getBaseDir(), projectFile.split("/"));
        if(relativeFile == null) {
            return Collections.emptyList();
        }

        PsiFile file = PsiManager.getInstance(parameter.getProject()).findFile(relativeFile);
        if(file == null) {
            return Collections.emptyList();
        }

        Collection<PsiElement> targets = new ArrayList<>();
        targets.add(file);

        return targets;
    }
}
