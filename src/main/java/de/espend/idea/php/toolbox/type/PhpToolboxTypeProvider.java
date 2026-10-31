package de.espend.idea.php.toolbox.type;

import consulo.annotation.component.ExtensionImpl;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import de.espend.idea.php.toolbox.PhpToolboxApplicationService;
import de.espend.idea.php.toolbox.dict.json.JsonRawLookupElement;
import de.espend.idea.php.toolbox.dict.json.JsonRegistrar;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;
import de.espend.idea.php.toolbox.extension.PhpToolboxProviderInterface;
import de.espend.idea.php.toolbox.matcher.php.container.ContainerConditions;
import de.espend.idea.php.toolbox.symfony.Symfony2InterfacesUtil;
import de.espend.idea.php.toolbox.type.utils.PhpTypeProviderUtil;
import de.espend.idea.php.toolbox.utils.ExtensionProviderUtil;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;

import java.util.*;

import static consulo.util.lang.Pair.pair;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
@ExtensionImpl
public class PhpToolboxTypeProvider implements PhpTypeProvider4 {

    final static char TRIM_KEY = '\u0195';

    @Override
    public char getKey() {
        return '\u0196';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement e) {
        if(!(e instanceof FunctionReference)) {
            return null;
        }

        Collection<JsonRegistrar> types = ExtensionProviderUtil.getTypes(e.getProject());
        if(types.size() == 0) {
            return null;
        }

        String name = ((FunctionReference) e).getName();

        // @TODO: pipe provider names
        Set<Integer> indexes = new HashSet<>();
        for (JsonRegistrar type : types) {
            for (JsonSignature signature: ContainerUtil.filter(new ArrayList<>(type.getSignatures()), ContainerConditions.RETURN_TYPE_TYPE)) {
                // $this->foo('bar')
                // Foo::app('bar')
                if(e instanceof MethodReference && StringUtils.isNotBlank(signature.getClassName()) && StringUtils.isNotBlank(signature.getMethod()) && signature.getMethod().equals(name)) {
                    indexes.add(signature.getIndex());
                }
                // foo('bar')
                else if(StringUtils.isNotBlank(signature.getFunction()) && signature.getFunction().equals(name)) {
                    indexes.add(signature.getIndex());
                }
            }
        }

        if(indexes.isEmpty()) {
            return null;
        }

        String referenceSignature = PhpTypeProviderUtil.getReferenceSignature((FunctionReference) e, TRIM_KEY, indexes);
        if(referenceSignature != null) {
            return new PhpType().add("#" + this.getKey() + referenceSignature);
        }

        return null;
    }

    @Nullable
    @Override
    public PhpType complete(String expression, Project project) {
        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Set<String> visited, int depth, Project project) {
        // consulo-php passes the full signature including our "#<key>" prefix
        String keyPrefix = "#" + getKey();
        if(expression.startsWith(keyPrefix)) {
            expression = expression.substring(keyPrefix.length());
        }

        // get back our original call
        // since phpstorm 7.1.2 we need to validate this
        int endIndex = expression.indexOf(String.valueOf(TRIM_KEY));
        if(endIndex == -1) {
            return Collections.emptySet();
        }

        String originalSignature = expression.substring(0, endIndex);
        String parametersSignature = expression.substring(endIndex + 1);

        // search for called method
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<? extends PhpNamedElement> phpNamedElements = phpIndex.getBySignature(originalSignature, null, 0);
        if(phpNamedElements.size() == 0) {
            return Collections.emptySet();
        }

        // get first matched item
        PhpNamedElement phpNamedElement = phpNamedElements.iterator().next();
        if(!(phpNamedElement instanceof Function)) {
            return phpNamedElements;
        }

        Map<Integer, String> parameters = getParameters(parametersSignature, phpIndex);
        if (parameters.isEmpty()) {
            return phpNamedElements;
        }

        Map<String, Collection<JsonRawLookupElement>> providerMap = ExtensionProviderUtil.getProviders(
            project,
            PhpToolboxApplicationService.getInstance(project)
        );

        Set<Pair<String, Integer>> providers = getProviderNames(project, (Function) phpNamedElement);

        Collection<PhpNamedElement> elements = new HashSet<>();
        elements.addAll(phpNamedElements);

        for (Pair<String, Integer> providerPair : providers) {
            String providerName = providerPair.first;
            Integer index = providerPair.second;

            String parameter = parameters.get(index);
            if (parameter == null) {
                continue;
            }

            PhpToolboxProviderInterface provider = ExtensionProviderUtil.getProvider(project, providerName);
            if(!(provider instanceof PhpToolboxTypeProviderInterface)) {
                continue;
            }

            PhpToolboxTypeProviderArguments args = new PhpToolboxTypeProviderArguments(
                project,
                parameter,
                providerMap.containsKey(providerName) ? providerMap.get(providerName) : Collections.emptyList()
            );

            Collection<PhpNamedElement> items = ((PhpToolboxTypeProviderInterface) provider).resolveParameter(args);
            if(items != null && items.size() > 0) {
                elements.addAll(items);
            }
        }

        return elements;
    }

    private Set<Pair<String, Integer>> getProviderNames(Project project, Function method) {
        Collection<JsonRegistrar> types = ExtensionProviderUtil.getTypes(project);

        Set<Pair<String, Integer>> providers = new HashSet<>();

        Symfony2InterfacesUtil symfony2InterfacesUtil = null;

        String funcName = method.getName();

        // stuff called often; so try reduce calls as possible :)
        for (JsonRegistrar type : types) {
            for (JsonSignature sig: type.getSignatures()) {
                // method or function must be equal
                if(method instanceof Method) {
                    if(sig.getMethod() != null && funcName.equals(sig.getMethod()) && StringUtils.isNotBlank(sig.getClassName())) {
                        if(symfony2InterfacesUtil == null) {
                            symfony2InterfacesUtil = new Symfony2InterfacesUtil();
                        }

                        if (symfony2InterfacesUtil.isCallTo((Method) method, sig.getClassName(), sig.getMethod())) {
                            providers.add(pair(type.getProvider(), sig.getIndex()));
                            break;
                        }
                    }
                }
                else if(StringUtils.isNotBlank(sig.getFunction()) && funcName.equals(sig.getFunction())) {
                    // function condition
                    providers.add(pair(type.getProvider(), sig.getIndex()));
                    break;
                }
            }
        }

        return providers;
    }

    /**
     * Split possible multi index parameter "1#foobar|2#foobar"
     */

    private Map<Integer, String> getParameters(String parametersSignature, PhpIndex phpIndex) {
        Map<Integer, String> parameters = new HashMap<>();

        // "1#foobar|2#foobar"
        for (String s : parametersSignature.split(String.valueOf(TRIM_KEY))) {
            String[] split = s.split(String.valueOf(PhpTypeProviderUtil.PARAMETER_SPLIT_CHAR));
            if(split.length != 2) {
                continue;
            }

            int i;
            try {
                i = Integer.parseInt(split[0]);
            }
            catch (NumberFormatException e) {
                continue;
            }

            String parameter = PhpTypeProviderUtil.getResolvedParameter(phpIndex, split[1]);
            if(parameter == null) {
                continue;
            }

            parameters.put(i, parameter);
        }

        return parameters;
    }
}
