package messages.kafka;

import java.util.Map;

/**
 * Created by lowery on 1/10/2017.
 */
public class PublishData {
    public final Map<String, String> data;

    public PublishData(Map<String, String> data) {
        this.data = data;
    }
}
