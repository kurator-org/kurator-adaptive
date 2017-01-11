package actors.kafka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import messages.Start;
import messages.kafka.PublishData;
import org.apache.kafka.clients.producer.KafkaProducer;
import util.Reader;

import java.util.Map;

/**
 * Created by lowery on 1/10/2017.
 */
public class InputActor extends UntypedActor {
    private Reader reader;
    private ActorRef kafkaProducer;

    public InputActor(Reader reader) {
        this.reader = reader;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof Start) {
            kafkaProducer = context().actorOf(Props.create(KafkaProducerActor.class));
            Map<String, String> record = reader.next();

            do {
                kafkaProducer.tell(new PublishData(record), sender());
                record = reader.next();
            } while (record != null);
        }
    }
}
