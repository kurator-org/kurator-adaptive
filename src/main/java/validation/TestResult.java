package validation;

import java.util.Map;

/**
 * Created by lowery on 1/5/17.
 */
public class TestResult {
    private ValidationTest test;
    private Map<String, String> values;
    private boolean success;
    private String message;

    public TestResult(ValidationTest test, Map<String, String> values, boolean success, String message) {
        this.test = test;
        this.values = values;
        this.success = success;
        this.message = message;
    }

    public ValidationTest getTest() {
        return test;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(test.getTerm() + ": {\n");
        sb.append("    " + test.getName() + ": {\n");
        sb.append("        id: " + values.get("occurrenceID") + ",\n");
        sb.append("        value: " + values.get(test.getTerm()) + "\n");
        sb.append("        success: " + success + ",\n");
        sb.append("        message: " + message + "\n");
        sb.append("    }\n");
        sb.append("}\n\n");

        return sb.toString();
    }
}
