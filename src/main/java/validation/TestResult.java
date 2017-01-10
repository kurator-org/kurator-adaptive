package validation;

import java.util.Map;

/**
 * Created by lowery on 1/5/17.
 */
public class TestResult {
    private ValidationTest test;
    private Map<String, String> values;
    private boolean result;

    public TestResult(ValidationTest test, Map<String, String> values, boolean result) {
        this.test = test;
        this.values = values;
        this.result = result;
    }

    @Override
    public String toString() {
//        return "TestResult{" +
//                "test=" + test +
//                ", result=" + result +
//                ", values=" + values +
//                '}';

        StringBuilder sb = new StringBuilder();
        sb.append(test.getTerm() + ": {\n");
        sb.append("    " + test.getName() + ": {\n");
        sb.append("        id: " + values.get("occurrenceID") + ",\n");
        sb.append("        value: " + values.get(test.getTerm()) + "\n");
        sb.append("        result: " + result + ",\n");
        sb.append("    }\n");
        sb.append("}\n\n");

        return sb.toString();
    }

    public boolean passed() {
        return result;
    }
}
