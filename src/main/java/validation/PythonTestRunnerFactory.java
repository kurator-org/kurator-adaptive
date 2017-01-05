package validation;

/**
 * Created by lowery on 1/5/17.
 */
public class PythonTestRunnerFactory implements TestRunnerFactory {
    private String pythonScript;
    private String yamlFile;

    public PythonTestRunnerFactory(String pythonScript, String yamlFile) {
        this.pythonScript = pythonScript;
        this.yamlFile = yamlFile;
    }

    public TestRunner createInstance() {
        return new PythonTestRunner(pythonScript, yamlFile);
    }
}
