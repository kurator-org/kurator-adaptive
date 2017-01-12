package python;

import org.python.core.PyException;

import java.io.OutputStream;
import java.io.Writer;

/**
 * Created by lowery on 1/12/17.
 */
public class PythonConfig {
    private String path;
    private String script;
    private String module;
    private String code;

    private OutputStream outStream;
    private OutputStream errStream;

    public void setPath(String path) {
        this.path = path;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setOutStream(OutputStream outStream) {
        this.outStream = outStream;
    }

    public void setErrStream(OutputStream errStream) {
        this.errStream = errStream;
    }

    public String getPath() {
        return path;
    }

    public String getScript() {
        return script;
    }

    public String getModule() {
        return module;
    }

    public String getCode() {
        return code;
    }

    public OutputStream getOutStream() {
        return outStream;
    }

    public OutputStream getErrStream() {
        return errStream;
    }
}
