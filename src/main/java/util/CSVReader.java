package util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lowery on 1/5/17.
 */
public class CSVReader {
    private static final CSVFormat csvFormat = CSVFormat.TDF.withQuote(null).withHeader();

    private String[] headers;
    private Iterator<CSVRecord> iterator;

    public CSVReader(File file) {
        // set up reader
        try {
            CSVParser csvParser = new CSVParser(new FileReader(file), csvFormat);

            Map<String, Integer> csvHeader = csvParser.getHeaderMap();
            headers = new String[csvHeader.size()];
            int i = 0;
            for (String header : csvHeader.keySet()) {
                headers[i++] = header;
            }

            iterator = csvParser.iterator();
        } catch (FileNotFoundException e) {
            // TODO: Handle exceptions
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> next() {
        if (iterator.hasNext()) {
            CSVRecord csvRecord = iterator.next();

            if (!csvRecord.isConsistent()) {
                throw new RuntimeException("Wrong number of fields in record " + csvRecord.getRecordNumber());
            }

            Map<String, String> record = new HashMap<>();

            for (String header : headers) {
                String value = csvRecord.get(header);
                record.put(header, value);
            }

            return record;
        }

        return null;
    }
}
