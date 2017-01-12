package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by lowery on 1/10/2017.
 */
public class TextWriter {
    private FileWriter fileWriter;

    public TextWriter(File outputFile){
        try {
            this.fileWriter = new FileWriter(outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void append(String line) {
        try {
            fileWriter.append(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
