package de.espend.idea.php.toolbox.utils;

import consulo.project.DumbService;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.util.io.StreamUtil;
import consulo.language.psi.PsiFile;
import consulo.language.psi.search.FilenameIndex;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.util.collection.ContainerUtil;
import de.espend.idea.php.toolbox.PhpToolboxApplicationService;
import de.espend.idea.php.toolbox.dict.json.*;
import de.espend.idea.php.toolbox.extension.JsonStreamResource;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;
import de.espend.idea.php.toolbox.extension.SourceContributorInterface;
import de.espend.idea.php.toolbox.extension.cache.JsonFileCache;
import de.espend.idea.php.toolbox.matcher.php.container.ContainerConditions;
import de.espend.idea.php.toolbox.provider.SourceProvider;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ExtensionProviderUtil {

    private static final Object STREAM_RESOURCES_LOCK = new Object();

    final private static JsonFileCache APPLICATION_CACHE = new JsonFileCache();
    @Nullable
    private static Collection<JsonConfigFile> RESOURCE_FILES = null;

    private static final Key<CachedValue<Collection<JsonRegistrar>>> TYPE_CACHE = Key.create("PHP_TOOLBOX_TYPE_CACHE");
    private static final Key<CachedValue<Collection<PhpToolboxProviderInterface>>> PROVIDER_CACHE = Key.create("PHP_TOOLBOX_PROVIDER_CACHE");
    private static final Key<CachedValue<Collection<JsonConfigFile>>> CONFIGS_CACHE = Key.create("PHP_TOOLBOX_CONFIGS");
    private static final Key<CachedValue<Collection<JsonConfigFile>>> CONFIGS_CACHE_INDEX = Key.create("PHP_TOOLBOX_CONFIGS_INDEX");
    private static final Key<CachedValue<Collection<JsonRegistrar>>> REGISTRAR_CACHE = Key.create("PHP_TOOLBOX_REGISTRAR");

    @Nullable
    public static PhpToolboxProviderInterface getProvider(Project project, final String key) {
        return ContainerUtil.find(getProviders(project), provider -> key.equalsIgnoreCase(provider.getName()));
    }

    synchronized public static Collection<PhpToolboxProviderInterface> getProviders(final Project project) {
        CachedValue<Collection<PhpToolboxProviderInterface>> cache = project.getUserData(PROVIDER_CACHE);

        if(cache == null) {
            cache = CachedValuesManager.getManager(project).createCachedValue(() -> CachedValueProvider.Result.create(getProvidersInner(project), PsiModificationTracker.MODIFICATION_COUNT), false);

            project.putUserData(PROVIDER_CACHE, cache);
        }

        return cache.getValue();
    }

    public static Collection<PhpToolboxProviderInterface> getProvidersInner(Project project) {
        PhpToolboxApplicationService phpToolboxApplicationService = PhpToolboxApplicationService.getInstance(project);

        Collection<PhpToolboxProviderInterface> providers = new ArrayList<>();
        project.getApplication().getExtensionPoint(PhpToolboxProviderInterface.class).forEachExtensionSafe(providers::add);

        for (Map.Entry<String, Collection<JsonRawLookupElement>> entry : ExtensionProviderUtil.getProviders(project, phpToolboxApplicationService).entrySet()) {
            providers.add(new JsonRawContainerProvider(entry.getKey(), entry.getValue()));
        }

        Map<String, Collection<JsonProvider>> sourceProviders = null;

        for(JsonConfigFile jsonConfig: getJsonConfigs(project, phpToolboxApplicationService)) {

            Collection<JsonProvider> fileProviders = jsonConfig.getProviders();
            if(fileProviders == null) {
                continue;
            }

            for (JsonProvider provider : fileProviders) {
                JsonProviderSource source = provider.getSource();
                if(source != null) {
                    if(sourceProviders == null) {
                        sourceProviders = new HashMap<>();
                    }

                    String name = provider.getName();
                    if(name == null) {
                        continue;
                    }

                    if(!sourceProviders.containsKey(name)) {
                        sourceProviders.put(name, new ArrayList<>(Collections.singletonList(provider)));
                    }
                    else {
                        sourceProviders.get(name).add(provider);
                    }
                }
            }
        }

        if(sourceProviders != null && sourceProviders.size() > 0) {
            for (Map.Entry<String, Collection<JsonProvider>> entry : sourceProviders.entrySet()) {
                providers.add(new SourceProvider(entry.getKey(), entry.getValue()));
            }
        }

        return providers;
    }

    synchronized public static Collection<JsonRegistrar> getRegistrar(final Project project, final PhpToolboxApplicationService phpToolboxApplicationService) {
        CachedValue<Collection<JsonRegistrar>> cache = project.getUserData(REGISTRAR_CACHE);

        if(cache == null) {
            cache = CachedValuesManager.getManager(project).createCachedValue(() -> CachedValueProvider.Result.create(getRegistrarInner(project, phpToolboxApplicationService), PsiModificationTracker.MODIFICATION_COUNT), false);

            project.putUserData(REGISTRAR_CACHE, cache);
        }

        return cache.getValue();
    }

    private static Collection<JsonRegistrar> getRegistrarInner(Project project, PhpToolboxApplicationService phpToolboxApplicationService) {
        Collection<JsonRegistrar> jsonRegistrars = new ArrayList<>();
        for(JsonConfigFile jsonConfig: getJsonConfigs(project, phpToolboxApplicationService)) {
            jsonRegistrars.addAll(jsonConfig.getRegistrar());
        }

        return jsonRegistrars;
    }

    synchronized public static Collection<JsonRegistrar> getTypes(final Project project) {
        CachedValue<Collection<JsonRegistrar>> cache = project.getUserData(TYPE_CACHE);

        if(cache == null) {
            cache = CachedValuesManager.getManager(project).createCachedValue(() -> CachedValueProvider.Result.create(getTypesInner(project), PsiModificationTracker.MODIFICATION_COUNT), false);

            project.putUserData(TYPE_CACHE, cache);
        }

        return cache.getValue();
    }

    private static Collection<JsonRegistrar> getTypesInner(Project project) {
        Collection<JsonRegistrar> jsonRegistrars = new ArrayList<>();
        PhpToolboxApplicationService component = PhpToolboxApplicationService.getInstance(project);
        for(JsonConfigFile jsonConfig: getJsonConfigs(project, component)) {
            Collection<JsonRegistrar> registrar = jsonConfig.getRegistrar();
            for (JsonRegistrar jsonRegistrar : registrar) {
                if(
                    !"php".equals(jsonRegistrar.getLanguage()) ||
                    ContainerUtil.find(jsonRegistrar.getSignatures(), ContainerConditions.RETURN_TYPE_TYPE) == null
                  )
                {
                    continue;
                }

                jsonRegistrars.addAll(registrar);
            }
        }

        return jsonRegistrars;
    }

    public static Map<String, Collection<JsonRawLookupElement>> getProviders(Project project, PhpToolboxApplicationService phpToolboxApplicationService) {
        Map<String, Collection<JsonRawLookupElement>> providers = new HashMap<>();
        for(JsonConfigFile jsonConfig: getJsonConfigs(project, phpToolboxApplicationService)) {
            providers.putAll(JsonParseUtil.getProviderJsonRawLookupElements(jsonConfig.getProviders()));
        }

        return providers;
    }

    synchronized public static Collection<JsonConfigFile> getJsonConfigs(final Project project, final PhpToolboxApplicationService phpToolboxApplicationService) {
        CachedValue<Collection<JsonConfigFile>> cache = project.getUserData(CONFIGS_CACHE);

        if(cache == null) {
            cache = CachedValuesManager.getManager(project).createCachedValue(() -> CachedValueProvider.Result.create(getJsonConfigsInner(project, phpToolboxApplicationService), PsiModificationTracker.MODIFICATION_COUNT), false);
            project.putUserData(CONFIGS_CACHE, cache);
        }

        Collection<JsonConfigFile> jsonConfigFiles = new ArrayList<>(cache.getValue());

        // prevent reindex issues
        if (!DumbService.getInstance(project).isDumb()) {
            CachedValue<Collection<JsonConfigFile>> indexCache = project.getUserData(CONFIGS_CACHE_INDEX);

            if (indexCache == null) {
                indexCache = CachedValuesManager.getManager(project).createCachedValue(() -> {
                    Collection<JsonConfigFile> jsonConfigFiles1 = new ArrayList<>();

                    for (final PsiFile psiFile : FilenameIndex.getFilesByName(project, ".ide-toolbox.metadata.json", GlobalSearchScope.allScope(project))) {
                        JsonConfigFile cachedValue = LanguageCachedValueUtil.getCachedValue(psiFile, () -> new CachedValueProvider.Result<>(
                            JsonParseUtil.getDeserializeConfig(psiFile.getText()),
                            psiFile
                        ));

                        if(cachedValue != null) {
                            jsonConfigFiles1.add(cachedValue);
                        }
                    }

                    return CachedValueProvider.Result.create(jsonConfigFiles1, PsiModificationTracker.MODIFICATION_COUNT);
                }, false);
            }

            project.putUserData(CONFIGS_CACHE_INDEX, indexCache);
            jsonConfigFiles.addAll(indexCache.getValue());
        }

        return jsonConfigFiles;
    }

    synchronized private static Collection<JsonConfigFile> getJsonConfigsInner(Project project, PhpToolboxApplicationService phpToolboxApplicationService) {
        Collection<JsonConfigFile> jsonConfigFiles = new ArrayList<>();

        synchronized (APPLICATION_CACHE) {
            jsonConfigFiles.addAll(APPLICATION_CACHE.get(new HashSet<>(Arrays.asList(phpToolboxApplicationService.getApplicationJsonFiles()))));
        }

        // resources files by plugins
        jsonConfigFiles.addAll(getResourceFiles(project));

        return jsonConfigFiles;
    }

    private static Collection<JsonConfigFile> getResourceFiles(Project project) {
        synchronized (STREAM_RESOURCES_LOCK) {
            if(RESOURCE_FILES != null) {
                return RESOURCE_FILES;
            }

            Collection<JsonConfigFile> files = new ArrayList<>();
            project.getApplication().getExtensionPoint(JsonStreamResource.class).forEachExtensionSafe(resource -> {
                for (InputStream inputStream : resource.getInputStreams()) {
                    String contents;
                    try (InputStream stream = inputStream) {
                        contents = StreamUtil.readText(stream, StandardCharsets.UTF_8);
                    }
                    catch (IOException e) {
                        continue;
                    }

                    JsonConfigFile config = JsonParseUtil.getDeserializeConfig(contents);
                    if(config != null) {
                        files.add(config);
                    }
                }
            });

            return RESOURCE_FILES = files;
        }
    }

    public static List<SourceContributorInterface> getSourceContributors(Project project) {
        List<SourceContributorInterface> contributors = new ArrayList<>();
        project.getApplication().getExtensionPoint(SourceContributorInterface.class).forEachExtensionSafe(contributors::add);
        return contributors;
    }

    @Nullable
    public static SourceContributorInterface getSourceContributor(Project project, String name) {
        for (SourceContributorInterface sourceContributor : getSourceContributors(project)) {
            if(name.equals(sourceContributor.getName())) {
                return sourceContributor;
            }
        }

        return null;
    }
}
