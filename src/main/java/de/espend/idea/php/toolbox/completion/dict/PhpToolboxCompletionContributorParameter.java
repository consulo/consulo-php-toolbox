package de.espend.idea.php.toolbox.completion.dict;

import org.jspecify.annotations.Nullable;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.project.Project;
import consulo.language.util.ProcessingContext;
import de.espend.idea.php.toolbox.dict.MapObjectBag;
import de.espend.idea.php.toolbox.dict.json.JsonRegistrar;

import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpToolboxCompletionContributorParameter {

    private final CompletionParameters completionParameters;

    private final ProcessingContext processingContext;

    private final CompletionResultSet completionResultSet;

    private final Collection<JsonRegistrar> registrars;

    @Nullable
    private MapObjectBag bag;

    public PhpToolboxCompletionContributorParameter(
        CompletionParameters completionParameters,
        ProcessingContext processingContext,
        CompletionResultSet completionResultSet,
        Collection<JsonRegistrar> registrars
    ) {
        this.completionParameters = completionParameters;
        this.processingContext = processingContext;
        this.completionResultSet = completionResultSet;
        this.registrars = registrars;
    }

    public CompletionParameters getCompletionParameters() {
        return completionParameters;
    }

    public ProcessingContext getProcessingContext() {
        return processingContext;
    }

    public CompletionResultSet getCompletionResultSet() {
        return completionResultSet;
    }

    public Project getProject () {
        return completionParameters.getPosition().getProject();
    }

    public JsonRegistrar getRegistrar() {
        return registrars.iterator().next();
    }

    public MapObjectBag getParameterBag() {
        return this.bag != null ? this.bag : (this.bag = new MapObjectBag(registrars.iterator().next().getParameters()));
    }

    public Collection<JsonRegistrar> getRegistrars() {
        return registrars;
    }
}
