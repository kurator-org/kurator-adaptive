package util;

import java.io.IOException;
import java.util.Map;

/**
 * Created by lowery on 1/10/2017.
 */
public interface Writer {
    public void append(String line);
    public void close();
}
