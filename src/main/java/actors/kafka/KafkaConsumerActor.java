package actors.kafka;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import messages.MoreData;
import messages.RequestData;
import messages.kafka.PollConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by lowery on 7/20/16.
 */
public class KafkaConsumerActor<T> extends UntypedActor {
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private static final long CONSUMER_TIMEOUT = 1000; // Consumer timeout in ms

    private KafkaConsumer<String, String> consumer;
    private String groupId;

    private String topic;

    private Queue<String> buffer = new LinkedList<>();

    public KafkaConsumerActor(String topic, String groupId) {
        this.topic = topic;
        this.groupId = groupId;

        // Load the kafka consumer properties via typesafe config and construct the properties argument
        Config config = ConfigFactory.load().getConfig("kafka.consumer");
        Properties props = new Properties();

        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            props.put(entry.getKey(), entry.getValue().unwrapped());
        }

        // Add the kafka consumer group id
        props.put("group.id", groupId);

        // Create the kafka consumer instance and subscribe to this actor's topic
        try {
            consumer = new KafkaConsumer<String, String>(props);
        } catch (Exception e) {
            e.printStackTrace();
        }

        consumer.subscribe(Collections.singletonList(topic));
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof PollConsumer) {
            if (buffer.isEmpty()) {
                logger.debug("Consumer polling for messages...");

                ConsumerRecords<String, String> records = consumer.poll(CONSUMER_TIMEOUT);

                for (ConsumerRecord<String, String> record : records) {
                    buffer.add(record.value());
                }

                logger.debug("Consumed {} messages from topic {} to buffer", records.count(), topic);

                // TODO: Handle failure

                // alert listeners that may be idle that the consumer has more data
                if (!buffer.isEmpty()) {
                    sender().tell(buffer.remove(), self());
                }
            }
        } else if (message instanceof RequestData) {
            if (buffer.isEmpty()) {
                self().tell(new PollConsumer(), sender());
            } else {
                // send the next message in the buffer
                sender().tell(buffer.remove(), self());
            }
        }
    }
}
