package messages.kafka;

import org.apache.kafka.clients.producer.RecordMetadata;

public class SendSuccessful {
    // sent message has been acknowledged by the kafka message broker

    private RecordMetadata metadata;

    public SendSuccessful(RecordMetadata metadata) {
            this.metadata = metadata;
        }

    public RecordMetadata metadata() {
            return metadata;
        }
}