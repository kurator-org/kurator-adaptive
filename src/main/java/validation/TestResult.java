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
        return "TestResult{" +
                "test=" + test +
                ", values=" + values +
                ", result=" + result +
                '}';
    }
}
