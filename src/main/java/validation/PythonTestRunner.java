package validation;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lowery on 1/3/17.
 */
public class PythonTestRunner implements TestRunner {
    private PythonInterpreter interpreter;
    private List<ValidationTest> tests = new ArrayList<>();

    private String pythonScript;
    private String yamlFile;

    public PythonTestRunner(String pythonScript, String yamlFile) {
        this.pythonScript = pythonScript;
        this.yamlFile = yamlFile;
    }

    public void init() {
        // initialize interpreter
        interpreter = new PythonInterpreter();

        interpreter.setOut(System.out);
        interpreter.setErr(System.err);
        interpreter.execfile(pythonScript);

        // parse yaml config
        try {
            YamlReader reader = new YamlReader(new FileReader(yamlFile));
            Map config = (Map) reader.read();

            for (Object termName : config.keySet()) {
                Map termTests = (Map) config.get(termName);

                for (Object testName : termTests.keySet()) {
                    Object parameter = termTests.get(testName);
                    tests.add(new ValidationTest((String) testName, (String) termName, parameter));
                }
            }
        } catch (FileNotFoundException e) {
            // TODO: handle exceptions
            e.printStackTrace();
        } catch (YamlException e) {
            e.printStackTrace();
        }
    }

    public List<TestResult> run(Map<String, String> specimenRecord) {
        List<TestResult> results = new ArrayList<>();

        for (ValidationTest test : tests) {
            // get term value from the record
            String value = specimenRecord.get(test.getTerm());

            // map test parameter to python object and construct input dictionary
            PyObject pyObject = mapParameter(test);

            Map<PyObject, PyObject> map = new HashMap<PyObject, PyObject>();
            map.put(new PyString("term"), new PyString((String) test.getTerm()));
            map.put(new PyString("value"), new PyString(value));
            map.put(new PyString("parameter"), pyObject);

            PyDictionary pyDictionary = new PyDictionary(map);

            // map test name to python function and invoke it
            PyFunction pyFunction = (PyFunction) interpreter.get((String) test.getName());
            PyObject retVal = pyFunction.__call__(pyDictionary);

            // process the test result from the return value
            boolean result = (Boolean) retVal.__tojava__(Boolean.class);
            results.add(new TestResult(test, specimenRecord, result));
        }

        return results;
    }

    private PyObject mapParameter(ValidationTest test) {
        // Produce the valid typed python object from test parameters
        Object parameter = test.getParameter();
        PyObject pyObject;

        if (parameter instanceof ArrayList) {
            List list = (List) parameter;
            PyObject[] elements = new PyObject[list.size()];

            for (int i = 0; i < list.size(); i++) {
                PyString pyString = new PyString((String) list.get(i));
                elements[i] = pyString;
            }

            pyObject = new PyList(elements);
        } else {
            pyObject = new PyString(parameter.toString());
        }

        return pyObject;
    }

    public void close() {
        interpreter.cleanup();
        interpreter.close();
    }
}
