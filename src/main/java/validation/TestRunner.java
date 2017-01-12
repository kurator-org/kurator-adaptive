package validation;

import api.Worker;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.python.core.*;
import org.python.util.PythonInterpreter;
import python.JythonInterpreter;
import python.PythonConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by lowery on 1/3/17.
 */
public class TestRunner implements Worker {
    private JythonInterpreter interpreter;
    private List<ValidationTest> tests = new ArrayList<>();

    public TestRunner(PythonConfig pythonConfig, File yamlFile) {
        interpreter = new JythonInterpreter(pythonConfig);

        // parse yaml config
        try {
            YamlReader reader = new YamlReader(new FileReader(yamlFile));
            Map testConf = (Map) reader.read();

            for (Object termName : testConf.keySet()) {
                Map termTests = (Map) testConf.get(termName);

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

    @Override
    public void initialize() {
        interpreter.init();
    }

    @Override
    public Callable<WorkComplete> callable(Object obj) {
        return () -> {
            Map<String, String> record = (Map<String, String>) obj;
            List<TestResult> results = new ArrayList<>();

            for (ValidationTest test : tests) {
                // get term value from the record
                String value = record.get(test.getTerm());

                // map parameter
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

                // construct input dictionary
                Map<PyObject, PyObject> map = new HashMap<>();

                map.put(new PyString("term"), new PyString(test.getTerm()));
                map.put(new PyString("value"), new PyString(value));
                map.put(new PyString("parameter"), pyObject);

                PyDictionary pyDictionary = new PyDictionary(map);

                // call test function
                PyObject retVal = interpreter.call(test.getName(), (PyObject) pyDictionary);
                Map<String, Object> result = Py.tojava(retVal, Map.class);

                results.add(new TestResult(test, record, (Boolean) result.get("success"), (String) result.get("message")));
            }

            return new WorkComplete(results);
        };
    }

    @Override
    public void shutdown() {
        interpreter.close();
    }
}
