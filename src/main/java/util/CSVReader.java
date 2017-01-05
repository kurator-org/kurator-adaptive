package util;

import messages.EndOfStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lowery on 1/5/17.
 */
public class CSVReader implements Reader {
    private static final CSVFormat csvFormat = CSVFormat.TDF.withQuote(null).withHeader();

    private String[] headers;
    private Iterator<CSVRecord> iterator;

    public CSVReader(String filename) {
        // set up reader
        try {
            CSVParser csvParser = new CSVParser(new FileReader(filename), csvFormat);

            Map<String, Integer> csvHeader = csvParser.getHeaderMap();
            headers = new String[csvHeader.size()];
            int i = 0;
            for (String header : csvHeader.keySet()) {
                headers[i++] = header;
            }

            iterator = csvParser.iterator();
        } catch (FileNotFoundException e) {
            // TODO: Handle exceptions
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
