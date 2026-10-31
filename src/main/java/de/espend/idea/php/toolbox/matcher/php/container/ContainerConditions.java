package de.espend.idea.php.toolbox.matcher.php.container;

import java.util.function.Predicate;
import de.espend.idea.php.toolbox.dict.json.JsonSignature;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ContainerConditions {

    final public static Predicate<JsonSignature> DEFAULT_TYPE_FILTER = new TypeSignatureCondition(JsonSignature.DEFAULT_TYPE);
    final public static Predicate<JsonSignature> RETURN_TYPE_FILTER = new TypeSignatureCondition("return");
    final public static Predicate<JsonSignature> RETURN_TYPE_TYPE = new TypeSignatureCondition("type");

    final public static Predicate<JsonSignature> ARRAY_KEY_AND_FUNCTION_FILTER = new ArrayKeyAndFunctionCondition();
    final public static Predicate<JsonSignature> FUNCTION_FILTER = new FunctionCondition();

    final public static Predicate<JsonSignature> CONSTRUCTOR_FILTER = new ConstructorJsonSignatureCondition();

    private static class ArrayKeyAndFunctionCondition implements Predicate<JsonSignature> {
        @Override
        public boolean test(JsonSignature signature) {
            return "array_key".equals(signature.getType()) && StringUtils.isNotBlank(signature.getFunction());
        }
    }

    private static class FunctionCondition implements Predicate<JsonSignature> {
        @Override
        public boolean test(JsonSignature s) {
            return StringUtils.isNotBlank(s.getFunction());
        }
    }

    public static class TypeSignatureCondition implements Predicate<JsonSignature> {
        final private String type;

        public TypeSignatureCondition(String type) {
            this.type = type;
        }

        @Override
        public boolean test(JsonSignature signature) {
            return type.equals(signature.getType());
        }
    }

    private static class ConstructorJsonSignatureCondition implements Predicate<JsonSignature> {
        @Override
        public boolean test(JsonSignature signature) {
            return "__construct".equalsIgnoreCase(signature.getMethod());
        }
    }
}
