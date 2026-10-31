package de.espend.idea.php.toolbox.navigation.dict;

import consulo.virtualFileSystem.fileType.FileType;
import consulo.project.Project;
import consulo.language.psi.PsiElement;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpToolboxDeclarationHandlerParameter {

    private final PsiElement psiElement;

    private final String element;

    private final FileType fileType;

    public PhpToolboxDeclarationHandlerParameter(PsiElement psiElement, String element, FileType fileType) {
        this.psiElement = psiElement;
        this.element = element;
        this.fileType = fileType;
    }

    public String getContents() {
        return element;
    }

    public Project getProject() {
        return psiElement.getProject();
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public FileType getFileType() {
        return fileType;
    }
}
