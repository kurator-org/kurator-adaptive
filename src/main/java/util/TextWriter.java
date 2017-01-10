package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by lowery on 1/10/2017.
 */
public class TextWriter implements Writer {
    private FileWriter fileWriter;

    public TextWriter(String outputFile){
        try {
            this.fileWriter = new FileWriter(new File(outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void append(String line) {
        try {
            fileWriter.append(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
