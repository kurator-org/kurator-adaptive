package actors.kafka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import messages.RequestData;
import messages.Start;
import messages.kafka.SendSuccessful;
import util.Reader;

/**
 * Created by lowery on 1/11/2017.
 */
public class KafkaSupervisor extends UntypedActor {
    private ActorRef inputActor;
    private ActorRef outputActor;

    public KafkaSupervisor(Reader reader, String term) {
        inputActor = context().actorOf(Props.create(InputActor.class, reader), "input");
        outputActor = context().actorOf(Props.create(OutputActor.class, term));
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof Start) {
            inputActor.tell(new Start(), self());
            outputActor.tell(new Start(), self());
        } else if (message instanceof SendSuccessful) {
            outputActor.tell(new RequestData(), self());
        } else if (message instanceof String) {
            System.out.println(message);
        }
    }
}
