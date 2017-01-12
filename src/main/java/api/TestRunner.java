package api;

import validation.TestResult;

import java.util.List;
import java.util.Map;

/**
 * Created by lowery on 1/5/17.
 */
public interface TestRunner {
    public void init();
    public List<TestResult> run(Map<String, String> record);
    public void close();
}
