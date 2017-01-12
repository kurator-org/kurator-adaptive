package python;

import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Created by lowery on 1/11/17.
 */
public class JythonInterpreter {
    private PythonInterpreter interpreter;
    private PythonConfig config;

    public JythonInterpreter(PythonConfig config) {
        this.config = config;
    }

    public void init() {
        // set python home
        if (System.getProperty("python.home") == null && config.getPath() != null) {
            System.setProperty("python.home", config.getPath());
        }

        // initialize interpreter
        interpreter = new PythonInterpreter(null, new PySystemState());

        interpreter.setOut(config.getOutStream());
        interpreter.setErr(config.getErrStream());

            // load python code
            if (config.getModule() != null)
                interpreter.exec("import " + config.getModule());

            if (config.getScript() != null)
                interpreter.execfile(config.getScript());

            if (config.getCode() != null) {
                interpreter.exec(config.getCode());
            }
    }

    public Map<String, Object> call(String function, Map<String, Object> options) {
        // map function name to python function and invoke it
        PyFunction pyFunction = (PyFunction) interpreter.get(function);
        PyObject retVal = pyFunction.__call__(Py.java2py(options));

        // construct map from return value
        return Py.tojava(retVal, Map.class);
    }

    public PyObject call(String function, PyObject pyObject) {
        PyFunction pyFunction = (PyFunction) interpreter.get(function);
        return pyFunction.__call__(pyObject);
    }

    public void close() {
        interpreter.cleanup();
        interpreter.close();
    }
}
