package validation;

/**
 * Created by lowery on 1/5/17.
 */
public class ValidationTest {
    private String name;
    private String term;

    private Object parameter;

    public ValidationTest(String name, String term, Object parameter) {
        this.name = name;
        this.term = term;
        this.parameter = parameter;
    }

    public String getName() {
        return name;
    }

    public String getTerm() {
        return term;
    }

    public Object getParameter() {
        return parameter;
    }
}
