package actors.kafka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import messages.MoreData;
import messages.RequestData;
import messages.Start;

/**
 * Created by lowery on 1/11/2017.
 */
public class OutputActor extends UntypedActor {
    private ActorRef consumer;
    private String term;

    public OutputActor(String term) {
        this.term = term;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof Start) {
            consumer = context().actorOf(Props.create(KafkaConsumerActor.class, term, term + "-consumer-group"), term + "Actor");
        } else if (message instanceof RequestData) {
            consumer.tell(new RequestData(), self());
        } else if (message instanceof String) {
            System.out.println(message);

            consumer.tell(new RequestData(), self());
        }
    }
}
